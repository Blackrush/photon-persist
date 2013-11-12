package org.photon.common.persist

object ModelState extends Enumeration {
  type ModelState = Value

  val None, Persisted, Removed = Value
}
