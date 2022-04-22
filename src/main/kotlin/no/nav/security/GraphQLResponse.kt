package no.nav.security

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
class GraphQLResponse(private val rawResponse: JsonObject) {
    private val data = rawResponse["data"]?.jsonObject
    private val errors = rawResponse["errors"]?.jsonArray

    fun hasErrors() = errorMsgs().isNotEmpty()

    fun errorMsgs() = errors?.jsonArray
        ?.map { it.jsonObject["message"] }
        ?.map { it.toString() }
        ?: emptyList()

    fun repoCount() =
        data?.get("repos")?.jsonObject
            ?.get("repositoryCount")?.jsonPrimitive
            ?.intOrNull
}