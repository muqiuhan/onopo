package com.muqiuhan.onopo.intf

/** Represents the mechanism to create new objects to be used in an object pool.
  * Sine our object pools will be generic, they must have knowledge of how to create new objects to populate its pool.
  * This functionality must also not depend on the type of the object pool and must be a common way to create new objects. */
trait ObjectFactory[T]:
  
  /** Returns a new instance of an object of type T. */
  def createNew(): T