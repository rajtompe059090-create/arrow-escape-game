import React, { useEffect } from 'react';
import confetti from 'canvas-confetti';
import { ArrowRight, RotateCcw, Home, Award, Sparkles, Star, Trophy } from 'lucide-react';
import { sounds } from '../utils/audio';

interface LevelCompleteModalProps {
  levelId: number;
  rewardRupees: number;
  remainingLives: number;
  hasNextLevel: boolean;
  onNextLevel: () => void;
  onReplay: () => void;
  onHome: () => void;
}

export const LevelCompleteModal: React.FC<LevelCompleteModalProps> = ({
  levelId,
  rewardRupees,
  remainingLives,
  hasNextLevel,
  onNextLevel,
  onReplay,
  onHome,
}) => {
  useEffect(() => {
    // Fire festive celebratory confetti
    try {
      confetti({
        particleCount: 75,
        spread: 65,
        origin: { y: 0.6 },
        colors: ['#0284C7', '#38BDF8', '#F59E0B', '#10B981', '#EC4899'],
      });
    } catch {}
  }, []);

  const handleAction = (cb: () => void) => {
    sounds.playTap();
    cb();
  };

  return (
    <div
      id="level-complete-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-fade-in"
    >
      <div className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-6 shadow-2xl flex flex-col items-center text-center space-y-4 border border-slate-200">
        {/* Animated Trophy Banner */}
        <div className="relative flex items-center justify-center w-20 h-20 bg-blue-600 rounded-3xl shadow-xl shadow-blue-200 text-white">
          <Trophy className="w-10 h-10 animate-bounce" />
          <Sparkles className="absolute -top-2 -right-2 w-6 h-6 text-amber-300 fill-amber-300 animate-spin" />
        </div>

        <div>
          <span className="text-[10px] font-bold text-blue-600 uppercase tracking-widest">
            Level {levelId} Cleared
          </span>
          <h2 className="text-2xl font-black text-slate-900 mt-0.5">
            Brilliant Escape!
          </h2>
        </div>

        {/* 3 Stars rating based on remaining lives */}
        <div className="flex items-center space-x-1.5 py-1">
          {[1, 2, 3].map(starIndex => {
            const hasStar = starIndex <= remainingLives;
            return (
              <Star
                key={starIndex}
                className={`w-7 h-7 transition-all ${
                  hasStar
                    ? 'text-amber-400 fill-amber-400 scale-110'
                    : 'text-slate-200 fill-slate-200 scale-90'
                }`}
              />
            );
          })}
        </div>

        {/* Level Reward Card */}
        <div className="w-full bg-green-50 border border-green-200 rounded-2xl p-4 flex items-center justify-between">
          <div className="flex items-center space-x-2.5 text-left">
            <div className="p-2 bg-green-100/80 rounded-xl">
              <Award className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <p className="text-[10px] font-bold text-green-800 uppercase tracking-wider">Reward Earned</p>
              <p className="text-xs text-green-700 font-medium">To Offline Wallet</p>
            </div>
          </div>
          <div className="text-xl font-black text-green-700">
            +₹{rewardRupees}.00
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col space-y-2.5 w-full pt-1">
          {hasNextLevel ? (
            <button
              id="complete-next-button"
              onClick={() => handleAction(onNextLevel)}
              className="w-full py-4 px-6 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-2xl shadow-lg shadow-blue-200 flex items-center justify-center space-x-2 text-sm transition-all"
            >
              <span>Play Next Level</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          ) : (
            <div className="p-3 bg-blue-50 border border-blue-200 rounded-2xl text-xs font-bold text-blue-800">
              🎉 Congratulations! You have solved all 20 levels!
            </div>
          )}

          <div className="grid grid-cols-2 gap-2.5 w-full">
            <button
              id="complete-replay-button"
              onClick={() => handleAction(onReplay)}
              className="py-3 px-4 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-bold rounded-2xl border border-slate-200 flex items-center justify-center space-x-1.5 text-xs transition-all"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Replay</span>
            </button>

            <button
              id="complete-home-button"
              onClick={() => handleAction(onHome)}
              className="py-3 px-4 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-bold rounded-2xl border border-slate-200 flex items-center justify-center space-x-1.5 text-xs transition-all"
            >
              <Home className="w-4 h-4" />
              <span>Main Menu</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
