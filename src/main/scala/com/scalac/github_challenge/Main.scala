package com.scalac.github_challenge

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.scalac.github_challenge.api.Controller
import com.scalac.github_challenge.service.GitHubContributionService
import com.scalac.github_challenge.util.{AkkaHttpClient, Configs, ExecutionContexts}

object Main extends App {
  implicit val system = ActorSystem("scalac-github-challenge")
  implicit val ec = ExecutionContexts
  val controller = new Controller(new GitHubContributionService(new AkkaHttpClient, Configs))
  Http().newServerAt("localhost", 8080).bind(controller.routes)
}
