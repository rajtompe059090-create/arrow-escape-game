import React, { useState, useEffect, useRef } from 'react';
import { X, Play, ShieldCheck, Sparkles, CheckCircle2 } from 'lucide-react';
import { sounds } from '../utils/audio';

export type AdType = 'INTERSTITIAL' | 'REWARDED_VIDEO';

interface AdModalProps {
  type: AdType;
  title?: string;
  rewardLabel?: string;
  onAdCompleted: () => void;
  onClose: () => void;
}

export const AdModal: React.FC<AdModalProps> = ({
  type,
  title = 'Sponsored Advertisement',
  rewardLabel,
  onAdCompleted,
  onClose,
}) => {
  const isRewarded = type === 'REWARDED_VIDEO';
  const duration = isRewarded ? 5 : 3; // 5 seconds for rewarded, 3 seconds for interstitial
  const [secondsRemaining, setSecondsRemaining] = useState(duration);
  const [isFinished, setIsFinished] = useState(false);
  const claimedRef = useRef(false);

  const unitId = isRewarded
    ? 'ca-app-pub-3940256099942544/5224354917 (AdMob Test Rewarded Video)'
    : 'ca-app-pub-3940256099942544/1033173712 (AdMob Test Interstitial)';

  useEffect(() => {
    const timer = setInterval(() => {
      setSecondsRemaining(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          setIsFinished(true);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const handleClaim = () => {
    // Prevent duplicate reward execution
    if (claimedRef.current) return;
    claimedRef.current = true;
    sounds.playLevelComplete();
    onAdCompleted();
  };

  const handleCloseOrSkip = () => {
    if (type === 'INTERSTITIAL') {
      // Interstitial ads can always be skipped/closed to continue gameplay
      if (!claimedRef.current) {
        claimedRef.current = true;
        onAdCompleted();
      }
    } else {
      onClose();
    }
  };

  const progressPercent = Math.min(100, Math.round(((duration - secondsRemaining) / duration) * 100));

  return (
    <div
      id="ad-modal-backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200 select-none"
    >
      <div
        id="ad-modal"
        className="w-full max-w-sm bg-slate-900 text-white rounded-3xl p-5 shadow-2xl border border-slate-700 flex flex-col space-y-4 animate-in zoom-in-95 duration-200 relative overflow-hidden"
      >
        {/* Ad Tag Bar */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <span className="px-2 py-0.5 bg-amber-400 text-slate-950 font-black text-[10px] rounded-md uppercase">
              Ad
            </span>
            <span className="text-xs text-slate-400 font-medium">Google AdMob</span>
          </div>

          <div className="flex items-center space-x-2">
            {!isFinished && type === 'INTERSTITIAL' ? (
              <button
                onClick={() => {
                  sounds.playTap();
                  handleCloseOrSkip();
                }}
                className="w-7 h-7 bg-slate-800 hover:bg-slate-700 rounded-full flex items-center justify-center text-slate-400 hover:text-slate-200 transition-all text-xs"
                title="Skip Ad"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            ) : !isFinished ? (
              <span className="text-xs font-mono text-slate-400 bg-slate-800 px-2.5 py-1 rounded-full border border-slate-700">
                Reward in {secondsRemaining}s
              </span>
            ) : (
              <button
                onClick={() => {
                  sounds.playTap();
                  handleClaim();
                }}
                className="w-8 h-8 bg-slate-800 hover:bg-slate-700 rounded-full flex items-center justify-center text-slate-200 transition-all"
                title="Close Ad"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>

        {/* Ad Mock Video Frame */}
        <div className="relative w-full aspect-video bg-gradient-to-tr from-slate-800 to-slate-700 rounded-2xl border border-slate-600/60 flex flex-col items-center justify-center p-4 overflow-hidden text-center shadow-inner">
          <div className="w-12 h-12 rounded-2xl bg-blue-600 flex items-center justify-center text-white mb-2 shadow-lg shadow-blue-500/30">
            <Sparkles className="w-6 h-6 animate-pulse" />
          </div>

          <h3 className="text-sm font-bold text-white tracking-wide">
            {title}
          </h3>
          <p className="text-[11px] text-slate-300 mt-1 max-w-[200px]">
            {rewardLabel ? `Earn ${rewardLabel} upon completion` : 'Arrow Escape Sponsor Network'}
          </p>

          {/* Progress bar at bottom of ad video */}
          <div className="absolute bottom-0 left-0 right-0 h-1.5 bg-slate-800">
            <div
              className="h-full bg-blue-500 transition-all duration-1000 ease-linear"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>

        {/* AdMob Integration Unit ID Meta */}
        <div className="bg-slate-950 p-2.5 rounded-xl border border-slate-800 text-[10px] font-mono text-slate-400 space-y-0.5">
          <p className="text-slate-500 uppercase text-[9px] font-bold">Android AdMob Integration Point</p>
          <p className="text-blue-400 truncate">{unitId}</p>
        </div>

        {/* Ad State Indicators */}
        <div className="flex items-center justify-between px-1 text-[11px] font-bold">
          <span className="text-slate-400">Status:</span>
          {isFinished ? (
            <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 flex items-center gap-1">
              <CheckCircle2 className="w-3 h-3" />
              <span>Ad Completed • Ready to Claim</span>
            </span>
          ) : (
            <span className="px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/40 flex items-center gap-1">
              <Play className="w-3 h-3 fill-current" />
              <span>Watching Ad ({secondsRemaining}s remaining)</span>
            </span>
          )}
        </div>

        {/* Action Button */}
        {isFinished ? (
          <button
            onClick={handleClaim}
            className="w-full py-3 px-4 bg-emerald-600 hover:bg-emerald-500 active:scale-98 text-white font-bold rounded-2xl shadow-lg shadow-emerald-900/30 flex items-center justify-center space-x-2 text-xs transition-all"
          >
            <CheckCircle2 className="w-4 h-4" />
            <span>{rewardLabel ? `Claim ${rewardLabel}` : 'Continue to Next Level'}</span>
          </button>
        ) : (
          <button
            disabled
            className="w-full py-3 px-4 bg-slate-800 text-slate-500 font-bold rounded-2xl text-xs cursor-not-allowed flex items-center justify-center space-x-2"
          >
            <span>Watch Ad to Receive Reward ({secondsRemaining}s)</span>
          </button>
        )}
      </div>
    </div>
  );
};
