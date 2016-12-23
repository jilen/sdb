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


object SimpleBench extends Bench.OfflineRegressionReport {

  val concurreny = Gen.enumeration("concurreny level")(16, 64, 128, 256, 512)
  val concurrenyRead = Gen.enumeration("concurreny read level")(1024, 2048, 4096, 8192)
  val asyncio64 = new AsyncIOSourceDao("asyncio64")
  val slick64 = SlickDao(64, 64)
  val quill64 = new QuillDao("quill64")


  performance of "data access library" in {
    measure method "select by id" in {
      using(concurrenyRead) curve("asyncio64") beforeTests {
        prepareData()
      } in { c =>
        runSelect(asyncio64, c)
      }

      using(concurrenyRead) curve("slick64") beforeTests {
        prepareData()
      } in { c =>
        runSelect(slick64, c)
      }

      using(concurrenyRead) curve("quill64") beforeTests {
        prepareData()
      } in { c =>
        runSelect(quill64, c)
      }
    }
  }

  def runTrans(dao: Dao, concurrency: Int) = {
    val futs = (1 to concurrency).map(_ => dao.trans(user.id.get, order))
    Await.result(Future.sequence(futs), Duration.Inf)
  }

  def prepareData() = {
  }

  def runSelect(dao: Dao, concurrent: Int) = {
    val futs = (1 to concurrent).map { _ =>
      val id = scala.util.Random.nextLong % 100
      dao.getById(id)
    }
    Await.result(Future.sequence(futs), Duration.Inf)
  }

  val selectSize = 10000

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


}
