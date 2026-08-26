package com.arrowescape.game.engine

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.random.Random

/**
 * Cryptographic security and unique identifier utilities for Arrow Escape.
 * Provides PBKDF2-HMAC-SHA256 salted password hashing, UID generation, and referral codes.
 */
object SecurityUtils {

    private val secureRandom = SecureRandom()
    private const val PBKDF2_ITERATIONS = 10_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTE_LENGTH = 16

    private val ALPHANUMERIC_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()

    /**
     * Generates a cryptographically secure random salt encoded in hex.
     */
    fun generateSalt(byteLength: Int = SALT_BYTE_LENGTH): String {
        val salt = ByteArray(byteLength)
        secureRandom.nextBytes(salt)
        return bytesToHex(salt)
    }

    /**
     * Computes a PBKDF2WithHmacSHA256 hash of the password with the provided salt.
     */
    fun hashPassword(password: String, saltHex: String): String {
        val saltBytes = hexToBytes(saltHex)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return bytesToHex(hash)
    }

    /**
     * Verifies a candidate password against the stored salt and hash.
     */
    fun verifyPassword(password: String, saltHex: String, expectedHashHex: String): Boolean {
        if (password.isBlank() || saltHex.isBlank() || expectedHashHex.isBlank()) return false
        val computedHash = hashPassword(password, saltHex)
        return slowEquals(computedHash, expectedHashHex)
    }

    /**
     * Generates a unique user code / game UID (e.g. AE-849201).
     */
    fun generateUserCode(): String {
        val chars = CharArray(6) {
            ALPHANUMERIC_CHARS[secureRandom.nextInt(ALPHANUMERIC_CHARS.size)]
        }
        return "AE-${String(chars)}"
    }

    /**
     * Generates a unique referral code derived from user code or random seed (e.g. ESC-982431).
     */
    fun generateReferralCode(): String {
        val chars = CharArray(6) {
            ALPHANUMERIC_CHARS[secureRandom.nextInt(ALPHANUMERIC_CHARS.size)]
        }
        return "ESC-${String(chars)}"
    }

    /**
     * Generates a random joining bonus amount between ₹9.00 and ₹25.00 in ₹0.50 increments.
     */
    fun generateJoiningBonus(): Double {
        // Steps of 0.50 between 9.00 and 25.00 (33 possible steps: 9.0, 9.5, ..., 25.0)
        val step = Random.nextInt(0, 33)
        return 9.0 + (step * 0.5)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private fun slowEquals(a: String, b: String): Boolean {
        var diff = a.length xor b.length
        val minLen = minOf(a.length, b.length)
        for (i in 0 until minLen) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}
