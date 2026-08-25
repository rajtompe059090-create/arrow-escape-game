import React from 'react';
import { X, Lightbulb, PlayCircle, Sparkles, CheckCircle2 } from 'lucide-react';
import { sounds } from '../utils/audio';
import { UserStats } from '../types/game';

interface HintsModalProps {
  stats: UserStats;
  onClose: () => void;
  onWatchAdForHints: () => void;
}

export const HintsModal: React.FC<HintsModalProps> = ({
  stats,
  onClose,
  onWatchAdForHints,
}) => {
  return (
    <div
      id="hints-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs animate-in fade-in duration-200"
    >
      <div className="w-full max-w-sm bg-white rounded-3xl p-6 shadow-2xl border border-slate-100 flex flex-col space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-9 h-9 rounded-2xl bg-amber-500/10 flex items-center justify-center text-amber-500">
              <Lightbulb className="w-5 h-5 fill-current" />
            </div>
            <div>
              <h2 className="text-base font-black text-slate-900">Puzzle Hints</h2>
              <p className="text-xs text-slate-400">Get assistance on difficult puzzles</p>
            </div>
          </div>
          <button
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 active:scale-95 flex items-center justify-center text-slate-500 transition-all text-xs"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Current Inventory Banner */}
        <div className="bg-gradient-to-br from-amber-50 to-amber-100/60 p-4 rounded-2xl border border-amber-200/80 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-2xl bg-amber-500 text-white flex items-center justify-center shadow-xs">
              <Lightbulb className="w-5 h-5 fill-current" />
            </div>
            <div>
              <span className="text-[10px] uppercase font-extrabold tracking-wider text-amber-800">
                Available Hints
              </span>
              <p className="text-xl font-black text-amber-950">
                {stats.hintsRemaining} {stats.hintsRemaining === 1 ? 'Hint' : 'Hints'}
              </p>
            </div>
          </div>
          <div className="px-2.5 py-1 bg-white/80 rounded-full border border-amber-300 text-amber-800 text-[10px] font-bold">
            Ready to use
          </div>
        </div>

        {/* How hints work */}
        <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-100 space-y-2 text-xs text-slate-600">
          <div className="flex items-start space-x-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
            <span>Highlights a guaranteed free arrow during gameplay.</span>
          </div>
          <div className="flex items-start space-x-2">
            <Sparkles className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
            <span>Never solves the entire board, preserving your puzzle mastery.</span>
          </div>
        </div>

        {/* Watch Ad Button */}
        <div className="space-y-2 pt-1">
          <button
            onClick={() => {
              sounds.playTap();
              onWatchAdForHints();
            }}
            className="w-full py-3.5 px-4 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 active:scale-98 text-white font-black rounded-2xl shadow-md shadow-amber-200 flex items-center justify-center space-x-2 text-xs transition-all tracking-wide"
          >
            <PlayCircle className="w-4 h-4 fill-current" />
            <span>Watch Ad for +2 Free Hints</span>
          </button>

          <button
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="w-full py-2.5 px-4 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-bold rounded-2xl text-xs transition-all"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
