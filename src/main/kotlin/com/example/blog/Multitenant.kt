package com.example.blog

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*
import javax.sql.DataSource

object TenantContext {
    private val CURRENT_TENANT: ThreadLocal<String?> = ThreadLocal()

    @JvmStatic
    fun getCurrentTenant(): String? = CURRENT_TENANT.get()

    @JvmStatic
    fun setCurrentTenant(tenant: String) {
        CURRENT_TENANT.set(tenant)
    }
}

class MultitenantDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): String? {
        return TenantContext.getCurrentTenant()
    }
}

@Configuration
class MultitenantConfiguration(
    private val blogProperties: BlogProperties,
) {

    @Bean
    @ConfigurationProperties(prefix = "tenants")
    fun dataSource(): DataSource {

        // Load tenant files using resource resolver instead of using file paths.
        val resolver: ResourcePatternResolver = PathMatchingResourcePatternResolver()
        val resources: Array<Resource>?
        try {
            resources = resolver.getResources("classpath:/tenants/*.properties")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        if (resources.size == 0) {
            throw RuntimeException("Tenant configuration files missing!")
        }

        // Convert loaded properties file into DataSource objects.
        val tenantNameToDataSource: MutableMap<String, DataSource> = HashMap<String, DataSource>()
        for (resourceFile in resources) {
            val tenantProperties = Properties()
            val dataSourceBuilder = DataSourceBuilder.create()

            try {
                tenantProperties.load(resourceFile.inputStream)
                val tenantName = tenantProperties.getProperty("name")

                dataSourceBuilder.driverClassName(tenantProperties.getProperty("datasource.driver-class-name"))
                dataSourceBuilder.username(tenantProperties.getProperty("datasource.username"))
                dataSourceBuilder.password(tenantProperties.getProperty("datasource.password"))
                dataSourceBuilder.url(tenantProperties.getProperty("datasource.url"))
                val dataSource = dataSourceBuilder.build() ?: throw RuntimeException("DataSource could not be created!")
                tenantNameToDataSource[tenantName] = dataSource
            } catch (exp: IOException) {
                throw RuntimeException("Problem in tenant datasource:$exp")
            }
        }

        val dataSource: AbstractRoutingDataSource = MultitenantDataSource()
        // We need a null check here because
        val defaultDataSource = tenantNameToDataSource[blogProperties.defaultTenant]
            ?: throw RuntimeException("Default DataSource not found.")
        dataSource.setDefaultTargetDataSource(defaultDataSource)
        dataSource.setTargetDataSources(tenantNameToDataSource as MutableMap<Any, Any>)
        dataSource.afterPropertiesSet()
        return dataSource
    }
}

@Component
@Order(1)
internal class TenantFilter(
    private val blogProperties: BlogProperties,
) : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest?, response: ServletResponse?,
        chain: FilterChain
    ) {
        val req = request as HttpServletRequest
        val tenantHeader = req.getHeader("X-TenantID")
        val tenantName = if (tenantHeader != null) tenantHeader else {
            val defaultTenant = blogProperties.defaultTenant
            println("No [X-TenantID] header. Using default tenant: $defaultTenant")
            defaultTenant
        }
        TenantContext.setCurrentTenant(tenantName)

        try {
            chain.doFilter(request, response)
        } finally {
            TenantContext.setCurrentTenant("")
        }
    }
}