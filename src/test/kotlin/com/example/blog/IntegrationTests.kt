package com.example.blog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class IntegrationTests(@Autowired val restClient: RestTestClient) {

    // We need the junit-platform.properties to support this and @AfterAll
    // OR: Annotate every test class with @TestInstance(Lifecycle.PER_CLASS)
    @BeforeAll
    fun setup() {
        println(">> Setup")
    }

    @Test
    fun `Assert blog page title, content and status code`() {
        println(">> Assert blog page title, content and status code")
        restClient.get().uri("/")
            .exchangeSuccessfully()
            .expectBody<String>()
            .value { assertThat(it).contains("<h1>Blog</h1>", "Blog") }
    }

    @Test
    fun `Assert article page title, content and status code`() {
        println(">> TODO")
    }

    @AfterAll
    fun teardown() {
        println(">> Tear down")
    }
}