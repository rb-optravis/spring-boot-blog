package com.example.blog

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.core.mapping.AggregateReference
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("article")
data class Article(
    val title: String,
    val headline: String,
    val content: String,
    // Used for the foregin key relationship. The AggregateReference
    // stores an ID instead of the user object.
    @Column("AUTHOR_ID")
    val author: AggregateReference<User, Long>,
    // Define fields with default values last, which makes it possible
    // to omit them when using positional arguments.
    val slug: String = title.toSlug(),
    val addedAt: LocalDateTime = LocalDateTime.now(),
    @Id val id: Long? = null
)

@Table("users")
data class User(
    val username: String,
    val firstname: String,
    val lastname: String,
    val description: String? = null,
    @Id val id: Long? = null
)

