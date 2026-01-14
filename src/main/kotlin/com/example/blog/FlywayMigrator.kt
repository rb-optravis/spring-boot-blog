package com.example.blog

import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class FlywayMigrator(
    private val multitenantConfiguration: MultitenantConfiguration
) {
    private val logger: Logger = LoggerFactory.getLogger(FlywayMigrator::class.java)

    @PostConstruct
    fun migrateAllTenants() {
        val dataSourceMap = multitenantConfiguration.dataSource() as MultitenantDataSource
        val tenantDataSources = dataSourceMap.resolvedDataSources

        tenantDataSources.forEach { (tenantName, dataSource) ->
            logger.info("Running Flyway migration for tenant: $tenantName")
            val flyway = Flyway.configure()
                .dataSource(dataSource as DataSource)
                .load()
            flyway.migrate()
        }
    }
}