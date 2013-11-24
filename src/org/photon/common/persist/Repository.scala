package org.photon.common.persist

import com.twitter.util.Future
import java.sql.PreparedStatement
import scala.collection.generic.CanBuildFrom

trait Repository[T <: Model] {
  def all: Future[Seq[T]]
  def find(id: T#PrimaryKey): Future[T]
  def where[Result, Ignored](query: String)(fn: PreparedStatement => Ignored)(implicit cbf: CanBuildFrom[_, T, Result]): Future[Result]
  def persist(o: T): Future[T]
  def remove(o: T): Future[T]

  def filter[V: Parameter](column: String, value: V): Future[Seq[T]] =
    where(s"$column=?")(implicitly[Parameter[V]].set(_, 1, value))
  
  def findBy[V: Parameter](column: String, value: V): Future[T] =
    filter(column, value) map (_.head) // force single value?
}
