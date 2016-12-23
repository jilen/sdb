package io.github.scalax
package dal

import com.zaxxer.hikari._
import java.util.Date
import model._
import scala.concurrent.Future
import slick.driver.MySQLDriver.api._

class SlickDao(db: Database) extends Dao {

  def prepare() = db.run {
    sqlu"truncate table `order`" >> sqlu"truncate table `user`"
  }

  def newUser(user: User) = db.run(Users += user)
  def newOrder(order: Order) = db.run(Orders += order)
  def insertBatch(users: Seq[User]) = db.run(Users ++= users)
  def getById(id: Long) = db.run(Users.filter(_.id === id).result.headOption)
  def getByIds(ids: Set[Long]) = db.run(Users.filter(_.id inSet ids).result)
  def destory() = db.close

  def trans(userId: Long, order: Order) = db.run {
    val mutateIO = sqlu"update user set remain = remain - ${order.totalFee} where id = ${userId}"
    val insertIO = Orders += order
      (insertIO >> mutateIO).transactionally
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


object SlickDao {
  def apply(connections: Int, threads: Int) = {
   val config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://localhost:3306/sdb")
    config.setUsername("root")
    config.setPassword("")
    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    config.addDataSourceProperty("maximumPoolSize", connections)
    val ds = new HikariDataSource(config)
    val db = Database.forDataSource(ds, executor = AsyncExecutor("slick-executor", threads, 1000))
    new SlickDao(db)
  }
}
