package io.github.scalax.dal


import io.getquill._
import io.getquill.naming._
import io.getquill.sources.async._
import io.github.scalax.model._
import scala.concurrent.ExecutionContext.Implicits.{global => ec}

class QuillDao(cfg: String) extends Dao {

  lazy val db = source(new MysqlAsyncSourceConfig[SnakeCase with MysqlEscape](cfg))

  def prepare() = {
    for {
      _ <- db.execute("truncate table `user`")
      _ <- db.execute("truncate table `order`")
    } yield {}
  }

  def newUser(user: User) = db.run(insertUser)(user)
  def newOrder(order: Order) = db.run(insertOrder)(order)

  def getById(id: Long) = db.run(queryById)(id).map(_.headOption)

  def trans(user: Long, order: Order) = db.transaction { implicit ec =>
    for {
      _ <- db.run(insertOrder)(order)
      _ <- db.run(mutateRemain)(user -> order.totalFee)
    } yield {}
  }

  def insertBatch(user: Seq[User]) = db.run(insertUser)(user.toList)

  def getByIds(ids: Set[Long]) = db.run(queryByIds)(ids)

  protected val insertUser = quote(query[User].insert)
  protected val insertOrder = quote(query[Order].insert)
  protected val mutateRemain = quote { ( userId: Long, totalFee: Long) =>
    query[User].filter(_.id == userId).update(e => e.remain -> (e.remain - totalFee))
  }
  protected val queryById = quote { (id: Long) =>
    query[User].filter(_.id == id)
  }
  protected val queryByIds = quote { (ids: Set[Long]) =>
    query[User].filter(u => u.id.forall(id => ids.contains(id)))
  }

}
