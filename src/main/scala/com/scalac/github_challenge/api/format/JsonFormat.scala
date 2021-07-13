package com.scalac.github_challenge.api.format

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.scalac.github_challenge.api.dto.{Contribution, Failure}
import spray.json.DefaultJsonProtocol

trait JsonFormat extends DefaultJsonProtocol {

 implicit val contributionFormat = jsonFormat2(Contribution)
 implicit val failureFormat = jsonFormat1(Failure)

}

object JsonFormat extends JsonFormat