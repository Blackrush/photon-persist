package org.photon.common.persist

import ModelState.ModelState

trait Model {
  type PrimaryKey

  def id: PrimaryKey
  def state: ModelState
}
