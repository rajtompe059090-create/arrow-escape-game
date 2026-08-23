import { Arrow, Direction, GridPoint, LevelData } from '../types/game';

/**
 * Checks if a point is occupied by any segment of any arrow in the list (excluding optional ignored arrow)
 */
export function isPointOccupied(
  point: GridPoint,
  arrows: Arrow[],
  excludeArrowId?: string
): boolean {
  for (const arrow of arrows) {
    if (excludeArrowId && arrow.id === excludeArrowId) continue;
    for (let i = 0; i < arrow.points.length - 1; i++) {
      const p1 = arrow.points[i];
      const p2 = arrow.points[i + 1];
      if (isPointOnSegment(point, p1, p2)) {
        return true;
      }
    }
  }
  return false;
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
): { isClear: boolean; blockingArrowId?: string; blockingPoint?: GridPoint } {
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
      const isHit = occupied.some(p => p.x === targetPoint.x && p.y === targetPoint.y);
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

/**
 * Validates level data to verify it has at least one valid solution sequence
 */
export function isLevelSolvable(level: LevelData): boolean {
  let remaining = [...level.arrows];
  const maxSteps = remaining.length + 5;
  let steps = 0;

  while (remaining.length > 0 && steps < maxSteps) {
    const free = findFreeArrow(remaining, level.gridWidth, level.gridHeight);
    if (!free) {
      return false; // Deadlock encountered
    }
    remaining = remaining.filter(a => a.id !== free.id);
    steps++;
  }

  return remaining.length === 0;
}

/**
 * Calculates reward according to level range rule:
 * Levels 1-50: ₹2
 * Levels 51-100: ₹3
 * Levels 101-150: ₹5
 * Levels 151-200: ₹10
 * Levels 201+: ₹15
 */
export function calculateLevelReward(levelId: number): number {
  if (levelId <= 50) return 2;
  if (levelId <= 100) return 3;
  if (levelId <= 150) return 5;
  if (levelId <= 200) return 10;
  return 15;
}
