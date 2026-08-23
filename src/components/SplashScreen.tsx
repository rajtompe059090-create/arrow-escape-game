import React, { useEffect } from 'react';
import { Play, Sparkles } from 'lucide-react';
import { sounds } from '../utils/audio';

interface SplashScreenProps {
  onStart: () => void;
}

export const SplashScreen: React.FC<SplashScreenProps> = ({ onStart }) => {
  useEffect(() => {
    // Optional gentle auto-advance after 3.5s or click
    const timer = setTimeout(() => {
      onStart();
    }, 4000);
    return () => clearTimeout(timer);
  }, [onStart]);

  const handleStart = () => {
    sounds.playTap();
    onStart();
  };

  return (
    <div
      id="splash-screen"
      onClick={handleStart}
      className="flex flex-col items-center justify-between h-full w-full py-10 px-6 bg-white text-slate-900 cursor-pointer select-none"
    >
      <div className="w-full flex justify-end">
        <span className="text-[10px] font-bold text-blue-600 tracking-wider uppercase px-3 py-1 bg-blue-50 border border-blue-100 rounded-full">
          v1.0.0
        </span>
      </div>

      <div className="flex flex-col items-center text-center space-y-5">
        {/* Animated Brand Logo Icon */}
        <div className="relative flex items-center justify-center w-24 h-24 bg-blue-600 text-white rounded-3xl shadow-xl shadow-blue-200 animate-bounce">
          <svg className="w-14 h-14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12h14" />
            <path d="m12 5 7 7-7 7" />
          </svg>
          <div className="absolute -top-1 -right-1">
            <Sparkles className="w-6 h-6 text-amber-300 fill-amber-300 animate-spin" />
          </div>
        </div>

        <div>
          <h1 className="text-3xl font-black tracking-tight text-slate-900">
            Arrow Escape
          </h1>
          <p className="text-xs font-bold tracking-widest text-blue-600 mt-1 uppercase">
            Tap • Solve • Escape
          </p>
        </div>

        <p className="text-xs text-slate-500 max-w-[240px] leading-relaxed">
          Untangle intricate arrow mazes. Find unobstructed paths and clear the board.
        </p>
      </div>

      <div className="flex flex-col items-center space-y-3 w-full max-w-xs">
        <button
          id="splash-start-button"
          onClick={handleStart}
          className="w-full py-4 px-6 bg-blue-600 hover:bg-blue-700 active:scale-95 text-white font-bold rounded-2xl shadow-lg shadow-blue-200 flex items-center justify-center space-x-2 transition-all"
        >
          <Play className="w-5 h-5 fill-current" />
          <span>Tap to Start</span>
        </button>

        <span className="text-[11px] text-slate-400 font-medium">
          Offline • 20 Puzzles • Jetpack Compose
        </span>
      </div>
    </div>
  );
};
