package com.scalac.github_challenge.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}

import scala.concurrent.Future
import com.scalac.github_challenge.api
import com.scalac.github_challenge.service.model.Failures._
import com.scalac.github_challenge.service.model.{Contribution, Contributor, GitHubContributor, GitHubOrg, GitHubRepo, Organization, Repository}
import com.scalac.github_challenge.util.{ConfigProvider, ExecutionContextProvider, Logging}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import scala.collection.immutable
import spray.json._

class GitHubContributionService(ecProvider: ExecutionContextProvider, config: ConfigProvider)(implicit actorSystem: ActorSystem)
  extends DevContributionService with api.format.JsonFormat with Logging {

  private val gitHubOrganizationsUrl = config.getStringConfigVal("github.organizations.url").getOrElse("/github/orgs")
  private def gitHubReposUri(org: Organization) = config.getStringConfigVal("github.repositories.url")
    .getOrElse("/github/repos").replace("{org}", org.name)
  private def gitHubContributorsUri(org: Organization, repo: Repository) =
    config.getStringConfigVal("github.contributors.url")
    .getOrElse("/github/contributors")
      .replace("{org}", org.name)
    .replace("{repo}", repo.name)

  private val requestHeaders = config.getStringConfigVal("github.auth-token")
    .map(token=> immutable.Seq(RawHeader("Authorization", s"Bearer $token")))
    .getOrElse(immutable.Seq.empty)


  override def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]] = {
    val uri = gitHubOrganizationsUrl + limit.map(lim=> s"?per_page=$lim").getOrElse("")
    Http().singleRequest(HttpRequest(uri = uri, headers = requestHeaders))
      .flatMap {
        case response @ HttpResponse(StatusCodes.OK, _, _, _)=>
          Unmarshal(response).to[JsArray].map {
            result=>
             Right(result.convertTo[Seq[GitHubOrg]].map(githubOrg=> Organization(githubOrg.login)).sorted)
          }(ecProvider.cpuBoundExCtx)
        case response=>
          logger.error(s"${Unmarshal(response).to[String]}")
          Future.successful(Left(ExternalCallError(s"Unexpected error from [$uri].")))

      }(ecProvider.ioBoundExCtx)
  }

  override def getRepos(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Repository]]] = {
    val uri = gitHubReposUri(organization) + limit.map(lim=> s"?per_page=$lim").getOrElse("")
    Http().singleRequest(HttpRequest(uri = uri, headers = requestHeaders))
      .flatMap {
        case response @ HttpResponse(StatusCodes.OK, _, _, _)=>
          Unmarshal(response).to[JsArray].map {
            result=>
              Right(result.convertTo[Seq[GitHubRepo]].map(githubRepo=> Repository(githubRepo.name)).sorted)
          }(ecProvider.cpuBoundExCtx)
        case response=>
          logger.error(s"${Unmarshal(response).to[String]}")
          Future.successful(Left(ExternalCallError(s"Unexpected error from [$uri].")))

      }(ecProvider.ioBoundExCtx)
  }

  override def getContributors(organization: Organization, numRepos: Option[Int]): Future[Either[ServiceFailure, Seq[Contribution]]] = {
    getRepos(organization, numRepos).flatMap {
      case Right(repositories)=>
        val calls: Seq[Future[Either[ExternalCallError, Seq[Contribution]]]] = repositories.map { repo=>
          val uri = gitHubContributorsUri(organization, repo)
          Http().singleRequest(HttpRequest(uri = uri, headers = requestHeaders))
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
      case Left(failure)=> Future.successful(Left(failure))
    }(ecProvider.cpuBoundExCtx)

  }

}
