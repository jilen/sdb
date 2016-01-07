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

  val tps = Gen.enumeration("tps")(10, 20)
  object quillDB extends MysqlAsyncSource[SnakeCase] {
  }
  val quillLayer = new QuillDataAccessLayer(quillDB)(ExecutionContext.global)
  object slickLayer extends SlickDataAccessLayer with slick.driver.MySQLDriver {
    import profile.api._
    val config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://localhost:3306/sdb")
    config.setUsername("root")
    config.setPassword("")
    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    val ds = new HikariDataSource(config);
    val DB = Database.forDataSource(ds)
  }

  performance of "Data access library" in {
    measure method "trans" config (
      reports.regression.significance -> 1e-13
    ) in {
      using(tps) curve("quill") in { t =>
        runWithLayer(quillLayer, t)
      }

      using(tps) curve("slick") in { t =>
        runWithLayer(slickLayer, t)
      }
    }
  }

  private def runWithLayer(layer: DataAccessLayer, concurrenyLevel: Int) = {

    val now = new java.util.Date()
    val userId = 1L

    def order = Order1(
      userId = userId,
      totalFee = 100,
      gmtCreate = now,
      gmtModified = now
    )
    println(s"[Bench] Running with concurrenyLevel ${concurrenyLevel}")
    val futs = (1 to concurrenyLevel).map(_ => layer.trans(userId, order))
    Await.result(Future.sequence(futs), Duration.Inf)
    println("[Bench] Finish concurrenyLevel ${concurrenyLevel}")
  }
}
