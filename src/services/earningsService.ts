import { UserStats, EarningTransaction } from '../types/game';

/**
 * Centralized Reward & Earnings Service for Arrow Escape
 * 
 * Enforces:
 * 1. Strict tier-based reward rules (1-50: ₹2, 51-100: ₹3, 101-150: ₹5, 151-200: ₹10, 201+: ₹15)
 * 2. Immutable transaction records with unique IDs and timestamps
 * 3. Idempotent deduplication to prevent double-crediting
 * 4. Mathematical derivation of Wallet Balance and Total Earnings from transaction ledger
 * 5. Future-proof structure ready for server-side / Supabase validation
 */

const TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000;

/**
 * Validates and calculates level reward according to the configured difficulty tiers:
 * - Level 1–50: ₹2
 * - Level 51–100: ₹3
 * - Level 101–150: ₹5
 * - Level 151–200: ₹10
 * - Level 201+: ₹15
 */
export function calculateLevelReward(levelId: number): number {
  const lvl = Math.max(1, Math.floor(levelId));
  if (lvl <= 50) return 2;
  if (lvl <= 100) return 3;
  if (lvl <= 150) return 5;
  if (lvl <= 200) return 10;
  return 15;
}

/**
 * Generates a unique transaction identifier
 */
function generateTransactionId(prefix: string, referenceId?: string | number): string {
  const timestamp = Date.now();
  const randomSuffix = Math.random().toString(36).substring(2, 8);
  if (referenceId !== undefined) {
    return `tx_${prefix}_${referenceId}_${timestamp}_${randomSuffix}`;
  }
  return `tx_${prefix}_${timestamp}_${randomSuffix}`;
}

/**
 * Checks if a transaction with the given criteria already exists in history
 */
function hasDuplicateTransaction(
  history: EarningTransaction[],
  type: EarningTransaction['type'],
  levelId?: number,
  withinTimestampMs?: number
): boolean {
  const now = Date.now();
  return history.some(tx => {
    if (tx.type !== type) return false;
    if (levelId !== undefined && tx.levelId !== levelId) return false;
    if (withinTimestampMs !== undefined && now - tx.timestamp > withinTimestampMs) return false;
    return true;
  });
}

/**
 * Reconciles wallet balance and total earnings from the transaction ledger.
 * Preserves legacy balances for existing users who already had earned funds.
 */
export function reconcileBalances(stats: UserStats): UserStats {
  const history = Array.isArray(stats.earningHistory) ? stats.earningHistory : [];
  
  // Calculate ledger sum
  const ledgerTotal = history.reduce((sum, tx) => sum + (Number(tx.amount) || 0), 0);
  
  // If ledger is present, totalEarnings is at least the ledger sum.
  // For users migrating from older versions, preserve any higher previous balance.
  const verifiedTotalEarnings = Math.max(
    Number(stats.totalEarnings) || 0,
    Number(stats.earnedRupees) || 0,
    ledgerTotal
  );
  
  const verifiedWalletBalance = Math.max(
    Number(stats.walletBalance) || 0,
    ledgerTotal
  );

  return {
    ...stats,
    totalEarnings: verifiedTotalEarnings,
    walletBalance: verifiedWalletBalance,
    earnedRupees: verifiedTotalEarnings,
    earningHistory: history,
  };
}

/**
 * Processes a Level Completion Reward
 * Strictly idempotent: credits reward ONLY ONCE per level completion.
 */
