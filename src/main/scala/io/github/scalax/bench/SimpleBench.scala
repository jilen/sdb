package io.github.scalax
package bench

import java.util.Date
import io.github.scalax.model._
import io.github.scalax.dal._
import org.scalameter.api._
import org.scalameter.Measurer._
import org.scalameter.picklers.Implicits._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import io.getquill.sources._
import io.getquill.sources._
import io.getquill.naming._



object SimpleBench extends Bench.OfflineRegressionReport {

 val concurreny = Gen.enumeration("concurreny level")(4, 8, 16, 32, 64)

  performance of "data access library" in  {
    measure method "transaction" in {
      using(concurreny) curve("quill256") beforeTests {
        prepareTrans(quill256)
      } in { c =>
        runTrans(quill256, c)
      }

      using(concurreny) curve("slick64") beforeTests {
        prepareTrans(slick64)
      } in { c =>
        runTrans(slick64, c)
      }
    }
    measure method "select by id" in {
      using(concurreny) curve("quill256") beforeTests {
        prepareSelect()
      } in { c =>
        runSelect(quill256, c)
      }

      using(concurreny) curve("slick64") beforeTests {
        prepareSelect()
      } in { c =>
        runSelect(slick64, c)
      }
    }
  }

  def prepareTrans(dao: Dao) = {
    val fut = for {
      _ <- dao.prepare()
      _ <- dao.newUser(user)
      _ <- dao.newOrder(order)
    } yield {}
    Await.result(fut, Duration.Inf)
  }

  def runTrans(dao: Dao, concurrency: Int) = {
    val futs = (1 to concurrency).map(_ => dao.trans(user.id.get, order))
    Await.result(Future.sequence(futs), Duration.Inf)
  }

  def prepareSelect() = {
  }

  def runSelect(dao: Dao, concurrent: Int) = {
    val futs = (1 to concurrent).map { _ =>
      val id = scala.util.Random.nextLong % 100
      dao.getById(id)
    }
    Await.result(Future.sequence(futs), Duration.Inf)
  }

  val user = User(
    id = Some(1L),
    name = "user_name",
    birth = new Date,
    remain = 999999L,
    gmtCreate = new Date,
    gmtModified = new Date)

  def order = Order(
    userId = user.id.get,
    totalFee = 100,
    gmtCreate = new Date,
    gmtModified = new Date)

  val quill256 = new QuillDao("quill256")
  val slick64 = SlickDao(64, 64)

}
