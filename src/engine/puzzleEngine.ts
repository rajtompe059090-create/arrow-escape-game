import { Arrow, Difficulty, Direction, GridPoint, LevelData } from '../types/game';
import { calculateLevelReward } from '../services/earningsService';
export { calculateLevelReward };

export interface PathCheckResult {
  isClear: boolean;
  blockingArrowId?: string;
  blockingPoint?: GridPoint;
}

export interface SolverMetrics {
  isSolvable: boolean;
  solutionDepth: number;
  dependencyDepth: number;
  initialFreeCount: number;
  maxBranchingFactor: number;
  solutionOrder: string[];
}

/**
 * Checks if a point lies on an axis-aligned line segment between p1 and p2
 */
export function isPointOnSegment(p: GridPoint, p1: GridPoint, p2: GridPoint): boolean {
  const minX = Math.min(p1.x, p2.x);
  const maxX = Math.max(p1.x, p2.x);
  const minY = Math.min(p1.y, p2.y);
  const maxY = Math.max(p1.y, p2.y);

  if (p1.x === p2.x && p.x === p1.x) {
    return p.y >= minY && p.y <= maxY;
  }
  if (p1.y === p2.y && p.y === p1.y) {
    return p.x >= minX && p.x <= maxX;
  }
  return false;
}

/**
 * Returns all discrete integer grid points occupied by an arrow
 */
export function getAllOccupiedPoints(arrow: Arrow): GridPoint[] {
  const pointsSet = new Map<string, GridPoint>();

  for (let i = 0; i < arrow.points.length - 1; i++) {
    const p1 = arrow.points[i];
    const p2 = arrow.points[i + 1];

    const dx = Math.sign(p2.x - p1.x);
    const dy = Math.sign(p2.y - p1.y);

    let curX = p1.x;
    let curY = p1.y;

    pointsSet.set(`${curX},${curY}`, { x: curX, y: curY });
    while (curX !== p2.x || curY !== p2.y) {
      curX += dx;
      curY += dy;
      pointsSet.set(`${curX},${curY}`, { x: curX, y: curY });
    }
  }

  return Array.from(pointsSet.values());
}

/**
 * Checks whether the arrow has a clear direct exit line in its head direction.
 * Raycasts from the arrow head outward to board boundary.
 */
export function isArrowPathClear(
  arrow: Arrow,
  allArrows: Arrow[],
  gridWidth: number,
  gridHeight: number
): PathCheckResult {
  const head = arrow.points[arrow.points.length - 1];
  const dir = arrow.headDirection;

  let dx = 0;
  let dy = 0;
  switch (dir) {
    case 'UP':
      dy = -1;
      break;
    case 'DOWN':
      dy = 1;
      break;
    case 'LEFT':
      dx = -1;
      break;
    case 'RIGHT':
      dx = 1;
      break;
  }

  let checkX = head.x + dx;
  let checkY = head.y + dy;

  // Raycast until outside board bounds
  while (checkX >= 0 && checkX < gridWidth && checkY >= 0 && checkY < gridHeight) {
    const targetPoint: GridPoint = { x: checkX, y: checkY };

    for (const other of allArrows) {
      if (other.id === arrow.id) continue;

      const occupied = getAllOccupiedPoints(other);
      const isHit = occupied.some((p) => p.x === targetPoint.x && p.y === targetPoint.y);
      if (isHit) {
        return {
          isClear: false,
          blockingArrowId: other.id,
          blockingPoint: targetPoint,
        };
      }
    }

    checkX += dx;
    checkY += dy;
  }

  return { isClear: true };
}

/**
 * Finds the first free arrow that can escape immediately (useful for Hint feature)
 */
export function findFreeArrow(
  arrows: Arrow[],
  gridWidth: number,
  gridHeight: number
): Arrow | null {
  for (const arrow of arrows) {
    const check = isArrowPathClear(arrow, arrows, gridWidth, gridHeight);
    if (check.isClear) {
      return arrow;
    }
  }
  return null;
}

