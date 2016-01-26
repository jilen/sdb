package io.github.scalax
package dal

import io.github.scalax.model._
import io.getquill._
import io.getquill.naming._
import io.getquill.source.sql.idiom._
import io.getquill.source.async.mysql._
import com.github.mauricio.async.db._
import java.util.Date
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import slick.driver.JdbcProfile

trait DataAccessLayer {
  def insert(user: User): Future[_]
  def insertBatch(user: Seq[User]): Future[_]
  def queryById(id: Long): Future[Option[User]]
  def trans(user: Long, order: Order): Future[_]
}

class QuillDataAccessLayer(db: MysqlAsyncSource[SnakeCase with MysqlEscape])(implicit val ec: ExecutionContext) extends DataAccessLayer {

  def insert(user: User) = {
    val action = quote(query[User].insert)
    db.run(action)(List(user))
  }

  def insertBatch(users: Seq[User]) = {
    val action = quote(query[User].insert)
    db.run(action)(users.toList)
  }

  def queryById(id: Long) = {
    val q = quote{
      id: Long => query[User].filter(_.id == id)
    }
    db.run(q)(id).map(_.headOption)
  }

  def trans(userId: Long, order: Order) = {

    def insert = quote { (order: Order) =>
      query[Order].insert(order)
    }

    def mutate = quote { ( userId: Long, totalFee: Int) =>
      query[User].filter(_.id == userId).update(e => e.remain -> (e.remain - totalFee))
    }

    db.transaction { implicit ec =>
      for {
        _ <- db.run(insert)(List(order))
        _ <- db.run(mutate)(List(order.userId -> order.totalFee.toInt))
      } yield {}
    }
  }
}

trait SlickDataAccessLayer extends DataAccessLayer { profile: JdbcProfile =>

  import profile.api._

  val DB: Database

  def insert(user: User) = DB.run(Users += user)

  def insertBatch(users: Seq[User]) = DB.run(Users ++= users)

  def queryById(id: Long) = DB.run(Users.filter(_.id === id).result.headOption)

  def trans(userId: Long, order: Order) = DB.run {
    val mutateIO = sqlu"update user set remain = remain - ${order.totalFee} where id = ${userId}"
    val insertIO = Orders += order
    insertIO >> mutateIO
  }

  implicit lazy val dateColumnMap  =
    MappedColumnType.base[java.util.Date, java.sql.Timestamp](
      dt => new java.sql.Timestamp(dt.getTime()),
      ts => new Date(ts.getTime)
    )

  class Users(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id")
    def name = column[String]("name")
    def birth = column[Date]("birth")
    def remain = column[Long]("remain")
    def gmtCreate = column[Date]("gmt_create")
    def gmtModified = column[Date]("gmt_modified")
    def * = (id.?, name, birth, remain, gmtCreate, gmtModified) <> (User.tupled, User.unapply)
  }

  class Orders(tag: Tag) extends Table[Order](tag, "order") {
    def userId = column[Long]("user_id")
    def totalFee = column[Long]("total_fee")
    def gmtCreate = column[Date]("gmt_create")
    def gmtModified = column[Date]("gmt_modified")
    def * = (userId, totalFee, gmtCreate, gmtModified) <> (Order.tupled, Order.unapply)
  }

  val Users = TableQuery[Users]
  val Orders = TableQuery[Orders]
}
