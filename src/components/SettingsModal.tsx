import React from 'react';
import {
  X,
  Volume2,
  VolumeX,
  Vibrate,
  Music,
  Moon,
  Sun,
  Bell,
  BellOff,
  Info,
  Smartphone,
  Code2,
  CheckCircle2,
  Sparkles,
  ShieldCheck,
} from 'lucide-react';
import { UserStats } from '../types/game';
import { sounds } from '../utils/audio';

interface SettingsModalProps {
  stats: UserStats;
  onClose: () => void;
  onToggleSound: () => void;
  onToggleVibration: () => void;
  onToggleMusic: () => void;
  onToggleTheme: () => void;
  onToggleNotifications: () => void;
  onOpenCodeExplorer: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  stats,
  onClose,
  onToggleSound,
  onToggleVibration,
  onToggleMusic,
  onToggleTheme,
  onToggleNotifications,
  onOpenCodeExplorer,
}) => {
  const isDark = stats.theme === 'dark';
  const isSoundOn = stats.soundEnabled !== false;
  const isVibrationOn = stats.hapticsEnabled !== false;
  const isMusicOn = Boolean(stats.musicEnabled);
  const isNotifsOn = stats.notificationsEnabled !== false;

  return (
    <div
      id="settings-screen-backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-in fade-in duration-200"
    >
      <div
        id="settings-screen-modal"
        className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-5 shadow-2xl flex flex-col space-y-4 border border-slate-200 max-h-[92vh] overflow-y-auto animate-in zoom-in-95 duration-200"
      >
        {/* 1. Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 bg-slate-100 rounded-2xl border border-slate-200 flex items-center justify-center text-slate-700 shadow-2xs">
              <span className="text-lg">⚙️</span>
            </div>
            <div>
              <h2 className="text-base font-black text-slate-900">Settings</h2>
              <p className="text-xs text-slate-400 font-semibold">Game audio & preferences</p>
            </div>
          </div>
          <button
            id="settings-modal-close-btn"
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="w-8 h-8 bg-slate-100 hover:bg-slate-200 active:scale-95 rounded-full flex items-center justify-center text-slate-600 transition-all"
            title="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* 2. Audio & Haptics Section */}
        <div className="space-y-2">
          <div className="text-[10px] font-black uppercase tracking-wider text-slate-400 px-1">
            Audio & Haptics
          </div>

          {/* 1. 🔊 Sound Toggle */}
          <div
            id="setting-row-sound"
            className="flex items-center justify-between p-3 bg-slate-50 rounded-2xl border border-slate-200/80"
          >
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-2xs text-blue-600">
                {isSoundOn ? <Volume2 className="w-4 h-4" /> : <VolumeX className="w-4 h-4 text-slate-400" />}
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 flex items-center space-x-1">
                  <span>🔊 Sound</span>
                </p>
                <p className="text-[10px] text-slate-500 font-medium">Taps, escapes & level wins</p>
              </div>
            </div>
            <button
              id="setting-toggle-sound-btn"
              onClick={() => {
                sounds.playTap();
                onToggleSound();
              }}
              className={`w-11 h-6 flex items-center rounded-full p-0.5 transition-colors cursor-pointer ${
                isSoundOn ? 'bg-blue-600' : 'bg-slate-300'
              }`}
              aria-label="Toggle Sound"
            >
              <div
                className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
                  isSoundOn ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* 2. 📳 Vibration Toggle */}
          <div
            id="setting-row-vibration"
            className="flex items-center justify-between p-3 bg-slate-50 rounded-2xl border border-slate-200/80"
          >
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-2xs text-indigo-600">
                <Vibrate className={`w-4 h-4 ${isVibrationOn ? 'text-indigo-600' : 'text-slate-400'}`} />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 flex items-center space-x-1">
                  <span>📳 Vibration</span>
                </p>
                <p className="text-[10px] text-slate-500 font-medium">Haptics on arrow moves & collisions</p>
              </div>
            </div>
            <button
              id="setting-toggle-vibration-btn"
              onClick={() => {
                onToggleVibration();
                sounds.vibrate(40);
                sounds.playTap();
              }}
              className={`w-11 h-6 flex items-center rounded-full p-0.5 transition-colors cursor-pointer ${
                isVibrationOn ? 'bg-indigo-600' : 'bg-slate-300'
              }`}
              aria-label="Toggle Vibration"
            >
              <div
                className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
                  isVibrationOn ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* 3. 🎵 Music Toggle */}
          <div
            id="setting-row-music"
            className="flex items-center justify-between p-3 bg-slate-50 rounded-2xl border border-slate-200/80"
          >
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-2xs text-purple-600">
                <Music className={`w-4 h-4 ${isMusicOn ? 'text-purple-600' : 'text-slate-400'}`} />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 flex items-center space-x-1">
                  <span>🎵 Music</span>
                </p>
                <p className="text-[10px] text-slate-500 font-medium">Ambient puzzle soundtrack</p>
              </div>
            </div>
            <button
              id="setting-toggle-music-btn"
              onClick={() => {
                sounds.playTap();
                onToggleMusic();
              }}
              className={`w-11 h-6 flex items-center rounded-full p-0.5 transition-colors cursor-pointer ${
                isMusicOn ? 'bg-purple-600' : 'bg-slate-300'
              }`}
              aria-label="Toggle Music"
            >
              <div
                className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
                  isMusicOn ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
        </div>

        {/* 3. Display & Notifications Section */}
        <div className="space-y-2">
          <div className="text-[10px] font-black uppercase tracking-wider text-slate-400 px-1">
            Display & Notifications
          </div>

          {/* 4. 🌙 Theme Switcher */}
          <div
            id="setting-row-theme"
            className="flex items-center justify-between p-3 bg-slate-50 rounded-2xl border border-slate-200/80"
          >
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-2xs text-amber-600">
                {isDark ? <Moon className="w-4 h-4 text-indigo-500" /> : <Sun className="w-4 h-4 text-amber-500" />}
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 flex items-center space-x-1">
                  <span>🌙 Theme</span>
                </p>
                <p className="text-[10px] text-slate-500 font-medium">
                  {isDark ? 'Dark Mode' : 'Light Mode'}
                </p>
              </div>
            </div>
            <button
              id="setting-toggle-theme-btn"
              onClick={() => {
                sounds.playTap();
                onToggleTheme();
              }}
              className={`w-11 h-6 flex items-center rounded-full p-0.5 transition-colors cursor-pointer ${
                isDark ? 'bg-indigo-600' : 'bg-amber-400'
              }`}
              aria-label="Toggle Theme"
            >
              <div
                className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
                  isDark ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* 5. 🔔 Notifications Toggle */}
          <div
            id="setting-row-notifications"
            className="flex items-center justify-between p-3 bg-slate-50 rounded-2xl border border-slate-200/80"
          >
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center shadow-2xs text-emerald-600">
                {isNotifsOn ? <Bell className="w-4 h-4 text-emerald-600" /> : <BellOff className="w-4 h-4 text-slate-400" />}
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 flex items-center space-x-1">
                  <span>🔔 Notifications</span>
                </p>
                <p className="text-[10px] text-slate-500 font-medium">Daily streak & reward reminder</p>
              </div>
            </div>
            <button
              id="setting-toggle-notifications-btn"
              onClick={() => {
                sounds.playTap();
                onToggleNotifications();
              }}
              className={`w-11 h-6 flex items-center rounded-full p-0.5 transition-colors cursor-pointer ${
                isNotifsOn ? 'bg-emerald-600' : 'bg-slate-300'
              }`}
              aria-label="Toggle Notifications"
            >
              <div
                className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
                  isNotifsOn ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
        </div>

        {/* 6. ℹ️ About Section */}
        <div id="setting-section-about" className="space-y-2">
          <div className="text-[10px] font-black uppercase tracking-wider text-slate-400 px-1">
            About
          </div>

          <div className="p-3.5 bg-slate-50 rounded-2xl border border-slate-200/80 space-y-2 text-left">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center font-black text-sm">
                  ℹ️
                </div>
                <div>
                  <h3 className="text-xs font-black text-slate-900 tracking-tight">Arrow Escape</h3>
                  <p className="text-[10px] font-bold text-slate-500">App Version 1.0.0</p>
                </div>
              </div>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-800 border border-emerald-200">
                100% Solvable
              </span>
            </div>

            <p className="text-[11px] text-slate-600 leading-relaxed">
              Arrow Escape is a minimalist logic puzzle game where you unblock arrows to clear the grid. Solve procedurally generated puzzles to earn real ₹ cash rewards, daily streak bonuses, and hint powerups.
            </p>

            <div className="grid grid-cols-2 gap-2 pt-1 text-[10px] font-semibold text-slate-500">
              <div className="bg-white p-2 rounded-xl border border-slate-200/60 flex items-center space-x-1.5">
                <CheckCircle2 className="w-3 h-3 text-emerald-500 shrink-0" />
                <span>Offline Capable</span>
              </div>
              <div className="bg-white p-2 rounded-xl border border-slate-200/60 flex items-center space-x-1.5">
                <ShieldCheck className="w-3 h-3 text-blue-500 shrink-0" />
                <span>Instant ₹ Wallet</span>
              </div>
            </div>
          </div>

          {/* Android Kotlin Source Code Explorer */}
          <button
            id="settings-open-code-explorer-btn"
            onClick={() => {
              sounds.playTap();
              onOpenCodeExplorer();
            }}
            className="w-full flex items-center justify-between p-3 bg-blue-50/70 hover:bg-blue-100/70 active:scale-98 rounded-2xl border border-blue-200 text-left transition-all group cursor-pointer"
          >
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 rounded-xl bg-white flex items-center justify-center shadow-2xs text-blue-600">
                <Code2 className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900">Android Source Code</p>
                <p className="text-[10px] text-blue-700">Kotlin & Jetpack Compose files</p>
              </div>
            </div>
            <Smartphone className="w-4 h-4 text-blue-600 group-hover:scale-110 transition-transform mr-1" />
          </button>
        </div>

        {/* Close Button */}
        <button
          id="settings-modal-close-footer"
          onClick={() => {
            sounds.playTap();
            onClose();
          }}
          className="w-full py-2.5 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-bold rounded-2xl text-xs transition-all border border-slate-200/80 cursor-pointer"
        >
          Close
        </button>
      </div>
    </div>
  );
};
