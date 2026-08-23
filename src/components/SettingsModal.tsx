import React from 'react';
import { X, Volume2, VolumeX, Sparkles, Smartphone, Code2 } from 'lucide-react';
import { UserStats } from '../types/game';
import { sounds } from '../utils/audio';

interface SettingsModalProps {
  stats: UserStats;
  onClose: () => void;
  onToggleSound: () => void;
  onOpenCodeExplorer: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  stats,
  onClose,
  onToggleSound,
  onOpenCodeExplorer,
}) => {
  return (
    <div
      id="settings-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-fade-in"
    >
      <div className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-6 shadow-2xl flex flex-col space-y-4 border border-slate-200">
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <h2 className="text-base font-black text-slate-900">Settings</h2>
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

        {/* Options List */}
        <div className="space-y-2.5">
          {/* Sound Toggle */}
          <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-2xl border border-slate-200/80">
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-2xs text-blue-600">
                {stats.soundEnabled ? <Volume2 className="w-4 h-4" /> : <VolumeX className="w-4 h-4 text-slate-400" />}
              </div>
              <div>
                <p className="text-xs font-bold text-slate-800">Game Audio</p>
                <p className="text-[10px] text-slate-400">Taps, escapes & fanfares</p>
              </div>
            </div>
            <button
              onClick={() => {
                sounds.playTap();
                onToggleSound();
              }}
              className={`w-11 h-6 flex items-center rounded-full p-0.5 transition-colors ${
                stats.soundEnabled ? 'bg-blue-600' : 'bg-slate-300'
              }`}
            >
              <div
                className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
                  stats.soundEnabled ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* Android Kotlin Source Code Explorer */}
          <button
            onClick={() => {
              sounds.playTap();
              onOpenCodeExplorer();
            }}
            className="w-full flex items-center justify-between p-3.5 bg-blue-50/70 hover:bg-blue-100/70 active:scale-98 rounded-2xl border border-blue-200 text-left transition-all"
          >
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-2xs text-blue-600">
                <Code2 className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900">Android Source Code</p>
                <p className="text-[10px] text-blue-700">Kotlin & Jetpack Compose files</p>
              </div>
            </div>
            <Smartphone className="w-4 h-4 text-blue-600 mr-1" />
          </button>
        </div>

        {/* Game Info */}
        <div className="p-3 bg-slate-50 rounded-2xl border border-slate-200 text-center space-y-0.5">
          <p className="text-xs font-black text-slate-800">Arrow Escape v1.0.0</p>
          <p className="text-[10px] text-slate-400">Offline Original Puzzle Game • Jetpack Compose</p>
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
