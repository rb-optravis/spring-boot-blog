package com.example.blog

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.mapping.AggregateReference

@Configuration
class BlogConfiguration {

    // This is an expression-bodied function, where ApplicationRunner is used as return value.
    // - Expression after = is returned, no return keyword needed
    // - The function return type is automatically inferred
    @Bean
    fun databaseInitializer(
        userRepository: UserRepository,
        articleRepository: ArticleRepository
    )
    // Because the ApplicationRunner only has a single abstract method we can directly overwrite it
    // with the ApplicationRunner { args -> ... } syntax. This is the short form of creating an anonymous
    // object and overriding the single function, like this:
    // object : ApplicationRunner {
    //    override fun run(args: ApplicationArguments) {
    //        ...
    //    }
    //}
    // - We don't have to use the `args -> ` syntax if we don't use the arguments.
    // - We only create the object here. The code is executed when Spring Boot calls the run function.
    // - ApplicationRunner is a functional interface (an interface with a single abstract method).
    //    - By using Kotlin's SAM conversion we can directly implement the single method with a lambda.
    = ApplicationRunner {
        // Make sure that we only create a single user with two articles.
        // We need to check first because Postgres persists the data.
        val johnDoe = User("johnDoe", "John", "Doe")
        val johnDoeSaved = userRepository.findByUsername(johnDoe.username)
            ?: userRepository.save(User("johnDoe", "John", "Doe"))

        val loremArticle = Article(
            title = "Lorem",
            headline = "Lorem",
            content = "dolor sit amet",
            author = AggregateReference.to(johnDoeSaved.id!!)
        )
        articleRepository.findBySlug(loremArticle.slug) ?: articleRepository.save(loremArticle)

        val ipsumArticle = Article(
            title = "Ipsum",
            headline = "Ipsum",
            content = "dolor sit amet",
            author = AggregateReference.to(johnDoeSaved.id)
        )
        articleRepository.findBySlug(ipsumArticle.slug) ?: articleRepository.save(ipsumArticle)
    }
}