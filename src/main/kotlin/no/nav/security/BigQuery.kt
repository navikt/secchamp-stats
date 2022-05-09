package no.nav.security

import com.google.api.client.util.DateTime
import com.google.cloud.bigquery.*
import java.time.Instant
import java.util.*


class BigQuery(projectID: String) {
    private val bq = BigQueryOptions.newBuilder()
        .setProjectId(projectID)
        .build()
        .service

    private val datasetName = "snyk_issue_count"
    private val tableName = "snyk_issues"
    private val schema =
        Schema.of(
            Field.of("when_collected", StandardSQLTypeName.TIMESTAMP),
            Field.of("gh_repos", StandardSQLTypeName.INT64),
            Field.of("issues_critical", StandardSQLTypeName.INT64),
            Field.of("issues_high", StandardSQLTypeName.INT64),
            Field.of("issues_medium", StandardSQLTypeName.INT64),
            Field.of("issues_low", StandardSQLTypeName.INT64)
        )

    fun insert(record: IssueCountRecord) = runCatching {
        createTableIfNotExists()
        val rowContent = mapOf(
            "gh_repos" to record.ghRepoCount,
            "issues_critical" to record.critical,
            "issues_high" to record.high,
            "issues_medium" to record.medium,
            "issues_low" to record.low,
            "when_collected" to DateTime(record.whenCreated.toEpochMilli())
        )
        val response = bq.insertAll(InsertAllRequest.newBuilder(TableId.of(datasetName, tableName))
            .addRow(UUID.randomUUID().toString(), rowContent).build())
        if (response.hasErrors()) {
            throw RuntimeException(response.insertErrors.map { it.value.toString() }.joinToString())
        }
        1
    }

    private fun createTableIfNotExists() {
        val tableId = TableId.of(datasetName, tableName)
        val tableExists = bq.getTable(tableId) != null
        if (!tableExists) {
            val tableDefinition: TableDefinition = StandardTableDefinition.of(schema)
            val tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build()
            bq.create(tableInfo)
        }
    }
}

class IssueCountRecord(
    val ghRepoCount: Int,
    val critical: Int,
    val high: Int,
    val medium: Int,
    val low: Int,
    val whenCreated: Instant = Instant.now()
)