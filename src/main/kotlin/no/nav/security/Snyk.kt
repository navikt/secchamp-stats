package no.nav.security

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders.Authorization
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class Snyk constructor(private val http: HttpClient, private val accessToken: String? = null) {
    private val baseUrl = "https://snyk.io/api/v1"

    suspend fun orgs(): List<String>  = http.get("$baseUrl/orgs") {
        accessToken?.let { header(Authorization, "token $it") }
    }.body<JsonObject>().jsonObject["orgs"]?.jsonArray?.map {
        it.jsonObject["id"].toString().trim('"')
    } ?: emptyList()

    suspend fun issueCountsFor(orgs: List<String>) = withContext(IO) {
        orgs.map { async { requestIssuesFor(it) } }
            .awaitAll()
            .flatMap { it.projects }
            .filter { it.hasIssues() }
    }

    private suspend fun requestIssuesFor(org: String): AllProjectsResponse =
        http.post("$baseUrl/org/$org/projects") {
            accessToken?.let { header(Authorization, "token $it") }
        }.body()

}

@kotlinx.serialization.Serializable
data class AllProjectsResponse(val org: Organization, val projects: List<Project>)

@kotlinx.serialization.Serializable
data class Organization(val name: String)

@kotlinx.serialization.Serializable
data class Project(val name: String, val type: String, val isMonitored: Boolean, val issueCountsBySeverity: IssueCounts) {
    fun hasIssues() = issueCountsBySeverity.isNotZero()
}

@kotlinx.serialization.Serializable
data class IssueCounts(val critical: Int = 0, val high: Int = 0, val medium: Int = 0, val low: Int = 0) {
    fun isNotZero() = critical != 0 || high != 0 || medium != 0 || low != 0
}

