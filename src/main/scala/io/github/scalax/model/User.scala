package io.github.scalax
package model

import java.util.Date

case class User(
  id: Option[Long],
  name: String,
  birth: Date,
  remain: Long,
  gmtCreate: Date,
  gmtModified: Date)
