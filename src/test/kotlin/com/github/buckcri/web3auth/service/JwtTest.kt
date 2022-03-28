package com.github.buckcri.web3auth.service

import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JwtTest {

    @Test
    fun testBuildJWT() {
        val jwt = Jwt().buildJWT("0xf00")

        val claims = SignedJWT.parse(jwt).jwtClaimsSet

        val now = Date()
        val issuedAt = claims.issueTime

        assertEquals("0xf00", claims.subject)
        assertEquals("web3auth", claims.issuer)
        assertTrue { issuedAt.equals(now) or issuedAt.before(now) }
    }
}