package com.github.buckcri.web3auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.security.KeyPair
import java.util.*

class Jws {

    companion object {
        /**
         * Keypair to sign JWS with. NB: Will be regenerated at each application start.
         */
        var keyPair: KeyPair = Keys.keyPairFor(SignatureAlgorithm.ES512)
    }

    /**
     * Create a JWS with the given account as subject.
     */
    fun buildJWS(account: String): String {
        return Jwts.builder()
            .setSubject(account)
            .setIssuedAt(Date())
            .setIssuer("web3auth")
            .setId(UUID.randomUUID().toString())
            .signWith(keyPair.private)
            .compact()
    }
}