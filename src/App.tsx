import React, { useState, useEffect, useMemo } from 'react';
import { Smartphone, Monitor, Code2, Volume2, VolumeX, WifiOff, RefreshCw } from 'lucide-react';
import { HomeScreen } from './components/HomeScreen';
import { LevelSelectScreen } from './components/LevelSelectScreen';
import { GameScreen } from './components/GameScreen';
import { LevelCompleteModal } from './components/LevelCompleteModal';
import { GameOverModal } from './components/GameOverModal';
import { WalletModal } from './components/WalletModal';
import { DailyRewardModal } from './components/DailyRewardModal';
import { RewardsModal } from './components/RewardsModal';
import { SettingsModal } from './components/SettingsModal';
import { HintsModal } from './components/HintsModal';
import { ProfileModal } from './components/ProfileModal';
import { CodeExplorerModal } from './components/CodeExplorerModal';
import { AdModal, AdType } from './components/AdModal';
import { getLevel } from './data/levels';
import { UserStats, EarningTransaction } from './types/game';
import { loadUserStats, saveUserStats, resetGameProgress } from './utils/storage';
import { sounds } from './utils/audio';
import {
  calculateLevelReward,
  processLevelReward,
  processDoubleLevelReward,
  processDailyReward,
  processAdCashReward,
} from './services/earningsService';

type ScreenType = 'HOME' | 'LEVEL_SELECT' | 'GAME';

