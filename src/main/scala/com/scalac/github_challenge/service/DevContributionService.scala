package com.scalac.github_challenge.service

import com.scalac.github_challenge.service.model.Failures.ServiceFailure
import com.scalac.github_challenge.service.model.{Contribution, Contributor, Organization}

import scala.concurrent.Future

trait DevContributionService {
  def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]]
  def getContributors(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Contributor]]]
  def getContributions(contributor: Contributor, organization: Organization): Future[Either[ServiceFailure, Contribution]]
}
