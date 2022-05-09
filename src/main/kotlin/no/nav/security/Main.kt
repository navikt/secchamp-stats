package no.nav.security

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("secchamp-stats")

fun main() = runBlocking {
    val gitHub = GitHub(httpCLient(), requiredFromEnv("GH_TOKEN"))
    val snyk = Snyk(httpCLient(), requiredFromEnv("SNYK_TOKEN"))
    val bq = BigQuery(requiredFromEnv("GCP_TEAM_PROJECT_ID"))
    val ghRepoCount = gitHub.repoCount()
    val snykOrgs = snyk.orgs()
    val issuesBySeverity = bySeverity(snyk.issuesFor(snykOrgs))
    println(snyk.issuesFor(snykOrgs))
    bq.insert(
        IssueCountRecord(
            ghRepoCount = ghRepoCount,
            critical = issuesBySeverity["critical"] ?: 0,
            high = issuesBySeverity["high"] ?: 0,
            medium = issuesBySeverity["medium"] ?: 0,
            low = issuesBySeverity["low"] ?: 0
        )
    ).fold(
        { logger.info("Inserted $it rows") },
        { logger.error("An error occurred: ${it.message}") }
    )
}

fun bySeverity(raw: JsonObject) =
    raw["results"]?.jsonArray?.get(0)?.jsonObject?.get(("severity"))?.jsonObject?.let {
        mapOf(
            "critical" to it["critical"]?.jsonPrimitive?.int,
            "high" to it["high"]?.jsonPrimitive?.int,
            "medium" to it["medium"]?.jsonPrimitive?.int,
            "low" to it["low"]?.jsonPrimitive?.int
        )
    } ?: throw RuntimeException("error while parsing json")

@OptIn(ExperimentalSerializationApi::class)
private fun httpCLient() = HttpClient(CIO) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json = Json { explicitNulls = false })
    }
}

private fun requiredFromEnv(name: String) =
    System.getProperty(name)
        ?: System.getenv(name)
        ?: throw RuntimeException("unable to find '$name' in environment")
