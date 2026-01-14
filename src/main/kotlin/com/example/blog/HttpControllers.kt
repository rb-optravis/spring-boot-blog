package com.example.blog

import com.example.blog.share.api.models.CreateUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/article")
class ArticleController(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository
) {

    @GetMapping("/")
    fun findAll() = articleRepository.findAllByOrderByAddedAtDesc().map { it.toDto() }

    @GetMapping("/{slug}")
    fun findOne(@PathVariable slug: String) =
        articleRepository.findBySlug(slug)?.toDto()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")

    private fun Article.toDto(): ArticleDto {
        val author = userRepository.findById(author.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found") }
        return ArticleDto(slug, title, headline, content, author, addedAt.format())
    }

    data class ArticleDto(
        val slug: String,
        val title: String,
        val headline: String,
        val content: String,
        val author: User,
        val addedAt: String
    )
}

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/")
    fun findAll() = userService.getAllUsers()

    @GetMapping("/{username}")
    fun findOne(@PathVariable username: String) =
        userService.findUser(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist")

    @PostMapping("/")
    fun createUser(@RequestBody userDto: CreateUser): User {
        return userService.createUser(userDto)
    }

}