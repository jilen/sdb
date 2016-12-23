package io.github.scalax.dal

import com.typesafe.config._
import io.getquill._
import io.github.scalax.model._
import libs.dal._
import scala.concurrent.Future

class AsyncIOSourceDao(cfgKey: String) extends Dao with QuillSupport {
  val cfg = ConfigFactory.load().getConfig(cfgKey)

  def destory(): Unit = QDB.close()

  def prepare() = {
    QDB.probe("truncate table `order`")
    QDB.probe("truncate table `user`")
    Future.successful({})
  }

  def newUser(user: User) = QDB.run(query[User].schema(_.generated(_.id)).insert)(user).unsafePerformIO

  def newOrder(order: Order) = QDB.run(query[Order].insert)(order).unsafePerformIO

  def getById(id: Long) = QDB.run(query[User].filter(_.id == lift(Some(id)))).map(_.headOption).unsafePerformIO

  def getByIds(ids: Set[Long]) = QDB.run(query[User].filter(u => u.id.forall(i => lift(ids).contains(i)))).unsafePerformIO

  def insertBatch(user: Seq[User]) = QDB.run( query[User].insert)(user.toList).unsafePerformIO

  def trans(user: Long, order: Order) = {
    val actions = for {
      _ <- QDB.run(query[Order].insert)(order)
      _ <- QDB.run(query[User].filter(_.id == lift(Some(user))).update(u => u.remain -> (u.remain - lift(order.totalFee))))
    } yield {}
    actions.unsafePerformTrans
  }


}
