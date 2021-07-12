package com.scalac.github_challenge.service

import scala.concurrent.Future
import com.scalac.github_challenge.service.model.Failures._
import com.scalac.github_challenge.service.model.{Contribution, Contributor, Organization}

trait DevContributionService {
  def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]]
  def getContributors(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Contributor]]]
  def getContributions(contributor: Contributor, organization: Organization): Future[Either[ServiceFailure, Option[Contribution]]]
}

class GithubContributionService extends DevContributionService {
  override def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]] = ???
  override def getContributors(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Contributor]]] = ???
  override def getContributions(contributor: Contributor, organization: Organization): Future[Either[ServiceFailure, Option[Contribution]]] = ???
}
