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

export type Difficulty = 'Easy' | 'Normal' | 'Hard' | 'Very Hard' | 'Master' | 'Grandmaster' | 'Legendary';
export type DifficultyTier = Difficulty;

export interface LevelData {
  id: number;
  name: string;
  gridWidth: number;
  gridHeight: number;
  difficulty: Difficulty;
  arrows: Arrow[];
  rewardRupees: number; // 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 2.00
}

export interface EarningTransaction {
  id: string;
  title: string;
  amount: number;
  timestamp: number;
  type: 'LEVEL_REWARD' | 'DAILY_REWARD' | 'AD_BONUS' | 'HINT_REWARD';
  levelId?: number;
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
