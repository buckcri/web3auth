package com.github.buckcri.web3auth.api

import com.github.buckcri.web3auth.service.JwksService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@Tag(name = "JWKS retrieval API", description = "Use this endpoint to retrieve the public key used to sign JWTs generated by the Web3Auth API as a JWKS.")
@RestController
class JwksController(@Autowired val jwksService: JwksService) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Operation(summary = "Retrieve JWKS containing exactly the one public key used to sign JWTs generated by the Web3Auth API")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved JWKS", content = [
            (Content(mediaType = "application/json"))]),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()])]
    )
    @GetMapping(path = [".well-known/jwks.json"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun jwks(): String {

        val jwks = jwksService.jwks()

        logger.info("Provided JWKS: $jwks")

        return jwks
    }
}