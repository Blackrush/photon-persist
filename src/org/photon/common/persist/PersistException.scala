package org.photon.common.persist

case class PersistException(reason: String = "", nested: Throwable = null) extends RuntimeException(reason, nested)
