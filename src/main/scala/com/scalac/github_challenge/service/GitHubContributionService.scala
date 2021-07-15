package com.scalac.github_challenge.service


import scala.concurrent.Future
import com.scalac.github_challenge.api
import com.scalac.github_challenge.service.model.Failures._
import com.scalac.github_challenge.service.model.{Contribution, Contributor, GitHubContributor, GitHubOrg, GitHubRepo, Organization, Repository}
import com.scalac.github_challenge.util.{AsyncHttpClient, ConfigProvider, ExecutionContextProvider, Logging}
import spray.json._

class GitHubContributionService(httpClient: AsyncHttpClient, config: ConfigProvider)(implicit ecProvider: ExecutionContextProvider)
  extends DevContributionService with api.format.JsonFormat with Logging {

  private val gitHubOrganizationsUrl = config.getStringConfigVal("github.organizations.url").getOrElse("/github/orgs")
  private def gitHubReposUri(org: Organization) = config.getStringConfigVal("github.repositories.url")
    .getOrElse("/github/repos").replace("{org}", org.name)
  private def gitHubContributorsUri(org: Organization, repo: Repository) =
    config.getStringConfigVal("github.contributors.url")
    .getOrElse("/github/contributors")
      .replace("{org}", org.name)
    .replace("{repo}", repo.name)

  val reqHeaders: Map[String, String] = config.getStringConfigVal("github.auth-token")
    .map(token=> Map(("Authorization", s"Bearer $token")))
    .getOrElse(Map.empty)

  val genericFailure = (errorMsg:String) => {
    ExternalCallError(errorMsg)
  }

  override def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]] = {
    val uri = gitHubOrganizationsUrl + limit.map(lim=> s"?per_page=$lim").getOrElse("")

    val success = (result: JsValue)=> {
      result.convertTo[Seq[GitHubOrg]].map(githubOrg=> Organization(githubOrg.login)).sortBy(_.name)
    }

    httpClient.getRequest[ServiceFailure, Seq[Organization]](uri = uri, headers = reqHeaders)(success, genericFailure)
  }

  override def getRepos(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Repository]]] = {
    val uri = gitHubReposUri(organization) + limit.map(lim=> s"?per_page=$lim").getOrElse("")

    val success = (result: JsValue)=> {
      result.convertTo[Seq[GitHubRepo]].map(githubRepo=> Repository(githubRepo.name)).sortBy(_.name)
    }

    httpClient.getRequest[ServiceFailure, Seq[Repository]](uri = uri, headers = reqHeaders)(success, genericFailure)
  }

  override def getContributors(organization: Organization, numRepos: Option[Int]): Future[Either[ServiceFailure, Seq[Contribution]]] = {
    getRepos(organization, numRepos).flatMap {
      case Right(repositories)=>
        val calls = repositories.map { repo =>
          val uri = gitHubContributorsUri(organization, repo)

          val success = (result: JsValue) => {
            result.convertTo[Seq[GitHubContributor]]
              .map(contrib => Contribution(Contributor(contrib.login), organization, contrib.contributions))
          }
          httpClient.getRequest[ServiceFailure, Seq[Contribution]](uri = uri, headers = reqHeaders)(success, genericFailure)
        }

        implicit val ecHere = ecProvider.cpuBoundExCtx
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