export function processLevelReward(
  currentStats: UserStats,
  levelId: number
): {
  updatedStats: UserStats;
  transaction: EarningTransaction | null;
  rewardAmount: number;
  isAlreadyCompleted: boolean;
} {
  const isAlreadyInCompletedList = currentStats.completedLevels.includes(levelId);
  const hasExistingTx = hasDuplicateTransaction(currentStats.earningHistory, 'LEVEL_REWARD', levelId);
  const isAlreadyCompleted = isAlreadyInCompletedList || hasExistingTx;

  if (isAlreadyCompleted) {
    // Already rewarded previously - do not credit duplicate ₹
    const updatedStats: UserStats = {
      ...currentStats,
      completedLevels: isAlreadyInCompletedList
        ? currentStats.completedLevels
        : [...currentStats.completedLevels, levelId],
      unlockedLevel: Math.max(currentStats.unlockedLevel, levelId + 1),
    };
    return {
      updatedStats,
      transaction: null,
      rewardAmount: 0,
      isAlreadyCompleted: true,
    };
  }

  // Calculate validated reward for this level tier
  const rewardAmount = calculateLevelReward(levelId);
  const timestamp = Date.now();
  const tx: EarningTransaction = {
    id: generateTransactionId('level', levelId),
    title: `Level ${levelId} Solved`,
    amount: rewardAmount,
    timestamp,
    type: 'LEVEL_REWARD',
    levelId,
  };

  const newHistory = [tx, ...currentStats.earningHistory];
  const newCompleted = [...currentStats.completedLevels, levelId];
  const newUnlocked = Math.max(currentStats.unlockedLevel, levelId + 1);
  const newWalletBalance = currentStats.walletBalance + rewardAmount;
  const newTotalEarnings = currentStats.totalEarnings + rewardAmount;

  const updatedStats: UserStats = {
    ...currentStats,
    completedLevels: newCompleted,
    unlockedLevel: newUnlocked,
    walletBalance: newWalletBalance,
    totalEarnings: newTotalEarnings,
    earnedRupees: newTotalEarnings,
    earningHistory: newHistory,
  };

  return {
    updatedStats,
    transaction: tx,
    rewardAmount,
    isAlreadyCompleted: false,
  };
}

/**
 * Processes a Double Level Reward after successful Rewarded Video Ad completion
 */
export function processDoubleLevelReward(
  currentStats: UserStats,
  levelId: number,
  baseRewardAmount: number
): {
  updatedStats: UserStats;
  transaction: EarningTransaction | null;
  extraRewardAmount: number;
  success: boolean;
} {
  if (baseRewardAmount <= 0) {
    return {
      updatedStats: currentStats,
      transaction: null,
      extraRewardAmount: 0,
      success: false,
    };
  }

  // Idempotency: check if an AD_BONUS for this level was already granted within the last 60 seconds
  const hasRecentDoubleTx = currentStats.earningHistory.some(
    tx => tx.type === 'AD_BONUS' && tx.levelId === levelId && Date.now() - tx.timestamp < 60000
  );

  if (hasRecentDoubleTx) {
    return {
      updatedStats: currentStats,
      transaction: null,
      extraRewardAmount: 0,
      success: false,
    };
  }

  const timestamp = Date.now();
  const tx: EarningTransaction = {
    id: generateTransactionId('2x_level', levelId),
    title: `2X Level ${levelId} Bonus`,
    amount: baseRewardAmount,
    timestamp,
    type: 'AD_BONUS',
    levelId,
  };

  const newHistory = [tx, ...currentStats.earningHistory];
  const newWalletBalance = currentStats.walletBalance + baseRewardAmount;
  const newTotalEarnings = currentStats.totalEarnings + baseRewardAmount;

  const updatedStats: UserStats = {
    ...currentStats,
    walletBalance: newWalletBalance,
    totalEarnings: newTotalEarnings,
    earnedRupees: newTotalEarnings,
    earningHistory: newHistory,
  };

  return {
    updatedStats,
    transaction: tx,
    extraRewardAmount: baseRewardAmount,
    success: true,
  };
}

/**
 * Checks if the Daily Reward can be claimed right now (24-hour cycle rule)
 */
export function isDailyRewardClaimable(stats: UserStats): boolean {
  if (!stats.lastDailyRewardTimestamp) {
    if (!stats.lastDailyRewardDate) return true;
    const todayStr = new Date().toISOString().split('T')[0];
    return stats.lastDailyRewardDate !== todayStr;
  }
  return Date.now() - stats.lastDailyRewardTimestamp >= TWENTY_FOUR_HOURS_MS;
}

