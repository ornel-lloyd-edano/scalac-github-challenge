package com.scalac.github_challenge.util

import scala.concurrent.ExecutionContext

trait ExecutionContextProvider {
  val cpuBoundExecutionContext: ExecutionContext
  val ioBoundExecutionContext: ExecutionContext
  val eventLoopExecutionContext: ExecutionContext
}
