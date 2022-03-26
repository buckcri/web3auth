package com.github.buckcri.web3auth.service

import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JwsTest {

    @Test
    fun testBuildJWS() {
        val jws = Jws().buildJWS("0xf00")

        val claims = Jwts.parserBuilder().setSigningKey(Jws.keyPair.public).build().parseClaimsJws(jws)

        val now = Date()
        val issuedAt = claims.body.issuedAt

        assertEquals("0xf00", claims.body.subject)
        assertEquals("web3auth", claims.body.issuer)
        assertTrue { issuedAt.equals(now) or issuedAt.before(now) }
    }
}