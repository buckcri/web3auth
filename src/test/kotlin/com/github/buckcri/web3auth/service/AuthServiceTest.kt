package com.github.buckcri.web3auth.service

import com.github.buckcri.web3auth.model.ResponseModel
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import org.junit.Before
import org.junit.jupiter.api.Test
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
class AuthServiceTest(@Autowired val authService: AuthService) {

	/**
	 * Set keypair used in #Jwt to fixed test keypair read from configuration
	 */
	@Before
	fun setTestKeypair() {
		val jkws = JWKSet.load(File(Thread.currentThread().contextClassLoader.getResource("testKeyPair.jwks").file))
		Jwt.keyPair = jkws.keys[0].toECKey()
	}

	companion object {
		// The following are fixed records of a valid challenge-response-flow to be used in these unit tests:
		private const val TEST_ADDRESS = "0xab5783935e2b9c8f3e3ee0751a60693bb7451b76"
		private const val TEST_NONCE = "f70ebe3e-e55b-4766-bd00-34078b9e5721"
		/** sha3 of TEST_NONCE. Not used here, but given for completeness of test data. */
		//private const val TEST_MESSAGE = "0xc582fc7819e8c7dca92b7bd5befc0ec414518aa90fadff75eded7a5b55f8dadb"
		/** Signature for TEST_MESSAGE and TEST_ADDRESS */
		private const val TEST_SIGNED_MESSAGE = "0x873e6d413d9f4819a28193b34ba5e914d805caab84a623e92d43837c2af63b732a1b37d9ae70700d5899ae0fe67be59bb55d933805ff30962f0c4e37ff6ebb2d1b"
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
		assertEquals(Jwt.keyPair.keyID, signedJWT.header.keyID)

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
