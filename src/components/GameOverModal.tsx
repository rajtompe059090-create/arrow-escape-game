import React from 'react';
import { RotateCcw, Home, HeartCrack, Lightbulb } from 'lucide-react';
import { sounds } from '../utils/audio';

interface GameOverModalProps {
  levelId: number;
  onRetry: () => void;
  onHome: () => void;
}

export const GameOverModal: React.FC<GameOverModalProps> = ({
  levelId,
  onRetry,
  onHome,
}) => {
  const handleAction = (cb: () => void) => {
    sounds.playTap();
    cb();
  };

  return (
    <div
      id="game-over-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-fade-in"
    >
      <div className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-6 shadow-2xl flex flex-col items-center text-center space-y-4 border border-slate-200">
        
        {/* Broken Heart Icon */}
        <div className="w-16 h-16 rounded-3xl bg-rose-50 border border-rose-100 flex items-center justify-center text-rose-500 shadow-sm animate-bounce">
          <HeartCrack className="w-8 h-8" />
        </div>

        <div>
          <span className="text-[10px] font-bold text-rose-500 uppercase tracking-widest">
            Level {levelId}
          </span>
          <h2 className="text-2xl font-black text-slate-900 mt-0.5">
            Out of Lives
          </h2>
          <p className="text-xs text-slate-500 mt-1 max-w-[240px]">
            The path in front of the arrow was blocked by another arrow.
          </p>
        </div>

        {/* Tip Box */}
        <div className="w-full bg-blue-50 rounded-2xl p-3.5 border border-blue-100 flex items-start space-x-2.5 text-left">
          <Lightbulb className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
          <p className="text-xs text-blue-900 leading-relaxed">
            <strong className="font-bold">Pro Tip:</strong> Trace the straight line from the arrow's head to the board's edge before tapping!
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col space-y-2.5 w-full pt-1">
          <button
            id="game-over-retry-button"
            onClick={() => handleAction(onRetry)}
            className="w-full py-4 px-6 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-2xl shadow-lg shadow-blue-200 flex items-center justify-center space-x-2 text-sm transition-all"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Try Again</span>
          </button>

          <button
            id="game-over-home-button"
            onClick={() => handleAction(onHome)}
            className="py-3 px-4 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-bold rounded-2xl border border-slate-200 flex items-center justify-center space-x-2 text-xs transition-all"
          >
            <Home className="w-4 h-4" />
            <span>Main Menu</span>
          </button>
        </div>
      </div>
    </div>
  );
};
