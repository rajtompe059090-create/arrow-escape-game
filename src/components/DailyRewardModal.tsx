import React, { useState, useEffect } from 'react';
import {
  X,
  Gift,
  Sparkles,
  Check,
  PlayCircle,
  Flame,
  Clock,
  Wallet,
  Coins,
  Crown,
  Award,
  Calendar,
  CheckCircle2,
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { sounds } from '../utils/audio';
import { UserStats } from '../types/game';

interface DailyRewardModalProps {
  stats: UserStats;
  onClose: () => void;
  onClaimDaily: (amount: number, newStreak: number) => void;
  onWatchAdForDouble: (amount: number, newStreak: number) => void;
}

// 7-day reward cycle amounts:
// Day 1: ₹1, Day 2: ₹2, Day 3: ₹3, Day 4: ₹4, Day 5: ₹5, Day 6: ₹7, Day 7: ₹10
export const DAILY_REWARDS = [
  { day: 1, amount: 1 },
  { day: 2, amount: 2 },
  { day: 3, amount: 3 },
  { day: 4, amount: 4 },
  { day: 5, amount: 5 },
  { day: 6, amount: 7 },
  { day: 7, amount: 10 },
];

const TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000;
const FORTY_EIGHT_HOURS_MS = 48 * 60 * 60 * 1000;

export const DailyRewardModal: React.FC<DailyRewardModalProps> = ({
  stats,
  onClose,
  onClaimDaily,
  onWatchAdForDouble,
}) => {
  const [claimedJustNow, setClaimedJustNow] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [timeLeftStr, setTimeLeftStr] = useState<string>('');

  const now = Date.now();
  const lastTimestamp = stats.lastDailyRewardTimestamp;

  // 1. Calculate cooldown eligibility: strictly 1 claim per 24 hours
  const isWithin24Hours = Boolean(
    lastTimestamp && now - lastTimestamp < TWENTY_FOUR_HOURS_MS
  );

  const isClaimEligible = !claimedJustNow && !isWithin24Hours;

  // 2. Calculate streak status and allowed claim period
  // If player hasn't claimed in > 48h, streak resets to Day 1
  const isStreakBroken = Boolean(
    lastTimestamp && now - lastTimestamp > FORTY_EIGHT_HOURS_MS
  );

  // Active continuous streak count
  const effectiveStreak = isStreakBroken
    ? 0
    : stats.dailyStreak || 0;

  // Day in the 7-day cycle:
  // If claimed today: the day that was claimed today is ((stats.dailyStreak - 1) % 7) + 1
  // If eligible to claim: next day to claim is (effectiveStreak % 7) + 1
  let activeCycleDay: number;
  let claimedDaysInCycle: number;

  if (!isClaimEligible) {
    // Already claimed today
    activeCycleDay = ((stats.dailyStreak - 1) % 7) + 1;
    claimedDaysInCycle = activeCycleDay;
  } else {
    // Ready to claim
    activeCycleDay = (effectiveStreak % 7) + 1;
    claimedDaysInCycle = effectiveStreak % 7; // days before today's claim
  }

  const todayRewardObj = DAILY_REWARDS[activeCycleDay - 1] || DAILY_REWARDS[0];
  const todayRewardAmount = todayRewardObj.amount;

  // Live countdown timer until next 24-hour claim is available
  useEffect(() => {
    const updateCountdown = () => {
      if (!stats.lastDailyRewardTimestamp) {
        setTimeLeftStr('');
        return;
      }
      const diff = TWENTY_FOUR_HOURS_MS - (Date.now() - stats.lastDailyRewardTimestamp);
      if (diff <= 0) {
        setTimeLeftStr('');
      } else {
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        setTimeLeftStr(
          `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
        );
      }
    };

    updateCountdown();
    const interval = setInterval(updateCountdown, 1000);
    return () => clearInterval(interval);
  }, [stats.lastDailyRewardTimestamp, claimedJustNow]);

  // Handle single and double reward claims with rapid-click prevention
  const handleClaim = (multiplier: number = 1) => {
    if (!isClaimEligible || isProcessing) return;
    setIsProcessing(true);

    const nextStreak = isStreakBroken ? 1 : effectiveStreak + 1;
    const finalAmount = todayRewardAmount * multiplier;

    sounds.playLevelComplete();
    try {
      confetti({
        particleCount: 70,
        spread: 65,
        origin: { y: 0.6 },
        colors: ['#F59E0B', '#10B981', '#3B82F6', '#6366F1', '#EC4899'],
      });
    } catch {}

    setClaimedJustNow(true);

    if (multiplier === 2) {
      onWatchAdForDouble(finalAmount, nextStreak);
    } else {
      onClaimDaily(finalAmount, nextStreak);
    }

    setTimeout(() => {
      setIsProcessing(false);
    }, 1000);
  };

  const nextUpcomingDay = (activeCycleDay % 7) + 1;
  const nextUpcomingReward = DAILY_REWARDS[nextUpcomingDay - 1].amount;

  return (
    <div
      id="daily-reward-modal-backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200"
    >
      <div
        id="daily-reward-modal"
        className="w-full max-w-sm bg-white rounded-3xl p-5 shadow-2xl border border-slate-200 flex flex-col space-y-4 text-slate-900 animate-in zoom-in-95 duration-200 select-none max-h-[92vh] overflow-y-auto"
      >
        {/* 1. Header Bar */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 bg-amber-50 rounded-2xl border border-amber-200 flex items-center justify-center text-amber-600 shadow-2xs">
              <Gift className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center space-x-1.5">
                <span className="text-base font-black text-slate-900 tracking-tight">
                  🎁 DAILY REWARD
                </span>
              </div>
              <p className="text-xs text-slate-400 font-semibold">
                7-Day Free Real ₹ Cash Cycle
              </p>
            </div>
          </div>
          <button
            id="daily-reward-close-btn"
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

        {/* 2. Key Metrics Row: 🔥 Current Streak, 💰 Today's Reward, 💰 Wallet Balance */}
        <div className="grid grid-cols-3 gap-2">
          {/* 🔥 Current Streak */}
          <div className="bg-amber-50/90 border border-amber-200/90 rounded-2xl p-2.5 flex flex-col items-center text-center shadow-2xs">
            <div className="flex items-center space-x-1 text-amber-700 text-[10px] font-extrabold uppercase tracking-wide">
              <Flame className="w-3.5 h-3.5 fill-current text-amber-500" />
              <span>Streak</span>
            </div>
            <p className="text-base font-black text-amber-900 mt-1">
              {stats.dailyStreak} {stats.dailyStreak === 1 ? 'Day' : 'Days'}
            </p>
            <span className="text-[9px] font-bold text-amber-700 mt-0.5">
              Day {activeCycleDay} of 7
            </span>
          </div>

          {/* 💰 Today's Reward */}
          <div className="bg-emerald-50/90 border border-emerald-200/90 rounded-2xl p-2.5 flex flex-col items-center text-center shadow-2xs">
            <div className="flex items-center space-x-1 text-emerald-700 text-[10px] font-extrabold uppercase tracking-wide">
              <Award className="w-3.5 h-3.5 text-emerald-600" />
              <span>Today</span>
            </div>
            <p className="text-base font-black text-emerald-700 mt-1">
              ₹{todayRewardAmount.toFixed(2)}
            </p>
            <span className="text-[9px] font-bold text-emerald-600 mt-0.5">
              {isClaimEligible ? 'Ready to claim' : 'Claimed ✓'}
            </span>
          </div>

          {/* 💰 Wallet Balance */}
          <div className="bg-blue-50/90 border border-blue-200/90 rounded-2xl p-2.5 flex flex-col items-center text-center shadow-2xs">
            <div className="flex items-center space-x-1 text-blue-700 text-[10px] font-extrabold uppercase tracking-wide">
              <Wallet className="w-3.5 h-3.5 text-blue-600" />
              <span>Wallet</span>
            </div>
            <p className="text-base font-black text-blue-700 mt-1">
              ₹{stats.walletBalance.toFixed(2)}
            </p>
            <span className="text-[9px] font-bold text-blue-600 mt-0.5">
              Real Cash
            </span>
          </div>
        </div>

        {/* 3. 7-Day Reward Cycle Grid */}
        <div className="space-y-1.5 pt-1">
          <div className="flex items-center justify-between px-1">
            <span className="text-[10px] font-black uppercase tracking-wider text-slate-400">
              7-Day Rewards Path
            </span>
            <span className="text-[10px] font-extrabold text-blue-600">
              {claimedDaysInCycle} / 7 Claimed
            </span>
          </div>

          <div className="grid grid-cols-4 gap-2">
            {DAILY_REWARDS.map(item => {
              const isClaimedInThisCycle = item.day <= claimedDaysInCycle;
              const isTodayActive = item.day === activeCycleDay && isClaimEligible;
              const isGrandDay7 = item.day === 7;

              let cardStyle = 'bg-slate-50 border-slate-200 text-slate-500';
              if (isClaimedInThisCycle) {
                cardStyle = 'bg-emerald-50/90 border-emerald-300 text-emerald-800 shadow-2xs';
              } else if (isTodayActive) {
                cardStyle =
                  'bg-gradient-to-b from-amber-50 to-amber-100 border-2 border-amber-400 text-amber-900 ring-2 ring-amber-300/50 shadow-md font-black scale-102';
              } else if (isGrandDay7) {
                cardStyle =
                  'bg-gradient-to-br from-purple-50 to-indigo-50 border border-purple-200 text-purple-900';
              }

              return (
                <div
                  key={item.day}
                  id={`daily-reward-card-day-${item.day}`}
                  className={`p-2.5 rounded-2xl border flex flex-col items-center justify-between text-center relative transition-all duration-200 ${
                    isGrandDay7 ? 'col-span-2' : ''
                  } ${cardStyle}`}
                >
                  {/* Top Day Badge */}
                  <div className="w-full flex items-center justify-between">
                    <span className="text-[10px] font-extrabold uppercase tracking-tight">
                      Day {item.day}
                    </span>
                    {isGrandDay7 && (
                      <span className="flex items-center space-x-0.5 text-[9px] font-black text-purple-700 bg-purple-100/80 px-1.5 py-0.2 rounded-md">
                        <Crown className="w-3 h-3 text-amber-500 fill-amber-400" />
                        <span>Grand</span>
                      </span>
                    )}
                    {isTodayActive && (
                      <span className="w-2 h-2 rounded-full bg-amber-500 animate-ping" />
                    )}
                  </div>

                  {/* Center Reward Amount */}
                  <div className="my-1.5 flex flex-col items-center">
                    <span
                      className={`text-sm font-black ${
                        isClaimedInThisCycle
                          ? 'text-emerald-700'
                          : isTodayActive
                          ? 'text-amber-800 text-base'
                          : isGrandDay7
                          ? 'text-purple-700 text-base'
                          : 'text-slate-700'
                      }`}
                    >
                      ₹{item.amount.toFixed(2)}
                    </span>
                  </div>

                  {/* Status Indicator */}
                  <div className="w-full flex items-center justify-center pt-0.5">
                    {isClaimedInThisCycle ? (
                      <span className="flex items-center space-x-1 text-[9px] font-black text-emerald-700 bg-emerald-100/70 px-1.5 py-0.2 rounded-md">
                        <Check className="w-3 h-3 text-emerald-600 stroke-[3]" />
                        <span>Claimed</span>
                      </span>
                    ) : isTodayActive ? (
                      <span className="flex items-center space-x-1 text-[9px] font-black text-amber-800 bg-amber-200/80 px-1.5 py-0.2 rounded-md animate-pulse">
                        <Sparkles className="w-3 h-3 text-amber-600 fill-amber-500" />
                        <span>Today</span>
                      </span>
                    ) : (
                      <span className="text-[9px] font-bold text-slate-400">
                        Upcoming
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 4. Action Section or 24-Hour Cooldown Timer */}
        {isClaimEligible ? (
          <div className="space-y-2.5 pt-1">
            {/* 2X Reward Ad button */}
            <button
              id="daily-reward-double-btn"
              disabled={isProcessing}
              onClick={() => handleClaim(2)}
              className="w-full py-3.5 px-4 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 active:scale-98 text-white font-black rounded-2xl shadow-md shadow-amber-200 flex items-center justify-center space-x-2 text-xs transition-all disabled:opacity-60"
            >
              <PlayCircle className="w-4 h-4 fill-current" />
              <span>
                Watch Ad for 2X (₹{(todayRewardAmount * 2).toFixed(2)})
              </span>
            </button>

            {/* Standard claim button */}
            <button
              id="daily-reward-claim-btn"
              disabled={isProcessing}
              onClick={() => handleClaim(1)}
              className="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-black rounded-2xl shadow-sm shadow-blue-200 text-xs transition-all flex items-center justify-center space-x-1.5 disabled:opacity-60"
            >
              <span>Claim Today's ₹{todayRewardAmount.toFixed(2)}</span>
            </button>
          </div>
        ) : (
          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200 text-center space-y-2.5">
            <div className="flex items-center justify-center space-x-1.5 text-emerald-700 font-black text-xs">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              <span>Reward Claimed for Today!</span>
            </div>

            {timeLeftStr ? (
              <div className="flex items-center justify-center space-x-2 bg-white py-2 px-3.5 rounded-xl border border-slate-200 max-w-xs mx-auto text-slate-700 font-mono text-xs shadow-2xs">
                <Clock className="w-4 h-4 text-blue-600" />
                <span>
                  Next reward in:{' '}
                  <strong className="text-slate-900 font-black tracking-wider">
                    {timeLeftStr}
                  </strong>
                </span>
              </div>
            ) : (
              <p className="text-[11px] text-slate-500 font-medium">
                Next reward will unlock in 24 hours.
              </p>
            )}

            <p className="text-[10px] font-bold text-slate-500">
              Come back tomorrow to unlock{' '}
              <span className="text-blue-600 font-black">
                Day {nextUpcomingDay} (₹{nextUpcomingReward.toFixed(2)})
              </span>
              .
            </p>
          </div>
        )}

        {/* Close button */}
        <button
          id="daily-reward-modal-close-footer"
          onClick={() => {
            sounds.playTap();
            onClose();
          }}
          className="w-full py-2.5 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-600 font-bold rounded-2xl text-xs transition-all border border-slate-200/80"
        >
          Close
        </button>
      </div>
    </div>
  );
};
