package io.github.scalax
package model

import java.util.Date

case class Order(
  userId: Long,
  totalFee: Long,
  gmtCreate: Date,
  gmtModified: Date)
