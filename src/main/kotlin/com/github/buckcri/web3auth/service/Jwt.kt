package com.github.buckcri.web3auth.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.*

/**
 * This class contains a randomly generated EC256 keypair, and logic to build a JWT for an address as subject, signed with that key.
 * The key id is set to its JWK thumbprint, and provided in the signed JWT header to match the key when verifying the JWT signature.
 */
class Jwt {

    companion object {
        /**
         * Keypair to sign JWT with. NB: Will be regenerated at each application start.
         */
        var keyPair: ECKey = ECKeyGenerator(Curve.P_256).keyUse(KeyUse.SIGNATURE).keyIDFromThumbprint(true).generate()
    }

    /**
     * Create a JWT with the given account as subject.
     */
    fun buildJWT(account: String): String {

        val signer: JWSSigner = ECDSASigner(keyPair)

        val claimsSet = JWTClaimsSet.Builder()
            .subject(account)
            .issueTime(Date())
            .issuer("web3auth")
            .expirationTime(Date(Date().time + 60 * 1000))
            .jwtID(UUID.randomUUID().toString())
            .build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyPair.keyID).build(),
            claimsSet
        )

        signedJWT.sign(signer)

        return signedJWT.serialize()
    }
}