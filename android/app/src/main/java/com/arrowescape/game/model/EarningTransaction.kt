package com.arrowescape.game.model

enum class TransactionType {
    LEVEL_REWARD,
    DAILY_REWARD,
    AD_BONUS,
    HINT_PURCHASE,
    LIFE_PURCHASE
}

data class EarningTransaction(
    val id: String,
    val title: String,
    val amount: Int,
    val timestamp: Long,
    val type: TransactionType,
    val levelId: Int? = null,
    val status: String = "SUCCESS"
)
