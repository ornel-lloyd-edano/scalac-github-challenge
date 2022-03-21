package com.scalac.github_challenge.api.format

import com.scalac.github_challenge.api.dto.Contribution
import com.scalac.github_challenge.service.model.GitHubOrg
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import spray.json._

class JsonFormatTest extends AnyFlatSpec with should.Matchers {

  "JsonFormat" should "read json and convert it Contribution object" in {
    val json =
      s"""
         |{"name": "ornel-lloyd-edano", "contributions": 10}
         |""".stripMargin.parseJson
    val expected = Contribution(name = "ornel-lloyd-edano", contributions = 10)
    JsonFormat.contributionFormat.read(json) should be (expected)
  }

  "JsonFormat" should "write a Contribution object to json" in {
    val contribution = Contribution(name = "ornel-lloyd-edano", contributions = 10)
    val expected =
      s"""
         |{"name": "ornel-lloyd-edano", "contributions": 10}
         |""".stripMargin.parseJson
    JsonFormat.contributionFormat.write(contribution) should be (expected)
  }

  "JsonFormat" should "convert a json with other fields to a case class with a single matching field" in {
    val input =
      """
        |{"field1": 10, "id": 123, "field2": false, "login": "ornel-lloyd-edano"}
        |""".stripMargin.parseJson
    val expected = GitHubOrg(123, "ornel-lloyd-edano")
    JsonFormat.githubOrgFormat.read( input) should be (expected)
  }

}
