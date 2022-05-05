package no.nav.security

import com.google.cloud.bigquery.*


class BigQuery {
    private val bq = BigQueryOptions.getDefaultInstance().service

    private val datasetName = "snyk_issue_count"
    private val tableName = "snyk_issues"
    private val schema =
        Schema.of(
            Field.of("when_collected", StandardSQLTypeName.TIMESTAMP),
            Field.of("gh_repos", StandardSQLTypeName.INT64),
            Field.of("snyk_orgs", StandardSQLTypeName.INT64),
            Field.of("issues_critical", StandardSQLTypeName.INT64),
            Field.of("issues_high", StandardSQLTypeName.INT64),
            Field.of("issues_medium", StandardSQLTypeName.INT64),
            Field.of("issues_low", StandardSQLTypeName.INT64)
        )

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