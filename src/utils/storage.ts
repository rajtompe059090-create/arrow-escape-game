import { UserStats } from '../types/game';
import { reconcileBalances } from '../services/earningsService';

const STORAGE_KEY = 'arrow_escape_user_data_v1';

const DEFAULT_STATS: UserStats = {
  unlockedLevel: 1,
  completedLevels: [],
  earnedRupees: 0,
  walletBalance: 0,
  totalEarnings: 0,
  hintsRemaining: 3,
  soundEnabled: true,
  hapticsEnabled: true,
  musicEnabled: false,
  theme: 'light',
  notificationsEnabled: true,
  dailyStreak: 1,
  earningHistory: [],
  displayName: 'Player One',
  username: 'player_0590',
  uid: 'AE-849201',
  referralCode: 'ESC-849201',
  upiId: '',
  isRegistered: false,
};

export function loadUserStats(): UserStats {
  if (typeof window === 'undefined') return DEFAULT_STATS;
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return DEFAULT_STATS;
    const parsed = JSON.parse(raw);
    const earnedRupees = Number(parsed.earnedRupees || parsed.totalEarnings || 0);
    const walletBalance = Number(parsed.walletBalance ?? earnedRupees);
    const totalEarnings = Number(parsed.totalEarnings ?? earnedRupees);
    
    const loaded: UserStats = {
      ...DEFAULT_STATS,
      ...parsed,
      earnedRupees: totalEarnings,
      walletBalance,
      totalEarnings,
      earningHistory: Array.isArray(parsed.earningHistory) ? parsed.earningHistory : [],
    };

    return reconcileBalances(loaded);
  } catch {
    return DEFAULT_STATS;
  }
}

export function saveUserStats(stats: UserStats): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(stats));
  } catch {}
}

export function resetGameProgress(): UserStats {
  const initial = { ...DEFAULT_STATS };
  saveUserStats(initial);
  return initial;
}
