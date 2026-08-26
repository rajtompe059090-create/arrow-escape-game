export type Direction = 'UP' | 'DOWN' | 'LEFT' | 'RIGHT';

export interface GridPoint {
  x: number;
  y: number;
}

export interface Arrow {
  id: string;
  points: GridPoint[]; // Path from tail to head. points[points.length - 1] is the head.
  headDirection: Direction;
  color?: string;
}

export type Difficulty = 'Easy' | 'Normal' | 'Medium' | 'Hard' | 'Very Hard' | 'Master' | 'Grandmaster' | 'Legendary';
export type DifficultyTier = Difficulty;

export interface LevelData {
  id: number;
  name: string;
  gridWidth: number;
  gridHeight: number;
  difficulty: Difficulty;
  arrows: Arrow[];
  rewardRupees: number; // ₹1, ₹2, ₹3, ₹5, ₹10, ₹15, ₹20, ₹25
}

export interface EarningTransaction {
  id: string;
  title: string;
  amount: number;
  timestamp: number;
  type: 'LEVEL_REWARD' | 'DAILY_REWARD' | 'AD_BONUS' | 'HINT_REWARD' | 'WITHDRAWAL';
  levelId?: number;
  status?: string;
}

export interface UserStats {
  unlockedLevel: number;
  completedLevels: number[];
  earnedRupees: number; // Backwards compatible alias
  walletBalance: number;
  totalEarnings: number;
  hintsRemaining: number;
  soundEnabled: boolean;
  hapticsEnabled: boolean;
  musicEnabled?: boolean;
  theme?: 'light' | 'dark';
  notificationsEnabled?: boolean;
  dailyStreak: number;
  lastDailyRewardDate?: string;
  lastDailyRewardTimestamp?: number;
  earningHistory: EarningTransaction[];
  // Profile & Account fields
  displayName?: string;
  username?: string;
  uid?: string;
  upiId?: string;
  referralCode?: string;
  isRegistered?: boolean;
}

export interface ActiveGameState {
  levelId: number;
  remainingArrows: Arrow[];
  escapingArrowIds: string[];
  blockedArrowId: string | null;
  hintedArrowId: string | null;
  lives: number;
  maxLives: number;
  movesCount: number;
  isCompleted: boolean;
  isGameOver: boolean;
}
