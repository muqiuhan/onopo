package com.muqiuhan.onopo

/** Represents an abstract pool, that defines the procedure of returning an object to the pool. */
abstract class AbstractPool[T] extends Pool[T]:

  protected def isValid(obj: T): Boolean
  protected def returnToPool(obj: T): Unit
  protected def handleInvalidReturn(obj: T): Unit
  
  /** Returns the object to the pool.
   *  The method first validates the object if it is re-usable and then puts returns it to the pool.
   *  If the object validation fails, some implementations will try to create a new one and put it into the pool;
   *  however this behaviour is subject to change from implementation to implementation.
   */
  override final def release(obj: T): Unit = 
    if isValid(obj) then
      returnToPool(obj)
    else
      handleInvalidReturn(obj)