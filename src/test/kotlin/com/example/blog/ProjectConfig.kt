package com.example.blog

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

class ProjectConfig: AbstractProjectConfig() {
    override val extensions = listOf(SpringExtension())
    override val globalAssertSoftly = true
}