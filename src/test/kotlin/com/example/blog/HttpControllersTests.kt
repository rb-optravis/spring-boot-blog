package com.example.blog

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.jdbc.core.mapping.AggregateReference
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class HttpControllersTests(
    @MockkBean val userRepository: UserRepository,
    @MockkBean val articleRepository: ArticleRepository,
    @Autowired val mockMvc: MockMvc
) : FunSpec({
    val johnDoe = User("johnDoe", "John", "Doe", id = 1L)
    val authorRef = AggregateReference.to<User, Long>(1L)
    val lorem5Article = Article("Lorem", "Lorem", "dolor sit amet", authorRef)
    val ipsumArticle = Article("Ipsum", "Ipsum", "dolor sit amet", authorRef)

    beforeTest {
        every { userRepository.findById(1L) } returns Optional.of(johnDoe)
        every { articleRepository.findAllByOrderByAddedAtDesc() } returns listOf(lorem5Article, ipsumArticle)
    }

    test("should render expected page") {
        mockMvc.perform(get("/api/article/").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.[0].author.login").value(johnDoe.username))
            .andExpect(jsonPath("\$.[0].slug").value(lorem5Article.slug))
            .andExpect(jsonPath("\$.[1].author.login").value(johnDoe.username))
            .andExpect(jsonPath("\$.[1].slug").value(ipsumArticle.slug))
    }

    test("should list users") {
        val janeDoe = User("janeDoe", "Jane", "Doe")
        every { userRepository.findAll() } returns listOf(johnDoe, janeDoe)
        mockMvc.perform(get("/api/user/").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.[0].login").value(johnDoe.username))
            .andExpect(jsonPath("\$.[1].login").value(janeDoe.username))
    }

})