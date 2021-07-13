package com.scalac.github_challenge.util

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object ExecutionContexts extends ExecutionContextProvider {
  private def getFixedThreadPoolExecutionContext(threads: Int):ExecutionContext = new ExecutionContext {
    private val threadPool = Executors.newFixedThreadPool(threads)
    override def execute(runnable: Runnable): Unit = threadPool.submit(runnable)
    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
  }
  val cpuBoundExecutionContext = getFixedThreadPoolExecutionContext(4)
  val ioBoundExecutionContext = getFixedThreadPoolExecutionContext(12)
  val eventLoopExecutionContext = getFixedThreadPoolExecutionContext(1)
}
