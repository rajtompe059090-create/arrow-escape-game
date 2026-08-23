import { UserStats } from '../types/game';

const STORAGE_KEY = 'arrow_escape_user_data_v1';

const DEFAULT_STATS: UserStats = {
  unlockedLevel: 1,
  completedLevels: [],
  earnedRupees: 0,
  hintsRemaining: 3,
  soundEnabled: true,
  hapticsEnabled: true,
};

export function loadUserStats(): UserStats {
  if (typeof window === 'undefined') return DEFAULT_STATS;
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return DEFAULT_STATS;
    const parsed = JSON.parse(raw);
    return {
      ...DEFAULT_STATS,
      ...parsed,
    };
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
