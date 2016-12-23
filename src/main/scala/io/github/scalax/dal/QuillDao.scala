package io.github.scalax.dal

import io.getquill._
import io.getquill.naming._
import io.github.scalax.model._
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

class QuillDao(cfg: String) extends Dao {

  lazy val db = source(new MysqlAsyncSourceConfig[SnakeCase with MysqlEscape](cfg))

  def prepare() = {
    db.probe("truncate table `order`")
    db.probe("truncate table `user`")
    Future.successful({})
  }

  def newUser(user: User) = db.run(query[User].schema(_.generated(_.id)).insert)(user)
  def newOrder(order: Order) = db.run(query[Order].insert)(order)

  def getById(id: Long) = db.run(query[User].filter(_.id == lift(Some(id)))).map(_.headOption)

  def trans(user: Long, order: Order) = db.transaction { implicit ec =>
    for {
      _ <- db.run(query[Order].insert)(order)
      _ <- db.run(query[User].filter(_.id == lift(Some(user))).update(u => u.remain -> (u.remain - lift(order.totalFee))))
    } yield {}
  }

  def insertBatch(user: Seq[User]) = db.run( query[User].insert)(user.toList)

  def getByIds(ids: Set[Long]) = db.run(query[User].filter(u => u.id.forall(i => lift(ids).contains(i))))

  def destory() = {
    db.close()
  }
}
