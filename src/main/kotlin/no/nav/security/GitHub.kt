package no.nav.security

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import java.net.URL

class GitHub constructor(private val http: HttpClient, private val accessToken: String? = null) {
    private val url = URL("https://api.github.com/graphql")
    private val requestBody = GitHub::class.java.getResource("/repocountquery.graphql")
        ?.readText()?.replace("\n", " ")

    suspend fun repoCount(): Int {
        val rawResponse: JsonObject = http.post(url) {
            accessToken?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
            setBody(requestBody)
        }.body()
        val gqlResponse = GraphQLResponse(rawResponse)
        if (gqlResponse.hasErrors()) {
            throw RuntimeException(gqlResponse.errorMsgs().toString())
        }
        return gqlResponse.repoCount() ?: throw RuntimeException("unable to read repo count")
    }

}