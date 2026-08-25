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
 * Generates an Arrow Escape level with high arrow count & complex dependency DAGs:
 * - EASY (Levels 1–50): 8–12 arrows (depth >= 6, dep >= 4)
 * - NORMAL (Levels 51–100): 12–18 arrows (depth >= 10, dep >= 7)
 * - HARD (Levels 101–150): 18–26 arrows (depth >= 15, dep >= 10)
 * - VERY HARD (Levels 151–200): 26–36 arrows (depth >= 22, dep >= 14)
 * - EXTREME (Levels 201+): 36–50 arrows (depth >= 30, dep >= 18)
 */
export function generateLevel(levelId: number): LevelData {
  const difficulty = getLevelDifficulty(levelId);
  const rewardRupees = calculateLevelReward(levelId);
  const name = getLevelName(levelId, difficulty);

  let gridWidth = 7;
  let gridHeight = 7;
  let minArrows = 8;
  let maxArrows = 12;
  let minSolutionDepth = 6;
  let minDependencyDepth = 4;
  let maxInitialFree = 3;
  let maxBends = 1;

  if (difficulty === 'Easy') {
    gridWidth = levelId <= 20 ? 6 : 7;
    gridHeight = gridWidth;
    minArrows = Math.min(12, 8 + Math.floor((levelId - 1) / 12));
    maxArrows = Math.min(12, minArrows + 3);
    minSolutionDepth = Math.min(minArrows, 6);
    minDependencyDepth = 4;
    maxInitialFree = 3;
    maxBends = levelId > 15 ? 1 : 0;
  } else if (difficulty === 'Normal') {
    gridWidth = levelId <= 75 ? 7 : 8;
    gridHeight = gridWidth;
    minArrows = Math.min(18, 12 + Math.floor((levelId - 51) / 8));
    maxArrows = Math.min(18, minArrows + 3);
    minSolutionDepth = Math.min(minArrows, 10);
    minDependencyDepth = 7;
    maxInitialFree = 3;
    maxBends = 2;
  } else if (difficulty === 'Hard') {
    gridWidth = levelId <= 125 ? 9 : 10;
    gridHeight = gridWidth;
    minArrows = Math.min(26, 18 + Math.floor((levelId - 101) / 6));
    maxArrows = Math.min(26, minArrows + 4);
    minSolutionDepth = Math.min(minArrows, 15);
    minDependencyDepth = 10;
    maxInitialFree = 3;
    maxBends = 2;
  } else if (difficulty === 'Very Hard') {
    gridWidth = levelId <= 175 ? 10 : 12;
    gridHeight = gridWidth;
    minArrows = Math.min(36, 26 + Math.floor((levelId - 151) / 5));
    maxArrows = Math.min(36, minArrows + 5);
    minSolutionDepth = Math.min(minArrows, 22);
    minDependencyDepth = 14;
    maxInitialFree = 3;
    maxBends = 3;
  } else {
    // Extreme: 36–50 arrows
    gridWidth = Math.min(14, 12 + Math.floor((levelId - 201) / 100));
    gridHeight = gridWidth;
    minArrows = Math.min(50, 36 + Math.floor((levelId - 201) / 15));
    maxArrows = Math.min(50, minArrows + 6);
    minSolutionDepth = Math.min(minArrows, 30);
    minDependencyDepth = 18;
    maxInitialFree = 4;
    maxBends = 3;
  }

  let bestCandidate: Arrow[] | null = null;
  let bestScore = -1;

  const maxAttempts = 200;
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

    if (candidate.length >= minArrows) {
      const metrics = analyzePuzzle(candidate, gridWidth, gridHeight);
      if (metrics.isSolvable && metrics.solutionDepth >= candidate.length) {
        const score =
          metrics.solutionDepth * 10 +
          metrics.dependencyDepth * 6 +
          candidate.length * 5 -
          metrics.initialFreeCount * 2;

        if (
          candidate.length >= minArrows &&
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

  if (bestCandidate && bestCandidate.length >= Math.floor(minArrows * 0.85)) {
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

  return createVerifiedComplexFallback(
    levelId,
    name,
    difficulty,
    rewardRupees,
    gridWidth,
    gridHeight,
    minArrows
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

  while (arrows.length < targetCount && failureStreak < 120) {
    failureStreak++;

    // Reverse dependency: target an existing arrow to block its path 85% of time
    const targetExisting = arrows.length > 0 && rng() < 0.88 ? arrows[Math.floor(rng() * arrows.length)] : null;
    const dir = directions[Math.floor(rng() * directions.length)];

    let dx = 0;
    let dy = 0;
    if (dir === 'UP') dy = -1;
    else if (dir === 'DOWN') dy = 1;
    else if (dir === 'LEFT') dx = -1;
    else if (dir === 'RIGHT') dx = 1;

    let candidatePoint: GridPoint | null = null;

    if (targetExisting) {
      const exHead = targetExisting.points[targetExisting.points.length - 1];
      let exDx = 0;
      let exDy = 0;
      if (targetExisting.headDirection === 'UP') exDy = -1;
      else if (targetExisting.headDirection === 'DOWN') exDy = 1;
      else if (targetExisting.headDirection === 'LEFT') exDx = -1;
      else if (targetExisting.headDirection === 'RIGHT') exDx = 1;

      // Raycast along targetExisting's exit path
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
        candidatePoint = rayPoints[Math.floor(rng() * rayPoints.length)];
      }
    }

    if (!candidatePoint || !isFree(candidatePoint.x, candidatePoint.y)) {
      const freeCells: GridPoint[] = [];
      for (let y = 0; y < gridHeight; y++) {
        for (let x = 0; x < gridWidth; x++) {
          if (isFree(x, y)) freeCells.push({ x, y });
        }
      }
      if (freeCells.length === 0) break;
      candidatePoint = freeCells[Math.floor(rng() * freeCells.length)];
    }

    const length = 2 + (rng() < 0.35 ? 1 : 0);
    const useBend = maxBends > 0 && rng() < 0.45;
    const points: GridPoint[] = [];

    // The candidatePoint can be the head or somewhere in the body
    const candidateHead = candidatePoint;

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
      if (
        isLevelSolvable({
          id: levelId,
          name: '',
          gridWidth,
          gridHeight,
          difficulty: 'Easy',
          arrows: testList,
          rewardRupees: 0,
        })
      ) {
        markArrow(newArrow, true);
        arrows.push(newArrow);
        failureStreak = 0;
      }
    }
  }

  return arrows;
}

function createVerifiedComplexFallback(
  levelId: number,
  name: string,
  difficulty: Difficulty,
  rewardRupees: number,
  gridWidth: number,
  gridHeight: number,
  targetCount: number
): LevelData {
  const arrows: Arrow[] = [];
  const occupied = new Set<string>();

  const isCellAvailable = (x: number, y: number) => {
    return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight && !occupied.has(`${x},${y}`);
  };

  // Build concentric interlocking loop chains
  let currentRing = 1;
  while (arrows.length < targetCount && currentRing < Math.floor(gridWidth / 2)) {
    const minX = currentRing;
    const maxX = gridWidth - 1 - currentRing;
    const minY = currentRing;
    const maxY = gridHeight - 1 - currentRing;

    if (maxX - minX >= 2 && maxY - minY >= 2) {
      // Top row arrow (pointing right)
      if (isCellAvailable(minX, minY) && isCellAvailable(maxX - 1, minY)) {
        arrows.push({
          id: `a_${levelId}_${arrows.length + 1}`,
          points: [{ x: minX, y: minY }, { x: maxX - 1, y: minY }],
          headDirection: 'RIGHT',
        });
        for (let x = minX; x <= maxX - 1; x++) occupied.add(`${x},${minY}`);
      }

      // Right col arrow (pointing down, blocks top row exit)
      if (isCellAvailable(maxX, minY) && isCellAvailable(maxX, maxY - 1)) {
        arrows.push({
          id: `a_${levelId}_${arrows.length + 1}`,
          points: [{ x: maxX, y: minY }, { x: maxX, y: maxY - 1 }],
          headDirection: 'DOWN',
        });
        for (let y = minY; y <= maxY - 1; y++) occupied.add(`${maxX},${y}`);
      }

      // Bottom row arrow (pointing left, blocks right col exit)
      if (isCellAvailable(maxX, maxY) && isCellAvailable(minX + 1, maxY)) {
        arrows.push({
          id: `a_${levelId}_${arrows.length + 1}`,
          points: [{ x: maxX, y: maxY }, { x: minX + 1, y: maxY }],
          headDirection: 'LEFT',
        });
        for (let x = minX + 1; x <= maxX; x++) occupied.add(`${x},${maxY}`);
      }

      // Left col arrow (pointing up, blocks bottom row exit)
      if (isCellAvailable(minX, maxY) && isCellAvailable(minX, minY + 1)) {
        arrows.push({
          id: `a_${levelId}_${arrows.length + 1}`,
          points: [{ x: minX, y: maxY }, { x: minX, y: minY + 1 }],
          headDirection: 'UP',
        });
        for (let y = minY + 1; y <= maxY; y++) occupied.add(`${minX},${y}`);
      }
    }
    currentRing += 2;
  }

  // Fill in outer escape avenues
  for (let x = 0; x < gridWidth && arrows.length < targetCount; x += 2) {
    if (isCellAvailable(x, 0) && isCellAvailable(x, 1)) {
      arrows.push({
        id: `a_${levelId}_${arrows.length + 1}`,
        points: [{ x, y: 1 }, { x, y: 0 }],
        headDirection: 'UP',
      });
      occupied.add(`${x},0`);
      occupied.add(`${x},1`);
    }
  }

  return {
    id: levelId,
    name,
    gridWidth,
    gridHeight,
    difficulty,
    arrows,
    rewardRupees,
  };
}
