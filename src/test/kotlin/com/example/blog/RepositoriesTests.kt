package com.example.blog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.data.jdbc.core.mapping.AggregateReference
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.Test

@DataJdbcTest
class RepositoriesTests @Autowired constructor(
    val userRepository: UserRepository,
    val articleRepository: ArticleRepository
) {
    lateinit var johnDoe: User

    @BeforeAll
    fun setup(){
        johnDoe = userRepository.save(User("johnDoe", "John", "Doe"))
    }

    @Test
    fun `When findByIdOrNull then return Article`() {
        val article = articleRepository.save(
            Article("Lorem", "Lorem", "dolor sit amet", AggregateReference.to(johnDoe.id!!))
        )
        val found = articleRepository.findByIdOrNull(article.id!!)
        assertThat(found).isEqualTo(article)
    }

    @Test
    fun `When findByLogin then return User`() {
        val user = userRepository.findByLogin(johnDoe.login)
        assertThat(user).isEqualTo(johnDoe)
    }
}