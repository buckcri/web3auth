package com.github.buckcri.web3auth.service

import com.github.buckcri.web3auth.model.ResponseModel
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

/**
 * This class is testing #AuthService using a fixed ES256 test keypair read from application configuration.
 * The fixed keypair is not necessary for the tests to work (they will succeed even with a random keypair), but is used to provide a true repeatable test environment.
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest(@Autowired val authService: AuthService) {

	/**
	 * Set keypair used in #Jwt to fixed test keypair read from configuration
	 */
	@BeforeAll
	fun setTestKeypair() {
		val jkws = JWKSet.load(File(Thread.currentThread().contextClassLoader.getResource("testKeyPair.jwks").file))
		Jwt.keyPair = jkws.keys[0].toECKey()
	}

	companion object {
		// The following are fixed records of a valid challenge-response-flow to be used in these unit tests:
		private const val TEST_ADDRESS = "0xab5783935e2b9c8f3e3ee0751a60693bb7451b76"
		private const val TEST_NONCE = "7d78fd71-d2f9-4016-adb7-27a86b388d13"
		// TEST_MESSAGE: 0x0311be529d706f64bd182213e3ba48878453c3b6967a4b9aba3196f9def4cb03
		/** Signature for TEST_MESSAGE and TEST_ADDRESS */
		private const val TEST_SIGNED_MESSAGE = "0x553ea784390374a50b6d28fa3b957053ce7ed8b2968c251ad41ecfd4ed2e7928406328ffc72977421d4b7e8df7846d3a1db3128c2df657d897ec2a3d01cc33ae1c"
	}

	@Test
	fun testRequestChallengeWithWorkingInput() {

		val challenge = authService.requestChallenge(TEST_ADDRESS)

		assertEquals(36, challenge.nonce.length, "Wrong length for nonce '$challenge'.")
		assertEquals(challenge.challengedAccount, TEST_ADDRESS)
	}

	@Test
	fun testResponseWithWorkingInput() {

		val challenge = authService.requestChallenge(TEST_ADDRESS)
		// Because the nonce is random, and we don't want to create a signed message for that nonce in the backend just for tests (signing happens only in the frontend),
		// replace the random nonce with a fixed test nonce.
		authService.challenges[TEST_ADDRESS] = UUID.fromString(TEST_NONCE)

		val jwt = authService.challengeResponse(
			ResponseModel(
				TEST_SIGNED_MESSAGE,
				challenge.challengedAccount
			)
		)
		val signedJWT = SignedJWT.parse(jwt)

		// Check for valid signature
		val verifier: JWSVerifier = ECDSAVerifier(Jwt.keyPair.toPublicJWK())
		assertTrue(signedJWT.verify(verifier))

		// Check for correctly set key id
		assertEquals("42", signedJWT.header.keyID)

		// Check for correctly set subject
		assertEquals(TEST_ADDRESS, signedJWT.jwtClaimsSet.subject)
	}


	/**
	 * Test for replay attacks: Technically valid signature, but account in signature is not challenged
	 */
	@Test
	fun testResponseWithWrongAccount() {

		assertFails {authService.challengeResponse(
			ResponseModel(
				TEST_SIGNED_MESSAGE,
				"0xf00"
			)
		)
		}
	}

	/**
	 * Test for replay attacks: Technically valid signature, but nonce in signature is not challenged (no nonce set for address)
	 */
	@Test
	fun testResponseWithMissingNonce() {
		assertFails {authService.challengeResponse(
			ResponseModel(
				"0x15334b7deb92ac8983e09992df2276fc1aad54fddbe3d20d94286706ebe83440100e47aa65fd4a6a2212db7183a50512b150001ad853b528aac860df15eec5421c",
				TEST_ADDRESS
			))
		}
	}

	/**
	 * Test for replay attacks: Technically valid signature, but nonce in signature is not challenged (other nonce set for address)
	 */
	@Test
	fun testResponseWithWrongNonce() {
		// Set a nonce for the account
		authService.requestChallenge(TEST_ADDRESS)
		// Response for a different nonce. This test will fail when the dynamically generated nonce accidentally matches the nonce used to generate the static test signature.
		// When this happens to you, you should consider picking up playing the lottery.
		assertFails {authService.challengeResponse(
			ResponseModel(
				TEST_SIGNED_MESSAGE,
				TEST_ADDRESS
			))
		}
	}

	@Test
	fun testRecoverAddressFromNonceSignatureWorks() {
		assertEquals(TEST_ADDRESS, authService.recoverAddressFromNonceSignature(TEST_NONCE, TEST_SIGNED_MESSAGE))
	}

	@Test
	fun testRecoverAddressFromNonceSignatureNotHexSignature() {
		// Because of substring operations, signature needs to be prefixed with "0x"
		assertFails { authService.recoverAddressFromNonceSignature(TEST_NONCE, TEST_SIGNED_MESSAGE.substring(2)) }
	}
}
