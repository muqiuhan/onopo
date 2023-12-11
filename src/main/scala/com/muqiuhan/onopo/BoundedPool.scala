package com.muqiuhan.onopo

import intf.*

import java.util
import java.util.concurrent.Semaphore

/** Non blocking object pool implementation.
  * If an element is unavailable, return `None` */
class BoundedPool[T](
    size: Int,
    validator: Pool.Validator[T],
    objectFactory: ObjectFactory[T]
) extends AbstractPool[T]:

  private val objs: util.LinkedList[T] = new util.LinkedList[T]()
  private val permits: Semaphore = new Semaphore(1)
  @volatile private var shutdownCalled: Boolean = false

  for i <- 0 to size do objs.push(objectFactory.createNew())

  override protected def handleInvalidReturn(obj: T): Unit = ()

  override protected def isValid(obj: T): Boolean = validator.isValid(obj)

  override def get(): Either[IllegalStateException, Option[T]] =
    if !shutdownCalled then
      Right(
        if permits.tryAcquire() then
          objs.poll() match
            case null        => None
            case obj: Object => Some(obj)
        else None
      )
    else Left(IllegalStateException("Object pool already shutdown"))

  override def shutdown(): Unit =
    shutdownCalled = true

    // Clear resources
    objs.forEach(validator.invalidate(_))

  override def returnToPool(obj: T): Unit =
    if objs.add(obj) then permits.release()
