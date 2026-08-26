package com.arrowescape.game.model

/**
 * Model representing a registered player profile in Arrow Escape.
 * Passwords must never be stored as plaintext; use SecurityUtils for password hashes and salts.
 */
data class UserProfile(
    val uid: String,
    val username: String,
    val displayName: String,
    val upiId: String = "",
    val referralCode: String,
    val referredBy: String? = null,
    val joiningBonus: Double = 0.0,
    val isJoiningBonusClaimed: Boolean = false,
    val registrationTimestamp: Long = System.currentTimeMillis(),
    val passwordHash: String = "",
    val passwordSalt: String = ""
)
