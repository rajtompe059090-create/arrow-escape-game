import { LevelData } from '../types/game';
import { generateLevel } from '../engine/levelGenerator';

// Level cache for instant access
const levelCache = new Map<number, LevelData>();

/**
 * Returns a dynamically generated, 100% solvable level for any levelId (1 to 10,000+).
 */
export function getLevel(levelId: number): LevelData {
  if (levelCache.has(levelId)) {
    return levelCache.get(levelId)!;
  }
  const level = generateLevel(levelId);
  levelCache.set(levelId, level);
  return level;
}

/**
 * Pre-populated array of initial levels for backwards compatibility with list views
 */
export const LEVELS: LevelData[] = Array.from({ length: 50 }, (_, i) => getLevel(i + 1));
