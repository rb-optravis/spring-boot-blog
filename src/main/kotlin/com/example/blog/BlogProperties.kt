package com.example.blog

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("blog")
data class BlogProperties(var title: String, val banner: Banner, val defaultTenant: String) {
    data class Banner(val title: String? = null, val content: String)
}