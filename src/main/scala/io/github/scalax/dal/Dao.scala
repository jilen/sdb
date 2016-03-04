package io.github.scalax
package dal

import model._
import scala.concurrent.Future

trait Dao {
  def prepare(): Future[_]
  def newUser(user: User): Future[_]
  def newOrder(order: Order): Future[_]
  def insertBatch(user: Seq[User]): Future[_]
  def getById(id: Long): Future[Option[User]]
  def getByIds(id: Set[Long]): Future[Seq[User]]
  def trans(user: Long, order: Order): Future[_]
}
