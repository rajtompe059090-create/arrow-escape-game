package com.arrowescape.game.model

enum class TransactionType {
    LEVEL_REWARD,
    DAILY_REWARD,
    AD_BONUS,
    HINT_REWARD,
    WITHDRAWAL
}

data class EarningTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val type: TransactionType,
    val levelId: Int? = null,
    val status: String = "SUCCESS" // SUCCESS, PENDING, PROCESSING
)