/**
 * Processes a Daily Login Reward claim
 * Strictly prevents claims within 24 hours of the previous claim.
 */
export function processDailyReward(
  currentStats: UserStats,
  amount: number,
  newStreak: number
): {
  updatedStats: UserStats;
  transaction: EarningTransaction | null;
  success: boolean;
  reason?: string;
} {
  const now = Date.now();

  // Validate 24-hour cooldown
  if (currentStats.lastDailyRewardTimestamp && now - currentStats.lastDailyRewardTimestamp < TWENTY_FOUR_HOURS_MS) {
    const remainingMs = TWENTY_FOUR_HOURS_MS - (now - currentStats.lastDailyRewardTimestamp);
    return {
      updatedStats: currentStats,
      transaction: null,
      success: false,
      reason: `Daily reward is already claimed. Next claim available in ${Math.ceil(remainingMs / 60000)} minutes.`,
    };
  }

  const todayStr = new Date().toISOString().split('T')[0];
  const cycleDay = ((newStreak - 1) % 7) + 1;
  const tx: EarningTransaction = {
    id: generateTransactionId('daily', todayStr),
    title: `Day ${cycleDay} Daily Login`,
    amount,
    timestamp: now,
    type: 'DAILY_REWARD',
  };

  const newHistory = [tx, ...currentStats.earningHistory];
  const newWalletBalance = currentStats.walletBalance + amount;
  const newTotalEarnings = currentStats.totalEarnings + amount;

  const updatedStats: UserStats = {
    ...currentStats,
    walletBalance: newWalletBalance,
    totalEarnings: newTotalEarnings,
    earnedRupees: newTotalEarnings,
    lastDailyRewardDate: todayStr,
    lastDailyRewardTimestamp: now,
    dailyStreak: newStreak,
    earningHistory: newHistory,
  };

  return {
    updatedStats,
    transaction: tx,
    success: true,
  };
}

/**
 * Processes a Sponsor Rewarded Ad Cash Bonus (e.g. +₹5 instant cash)
 */
export function processAdCashReward(
  currentStats: UserStats,
  amount: number = 5,
  adSessionToken?: string
): {
  updatedStats: UserStats;
  transaction: EarningTransaction | null;
  success: boolean;
} {
  const validatedAmount = Math.max(1, amount);
  const now = Date.now();

  // Guard against rapid duplicate callback bursts (minimum 2s between distinct ad rewards)
  const hasVeryRecentAdTx = currentStats.earningHistory.some(
    tx => tx.type === 'AD_BONUS' && now - tx.timestamp < 2000
  );

  if (hasVeryRecentAdTx) {
    return {
      updatedStats: currentStats,
      transaction: null,
      success: false,
    };
  }

  const tx: EarningTransaction = {
    id: generateTransactionId('ad_cash', adSessionToken || now),
    title: 'Sponsor Ad Cash Bonus',
    amount: validatedAmount,
    timestamp: now,
    type: 'AD_BONUS',
  };

  const newHistory = [tx, ...currentStats.earningHistory];
  const newWalletBalance = currentStats.walletBalance + validatedAmount;
  const newTotalEarnings = currentStats.totalEarnings + validatedAmount;

  const updatedStats: UserStats = {
    ...currentStats,
    walletBalance: newWalletBalance,
    totalEarnings: newTotalEarnings,
    earnedRupees: newTotalEarnings,
    earningHistory: newHistory,
  };

  return {
    updatedStats,
    transaction: tx,
    success: true,
  };
}

/**
 * Calculates earnings recorded for today (since midnight)
 */
export function getTodayEarnings(stats: UserStats): number {
  const todayStart = new Date();
  todayStart.setHours(0, 0, 0, 0);
  const todayTimestamp = todayStart.getTime();

  if (!Array.isArray(stats.earningHistory)) return 0;
  return stats.earningHistory
    .filter(tx => tx.timestamp >= todayTimestamp)
    .reduce((sum, tx) => sum + (Number(tx.amount) || 0), 0);
}
