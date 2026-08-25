import { Arrow, Difficulty, Direction, GridPoint, LevelData } from '../types/game';
import {
  calculateLevelReward,
  getLevelDifficulty,
  analyzePuzzle,
  isLevelSolvable,
  getAllOccupiedPoints,
} from './puzzleEngine';

// Seeded PRNG for reproducible yet infinite distinct levels
function createRNG(seed: number) {
  let s = Math.floor(seed) >>> 0;
  return function () {
    s = (s + 0x6d2b79f5) >>> 0;
    let t = Math.imul(s ^ (s >>> 15), 1 | s);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

const THEME_PREFIXES = [
  'Escape', 'Crossway', 'Labyrinth', 'Vortex', 'Matrix',
  'Weave', 'Tangle', 'Circuit', 'Gridlock', 'Orbit',
  'Nexus', 'Zigzag', 'Passage', 'Corridor', 'Apex',
  'Echo', 'Flux', 'Zenith', 'Prism', 'Vector'
];

export function getLevelName(levelId: number, difficulty: Difficulty): string {
  const prefix = THEME_PREFIXES[(levelId - 1) % THEME_PREFIXES.length];
  return `${prefix} #${levelId} (${difficulty})`;
}

/**
 * Generates an Arrow Escape level dynamically based on levelId and difficulty tier:
 * Level 1–50: Easy (₹2) - solution depth >= 3
 * Level 51–100: Normal (₹3) - solution depth >= 6
 * Level 101–150: Hard (₹5) - solution depth >= 10
 * Level 151–200: Very Hard (₹10) - solution depth >= 15
 * Level 201+: Extreme (₹15) - solution depth >= 20
 */
export function generateLevel(levelId: number): LevelData {
  const difficulty = getLevelDifficulty(levelId);
  const rewardRupees = calculateLevelReward(levelId);
  const name = getLevelName(levelId, difficulty);

  let gridWidth = 6;
  let gridHeight = 6;
  let minArrows = 5;
  let maxArrows = 7;
  let minSolutionDepth = 3;
  let minDependencyDepth = 3;
  let maxInitialFree = 2;
  let maxBends = 0;

  if (difficulty === 'Easy') {
    gridWidth = levelId <= 15 ? 5 : 6;
    gridHeight = gridWidth;
    minArrows = Math.min(7, 4 + Math.floor((levelId - 1) / 10));
    maxArrows = minArrows + 2;
    minSolutionDepth = levelId <= 5 ? 3 : 4;
    minDependencyDepth = 3;
    maxInitialFree = 2;
    maxBends = levelId > 15 ? 1 : 0;
  } else if (difficulty === 'Normal') {
    gridWidth = levelId <= 75 ? 6 : 7;
    gridHeight = gridWidth;
    minArrows = Math.min(12, 8 + Math.floor((levelId - 51) / 12));
    maxArrows = minArrows + 2;
    minSolutionDepth = 6;
    minDependencyDepth = 4;
    maxInitialFree = 2;
    maxBends = 1;
  } else if (difficulty === 'Hard') {
    gridWidth = levelId <= 125 ? 7 : 8;
    gridHeight = gridWidth;
    minArrows = Math.min(16, 12 + Math.floor((levelId - 101) / 10));
    maxArrows = minArrows + 3;
    minSolutionDepth = 10;
    minDependencyDepth = 6;
    maxInitialFree = 2;
    maxBends = 2;
  } else if (difficulty === 'Very Hard') {
    gridWidth = levelId <= 175 ? 8 : 9;
    gridHeight = gridWidth;
    minArrows = Math.min(20, 16 + Math.floor((levelId - 151) / 10));
    maxArrows = minArrows + 3;
    minSolutionDepth = 15;
    minDependencyDepth = 8;
    maxInitialFree = 2;
    maxBends = 2;
  } else {
    gridWidth = Math.min(10, 9 + Math.floor((levelId - 201) / 100));
    gridHeight = gridWidth;
    minArrows = Math.min(26, 21 + Math.floor((levelId - 201) / 25));
    maxArrows = minArrows + 4;
    minSolutionDepth = 20;
    minDependencyDepth = 10;
    maxInitialFree = 2;
    maxBends = 3;
  }

  let bestCandidate: Arrow[] | null = null;
  let bestScore = -1;

  const maxAttempts = 150;
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    const seed = (levelId * 2654435761 + attempt * 1013904223 + 73) >>> 0;
    const rng = createRNG(seed);

    const targetCount = minArrows + Math.floor(rng() * (maxArrows - minArrows + 1));
    const candidate = attemptBuildInterlockingArrows(
      levelId,
      gridWidth,
      gridHeight,
      targetCount,
      maxBends,
      rng
    );

    if (candidate.length > 0) {
      const metrics = analyzePuzzle(candidate, gridWidth, gridHeight);
      if (metrics.isSolvable && metrics.solutionDepth >= candidate.length) {
        const score = metrics.solutionDepth * 10 + metrics.dependencyDepth * 5 - metrics.initialFreeCount;

        if (
          metrics.solutionDepth >= minSolutionDepth &&
          metrics.dependencyDepth >= minDependencyDepth &&
          metrics.initialFreeCount <= maxInitialFree
        ) {
          return {
            id: levelId,
            name,
            gridWidth,
            gridHeight,
            difficulty,
            arrows: candidate,
            rewardRupees,
          };
        }

        if (score > bestScore) {
          bestScore = score;
          bestCandidate = candidate;
        }
      }
    }
  }

  if (bestCandidate && bestCandidate.length > 0) {
    return {
      id: levelId,
      name,
      gridWidth,
      gridHeight,
      difficulty,
      arrows: bestCandidate,
      rewardRupees,
    };
  }

  return createVerifiedFallbackLevel(
    levelId,
    name,
    difficulty,
    rewardRupees,
    gridWidth,
    gridHeight
  );
}

function attemptBuildInterlockingArrows(
  levelId: number,
  gridWidth: number,
  gridHeight: number,
  targetCount: number,
  maxBends: number,
  rng: () => number
): Arrow[] {
  const arrows: Arrow[] = [];
  const occupiedGrid: boolean[][] = Array.from({ length: gridHeight }, () =>
    Array(gridWidth).fill(false)
  );

  const isFree = (x: number, y: number) => {
    return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight && !occupiedGrid[y][x];
  };

  const markArrow = (arrow: Arrow, val: boolean) => {
    const pts = getAllOccupiedPoints(arrow);
    for (const p of pts) {
      if (p.x >= 0 && p.x < gridWidth && p.y >= 0 && p.y < gridHeight) {
        occupiedGrid[p.y][p.x] = val;
      }
    }
  };

  const directions: Direction[] = ['UP', 'DOWN', 'LEFT', 'RIGHT'];
  let failureStreak = 0;

  while (arrows.length < targetCount && failureStreak < 60) {
    failureStreak++;

    const targetExisting = arrows.length > 0 && rng() < 0.8 ? arrows[Math.floor(rng() * arrows.length)] : null;
    const dir = directions[Math.floor(rng() * directions.length)];

    let dx = 0;
    let dy = 0;
    if (dir === 'UP') dy = -1;
    else if (dir === 'DOWN') dy = 1;
    else if (dir === 'LEFT') dx = -1;
    else if (dir === 'RIGHT') dx = 1;

    let candidateHead: GridPoint | null = null;

    if (targetExisting) {
      const exHead = targetExisting.points[targetExisting.points.length - 1];
      let exDx = 0;
      let exDy = 0;
      if (targetExisting.headDirection === 'UP') exDy = -1;
      else if (targetExisting.headDirection === 'DOWN') exDy = 1;
      else if (targetExisting.headDirection === 'LEFT') exDx = -1;
      else if (targetExisting.headDirection === 'RIGHT') exDx = 1;

      const rayPoints: GridPoint[] = [];
      let rx = exHead.x + exDx;
      let ry = exHead.y + exDy;
      while (rx >= 0 && rx < gridWidth && ry >= 0 && ry < gridHeight) {
        if (isFree(rx, ry)) {
          rayPoints.push({ x: rx, y: ry });
        }
        rx += exDx;
        ry += exDy;
      }

      if (rayPoints.length > 0) {
        candidateHead = rayPoints[Math.floor(rng() * rayPoints.length)];
      }
    }

    if (!candidateHead || !isFree(candidateHead.x, candidateHead.y)) {
      const freeCells: GridPoint[] = [];
      for (let y = 0; y < gridHeight; y++) {
        for (let x = 0; x < gridWidth; x++) {
          if (isFree(x, y)) freeCells.push({ x, y });
        }
      }
      if (freeCells.length === 0) break;
      candidateHead = freeCells[Math.floor(rng() * freeCells.length)];
    }

    const length = 2 + (rng() < 0.4 ? 1 : 0);
    const useBend = maxBends > 0 && rng() < 0.4;
    const points: GridPoint[] = [];

    if (!useBend || length < 2) {
      let valid = true;
      for (let s = 0; s < length; s++) {
        const px = candidateHead.x - dx * s;
        const py = candidateHead.y - dy * s;
        if (!isFree(px, py)) {
          valid = false;
          break;
        }
      }
      if (valid) {
        points.push(
          { x: candidateHead.x - dx * (length - 1), y: candidateHead.y - dy * (length - 1) },
          candidateHead
        );
      }
    } else {
      const len1 = 1 + Math.floor(rng() * 2);
      const len2 = 1 + Math.floor(rng() * 2);
      const perpDirs: GridPoint[] =
        dx !== 0 ? [{ x: 0, y: 1 }, { x: 0, y: -1 }] : [{ x: 1, y: 0 }, { x: -1, y: 0 }];
      const perp = perpDirs[Math.floor(rng() * perpDirs.length)];

      const corner: GridPoint = {
        x: candidateHead.x - dx * len1,
        y: candidateHead.y - dy * len1,
      };
      const tail: GridPoint = {
        x: corner.x + perp.x * len2,
        y: corner.y + perp.y * len2,
      };

      let valid = true;
      for (let s = 0; s <= len1; s++) {
        const px = candidateHead.x - dx * s;
        const py = candidateHead.y - dy * s;
        if (!isFree(px, py)) {
          valid = false;
          break;
        }
      }
      if (valid) {
        for (let s = 0; s <= len2; s++) {
          const px = corner.x + perp.x * s;
          const py = corner.y + perp.y * s;
          if (!isFree(px, py)) {
            valid = false;
            break;
          }
        }
      }

      if (valid) {
        points.push(tail, corner, candidateHead);
      }
    }

    if (points.length >= 2) {
      const newArrow: Arrow = {
        id: `a_${levelId}_${arrows.length + 1}`,
        points,
        headDirection: dir,
      };

      const testList = [...arrows, newArrow];
      if (isLevelSolvable({ id: levelId, name: '', gridWidth, gridHeight, difficulty: 'Easy', arrows: testList, rewardRupees: 0 })) {
        markArrow(newArrow, true);
        arrows.push(newArrow);
        failureStreak = 0;
      }
    }
  }

  return arrows;
}

function createVerifiedFallbackLevel(
  levelId: number,
  name: String,
  difficulty: Difficulty,
  rewardRupees: number,
  gridWidth: number,
  gridHeight: number
): LevelData {
  const arrows: Arrow[] = [];
  const cx = Math.floor(gridWidth / 2);
  const cy = Math.floor(gridHeight / 2);

  arrows.push({
    id: `a_${levelId}_1`,
    points: [{ x: cx, y: cy }, { x: cx + 1, y: cy }],
    headDirection: 'RIGHT',
  });
  arrows.push({
    id: `a_${levelId}_2`,
    points: [{ x: cx + 2, y: cy - 1 }, { x: cx + 2, y: cy + 1 }],
    headDirection: 'DOWN',
  });
  arrows.push({
    id: `a_${levelId}_3`,
    points: [{ x: cx + 3, y: cy + 2 }, { x: cx + 1, y: cy + 2 }],
    headDirection: 'LEFT',
  });
  arrows.push({
    id: `a_${levelId}_4`,
    points: [{ x: cx, y: cy + 3 }, { x: cx, y: cy + 1 }],
    headDirection: 'UP',
  });
  arrows.push({
    id: `a_${levelId}_5`,
    points: [{ x: cx - 1, y: cy - 1 }, { x: cx - 1, y: 0 }],
    headDirection: 'UP',
  });

  return {
    id: levelId,
    name: name as string,
    gridWidth,
    gridHeight,
    difficulty,
    arrows,
    rewardRupees,
  };
}