export default function App() {
  // Start on HOME screen directly
  const [currentScreen, setCurrentScreen] = useState<ScreenType>('HOME');
  const [currentLevelId, setCurrentLevelId] = useState<number>(1);
  const [stats, setStats] = useState<UserStats>(loadUserStats);
  const [isMobileFrame, setIsMobileFrame] = useState<boolean>(true);

  // Active Modals
  const [showLevelComplete, setShowLevelComplete] = useState<boolean>(false);
  const [showGameOver, setShowGameOver] = useState<boolean>(false);
  const [showWallet, setShowWallet] = useState<boolean>(false);
  const [showDailyReward, setShowDailyReward] = useState<boolean>(false);
  const [showRewards, setShowRewards] = useState<boolean>(false);
  const [showHints, setShowHints] = useState<boolean>(false);
  const [showSettings, setShowSettings] = useState<boolean>(false);
  const [showProfile, setShowProfile] = useState<boolean>(false);
  const [showCodeExplorer, setShowCodeExplorer] = useState<boolean>(false);

  // Ad Overlay State
  const [activeAd, setActiveAd] = useState<{
    type: AdType;
    title: string;
    rewardLabel?: string;
    onComplete: () => void;
  } | null>(null);

  const [isOnline, setIsOnline] = useState<boolean>(navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  const handleRetryConnection = () => {
    setIsOnline(navigator.onLine);
  };

  const [completedLevelSummary, setCompletedLevelSummary] = useState<{
    levelId: number;
    reward: number;
    remainingLives: number;
    isDoubled: boolean;
    isAlreadyClaimed?: boolean;
  } | null>(null);

  // Sync sound, haptics & music settings
  useEffect(() => {
    sounds.setSoundEnabled(stats.soundEnabled !== false);
    sounds.setHapticsEnabled(stats.hapticsEnabled !== false);
    sounds.setMusicEnabled(Boolean(stats.musicEnabled));
  }, [stats.soundEnabled, stats.hapticsEnabled, stats.musicEnabled]);

  // Ensure currentLevelId tracks unlockedLevel
  useEffect(() => {
    if (stats.unlockedLevel > 1 && currentLevelId === 1) {
      setCurrentLevelId(stats.unlockedLevel);
    }
  }, [stats.unlockedLevel]);

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
      const nextEnabled = !(prev.soundEnabled !== false);
      sounds.setSoundEnabled(nextEnabled);
      return { ...prev, soundEnabled: nextEnabled };
    });
  };

  const handleToggleVibration = () => {
    updateStats(prev => {
      const nextEnabled = !(prev.hapticsEnabled !== false);
      sounds.setHapticsEnabled(nextEnabled);
      return { ...prev, hapticsEnabled: nextEnabled };
    });
  };

  const handleToggleMusic = () => {
    updateStats(prev => {
      const nextEnabled = !Boolean(prev.musicEnabled);
      sounds.setMusicEnabled(nextEnabled);
      return { ...prev, musicEnabled: nextEnabled };
    });
  };

  const handleToggleTheme = () => {
    updateStats(prev => {
      const nextTheme = prev.theme === 'dark' ? 'light' : 'dark';
      return { ...prev, theme: nextTheme };
    });
  };

  const handleToggleNotifications = () => {
    updateStats(prev => {
      const nextNotifs = !(prev.notificationsEnabled !== false);
      return { ...prev, notificationsEnabled: nextNotifs };
    });
  };

  const handleUseHint = () => {
    updateStats(prev => ({
      ...prev,
      hintsRemaining: Math.max(0, prev.hintsRemaining - 1),
    }));
  };

  // Level Complete Handler (strictly centralized through earningsService)
  const handleLevelComplete = (levelId: number, moves: number, remainingLives: number) => {
    let rewardedAmount = 0;
    let isAlready = false;

    updateStats(prev => {
      const result = processLevelReward(prev, levelId);
      rewardedAmount = result.rewardAmount;
      isAlready = result.isAlreadyCompleted;
      return result.updatedStats;
    });

    setCompletedLevelSummary({
      levelId,
      reward: isAlready ? 0 : rewardedAmount,
      remainingLives,
      isDoubled: false,
      isAlreadyClaimed: isAlready,
    });
    setShowLevelComplete(true);
  };

  // Next Level Flow with Interstitial Ad
  const handleNextLevelWithAd = () => {
    setShowLevelComplete(false);
    const nextLevel = (completedLevelSummary?.levelId || currentLevelId) + 1;

    // Show Interstitial Ad before proceeding to next level
    setActiveAd({
      type: 'INTERSTITIAL',
      title: `Level ${nextLevel} Loading`,
      rewardLabel: undefined,
      onComplete: () => {
        setActiveAd(null);
        setCurrentLevelId(nextLevel);
        setCurrentScreen('GAME');
      },
    });
  };

  // Double Reward Ad Handler (centralized)
  const handleDoubleRewardAd = () => {
    if (!completedLevelSummary) return;
    const baseReward = completedLevelSummary.reward;
    const levelId = completedLevelSummary.levelId;

    setActiveAd({
      type: 'REWARDED_VIDEO',
      title: `Double Level ${levelId} Earnings`,
      rewardLabel: `+₹${baseReward}.00 Extra Cash`,
      onComplete: () => {
        setActiveAd(null);
        updateStats(prev => {
          const result = processDoubleLevelReward(prev, levelId, baseReward);
          return result.updatedStats;
        });

        setCompletedLevelSummary(prev => (prev ? { ...prev, isDoubled: true } : null));
      },
    });
  };

  // Claim Daily Reward (strictly centralized through earningsService with 24-hour cycle)
  const handleClaimDailyReward = (amount: number, newStreak: number) => {
    updateStats(prev => {
      const result = processDailyReward(prev, amount, newStreak);
      return result.updatedStats;
    });
  };

  // Watch Ad to 2X Daily Reward
  const handleWatchAdForDoubleDaily = (totalAmount: number, streak: number) => {
    setActiveAd({
      type: 'REWARDED_VIDEO',
      title: 'Daily Reward Multiplier',
      rewardLabel: `₹${totalAmount}.00 (2X Bonus)`,
      onComplete: () => {
        setActiveAd(null);
        handleClaimDailyReward(totalAmount, streak);
      },
    });
  };

  // Watch Ad for Instant Cash (+₹5) (centralized)
  const handleWatchCashAd = () => {
    setShowRewards(false);
    setActiveAd({
      type: 'REWARDED_VIDEO',
      title: 'Sponsor Bonus Reward',
      rewardLabel: '+₹5.00 Instant Cash',
      onComplete: () => {
        setActiveAd(null);
        updateStats(prev => {
          const result = processAdCashReward(prev, 5);
          return result.updatedStats;
        });
      },
    });
  };

  // Watch Ad for +2 Free Hints
  const handleWatchHintAd = () => {
    setShowRewards(false);
    setActiveAd({
      type: 'REWARDED_VIDEO',
      title: 'Extra Hints Reward',
      rewardLabel: '+2 Free Hints',
      onComplete: () => {
        setActiveAd(null);
        updateStats(prev => ({
          ...prev,
          hintsRemaining: prev.hintsRemaining + 2,
        }));
      },
    });
  };

  const handleGameOver = (levelId: number) => {
    setShowGameOver(true);
  };

  // Dynamically generated level for the active levelId
  const currentLevelData = useMemo(() => {
    return getLevel(currentLevelId);
  }, [currentLevelId]);

  return (
    <div className="min-h-screen w-full bg-slate-100 text-slate-800 flex flex-col items-center justify-between font-sans selection:bg-blue-500 selection:text-white p-3 sm:p-5 md:p-7">
      {/* Top Header */}
      <header className="w-full max-w-6xl flex items-center justify-between pb-4 mb-2 border-b border-slate-200 text-xs">
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
            <p className="text-[11px] text-slate-400 font-medium">Tap • Solve • Escape • Earn ₹</p>
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
            className="flex items-center space-x-1.5 px-3 py-2 bg-white hover:bg-slate-50 active:scale-95 text-blue-600 font-bold rounded-xl border border-slate-200 transition-all shadow-2xs"
          >
            <Code2 className="w-4 h-4 text-blue-600" />
            <span className="hidden sm:inline">Android Kotlin Code</span>
          </button>

          {/* Sound Toggle in Header */}
          <button
            id="header-sound-button"
            onClick={() => {
              sounds.playTap();
              handleToggleSound();
            }}
            className="p-2 bg-white hover:bg-slate-50 active:scale-95 rounded-xl border border-slate-200 text-slate-600 transition-all shadow-2xs"
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
            className="flex items-center space-x-1.5 px-3 py-2 bg-white hover:bg-slate-50 active:scale-95 text-slate-600 rounded-xl border border-slate-200 transition-all shadow-2xs"
            title="Toggle Phone Frame / Expanded View"
          >
            {isMobileFrame ? <Monitor className="w-4 h-4" /> : <Smartphone className="w-4 h-4" />}
            <span className="hidden sm:inline">{isMobileFrame ? 'Expanded' : 'Phone'}</span>
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 w-full max-w-6xl flex flex-col lg:flex-row items-center lg:items-start justify-center gap-8 my-auto py-2">
        {/* Left Side Panel (Progression & Earnings Overview) - Desktop view */}
        <div className="hidden lg:flex flex-col gap-5 w-72 shrink-0">
          {/* Wallet & Real Earnings Card */}
          <div className="bg-white p-5 rounded-3xl shadow-xs border border-slate-200 space-y-3">
            <h2 className="text-slate-400 uppercase tracking-widest text-[10px] font-black">
              Earning Overview
            </h2>
            <div className="space-y-2">
              <div className="bg-slate-50 p-3 rounded-2xl border border-slate-100 flex justify-between items-center">
                <span className="text-xs font-bold text-slate-500">Total Earnings</span>
                <span className="text-base font-black text-slate-900">
                  ₹{stats.totalEarnings.toFixed(2)}
                </span>
              </div>
              <div className="bg-emerald-50 p-3 rounded-2xl border border-emerald-100 flex justify-between items-center">
                <span className="text-xs font-bold text-emerald-800">Wallet Balance</span>
                <span className="text-base font-black text-emerald-700">
                  ₹{stats.walletBalance.toFixed(2)}
                </span>
              </div>
            </div>
            <button
              onClick={() => {
                sounds.playTap();
                setShowWallet(true);
              }}
              className="w-full py-2 bg-slate-900 hover:bg-slate-800 text-white font-bold rounded-xl text-xs transition-all"
            >
              View Full Wallet Ledger
            </button>
          </div>

          {/* Quick Level Jump Card */}
          <div className="bg-white p-5 rounded-3xl shadow-xs border border-slate-200 space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-slate-400 uppercase tracking-widest text-[10px] font-black">
                Recent Levels
              </h2>
              <span className="text-xs font-bold text-blue-600">
                Lv.{stats.unlockedLevel} Unlocked
              </span>
            </div>
            <div className="grid grid-cols-4 gap-2">
              {Array.from({ length: 12 }, (_, i) => {
                const lvlId = Math.max(1, stats.unlockedLevel - 5) + i;
                const isUnlocked = lvlId <= stats.unlockedLevel;
                const isCompleted = stats.completedLevels.includes(lvlId);
                const isCurrent = lvlId === stats.unlockedLevel;

                let btnClass = 'bg-slate-50 text-slate-300 border border-slate-200';
                if (isCompleted) {
                  btnClass = 'bg-blue-500 text-white shadow-xs hover:bg-blue-600';
                } else if (isCurrent) {
                  btnClass = 'bg-blue-100 text-blue-700 border border-blue-200 ring-2 ring-blue-400/40 font-black';
                }

                return (
                  <button
                    key={lvlId}
                    onClick={() => {
                      if (isUnlocked) {
                        sounds.playTap();
                        setCurrentLevelId(lvlId);
                        setCurrentScreen('GAME');
                      }
                    }}
                    disabled={!isUnlocked}
                    className={`h-9 flex items-center justify-center rounded-xl font-bold text-xs transition-all ${btnClass}`}
                  >
                    {lvlId}
                  </button>
                );
              })}
            </div>
            <button
              onClick={() => {
                sounds.playTap();
                setCurrentScreen('LEVEL_SELECT');
              }}
              className="w-full py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-xl text-xs transition-all"
            >
              Browse All Tiers (1–250+)
            </button>
          </div>
        </div>

        {/* Center Panel: Sleek Phone Mockup / Expanded Interface */}
        <div
          className={`transition-all duration-300 ${
            isMobileFrame
              ? 'w-full max-w-[390px] h-[740px] max-h-[92vh] bg-slate-900 rounded-[56px] p-3.5 shadow-2xl relative border-[6px] border-slate-800 shrink-0'
              : 'w-full max-w-lg h-[740px] max-h-[92vh] rounded-3xl shadow-lg border border-slate-200 bg-white relative'
          }`}
        >
          <div className="w-full h-full bg-white rounded-[40px] flex flex-col overflow-hidden relative">
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
              {currentScreen === 'HOME' && (
                <HomeScreen
                  stats={stats}
                  onPlayContinue={() => {
                    setCurrentLevelId(stats.unlockedLevel);
                    setCurrentScreen('GAME');
                  }}
                  onOpenDailyReward={() => setShowDailyReward(true)}
                  onOpenRewards={() => setShowRewards(true)}
                  onOpenWallet={() => setShowWallet(true)}
                  onOpenHints={() => setShowHints(true)}
                  onOpenSettings={() => setShowSettings(true)}
                  onOpenProfile={() => setShowProfile(true)}
                  onOpenLevels={() => setCurrentScreen('LEVEL_SELECT')}
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
                  onBack={() => setCurrentScreen('HOME')}
                  onLevelComplete={handleLevelComplete}
                  onGameOver={handleGameOver}
                  onOpenLevels={() => setCurrentScreen('LEVEL_SELECT')}
                  onOpenSettings={() => setShowSettings(true)}
                  onUseHint={handleUseHint}
                  onWatchHintAd={handleWatchHintAd}
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

        {/* Right Side Panel (Quick Actions & Jetpack Compose Details) */}
        <div className="hidden lg:flex flex-col gap-5 w-72 shrink-0">
          {/* Daily Reward & Multiplier Quick Action */}
          <div className="bg-gradient-to-br from-amber-500 to-amber-600 p-5 rounded-3xl shadow-md text-white space-y-3">
            <div>
              <span className="text-[10px] font-black uppercase tracking-wider text-amber-100">
                Daily Bonus
              </span>
              <h3 className="text-xl font-black mt-0.5">Free Daily Cash</h3>
              <p className="text-xs text-amber-100 mt-1">
                Streak: Day {Math.max(1, ((stats.dailyStreak - 1) % 7) + 1)} of 7
              </p>
            </div>
            <button
              onClick={() => {
                sounds.playTap();
                setShowDailyReward(true);
              }}
              className="w-full py-2.5 bg-white hover:bg-amber-50 active:scale-98 text-amber-800 rounded-xl font-bold text-xs shadow-xs transition-all"
            >
              Open Daily Rewards
            </button>
          </div>

          {/* Android Kotlin & Compose Architecture Box */}
          <div className="bg-white p-5 rounded-3xl shadow-xs border border-slate-200 space-y-3">
            <h2 className="text-slate-400 uppercase tracking-widest text-[10px] font-black">
              Android Engine
            </h2>
            <div className="space-y-1.5 text-xs text-slate-600">
              <div className="flex justify-between">
                <span>Language:</span>
                <strong className="text-slate-900">Kotlin</strong>
              </div>
              <div className="flex justify-between">
                <span>UI Framework:</span>
                <strong className="text-slate-900">Jetpack Compose</strong>
              </div>
              <div className="flex justify-between">
                <span>Levels:</span>
                <strong className="text-blue-600 font-bold">100% Solvable Dynamic</strong>
              </div>
              <div className="flex justify-between">
                <span>Monetization:</span>
                <strong className="text-emerald-700 font-bold">Real ₹ Rewards & Ads</strong>
              </div>
            </div>
            <button
              onClick={() => {
                sounds.playTap();
                setShowCodeExplorer(true);
              }}
              className="w-full py-2 bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-200 font-bold rounded-xl text-xs transition-all flex items-center justify-center space-x-1.5"
            >
              <Code2 className="w-3.5 h-3.5" />
              <span>Explore Kotlin Source</span>
            </button>
          </div>
        </div>
      </main>

      {/* Modals & Dialogs */}
      {showLevelComplete && completedLevelSummary && (
        <LevelCompleteModal
          levelId={completedLevelSummary.levelId}
          rewardRupees={completedLevelSummary.reward}
          totalEarnings={stats.totalEarnings}
          walletBalance={stats.walletBalance}
          remainingLives={completedLevelSummary.remainingLives}
          isRewardDoubled={completedLevelSummary.isDoubled}
          isAlreadyClaimed={completedLevelSummary.isAlreadyClaimed}
          onNextLevel={handleNextLevelWithAd}
          onDoubleRewardAd={handleDoubleRewardAd}
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

      {showDailyReward && (
        <DailyRewardModal
          stats={stats}
          onClose={() => setShowDailyReward(false)}
          onClaimDaily={handleClaimDailyReward}
          onWatchAdForDouble={handleWatchAdForDoubleDaily}
        />
      )}

      {showRewards && (
        <RewardsModal
          stats={stats}
          onClose={() => setShowRewards(false)}
          onWatchRewardAd={handleWatchCashAd}
          onWatchHintAd={handleWatchHintAd}
          onOpenDaily={() => {
            setShowRewards(false);
            setShowDailyReward(true);
          }}
          onOpenWallet={() => {
            setShowRewards(false);
            setShowWallet(true);
          }}
        />
      )}

      {showWallet && (
        <WalletModal
          stats={stats}
          onClose={() => setShowWallet(false)}
        />
      )}

      {showHints && (
        <HintsModal
          stats={stats}
          onClose={() => setShowHints(false)}
          onWatchAdForHints={() => {
            setShowHints(false);
            handleWatchHintAd();
          }}
        />
      )}

      {showSettings && (
        <SettingsModal
          stats={stats}
          onClose={() => setShowSettings(false)}
          onToggleSound={handleToggleSound}
          onToggleVibration={handleToggleVibration}
          onToggleMusic={handleToggleMusic}
          onToggleTheme={handleToggleTheme}
          onToggleNotifications={handleToggleNotifications}
          onOpenCodeExplorer={() => {
            setShowSettings(false);
            setShowCodeExplorer(true);
          }}
          onResetProgress={() => {
            const resetStats = resetGameProgress();
            setStats(resetStats);
            setCurrentLevelId(1);
          }}
        />
      )}

      {showProfile && (
        <ProfileModal
          stats={stats}
          onClose={() => setShowProfile(false)}
          onUpdateStats={updated => {
            updateStats(prev => ({ ...prev, ...updated }));
          }}
          onResetProgress={() => {
            const resetStats = resetGameProgress();
            setStats(resetStats);
            setCurrentLevelId(1);
          }}
        />
      )}

      {showCodeExplorer && (
        <CodeExplorerModal
          onClose={() => setShowCodeExplorer(false)}
        />
      )}

      {/* Fullscreen Ad Overlay */}
      {activeAd && (
        <AdModal
          type={activeAd.type}
          title={activeAd.title}
          rewardLabel={activeAd.rewardLabel}
          onAdCompleted={activeAd.onComplete}
          onClose={() => {
            // For interstitial ads (e.g. between levels), closing or skipping continues to the next level smoothly
            if (activeAd.type === 'INTERSTITIAL') {
              activeAd.onComplete();
            } else {
              setActiveAd(null);
            }
          }}
        />
      )}

      {/* Offline Blocking Overlay */}
      {!isOnline && (
        <div className="fixed inset-0 z-50 bg-slate-900/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl p-6 max-w-sm w-full shadow-2xl text-center flex flex-col items-center">
            <div className="w-16 h-16 rounded-full bg-red-50 text-red-500 flex items-center justify-center mb-4">
              <WifiOff className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-black text-slate-900 mb-2">Internet Connection Required</h3>
            <p className="text-xs text-slate-500 mb-6 leading-relaxed">
              Internet connection required.<br />
              Please turn on mobile data or Wi-Fi and try again.
            </p>
            <button
              onClick={handleRetryConnection}
              className="w-full py-3 bg-sky-600 hover:bg-sky-700 text-white font-black rounded-xl text-sm flex items-center justify-center gap-2 shadow-lg shadow-sky-600/20 active:scale-95 transition-all"
            >
              <RefreshCw className="w-4 h-4" />
              Retry
            </button>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="pt-3 pb-1 text-[11px] text-slate-400 text-center w-full">
        Arrow Escape • Tap • Solve • Escape • Real ₹ Rewards
      </footer>
    </div>
  );
}
