import React from 'react';
import { X, User, Trophy, Award, Zap, RotateCcw } from 'lucide-react';
import { UserStats } from '../types/game';
import { sounds } from '../utils/audio';

interface ProfileModalProps {
  stats: UserStats;
  onClose: () => void;
  onResetProgress: () => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({
  stats,
  onClose,
  onResetProgress,
}) => {
  return (
    <div
      id="profile-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-fade-in"
    >
      <div className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-6 shadow-2xl flex flex-col space-y-4 border border-slate-200">
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 bg-blue-50 rounded-2xl border border-blue-200 flex items-center justify-center">
              <User className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h2 className="text-base font-black text-slate-900">Player Profile</h2>
              <p className="text-xs text-slate-400">Offline Guest Account</p>
            </div>
          </div>
          <button
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="w-8 h-8 bg-slate-100 hover:bg-slate-200 rounded-full flex items-center justify-center text-slate-600 transition-all"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-2 gap-2.5">
          <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <Trophy className="w-5 h-5 text-blue-500 mb-1" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Cleared</span>
            <span className="text-base font-black text-slate-900">
              {stats.completedLevels.length} / 20
            </span>
          </div>

          <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <Award className="w-5 h-5 text-green-500 mb-1" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Earned</span>
            <span className="text-base font-black text-green-700">
              ₹{stats.earnedRupees}.00
            </span>
          </div>

          <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <Zap className="w-5 h-5 text-amber-500 mb-1" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Hints</span>
            <span className="text-base font-black text-slate-900">
              {stats.hintsRemaining} Left
            </span>
          </div>

          <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <User className="w-5 h-5 text-indigo-500 mb-1" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Current</span>
            <span className="text-base font-black text-blue-600">
              Level {stats.unlockedLevel}
            </span>
          </div>
        </div>

        {/* Reset Progress Section */}
        <div className="pt-2 border-t border-slate-100 flex flex-col space-y-2">
          <button
            onClick={() => {
              if (window.confirm('Are you sure you want to reset all game progress and start over?')) {
                sounds.playTap();
                onResetProgress();
                onClose();
              }
            }}
            className="w-full py-2.5 px-4 bg-rose-50 hover:bg-rose-100 text-rose-700 font-bold rounded-2xl border border-rose-200 flex items-center justify-center space-x-2 text-xs transition-all"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Reset Progress</span>
          </button>
        </div>

        {/* Close Button */}
        <button
          onClick={() => {
            sounds.playTap();
            onClose();
          }}
          className="w-full py-3 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-2xl text-xs transition-all shadow-md shadow-blue-200"
        >
          Close
        </button>
      </div>
    </div>
  );
};
