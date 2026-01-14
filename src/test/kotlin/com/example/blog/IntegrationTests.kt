package com.example.blog

import com.example.blog.share.api.models.CreateUser
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class IntegrationTests(
    @Autowired val restClient: RestTestClient
) : FunSpec({
    test("Assert article page title, content and status code") {
        val title = "Lorem"
        restClient.get().uri("/article/${title.toSlug()}")
            .header("X-TenantID", "tenant_1")
            .exchangeSuccessfully()
            .expectBody<String>()
            .value {
                listOf(title, "dolor sit amet").forEach { substring ->
                    it shouldContain substring
                }
            }
    }

    test("Assert blog page title, content and status code") {
        println(">> Assert blog page title, content and status code")
        restClient.get().uri("/")
            .header("X-TenantID", "tenant_1")
            .exchangeSuccessfully()
            .expectBody<String>()
            .value {
                listOf("<h1>Blog</h1>", "Lorem").forEach { substring ->
                    it shouldContain substring
                }
            }
    }

    test("Should create user") {
        // What is currently missing:
        // - Security (Check if we are allowed to use the header)
        // - Test is currently polluting the database. Transactional does not work because we are using an HTTP
        //   call.
        val createUser = CreateUser(
            username = "test_user_123",
            firstname = "test",
            lastname = "user"
        )

        restClient.post().uri("/api/user/")
            .body(createUser)
            .header("X-TenantID", "tenant_1")
            .exchangeSuccessfully()
            .expectBody<User>()
            .value {
                it.shouldNotBeNull()
                it.username shouldBe createUser.username
                it.firstname shouldBe createUser.firstname
                it.lastname shouldBe createUser.lastname
            }

    }
})