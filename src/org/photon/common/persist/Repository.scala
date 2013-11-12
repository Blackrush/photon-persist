package org.photon.common.persist

import com.twitter.util.Future
import java.sql.PreparedStatement
import scala.collection.generic.CanBuildFrom

trait Repository[T <: Model] {
  def findById(id: T#PrimaryKey): Future[T]
  def where[Result, Ignored](query: String)(fn: PreparedStatement => Ignored)(implicit cbf: CanBuildFrom[_, T, Result]): Future[Result]
  def persist(o: T): Future[T]
  def removeById(id: T#PrimaryKey): Future[Boolean]
  
  
  def remove(o: T): Future[Unit] =
    removeById(o.id) flatMap { _ => Future.Done }

  def filter[V: Parameter](column: String, value: V): Future[Seq[T]] =
    where(s"$column=?")(implicitly[Parameter[V]].set(_, 1, value))
  
  def find[V: Parameter](column: String, value: V): Future[T] =
    filter(column, value) map (_.head) // force single value?
}
