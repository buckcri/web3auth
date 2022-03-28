package com.github.buckcri.web3auth.api

import com.github.buckcri.web3auth.service.Jwt
import com.nimbusds.jose.jwk.JWKSet
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.io.File

/**
 * Thin controller layer tests focusing on controller concerns like JSON serialization and parameter validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwksControllerTest(@Autowired val mockMvc: MockMvc) {

    /**
     * Set keypair used in #Jwt to fixed test keypair read from configuration
     */
    @BeforeAll
    fun setTestKeypair() {
        val jkws = JWKSet.load(File(Thread.currentThread().contextClassLoader.getResource("testKeyPair.jwks").file))
        Jwt.keyPair = jkws.keys[0].toECKey()
        println("set keypair: ${Jwt.keyPair}")
    }

    /**
     * JWKS should contain the fixed test public key (represented by the 'x' element), but no private key (the 'd' element).
     */
    @Test
    fun testJwksRetrieval() {
        mockMvc.get("/.well-known/jwks.json")
            .andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.keys.[0].x") { value("ETmKMf5e9gou8_9CwyKJ1LScwGR0emlxQcayMEPnQjg") }
            jsonPath("$.keys.[0].d") { doesNotExist() }
        }.andDo { print() }
    }

}