export function findAllFreeArrows(
  arrows: Arrow[],
  gridWidth: number,
  gridHeight: number
): Arrow[] {
  return arrows.filter((arrow) => isArrowPathClear(arrow, arrows, gridWidth, gridHeight).isClear);
}

/**
 * Full solver analysis evaluating solvability, solution depth, and blocker dependency graph depth
 */
export function analyzePuzzle(
  arrows: Arrow[],
  gridWidth: number,
  gridHeight: number
): SolverMetrics {
  if (arrows.length === 0) {
    return {
      isSolvable: true,
      solutionDepth: 0,
      dependencyDepth: 0,
      initialFreeCount: 0,
      maxBranchingFactor: 0,
      solutionOrder: [],
    };
  }

  let remaining = [...arrows];
  const solutionOrder: string[] = [];
  const initialFree = findAllFreeArrows(remaining, gridWidth, gridHeight);
  const initialFreeCount = initialFree.length;
  let maxBranchingFactor = 0;

  while (remaining.length > 0) {
    const currentFree = findAllFreeArrows(remaining, gridWidth, gridHeight);
    if (currentFree.length === 0) {
      return {
        isSolvable: false,
        solutionDepth: solutionOrder.length,
        dependencyDepth: 0,
        initialFreeCount,
        maxBranchingFactor,
        solutionOrder,
      };
    }

    maxBranchingFactor = Math.max(maxBranchingFactor, currentFree.length);
    const picked = currentFree[0];
    solutionOrder.push(picked.id);
    remaining = remaining.filter((a) => a.id !== picked.id);
  }

  // Blocker graph analysis for dependency depth
  const blockerMap = new Map<string, Set<string>>();
  for (const arrow of arrows) {
    const res = isArrowPathClear(arrow, arrows, gridWidth, gridHeight);
    if (!res.isClear && res.blockingArrowId) {
      if (!blockerMap.has(arrow.id)) {
        blockerMap.set(arrow.id, new Set());
      }
      blockerMap.get(arrow.id)!.add(res.blockingArrowId);
    }
  }

  let maxDep = 1;
  for (const arrow of arrows) {
    let curr = arrow.id;
    let depth = 1;
    const visited = new Set<string>([curr]);

    while (true) {
      const blockers = blockerMap.get(curr);
      if (!blockers) break;
      const nextBlocker = Array.from(blockers).find((b) => !visited.has(b));
      if (!nextBlocker) break;
      visited.add(nextBlocker);
      curr = nextBlocker;
      depth++;
    }
    maxDep = Math.max(maxDep, depth);
  }

  return {
    isSolvable: true,
    solutionDepth: solutionOrder.length,
    dependencyDepth: maxDep,
    initialFreeCount,
    maxBranchingFactor,
    solutionOrder,
  };
}

/**
 * Validates level data to verify it has at least one valid solution sequence
 */
export function isLevelSolvable(level: LevelData): boolean {
  return analyzePuzzle(level.arrows, level.gridWidth, level.gridHeight).isSolvable;
}

/**
 * Returns level difficulty tier based on continuous level ID across 8 tiers:
 * Levels 1–50 = Easy (₹1)
 * Levels 51–125 = Normal (₹2)
 * Levels 126–250 = Medium (₹3)
 * Levels 251–400 = Hard (₹5)
 * Levels 401–550 = Very Hard (₹10)
 * Levels 551–700 = Master (₹15)
 * Levels 701–850 = Grandmaster (₹20)
 * Levels 851+ = Legendary (₹25)
 */
export function getLevelDifficulty(levelId: number): Difficulty {
  if (levelId <= 50) return 'Easy';
  if (levelId <= 125) return 'Normal';
  if (levelId <= 250) return 'Medium';
  if (levelId <= 400) return 'Hard';
  if (levelId <= 550) return 'Very Hard';
  if (levelId <= 700) return 'Master';
  if (levelId <= 850) return 'Grandmaster';
  return 'Legendary';
}
