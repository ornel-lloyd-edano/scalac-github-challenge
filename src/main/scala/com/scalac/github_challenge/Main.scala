package com.scalac.github_challenge

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.scalac.github_challenge.api.Controller
import com.scalac.github_challenge.service.GitHubContributionService
import com.scalac.github_challenge.util.ExecutionContexts

object Main extends App {
  implicit val system = ActorSystem(Behaviors.empty, "scalac-github-challenge")
  val controller = new Controller(new GitHubContributionService(ExecutionContexts))
  Http().newServerAt("localhost", 8080).bind(controller.routes)
}
