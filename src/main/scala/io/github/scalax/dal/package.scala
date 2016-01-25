package io.github.scalax

import io.getquill.naming._

trait MysqlEscape extends NamingStrategy {
  override def table(s: String) = escape(s)
  override def column(s: String) = escape(s)
  override def default(s: String) = s
  private def escape(str: String) = s"`$str`"
}

object MysqlEscape extends MysqlEscape
