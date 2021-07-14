package com.scalac.github_challenge.util

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object ExecutionContexts extends ExecutionContextProvider {
  private def getFixedThreadPoolExecutionContext(threads: Int):ExecutionContext = new ExecutionContext {
    private val threadPool = Executors.newFixedThreadPool(threads)
    override def execute(runnable: Runnable): Unit = threadPool.submit(runnable)
    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
  }
  val cpuBoundExCtx = getFixedThreadPoolExecutionContext(4)
  val ioBoundExCtx = getFixedThreadPoolExecutionContext(12)
  val eventLoopExCtx = getFixedThreadPoolExecutionContext(1)
}
