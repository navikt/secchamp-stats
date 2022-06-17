package no.nav.security

import com.google.cloud.Timestamp
import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert
import com.google.cloud.bigquery.Schema
import com.google.cloud.bigquery.StandardSQLTypeName
import com.google.cloud.bigquery.StandardTableDefinition
import com.google.cloud.bigquery.TableDefinition
import com.google.cloud.bigquery.TableId
import com.google.cloud.bigquery.TableInfo
import java.time.Instant
import java.util.Date
import java.util.UUID


class BigQuery(projectID: String) {
    private val bq = BigQueryOptions.newBuilder()
        .setProjectId(projectID)
        .build()
        .service

    private val datasetName = "snyk_issue_count"
    private val tableName = "issues_by_project"
    private val schema =
        Schema.of(
            Field.of("when_collected", StandardSQLTypeName.TIMESTAMP),
            Field.of("project", StandardSQLTypeName.STRING),
            Field.of("type", StandardSQLTypeName.STRING),
            Field.of("issues_critical", StandardSQLTypeName.INT64),
            Field.of("issues_high", StandardSQLTypeName.INT64),
            Field.of("issues_medium", StandardSQLTypeName.INT64),
            Field.of("issues_low", StandardSQLTypeName.INT64)
        )

    fun insert(records: List<IssueCountRecord>) = runCatching {
        createTableIfNotExists()
        val rows = records.map {
            RowToInsert.of(UUID.randomUUID().toString(), mapOf(
                "project" to it.project,
                "type" to it.type,
                "when_collected" to Timestamp.of(Date.from(it.whenCreated)),
                "issues_critical" to it.critical,
                "issues_high" to it.high,
                "issues_medium" to it.medium,
                "issues_low" to it.low,
            ))
        }

        val response = bq.insertAll(
            InsertAllRequest.newBuilder(TableId.of(datasetName, tableName))
                .setRows(rows).build()
        )
        if (response.hasErrors()) {
            throw RuntimeException(response.insertErrors.map { it.value.toString() }.joinToString())
        }
        records.size
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
    val whenCreated: Instant = Instant.now(),
    val project: String,
    val type: String,
    val critical: Int,
    val high: Int,
    val medium: Int,
    val low: Int,
)