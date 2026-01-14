package com.example.blog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableConfigurationProperties(BlogProperties::class)
@EnableAsync
class BlogApplication

fun main(args: Array<String>) {
	runApplication<BlogApplication>(*args)
}
