package com.example.blog

import io.kotest.core.spec.style.FunSpec
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.data.jdbc.core.mapping.AggregateReference
import org.springframework.data.repository.findByIdOrNull


@DataJdbcTest
class RepositoriesTest(
    @Autowired val userRepository: UserRepository,
    @Autowired val articleRepository: ArticleRepository
) : FunSpec({
    lateinit var johnDoe: User

    beforeSpec {
        johnDoe = userRepository.save(User("johnDoe", "John", "Doe"))
    }

    test("When findByIdOrNull then return Article") {
        val article = articleRepository.save(
            Article("Lorem", "Lorem", "dolor sit amet", AggregateReference.to(johnDoe.id!!))
        )
        val found = articleRepository.findByIdOrNull(article.id!!)
        assertThat(found).isEqualTo(article)
    }

    test("When findByUsername then return User") {
        val user = userRepository.findByUsername(johnDoe.username)
        assertThat(user).isEqualTo(johnDoe)
    }
})
