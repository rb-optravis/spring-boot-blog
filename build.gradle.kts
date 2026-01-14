plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    id("io.kotest") version "6.0.7"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.18.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

val kotestVersion = "6.0.7"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation("org.apache.commons:commons-csv:1.12.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Flyway
    implementation("org.flywaydb:flyway-core:11.20.1")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Generated code by the OpenAPI plugin
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")


    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-mustache-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // kotest
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-engine-jvm:${kotestVersion}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")
    testImplementation("com.ninja-squad:springmockk:5.0.1")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("kotest.framework.config.fqn", "com.example.blog.ProjectConfig")
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generate-resources/src/main/kotlin")
            println("srcDir: ${layout.buildDirectory.get()}/generate-resources/src/main/kotlin")
        }
    }
}

tasks {
    compileKotlin {
        dependsOn("openApiGenerate")
    }
}


openApiGenerate {
    // We should use the kotlin-spring generator. See docs:
    // https://openapi-generator.tech/docs/generators/kotlin-spring/
    generatorName.set("kotlin-spring")
    library.set("spring-boot")
    val openapiGroup = "${rootProject.group}.blog.share.api"
    packageName.set(openapiGroup)
    modelPackage.set("$openapiGroup.models")
    inputSpec.set("$rootDir/blog.yaml")
    outputDir.set(layout.buildDirectory.file("generate-resources").get().asFile.path)

    configOptions.set(
        mapOf(
            // Can be joda, legacy, java8-localdatetime, java8 (default)
            // In flow-analyzer-backend this is 'string', which is not a valid option?
            "dateLibrary" to "java8",
            // Enums uppercase
            "enumPropertyNaming" to "UPPERCASE",
            // I cannot find this option in the docs. There is a 'library' mentioned here, but
            // I think it refers to the java generator, not the spring one.
            "serializationLibrary" to "jackson",
            // We don't have a client, so we don't need the service interfaces or stub implementations.
            "serviceInterface" to "false",
            "serviceImplementation" to "false",
            // Generate dependencies for use with Spring Boot 3
            // Use jakarta instead of javax in imports (enables useJakartaEe)
            "useSpringBoot3" to "true",
        )
    )

    // See documentation for global properties: https://openapi-generator.tech/docs/globals/
    globalProperties.apply {
        put("models", "") // This somehow makes it only generate the models!
    }
}

tasks.named("openApiGenerate") {
    doFirst {
        println("before openApiGenerate")
        val buildDir = layout.buildDirectory.file("generate-resources").get().asFile.path
        delete(buildDir)
        println("Deleted $buildDir")
    }
}

