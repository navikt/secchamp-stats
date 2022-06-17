package no.nav.security

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SnykTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `responses are parseable`() {
        val expected = AllProjectsResponse(Organization("defaultOrg"), listOf(
            Project("atokeneduser/goof", "npm", true, IssueCounts(critical = 3, high = 10, medium = 15, low = 8)),
            Project("atokeneduser/clojure", "maven", false, IssueCounts(critical = 10, high = 3, medium = 21, low = 8)),
            Project("docker-image|alpine", "apk", false, IssueCounts(critical = 0, high = 0, medium = 0, low = 0))),
        )
        val actual = json.decodeFromString(AllProjectsResponse.serializer(), rawResponse)
        assertEquals(expected, actual)
    }

    @Test
    fun `projects know when they have issues`() {
        val projectWithIssues = Project("atokeneduser/clojure", "maven", false, IssueCounts(critical = 10, high = 3, medium = 21, low = 8))
        val projectWithoutIssues =
            Project("docker-image|alpine", "apk", false, IssueCounts(critical = 0, high = 0, medium = 0, low = 0))
        assertTrue(projectWithIssues.hasIssues())
        assertFalse(projectWithoutIssues.hasIssues())
    }

    private val rawResponse = """
        {
          "org": {
            "name": "defaultOrg",
            "id": "689ce7f9-7943-4a71-b704-2ba575f01089"
          },
          "projects": [
            {
              "name": "atokeneduser/goof",
              "id": "6d5813be-7e6d-4ab8-80c2-1e3e2a454545",
              "created": "2018-10-29T09:50:54.014Z",
              "origin": "cli",
              "type": "npm",
              "readOnly": false,
              "testFrequency": "daily",
              "totalDependencies": 438,
              "issueCountsBySeverity": {
                "low": 8,
                "medium": 15,
                "high": 10,
                "critical": 3
              },
              "remoteRepoUrl": "https://github.com/snyk/goof.git",
              "lastTestedDate": "2019-02-05T06:21:00.000Z",
              "importingUser": {
                "id": "e713cf94-bb02-4ea0-89d9-613cce0caed2",
                "name": "example-user@snyk.io",
                "username": "exampleUser",
                "email": "example-user@snyk.io"
              },
              "isMonitored": true,
              "owner": {
                "id": "e713cf94-bb02-4ea0-89d9-613cce0caed2",
                "name": "example-user@snyk.io",
                "username": "exampleUser",
                "email": "example-user@snyk.io"
              },
              "branch": "master",
              "tags": [
                {
                  "key": "example-tag-key",
                  "value": "example-tag-value"
                }
              ]
            },
            {
              "name": "atokeneduser/clojure",
              "id": "af127b96-6966-46c1-826b-2e79ac49bbd9",
              "created": "2018-10-29T09:50:54.014Z",
              "origin": "github",
              "type": "maven",
              "readOnly": false,
              "testFrequency": "daily",
              "totalDependencies": 42,
              "issueCountsBySeverity": {
                "low": 8,
                "medium": 21,
                "high": 3,
                "critical": 10
              },
              "remoteRepoUrl": "https://github.com/clojure/clojure.git",
              "lastTestedDate": "2019-02-05T07:01:00.000Z",
              "owner": {
                "id": "42ce0e0f-6288-4874-9266-ef799e7f31bb",
                "name": "example-user2@snyk.io",
                "username": "exampleUser2",
                "email": "example-user2@snyk.io"
              },
              "importingUser": {
                "id": "e713cf94-bb02-4ea0-89d9-613cce0caed2",
                "name": "example-user@snyk.io",
                "username": "exampleUser",
                "email": "example-user@snyk.io"
              },
              "isMonitored": false,
              "branch": "master",
              "tags": [
                {
                  "key": "example-tag-key",
                  "value": "example-tag-value"
                }
              ]
            },
            {
              "name": "docker-image|alpine",
              "id": "f6c8339d-57e1-4d64-90c1-81af0e811f7e",
              "created": "2019-02-04T08:54:07.704Z",
              "origin": "cli",
              "type": "apk",
              "readOnly": false,
              "testFrequency": "daily",
              "totalDependencies": 14,
              "issueCountsBySeverity": {
                "low": 0,
                "medium": 0,
                "high": 0,
                "critical": 0
              },
              "imageId": "sha256:caf27325b298a6730837023a8a342699c8b7b388b8d878966b064a1320043019",
              "imageTag": "latest",
              "lastTestedDate": "2019-02-05T08:54:07.704Z",
              "owner": null,
              "importingUser": null,
              "isMonitored": false,
              "branch": "master",
              "tags": [
                {
                  "key": "example-tag-key",
                  "value": "example-tag-value"
                }
              ]
            }
          ]
        }
    """.trimIndent()

}
