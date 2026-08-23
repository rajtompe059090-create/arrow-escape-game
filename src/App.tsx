import React, { useState, useEffect, useMemo } from 'react';
import { Smartphone, Monitor, Code2, Volume2, VolumeX, RotateCcw } from 'lucide-react';
import { SplashScreen } from './components/SplashScreen';
import { HomeScreen } from './components/HomeScreen';
import { LevelSelectScreen } from './components/LevelSelectScreen';
import { GameScreen } from './components/GameScreen';
import { LevelCompleteModal } from './components/LevelCompleteModal';
import { GameOverModal } from './components/GameOverModal';
import { WalletModal } from './components/WalletModal';
import { ProfileModal } from './components/ProfileModal';
import { SettingsModal } from './components/SettingsModal';
import { CodeExplorerModal } from './components/CodeExplorerModal';
import { LEVELS } from './data/levels';
import { UserStats } from './types/game';
import { loadUserStats, saveUserStats, resetGameProgress } from './utils/storage';
import { sounds } from './utils/audio';

type ScreenType = 'SPLASH' | 'HOME' | 'LEVEL_SELECT' | 'GAME';

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<ScreenType>('SPLASH');
  const [currentLevelId, setCurrentLevelId] = useState<number>(1);
  const [stats, setStats] = useState<UserStats>(loadUserStats);
  const [isMobileFrame, setIsMobileFrame] = useState<boolean>(true);

  // Active Modals
  const [showLevelComplete, setShowLevelComplete] = useState<boolean>(false);
  const [showGameOver, setShowGameOver] = useState<boolean>(false);
  const [showWallet, setShowWallet] = useState<boolean>(false);
  const [showProfile, setShowProfile] = useState<boolean>(false);
  const [showSettings, setShowSettings] = useState<boolean>(false);
  const [showCodeExplorer, setShowCodeExplorer] = useState<boolean>(false);

  const [completedLevelSummary, setCompletedLevelSummary] = useState<{
    levelId: number;
    reward: number;
    remainingLives: number;
  } | null>(null);

  // Sync sound settings
  useEffect(() => {
    sounds.setSoundEnabled(stats.soundEnabled);
  }, [stats.soundEnabled]);

  // Persist user stats on changes
  const updateStats = (updater: (prev: UserStats) => UserStats) => {
    setStats(prev => {
      const next = updater(prev);
      saveUserStats(next);
      return next;
    });
  };

  const handleToggleSound = () => {
    updateStats(prev => {
      const nextEnabled = !prev.soundEnabled;
      sounds.setSoundEnabled(nextEnabled);
      return { ...prev, soundEnabled: nextEnabled };
    });
  };

  const handleUseHint = () => {
    updateStats(prev => ({
      ...prev,
      hintsRemaining: Math.max(0, prev.hintsRemaining - 1),
    }));
  };

  const handleLevelComplete = (levelId: number, moves: number, remainingLives: number) => {
    const level = LEVELS.find(l => l.id === levelId) || LEVELS[0];
    const reward = level.rewardRupees;

    updateStats(prev => {
      const isAlreadyCompleted = prev.completedLevels.includes(levelId);
      const nextCompleted = isAlreadyCompleted ? prev.completedLevels : [...prev.completedLevels, levelId];
      const nextUnlocked = Math.max(prev.unlockedLevel, Math.min(20, levelId + 1));
      const nextEarned = isAlreadyCompleted ? prev.earnedRupees : prev.earnedRupees + reward;

      return {
        ...prev,
        completedLevels: nextCompleted,
        unlockedLevel: nextUnlocked,
        earnedRupees: nextEarned,
      };
    });

    setCompletedLevelSummary({
      levelId,
      reward,
      remainingLives,
    });
    setShowLevelComplete(true);
  };

  const handleGameOver = (levelId: number) => {
    setShowGameOver(true);
  };

  const currentLevelData = useMemo(() => {
    return LEVELS.find(l => l.id === currentLevelId) || LEVELS[0];
  }, [currentLevelId]);

  return (
    <div className="min-h-screen w-full bg-slate-100 text-slate-800 flex flex-col items-center justify-between font-sans selection:bg-blue-500 selection:text-white p-4 sm:p-6 md:p-8">
      {/* Top Header */}
      <header className="w-full max-w-7xl flex items-center justify-between pb-6 mb-2 border-b border-slate-200 text-xs">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 bg-blue-600 text-white flex items-center justify-center rounded-xl font-black text-lg shadow-md shadow-blue-200">
            →
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-extrabold text-slate-900 tracking-tight text-base">
                Arrow Escape
              </span>
              <span className="px-2 py-0.5 rounded-full bg-blue-50 text-blue-600 font-bold text-[10px] border border-blue-200">
                Sleek Interface
              </span>
            </div>
            <p className="text-[11px] text-slate-400 font-medium">Tap • Solve • Escape</p>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          {/* Kotlin Code Viewer Button */}
          <button
            id="header-code-button"
            onClick={() => {
              sounds.playTap();
              setShowCodeExplorer(true);
            }}
            className="flex items-center space-x-1.5 px-3.5 py-2 bg-white hover:bg-slate-50 active:scale-95 text-blue-600 font-bold rounded-xl border border-slate-200 transition-all shadow-xs"
          >
            <Code2 className="w-4 h-4 text-blue-600" />
            <span className="hidden sm:inline">Android Source</span>
          </button>

          {/* Sound Toggle in Header */}
          <button
            id="header-sound-button"
            onClick={() => {
              sounds.playTap();
              handleToggleSound();
            }}
            className="p-2 bg-white hover:bg-slate-50 active:scale-95 rounded-xl border border-slate-200 text-slate-600 transition-all shadow-xs"
            title={stats.soundEnabled ? 'Mute Audio' : 'Unmute Audio'}
          >
            {stats.soundEnabled ? (
              <Volume2 className="w-4 h-4 text-blue-600" />
            ) : (
              <VolumeX className="w-4 h-4 text-slate-400" />
            )}
          </button>

          {/* Frame Toggle */}
          <button
            id="frame-toggle-button"
            onClick={() => {
              sounds.playTap();
              setIsMobileFrame(!isMobileFrame);
            }}
            className="flex items-center space-x-1.5 px-3 py-2 bg-white hover:bg-slate-50 active:scale-95 text-slate-600 rounded-xl border border-slate-200 transition-all shadow-xs"
            title="Toggle Phone Frame / Expanded View"
          >
            {isMobileFrame ? <Monitor className="w-4 h-4" /> : <Smartphone className="w-4 h-4" />}
            <span className="hidden sm:inline">{isMobileFrame ? 'Expanded' : 'Phone'}</span>
          </button>
        </div>
      </header>

      {/* Main Content Area: Responsive 3-Panel Sleek Dashboard on Desktop, Clean Centered on Mobile */}
      <main className="flex-1 w-full max-w-7xl flex flex-col lg:flex-row items-center lg:items-start justify-center gap-8 my-auto py-2">
        
        {/* Left Side Panel (Level Progression & Rewards) - Visible on lg+ screens */}
        <div className="hidden lg:flex flex-col gap-6 w-72 shrink-0">
          {/* Level Progression Card */}
          <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-200">
            <h2 className="text-slate-400 uppercase tracking-widest text-xs font-bold mb-4">
              Level Progression
            </h2>
            <div className="grid grid-cols-4 gap-2.5">
              {LEVELS.slice(0, 16).map(lvl => {
                const isUnlocked = lvl.id <= stats.unlockedLevel;
                const isCompleted = stats.completedLevels.includes(lvl.id);
                const isCurrent = lvl.id === stats.unlockedLevel;

                let btnStyle = 'bg-slate-50 text-slate-300 border border-slate-200';
                if (isCompleted) {
                  btnStyle = 'bg-blue-500 text-white shadow-md shadow-blue-200 hover:bg-blue-600';
                } else if (isCurrent) {
                  btnStyle = 'bg-blue-100 text-blue-600 border border-blue-200 ring-4 ring-blue-50 font-black';
                }

                return (
                  <button
                    key={lvl.id}
                    onClick={() => {
                      if (isUnlocked) {
                        sounds.playTap();
                        setCurrentLevelId(lvl.id);
                        setCurrentScreen('GAME');
                      }
                    }}
                    disabled={!isUnlocked}
                    className={`w-10 h-10 flex items-center justify-center rounded-xl font-bold text-sm transition-all ${btnStyle}`}
                    title={`Level ${lvl.id} - ${lvl.difficulty}`}
                  >
                    {lvl.id}
                  </button>
                );
              })}
            </div>
            <p className="mt-5 text-slate-500 text-xs text-center italic">
              {stats.completedLevels.length} of 20 levels cleared
            </p>
          </div>

          {/* Current Rewards Card */}
          <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-200">
            <h2 className="text-slate-400 uppercase tracking-widest text-xs font-bold mb-2">
              Current Rewards
            </h2>
            <div className="flex items-center justify-between">
              <span className="text-2xl font-black text-slate-800">₹{stats.earnedRupees}.00</span>
              <button
                onClick={() => setShowWallet(true)}
                className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1 rounded-full text-[10px] font-bold transition-all"
              >
                +₹{currentLevelData.rewardRupees} NEXT
              </button>
            </div>
            <div className="mt-4 h-2 w-full bg-slate-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-blue-500 rounded-full transition-all duration-500"
                style={{ width: `${Math.min(100, Math.max(5, (stats.completedLevels.length / 20) * 100))}%` }}
              />
            </div>
            <div className="mt-3 flex items-center justify-between text-[10px] text-slate-400 font-bold uppercase tracking-tight">
              <span>Wallet (MVP Mode)</span>
              <button
                onClick={() => setShowWallet(true)}
                className="text-blue-600 hover:underline"
              >
                View Details
              </button>
            </div>
          </div>
        </div>

        {/* Center Panel: Sleek Phone Mockup / Expanded Interface */}
        <div
          className={`transition-all duration-300 ${
            isMobileFrame
              ? 'w-full max-w-[380px] h-[720px] max-h-[92vh] bg-slate-900 rounded-[60px] p-3.5 shadow-2xl relative border-[6px] border-slate-800 shrink-0'
              : 'w-full max-w-xl h-[740px] max-h-[92vh] rounded-3xl shadow-lg border border-slate-200 bg-white relative'
          }`}
        >
          <div className="w-full h-full bg-white rounded-[44px] flex flex-col overflow-hidden relative">
            {/* Sleek Camera Notch in Mobile Frame */}
            {isMobileFrame && (
              <div className="h-6 bg-white w-full flex justify-center items-end shrink-0 z-30">
                <div className="h-4 w-28 bg-slate-900 rounded-b-xl flex items-center justify-center space-x-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-slate-800" />
                  <div className="w-8 h-1 rounded-full bg-slate-800" />
                </div>
              </div>
            )}

            {/* Screen Content Container */}
            <div className="w-full h-full overflow-hidden flex flex-col">
              {currentScreen === 'SPLASH' && (
                <SplashScreen onStart={() => setCurrentScreen('HOME')} />
              )}

              {currentScreen === 'HOME' && (
                <HomeScreen
                  stats={stats}
                  onContinue={() => {
                    setCurrentLevelId(stats.unlockedLevel);
                    setCurrentScreen('GAME');
                  }}
                  onOpenLevels={() => setCurrentScreen('LEVEL_SELECT')}
                  onOpenWallet={() => setShowWallet(true)}
                  onOpenProfile={() => setShowProfile(true)}
                  onToggleSound={handleToggleSound}
                />
              )}

              {currentScreen === 'LEVEL_SELECT' && (
                <LevelSelectScreen
                  stats={stats}
                  onSelectLevel={levelId => {
                    setCurrentLevelId(levelId);
                    setCurrentScreen('GAME');
                  }}
                  onBack={() => setCurrentScreen('HOME')}
                />
              )}

              {currentScreen === 'GAME' && (
                <GameScreen
                  level={currentLevelData}
                  stats={stats}
                  onBack={() => setCurrentScreen('LEVEL_SELECT')}
                  onLevelComplete={handleLevelComplete}
                  onGameOver={handleGameOver}
                  onOpenLevels={() => setCurrentScreen('LEVEL_SELECT')}
                  onOpenSettings={() => setShowSettings(true)}
                  onUseHint={handleUseHint}
                />
              )}
            </div>

            {/* Sleek Home Indicator Bar */}
            {isMobileFrame && (
              <div className="h-6 bg-white w-full flex justify-center items-center shrink-0 z-30">
                <div className="w-24 h-1 bg-slate-200 rounded-full" />
              </div>
            )}
          </div>
        </div>

        {/* Right Side Panel (Hero Card & Account) - Visible on lg+ screens */}
        <div className="hidden lg:flex flex-col gap-6 w-72 shrink-0">
          {/* Blue Hero Card */}
          <div className="bg-blue-600 p-7 rounded-3xl shadow-xl text-white">
            <h1 className="text-2xl font-black mb-1 tracking-tight">Arrow Escape</h1>
            <p className="text-blue-100 text-xs italic mb-5">Tap • Solve • Escape</p>
            <div className="space-y-3">
              <button
                onClick={() => {
                  sounds.playTap();
                  setCurrentLevelId(stats.unlockedLevel);
                  setCurrentScreen('GAME');
                }}
                className="w-full py-3 bg-white hover:bg-blue-50 active:scale-98 text-blue-600 rounded-xl font-bold text-sm shadow-md transition-all flex items-center justify-center space-x-1.5"
              >
                <span>CONTINUE</span>
                <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-extrabold">
                  Lv.{stats.unlockedLevel}
                </span>
              </button>
              <button
                onClick={() => {
                  sounds.playTap();
                  setCurrentScreen('LEVEL_SELECT');
                }}
                className="w-full py-3 bg-blue-500 hover:bg-blue-400 active:scale-98 text-white rounded-xl font-bold text-sm border border-blue-400 transition-all"
              >
                LEVELS
              </button>
              <button
                onClick={() => {
                  sounds.playTap();
                  setShowCodeExplorer(true);
                }}
                className="w-full py-2.5 bg-blue-700/60 hover:bg-blue-700 active:scale-98 text-blue-100 rounded-xl font-bold text-xs border border-blue-400/40 transition-all flex items-center justify-center space-x-1.5"
              >
                <Code2 className="w-3.5 h-3.5" />
                <span>KOTLIN SOURCE</span>
              </button>
            </div>
          </div>

          {/* Account Profile Card */}
          <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-200">
            <h2 className="text-slate-400 uppercase tracking-widest text-xs font-bold mb-4">
              Account
            </h2>
            <div
              onClick={() => setShowProfile(true)}
              className="flex items-center gap-3.5 cursor-pointer p-2 rounded-2xl hover:bg-slate-50 transition-all"
            >
              <div className="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center text-xl shrink-0">
                👤
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-bold text-slate-800 truncate">Guest Player</p>
                <p className="text-xs text-slate-400 italic truncate">Local Storage Active</p>
              </div>
            </div>
          </div>
        </div>

      </main>

      {/* Modals & Dialogs */}
      {showLevelComplete && completedLevelSummary && (
        <LevelCompleteModal
          levelId={completedLevelSummary.levelId}
          rewardRupees={completedLevelSummary.reward}
          remainingLives={completedLevelSummary.remainingLives}
          hasNextLevel={completedLevelSummary.levelId < 20}
          onNextLevel={() => {
            setShowLevelComplete(false);
            setCurrentLevelId(prev => Math.min(20, prev + 1));
          }}
          onReplay={() => {
            setShowLevelComplete(false);
          }}
          onHome={() => {
            setShowLevelComplete(false);
            setCurrentScreen('HOME');
          }}
        />
      )}

      {showGameOver && (
        <GameOverModal
          levelId={currentLevelId}
          onRetry={() => {
            setShowGameOver(false);
          }}
          onHome={() => {
            setShowGameOver(false);
            setCurrentScreen('HOME');
          }}
        />
      )}

      {showWallet && (
        <WalletModal
          stats={stats}
          onClose={() => setShowWallet(false)}
        />
      )}

      {showProfile && (
        <ProfileModal
          stats={stats}
          onClose={() => setShowProfile(false)}
          onResetProgress={() => {
            const fresh = resetGameProgress();
            setStats(fresh);
            setCurrentLevelId(1);
            setCurrentScreen('HOME');
          }}
        />
      )}

      {showSettings && (
        <SettingsModal
          stats={stats}
          onClose={() => setShowSettings(false)}
          onToggleSound={handleToggleSound}
          onOpenCodeExplorer={() => {
            setShowSettings(false);
            setShowCodeExplorer(true);
          }}
        />
      )}

      {showCodeExplorer && (
        <CodeExplorerModal
          onClose={() => setShowCodeExplorer(false)}
        />
      )}

      {/* Footer */}
      <footer className="pt-4 pb-2 text-[11px] text-slate-400 text-center w-full">
        Arrow Escape • Sleek Interface • Offline Original Puzzle Game
      </footer>
    </div>
  );
}
