package com.muqiuhan.onopo

import intf.*

import java.util.concurrent.*
import scala.annotation.tailrec

class BoundedBlockingPool[T](
    size: Int,
    validator: Pool.Validator[T],
    objectFactory: ObjectFactory[T]
) extends AbstractPool[T],
      BlockingPool[T]:

  @volatile private var shutdownCalled: Boolean = false

  private val objs: LinkedBlockingQueue[T] = new LinkedBlockingQueue[T](size)
  private val executor: ExecutorService = Executors.newCachedThreadPool()

  for i <- 0 to size do objs.add(objectFactory.createNew())

  override protected def handleInvalidReturn(obj: T): Unit = ()

  override protected def isValid(obj: T): Boolean = validator.isValid(obj)

  override def get(
      time: Long,
      unit: TimeUnit
  ): Either[IllegalStateException, Option[T]] =
    if !shutdownCalled then
      try
        objs.poll() match
          case null        => Right(None)
          case obj: Object => Right(Some(obj))
      catch
        case e: InterruptedException =>
          Thread.currentThread().interrupt()
          Right(None)
    else Left(IllegalStateException("Object pool is already shutdown"))

  override def get(): Either[IllegalStateException, Option[T]] =
    if !shutdownCalled then
      try
        objs.take() match
          case null        => Right(None)
          case obj: Object => Right(Some(obj))
      catch
        case e: InterruptedException =>
          Thread.currentThread().interrupt()
          Right(None)
    else Left(IllegalStateException("Object pool is already shutdown"))

  override def shutdown(): Unit =
    shutdownCalled = true
    executor.shutdownNow()

    // Clear resources
    objs.forEach(validator.invalidate(_))

  override def returnToPool(obj: T): Unit =
    if validator.isValid(obj) then executor.submit(ObjectReturner[T](objs, obj))

  private sealed class ObjectReturner[E](queue: BlockingQueue[E], e: E)
      extends Callable[Void]:

    @tailrec
    final override def call(): Void =
      try
        queue.put(e)
        null
      catch
        case e: InterruptedException =>
          Thread.currentThread().interrupt()
          call()
