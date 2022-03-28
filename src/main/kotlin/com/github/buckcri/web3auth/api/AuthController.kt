package com.github.buckcri.web3auth.api

import com.github.buckcri.web3auth.model.AccountModel
import com.github.buckcri.web3auth.model.ChallengeModel
import com.github.buckcri.web3auth.model.ResponseModel
import com.github.buckcri.web3auth.service.AuthService
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@OpenAPIDefinition(
    info = Info(
        title = "Web3Auth",
        version = "1.0",
        description = "API to authenticate users with a Web3 wallet.",
        license = License(name = "AGPL-3.0", url = "https://www.gnu.org/licenses/agpl-3.0.html"),
        contact = Contact(url = "https://github.com/buckcri", name = "Christian Buck", email = "buckcri@protonmail.com")
    )
)
@Tag(name = "Web3Auth API", description = "This API can be used to authenticate users with a Web3 wallet. The client requests a challenge for its EVM address and receives a nonce to be signed with the Web3 wallet. The signed sha3 hash of the nonce is then sent back to be validated. Upon successful validation, a JWT for the challenged address is returned.")
@RestController
class AuthController(@Autowired val authService: AuthService) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Operation(summary = "Request a one-time challenge to be signed with the client's Web3 wallet.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully created a challenge for the client", content = [
            (Content(mediaType = "application/json", array = (
                    ArraySchema(schema = Schema(implementation = ChallengeModel::class, description = "Challenge containing a nonce to be signed (for be precised, the sha3 hash of the nonce needs to be signed) by the client and the challenged address")))))]),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()])]
    )
    @PostMapping(path = ["request"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun requestChallenge(@Valid @RequestBody account: AccountModel): ChallengeModel {
        val request = authService.requestChallenge(account.account)

        logger.info("Challenge requested for account ${account.account}. Nonce ${request.nonce}.")

        return request
    }

    @Operation(summary = "Submit signed signed sha3 hash of the nonce in the challenge to finish authentication. On successful validation, a JWT for the challenged account is returned.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully authenticated", content = [
            (Content(mediaType = "application/json", array = (
                    ArraySchema(schema = Schema(implementation = String::class, description = "JWT in compact serialization format")))))]),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()])]
    )
    @PostMapping(path = ["response"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun challengeResponse(@Valid @RequestBody response : ResponseModel): String {
        logger.info("Response received: ${response.signedMessage} for ${response.challengedAccount}")

        return authService.challengeResponse(response)
    }
}