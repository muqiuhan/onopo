package com.muqiuhan.onopo.intf

import java.util.concurrent.TimeUnit

/** Represents a pool of objects that makes the requesting threads wait if no object is available. */
trait BlockingPool[T] extends Pool[T]:

  /** Returns an instance of type `T` from the pool.
    * The call is a blocking call, and client threads are made to wait indefinitely until an object is available.
    * The call implements a fairness algorithm that ensures that a FCFS service is implemented.
    * Clients are advised to react to InterruptedException.
    * If the thread is interrupted while waiting for an object to become available,
    * the current implementations sets the interrupted state of the thread to `true` and returns `None`.
    * However this is subject to change from implementation to implementation. */
  override def get(): Option[T]

  /** Returns an instance of type `T` from the pool,
    * waiting up to the specified wait time if necessary for an object to become available.
    * The call is a blocking call, and client threads are made to wait for time until an object is available or until the timeout occurs.
    * The call implements a fairness algorithm that ensures that a FCFS service is implemented.
    * Clients are advised to react to InterruptedException.
    * If the thread is interrupted while waiting for an object to become available,
    * the current implementations set the interrupted state of the thread to `true` and returns null.
    * However this is subject to change from implementation to implementation. */
  def get(time: Long, unit: TimeUnit): Either[InterruptedException, T]