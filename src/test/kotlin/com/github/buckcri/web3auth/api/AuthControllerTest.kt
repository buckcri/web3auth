package com.github.buckcri.web3auth.api

import com.github.buckcri.web3auth.model.AccountModel
import com.github.buckcri.web3auth.model.ResponseModel
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

/**
 * Thin controller layer tests focusing on controller concerns like JSON serialization and parameter validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest(@Autowired val mockMvc: MockMvc) {

    /**
     * Given a valid account
     * when requesting a challenge for that account
     * then a nonce is returned for that account
     */
    @Test
    fun testRequestChallengeIsOk() {
        mockMvc.post("/request") {
            contentType = MediaType.APPLICATION_JSON
            content = Json.encodeToString(AccountModel.serializer(), AccountModel("0xab5783935e2b9c8f3e3ee0751a60693bb7451b76"))
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.challengedAccount") { value("0xab5783935e2b9c8f3e3ee0751a60693bb7451b76") }
            jsonPath("$.nonce") { isNotEmpty() }
        }.andDo { print() }
    }

    /**
     * Given an invalid account
     * when requesting a challenge for that account
     * then a constraint violation is returned
     */
    @Test
    fun testRequestChallengeConstraintViolation() {
        mockMvc.post("/request") {
            contentType = MediaType.APPLICATION_JSON
            content = Json.encodeToString(AccountModel.serializer(), AccountModel("0xf00"))
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }.andDo { print() }
    }

    /**
     * Given a valid, but not challenged account, and an invalid signed message
     * when responding with these
     * then a corresponding error is returned
     */
    @Test
    fun testChallengeResponseBadRequest() {
        mockMvc.post("/response") {
            contentType = MediaType.APPLICATION_JSON
            content = Json.encodeToString(
                ResponseModel.serializer(),
                ResponseModel(
                    "0x0000000000000000000000000000000000000000",
                    "0x0000000000000000000000000000000000000000"
                )
            )
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
            content { string("Wrong account in response") }
        }.andDo { print() }
    }
}