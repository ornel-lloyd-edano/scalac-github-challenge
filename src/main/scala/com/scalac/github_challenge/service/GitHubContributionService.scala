package com.scalac.github_challenge.service

import scala.concurrent.Future
import com.scalac.github_challenge.service.model.Failures._
import com.scalac.github_challenge.service.model.{Contribution, Contributor, Organization}

class GithubContributionService extends DevContributionService {
  override def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]] = ???
  override def getContributors(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Contributor]]] = ???
  override def getContributions(contributor: Contributor, organization: Organization): Future[Either[ServiceFailure, Contribution]] = ???
}
