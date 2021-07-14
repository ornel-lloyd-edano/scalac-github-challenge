package com.scalac.github_challenge.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}

import scala.concurrent.Future
import com.scalac.github_challenge.api
import com.scalac.github_challenge.service.model.Failures._
import com.scalac.github_challenge.service.model.{Contribution, Contributor, GitHubContributor, GitHubOrg, GitHubRepo, Organization, Repository}
import com.scalac.github_challenge.util.{ExecutionContextProvider, Logging}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

class GitHubContributionService(ecProvider: ExecutionContextProvider)(implicit actorSystem: ActorSystem)
  extends DevContributionService with api.format.JsonFormat with Logging {

  override def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]] = {
    val uri = "https://api.github.com/organizations" + limit.map(lim=> s"?per_page=$lim").getOrElse("")
    Http().singleRequest(HttpRequest(uri = uri))
      .flatMap {
        case response @ HttpResponse(StatusCodes.OK, _, _, _)=>
          Unmarshal(response).to[JsArray].map {
            result=>
             Right(result.convertTo[Seq[GitHubOrg]].map(githubOrg=> Organization(githubOrg.login)))
          }(ecProvider.cpuBoundExCtx)
        case response=>
          logger.error(s"${Unmarshal(response).to[String]}")
          Future.successful(Left(ExternalCallError(s"Unexpected error from [$uri].")))

      }(ecProvider.ioBoundExCtx)
  }

  override def getRepos(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Repository]]] = {
    //https://api.github.com/orgs/engineyard/repos
    val uri = s"https://api.github.com/orgs/$organization/repos" + limit.map(lim=> s"?per_page=$lim").getOrElse("")
    Http().singleRequest(HttpRequest(uri = uri))
      .flatMap {
        case response @ HttpResponse(StatusCodes.OK, _, _, _)=>
          Unmarshal(response).to[JsArray].map {
            result=>
              Right(result.convertTo[Seq[GitHubRepo]].map(githubRepo=> Repository(githubRepo.name)))
          }(ecProvider.cpuBoundExCtx)
        case response=>
          logger.error(s"${Unmarshal(response).to[String]}")
          Future.successful(Left(ExternalCallError(s"Unexpected error from [$uri].")))

      }(ecProvider.ioBoundExCtx)
  }

  override def getContributors(organization: Organization, numRepos: Option[Int]): Future[Either[ServiceFailure, Seq[Contribution]]] = {
    //https://api.github.com/orgs/engineyard/repos
    //get all repos under organization
    //https://api.github.com/repos/{organization}/{repo}/contributors
    //get all contributors under each repo

    getRepos(organization, numRepos).flatMap {
      case Right(repositories)=>
        val calls: Seq[Future[Either[ExternalCallError, Seq[Contribution]]]] = repositories.map { repo=>
          val uri = s"https://api.github.com/repos/$organization/${repo.name}/contributors"
          Http().singleRequest(HttpRequest(uri = uri))
            .flatMap {
              case response @ HttpResponse(StatusCodes.OK, _, _, _)=>
                Unmarshal(response).to[JsArray].map {
                  result=>
                    Right(result.convertTo[Seq[GitHubContributor]]
                      .map(contrib=> Contribution(Contributor(contrib.login), organization, contrib.contributions)))
                }(ecProvider.cpuBoundExCtx)
              case response=>
                logger.error(s"${Unmarshal(response).to[String]}")
                Future.successful(Left(ExternalCallError(s"Unexpected error from [$uri].")))

            }(ecProvider.ioBoundExCtx)
        }

        implicit val ecOnlyHere = ecProvider.cpuBoundExCtx
        Future.sequence(calls).map { futureResult=>
          futureResult.find(_.isLeft) match {
            case Some(failure)=>
              failure
            case None=>
              val contributions = futureResult.collect {
                case Right(contributions)=> contributions
              }.flatten.groupBy(_.contributor).mapValues(contrib=> {
                Contribution(contrib.head.contributor, contrib.head.organization, contrib.map(_.numContributions).sum)
              }).values.toSeq.sortBy(_.numContributions).reverse
              Right(contributions)
          }
        }

    }(ecProvider.cpuBoundExCtx)

  }

}
