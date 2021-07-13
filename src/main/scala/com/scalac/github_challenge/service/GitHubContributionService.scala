package com.scalac.github_challenge.service

import scala.concurrent.Future
import com.scalac.github_challenge.service.model.Failures._
import com.scalac.github_challenge.service.model.{Contribution, Contributor, Organization}
import com.scalac.github_challenge.util.ExecutionContextProvider

class GitHubContributionService(ecProvider: ExecutionContextProvider) extends DevContributionService {
  implicit val ec = ecProvider.ioBoundExecutionContext

  //mock implementation
  override def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]] = Future {
    Right(Seq("Apple", "Google", "Microsoft", "Netflix", "Oracle").map(Organization(_)))
  }
  override def getContributors(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Contributor]]] = ???
  override def getContributions(contributor: Contributor, organization: Organization): Future[Either[ServiceFailure, Contribution]] = ???
}
