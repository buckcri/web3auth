package com.github.buckcri.web3auth.model

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import jakarta.validation.constraints.Pattern

/** Regex to validate EIP-55 addresses with */
internal const val ACCOUNT_REGEXP = "0x[a-fA-F0-9]{40}"

@Schema(description="Model for an EVM address. May be in EIP-55 format (i.e. using uppercase and lowercase letters for checksum).")
@Serializable
data class AccountModel(@field:Pattern(regexp = ACCOUNT_REGEXP) val account: String)

@Schema(description="Challenge for the client containing the nonce to be signed and the address the nonce was requested for.")
@Serializable
data class ChallengeModel(val nonce: String, val challengedAccount: String)

/**
 * @param signedMessage Signed sha3 hash of nonce
 * @param challengedAccount Public address the nonce in signedMessage was generated for
 */
@Schema(description="Response to be sent from the client containing the signed nonce and the address the nonce was requested for.")
@Serializable
data class ResponseModel(val signedMessage: String, @field:Pattern(regexp = ACCOUNT_REGEXP) val challengedAccount: String)
