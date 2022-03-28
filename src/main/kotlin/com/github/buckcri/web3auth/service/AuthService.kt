package com.github.buckcri.web3auth.service

import com.github.buckcri.web3auth.model.ChallengeModel
import com.github.buckcri.web3auth.model.ResponseModel
import org.springframework.stereotype.Service
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class AuthService {

    /**
     * Used to map challenged accounts to their nonce.
     */
    internal val challenges: MutableMap<String, UUID> = ConcurrentHashMap()

    fun requestChallenge(account: String): ChallengeModel {

        val uuid = UUID.randomUUID()

        challenges[account] = uuid

        return ChallengeModel(uuid.toString(), account)
    }

    fun challengeResponse(response: ResponseModel): String {

        val nonce =
            challenges.getOrElse(response.challengedAccount) { throw IllegalArgumentException("Wrong account in response") }

        val recoveredAddress = recoverAddressFromNonceSignature(nonce.toString(), response.signedMessage)

        if (recoveredAddress != response.challengedAccount) {
            // Nonce is not removed here from challenges even though it might make sense security wise. Doing so might open up a DOS attack vector. Production systems should rate limit this service.
            throw IllegalArgumentException("Invalid signature for challenge!")
        }
        // Nonce is no longer valid after use. Remove to prevent replay attacks.
        challenges.remove(response.challengedAccount)

        return Jwt().buildJWT(recoveredAddress)
    }

    /**
     * Recover the public key (i.e. the address) used to create the given signature of the given nonce.
     * @param nonce The nonce for which the sha3 hash was computed and then signed
     * @param signature The signature of the sha3 hashed nonce
     */
    fun recoverAddressFromNonceSignature(nonce: String, signature: String): String {
        assert(signature.startsWith("0x"))

        val hex = HexFormat.of().formatHex(nonce.toByteArray())
        val messageHashed = Hash.sha3(hex)

        val messageHashBytes = Numeric.hexStringToByteArray(messageHashed)
        // Indexes assume signature string prefixed with "0x"
        val r = signature.substring(0, 66)
        val s = signature.substring(66, 130)
        val v = "0x" + signature.substring(130, 132)

        val msgBytes = ByteArray(messageHashBytes.size)

        System.arraycopy(messageHashBytes, 0, msgBytes, 0, messageHashBytes.size)

        // eth_sign does not prefix message with "\u0019Ethereum Signed Message:\n32". Recover from arbitrary hash:
        val pubKey = Sign.signedMessageHashToKey(
            msgBytes,
            Sign.SignatureData(
                Numeric.hexStringToByteArray(v)[0],
                Numeric.hexStringToByteArray(r),
                Numeric.hexStringToByteArray(s)
            )
        ).toString(16)

        return  "0x" + Keys.getAddress(pubKey)
    }

}