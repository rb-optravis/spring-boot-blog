package com.example.blog

import com.example.blog.share.api.models.CreateUser
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class MultitenantTests(
    @Autowired val restClient: RestTestClient,
) : FunSpec({

    test("Make sure that users are only created for the current tenant.") {
        val allTenants = listOf("tenant_1", "tenant_2")

        // Create one user for each tenant.
        allTenants.forEach { tenantId ->
            val createUser = CreateUser(
                username = tenantId,
                firstname = "test",
                lastname = "user"
            )
            restClient.post().uri("/api/user/")
                .body(createUser)
                .header("X-TenantID", tenantId)
                .exchangeSuccessfully()
                .expectBody<User>()
                .value {
                    it.shouldNotBeNull()
                    it.username shouldBe createUser.username
                    it.firstname shouldBe createUser.firstname
                    it.lastname shouldBe createUser.lastname
                }
        }

        allTenants.forEach { tenantId ->
            // User should exist for the current tenant
            restClient.get().uri("/api/user/$tenantId")
                .header("X-TenantID", tenantId)
                .exchangeSuccessfully()
                .expectBody<User>()
                .value {
                    it.shouldNotBeNull()
                    it.username shouldBe tenantId
                }

            // User should not exist for the other tenants.
            val otherTenants = allTenants.filter({ tenant -> tenant != tenantId }).toList()
            otherTenants.forEach { otherTenantId ->
                restClient.get()
                    .uri("/api/user/$tenantId")
                    .header("X-TenantID", otherTenantId)
                    .exchange()
                    .expectStatus().isNotFound()
            }
        }
    }
})
