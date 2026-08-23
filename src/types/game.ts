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

export type Difficulty = 'Easy' | 'Normal' | 'Hard' | 'Expert';

export interface LevelData {
  id: number;
  name: string;
  gridWidth: number;
  gridHeight: number;
  difficulty: Difficulty;
  arrows: Arrow[];
  rewardRupees: number; // e.g., 2 for levels 1-50
}

export interface UserStats {
  unlockedLevel: number;
  completedLevels: number[];
  earnedRupees: number;
  hintsRemaining: number;
  soundEnabled: boolean;
  hapticsEnabled: boolean;
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
