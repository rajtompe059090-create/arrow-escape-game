import React, { useMemo } from 'react';
import { Arrow, Direction, GridPoint } from '../types/game';

interface PuzzleBoardProps {
  gridWidth: number;
  gridHeight: number;
  arrows: Arrow[];
  escapingArrowIds: string[];
  blockedArrowId: string | null;
  hintedArrowId: string | null;
  onArrowTap: (arrow: Arrow) => void;
  disabled?: boolean;
}

export const PuzzleBoard: React.FC<PuzzleBoardProps> = ({
  gridWidth,
  gridHeight,
  arrows,
  escapingArrowIds,
  blockedArrowId,
  hintedArrowId,
  onArrowTap,
  disabled = false,
}) => {
  // Coordinate calculations
  const PADDING = 36;
  const CELL_SIZE = 40;
  const boardWidth = (gridWidth - 1) * CELL_SIZE + PADDING * 2;
  const boardHeight = (gridHeight - 1) * CELL_SIZE + PADDING * 2;

  // Converts integer grid point (x,y) to SVG coordinate (px, py)
  const toSvgCoord = (pt: GridPoint): { x: number; y: number } => {
    return {
      x: PADDING + pt.x * CELL_SIZE,
      y: PADDING + pt.y * CELL_SIZE,
    };
  };

  // Generate grid matrix dots
  const gridDots = useMemo(() => {
    const dots: { x: number; y: number; key: string }[] = [];
    for (let gx = 0; gx < gridWidth; gx++) {
      for (let gy = 0; gy < gridHeight; gy++) {
        dots.push({
          x: PADDING + gx * CELL_SIZE,
          y: PADDING + gy * CELL_SIZE,
          key: `dot-${gx}-${gy}`,
        });
      }
    }
    return dots;
  }, [gridWidth, gridHeight]);

  // Construct SVG Path `d` string with rounded turns
  const buildArrowPath = (points: GridPoint[]): string => {
    if (points.length < 2) return '';
    const svgPts = points.map(toSvgCoord);

    if (svgPts.length === 2) {
      return `M ${svgPts[0].x} ${svgPts[0].y} L ${svgPts[1].x} ${svgPts[1].y}`;
    }

    let d = `M ${svgPts[0].x} ${svgPts[0].y}`;
    for (let i = 1; i < svgPts.length - 1; i++) {
      const prev = svgPts[i - 1];
      const cur = svgPts[i];
      const next = svgPts[i + 1];

      // Round corner radius
      const r = 8;
      const dPrev = { x: prev.x - cur.x, y: prev.y - cur.y };
      const dNext = { x: next.x - cur.x, y: next.y - cur.y };

      const lenPrev = Math.hypot(dPrev.x, dPrev.y);
      const lenNext = Math.hypot(dNext.x, dNext.y);

      const actualR = Math.min(r, lenPrev / 2, lenNext / 2);

      const startCorner = {
        x: cur.x + (dPrev.x / lenPrev) * actualR,
        y: cur.y + (dPrev.y / lenPrev) * actualR,
      };

      const endCorner = {
        x: cur.x + (dNext.x / lenNext) * actualR,
        y: cur.y + (dNext.y / lenNext) * actualR,
      };

      d += ` L ${startCorner.x} ${startCorner.y} Q ${cur.x} ${cur.y} ${endCorner.x} ${endCorner.y}`;
    }

    const last = svgPts[svgPts.length - 1];
    d += ` L ${last.x} ${last.y}`;
    return d;
  };

  // Construct arrowhead polygon points
  const getArrowHeadPolygon = (headPt: GridPoint, dir: Direction): string => {
    const { x, y } = toSvgCoord(headPt);
    const headLen = 13;
    const headHalfWidth = 8;

    switch (dir) {
      case 'UP':
        return `${x},${y - 2} ${x - headHalfWidth},${y + headLen} ${x + headHalfWidth},${y + headLen}`;
      case 'DOWN':
        return `${x},${y + 2} ${x - headHalfWidth},${y - headLen} ${x + headHalfWidth},${y - headLen}`;
      case 'LEFT':
        return `${x - 2},${y} ${x + headLen},${y - headHalfWidth} ${x + headLen},${y + headHalfWidth}`;
      case 'RIGHT':
        return `${x + 2},${y} ${x - headLen},${y - headHalfWidth} ${x - headLen},${y + headHalfWidth}`;
    }
  };

  // Calculates escape translation for flying out smoothly
  const getEscapeTransform = (arrow: Arrow): string => {
    const isEscaping = escapingArrowIds.includes(arrow.id);
    if (!isEscaping) return 'translate(0px, 0px)';

    const distance = 450;
    switch (arrow.headDirection) {
      case 'UP':
        return `translateY(-${distance}px)`;
      case 'DOWN':
        return `translateY(${distance}px)`;
      case 'LEFT':
        return `translateX(-${distance}px)`;
      case 'RIGHT':
        return `translateX(${distance}px)`;
    }
  };

  return (
    <div
      id="puzzle-board-container"
      className="relative flex items-center justify-center p-1 select-none w-full max-w-md mx-auto"
      style={{ touchAction: 'manipulation' }}
    >
      <div className="relative bg-slate-50 rounded-3xl p-4 sm:p-5 shadow-sm border border-slate-100 overflow-hidden flex items-center justify-center">
        <svg
          viewBox={`0 0 ${boardWidth} ${boardHeight}`}
          className="w-full h-auto max-h-[56vh] transition-all duration-300"
          style={{ maxWidth: '100%' }}
        >
          <defs>
            {/* Pulsating hint glow filter */}
            <filter id="hint-glow" x="-30%" y="-30%" width="160%" height="160%">
              <feDropShadow dx="0" dy="0" stdDeviation="6" floodColor="#2563EB" floodOpacity="0.8" />
              <feDropShadow dx="0" dy="0" stdDeviation="12" floodColor="#60A5FA" floodOpacity="0.6" />
            </filter>

            {/* Blocked error shake glow filter */}
            <filter id="blocked-glow" x="-30%" y="-30%" width="160%" height="160%">
              <feDropShadow dx="0" dy="0" stdDeviation="5" floodColor="#EF4444" floodOpacity="0.85" />
            </filter>

            {/* Normal crisp drop shadow for arrows */}
            <filter id="arrow-shadow" x="-10%" y="-10%" width="120%" height="120%">
              <feDropShadow dx="0" dy="2" stdDeviation="2" floodColor="#0F172A" floodOpacity="0.1" />
            </filter>
          </defs>

          {/* Dot Matrix Background */}
          <g id="grid-dots" opacity="0.35">
            {gridDots.map(dot => (
              <circle
                key={dot.key}
                cx={dot.x}
                cy={dot.y}
                r="2.5"
                className="fill-slate-300 transition-colors"
              />
            ))}
          </g>

          {/* Arrows Rendering */}
          {arrows.map(arrow => {
            const isEscaping = escapingArrowIds.includes(arrow.id);
            const isBlocked = blockedArrowId === arrow.id;
            const isHinted = hintedArrowId === arrow.id;

            const pathD = buildArrowPath(arrow.points);
            const headPt = arrow.points[arrow.points.length - 1];
            const headPolygon = getArrowHeadPolygon(headPt, arrow.headDirection);

            // Color decisions
            let strokeColor = '#1E293B'; // Dark Navy/Slate default
            let filterId = 'url(#arrow-shadow)';
            let strokeWidth = 5.5;

            if (isEscaping) {
              strokeColor = '#2563EB'; // Vibrant Blue
              filterId = 'url(#hint-glow)';
            } else if (isBlocked) {
              strokeColor = '#EF4444'; // Red
              filterId = 'url(#blocked-glow)';
            } else if (isHinted) {
              strokeColor = '#2563EB'; // Blue
              filterId = 'url(#hint-glow)';
              strokeWidth = 6.5;
            }

            return (
              <g
                key={arrow.id}
                id={`arrow-group-${arrow.id}`}
                onClick={() => !disabled && onArrowTap(arrow)}
                className={`cursor-pointer transition-all duration-300 ${
                  isBlocked ? 'animate-shake' : ''
                } ${isHinted ? 'animate-pulse' : ''}`}
                style={{
                  transform: getEscapeTransform(arrow),
                  transition: isEscaping
                    ? 'transform 0.42s cubic-bezier(0.2, 0.9, 0.2, 1.2), opacity 0.42s ease'
                    : 'all 0.15s ease',
                  opacity: isEscaping ? 0.3 : 1,
                }}
              >
                {/* Generous invisible stroke for effortless touch hit-area */}
                <path
                  d={pathD}
                  fill="none"
                  stroke="transparent"
                  strokeWidth="32"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />

                {/* Visible Main Arrow Body Line */}
                <path
                  d={pathD}
                  fill="none"
                  stroke={strokeColor}
                  strokeWidth={strokeWidth}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  filter={filterId}
                  className="transition-colors duration-150"
                />

                {/* Arrow Head Triangle/Chevron */}
                <polygon
                  points={headPolygon}
                  fill={strokeColor}
                  filter={filterId}
                  className="transition-colors duration-150"
                />

                {/* Hint Halo Indicator if active */}
                {isHinted && (
                  <circle
                    cx={toSvgCoord(headPt).x}
                    cy={toSvgCoord(headPt).y}
                    r="16"
                    fill="none"
                    stroke="#60A5FA"
                    strokeWidth="2.5"
                    strokeDasharray="4 3"
                    className="animate-spin"
                    style={{ transformOrigin: `${toSvgCoord(headPt).x}px ${toSvgCoord(headPt).y}px` }}
                  />
                )}
              </g>
            );
          })}
        </svg>
      </div>
    </div>
  );
};
