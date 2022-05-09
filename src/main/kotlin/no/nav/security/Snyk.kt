package no.nav.security

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpHeaders.ContentType
import kotlinx.serialization.json.*

class Snyk constructor(private val http: HttpClient, private val accessToken: String? = null) {
    private val baseUrl = "https://snyk.io/api/v1"

    suspend fun orgs(): List<String>  = http.get("$baseUrl/orgs") {
        accessToken?.let { header(Authorization, "token $it") }
    }.body<JsonObject>().jsonObject["orgs"]?.jsonArray?.map {
        it.jsonObject["id"].toString().trim('"')
    } ?: emptyList()

    suspend fun issuesFor(orgs: List<String>): JsonObject =
        http.post("$baseUrl/reporting/counts/issues/latest?groupBy=severity") {
            accessToken?.let {
                header(Authorization, "token $it")
                header(ContentType, Json)
            }
            setBody(requestBody(orgs))
        }.body()

    private fun requestBody(orgs: List<String>) = mapOf(
        "filters" to mapOf(
            "orgs" to orgs
        )
    )

}


