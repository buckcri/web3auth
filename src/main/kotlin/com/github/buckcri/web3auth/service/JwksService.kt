package com.github.buckcri.web3auth.service

import com.nimbusds.jose.jwk.JWKSet
import org.springframework.stereotype.Service

@Service
class JwksService {

    /**
     * Returns a JWKS containing only the public key used for signing JWTs
     */
    fun jwks(): String {
        return JWKSet(Jwt.keyPair).toString(true)
    }

}