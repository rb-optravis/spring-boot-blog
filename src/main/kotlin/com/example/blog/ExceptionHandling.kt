package com.example.blog

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
internal class GlobalControllerExceptionHandler {

    private val logger: Logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleClientException(ex: Exception): ResponseEntity<String> {
        return ResponseEntity.status(500)
            .contentType(MediaType.TEXT_PLAIN)
            .body("Internal server error (${ex.message ?: "Unknown error"})")
            .also { logger.error("Request resulted in exception: ${ex.message}", ex) }
    }
}