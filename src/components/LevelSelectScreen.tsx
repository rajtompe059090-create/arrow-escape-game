import React, { useState } from 'react';
import { ArrowLeft, Lock, Star, CheckCircle, Award } from 'lucide-react';
import { LEVELS } from '../data/levels';
import { UserStats, Difficulty } from '../types/game';
import { sounds } from '../utils/audio';

interface LevelSelectScreenProps {
  stats: UserStats;
  onSelectLevel: (levelId: number) => void;
  onBack: () => void;
}

export const LevelSelectScreen: React.FC<LevelSelectScreenProps> = ({
  stats,
  onSelectLevel,
  onBack,
}) => {
  const [selectedDifficulty, setSelectedDifficulty] = useState<Difficulty | 'All'>('All');

  const difficulties: (Difficulty | 'All')[] = ['All', 'Easy', 'Normal', 'Hard', 'Expert'];

  const filteredLevels = LEVELS.filter(lvl => {
    if (selectedDifficulty === 'All') return true;
    return lvl.difficulty === selectedDifficulty;
  });

  const handleLevelClick = (levelId: number) => {
    if (levelId <= stats.unlockedLevel) {
      sounds.playTap();
      onSelectLevel(levelId);
    }
  };

  return (
    <div
      id="level-select-screen"
      className="flex flex-col h-full w-full bg-white text-slate-900 select-none p-5"
    >
      {/* Top Header */}
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <button
          id="level-select-back-button"
          onClick={() => {
            sounds.playTap();
            onBack();
          }}
          className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 active:scale-95 flex items-center justify-center text-slate-600 transition-all"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>

        <div className="text-center">
          <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
            Select Level
          </p>
          <p className="text-xs font-bold text-blue-600">
            {stats.completedLevels.length} / 20 Cleared
          </p>
        </div>

        {/* Total Earned Badge */}
        <div className="flex items-center gap-1 px-3 py-1 bg-green-50 border border-green-200 rounded-full text-xs font-bold text-green-700">
          <Award className="w-3.5 h-3.5 text-green-600" />
          <span>₹{stats.earnedRupees}</span>
        </div>
      </div>

      {/* Difficulty Filter Chips */}
      <div className="flex items-center gap-1.5 py-3 overflow-x-auto no-scrollbar">
        {difficulties.map(diff => (
          <button
            key={diff}
            onClick={() => {
              sounds.playTap();
              setSelectedDifficulty(diff);
            }}
            className={`px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
              selectedDifficulty === diff
                ? 'bg-blue-600 text-white shadow-sm shadow-blue-200'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200 border border-slate-200'
            }`}
          >
            {diff}
          </button>
        ))}
      </div>

      {/* Level Grid */}
      <div className="py-2 grid grid-cols-4 gap-2.5 overflow-y-auto flex-1 max-w-lg mx-auto w-full">
        {filteredLevels.map(level => {
          const isUnlocked = level.id <= stats.unlockedLevel;
          const isCompleted = stats.completedLevels.includes(level.id);
          const isCurrent = level.id === stats.unlockedLevel;

          return (
            <button
              key={level.id}
              id={`level-button-${level.id}`}
              onClick={() => handleLevelClick(level.id)}
              disabled={!isUnlocked}
              className={`w-full aspect-square flex flex-col items-center justify-between p-2 rounded-2xl transition-all duration-200 ${
                isCurrent
                  ? 'bg-blue-100 text-blue-600 border border-blue-200 ring-4 ring-blue-50 font-black shadow-sm'
                  : isCompleted
                  ? 'bg-blue-500 text-white shadow-md shadow-blue-200 hover:bg-blue-600'
                  : isUnlocked
                  ? 'bg-white hover:bg-slate-50 text-slate-800 border border-slate-200 shadow-2xs active:scale-95 font-bold'
                  : 'bg-slate-50 text-slate-300 border border-slate-200 cursor-not-allowed opacity-70'
              }`}
            >
              {/* Level Number */}
              <span className="text-xs font-black">{level.id}</span>

              {/* Status Icon */}
              <div className="my-auto">
                {isCompleted ? (
                  <CheckCircle className="w-4 h-4 text-white fill-white/20" />
                ) : isCurrent ? (
                  <Star className="w-4 h-4 text-blue-600 fill-blue-500 animate-pulse" />
                ) : isUnlocked ? (
                  <span className="text-[9px] font-bold uppercase text-slate-400">
                    {level.difficulty.slice(0, 3)}
                  </span>
                ) : (
                  <Lock className="w-3.5 h-3.5 text-slate-300" />
                )}
              </div>

              {/* Reward Mini Tag */}
              <span
                className={`text-[9px] font-extrabold ${
                  isCompleted ? 'text-blue-100' : isCurrent ? 'text-blue-600' : 'text-slate-400'
                }`}
              >
                +₹{level.rewardRupees}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
