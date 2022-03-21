package com.scalac.github_challenge.service.model

object Failures {
  sealed trait ServiceFailure
  final case class ContributorNotFound(contributor: String) extends ServiceFailure
  final case class OrganizationNotFound(organization: String) extends ServiceFailure
  final case class ExternalCallError(error: String) extends ServiceFailure
}
