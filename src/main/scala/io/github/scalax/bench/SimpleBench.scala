package io.github.scalax
package bench

import com.zaxxer.hikari._
import io.github.scalax.model._
import io.github.scalax.dal._
import org.scalameter.api._
import org.scalameter.Measurer._
import org.scalameter.picklers.Implicits._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import io.getquill.source._
import io.getquill.source.async.mysql._
import io.getquill.naming._



object SimpleBench extends Bench.OfflineRegressionReport {

  val idGen = new java.util.concurrent.atomic.AtomicLong(0L)
  val concurreny = Gen.enumeration("concurreny level")(16, 32, 64)
  object quilldb256 extends MysqlAsyncSource[SnakeCase with MysqlEscape]
  object quilldb512 extends MysqlAsyncSource[SnakeCase with MysqlEscape]
  val quillLayer256 = new QuillDataAccessLayer(quilldb256)(ExecutionContext.global)
  val quillLayer512 = new QuillDataAccessLayer(quilldb512)(ExecutionContext.global)

  case class SlickLayer(threadSize: Int, connections: Int) extends SlickDataAccessLayer with slick.driver.MySQLDriver {
    import profile.api._
    val config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://localhost:3306/sdb")
    config.setUsername("root")
    config.setPassword("")
    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    config.addDataSourceProperty("maximumPoolSize", connections)
    val ds = new HikariDataSource(config)
    val DB = Database.forDataSource(ds, executor = AsyncExecutor("slick-executor", threadSize, 1000))
  }

  lazy val slick32 = SlickLayer(32, 32)
  lazy val slick64 = SlickLayer(64, 64)
  lazy val slick128 = SlickLayer(128, 128)

  performance of "Data access library" in {
    measure method "trans" in {
      using(concurreny) curve("quill-256") in { t =>
        runWithLayer(quillLayer256, t)
      }

      using(concurreny) curve("quill-512") in { t =>
        runWithLayer(quillLayer512, t)
      }

      using(concurreny) curve("slick-32-32") in { t =>
        runWithLayer(slick32, t)
      }

      using(concurreny) curve("slick-64-64") in { t =>
        runWithLayer(slick64, t)
      }
    }
  }

  private def runWithLayer(layer: DataAccessLayer, concurrenyLevel: Int) = {

    val now = new java.util.Date()
    val userId = 1L

    def order = Order(
      userId = userId,
      totalFee = 100,
      gmtCreate = now,
      gmtModified = now)
    val futs = (1 to concurrenyLevel).map(_ => layer.trans(userId, order))
    Await.ready(Future.sequence(futs), Duration.Inf)
  }

  def test() = {
    Await.result(runWithLayer(quillLayer256, 1), Duration.Inf)
  }
}
