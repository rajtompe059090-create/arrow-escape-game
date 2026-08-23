import React from 'react';
import { X, Award, ShieldAlert, CheckCircle2 } from 'lucide-react';
import { UserStats } from '../types/game';
import { sounds } from '../utils/audio';

interface WalletModalProps {
  stats: UserStats;
  onClose: () => void;
}

export const WalletModal: React.FC<WalletModalProps> = ({ stats, onClose }) => {
  const tiers = [
    { range: 'Levels 1 - 50', rate: '₹2 / level', active: stats.unlockedLevel <= 50 },
    { range: 'Levels 51 - 100', rate: '₹3 / level', active: stats.unlockedLevel > 50 && stats.unlockedLevel <= 100 },
    { range: 'Levels 101 - 150', rate: '₹5 / level', active: stats.unlockedLevel > 100 && stats.unlockedLevel <= 150 },
    { range: 'Levels 151 - 200', rate: '₹10 / level', active: stats.unlockedLevel > 150 && stats.unlockedLevel <= 200 },
    { range: 'Levels 201+', rate: '₹15 / level', active: stats.unlockedLevel > 200 },
  ];

  return (
    <div
      id="wallet-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-fade-in"
    >
      <div className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-6 shadow-2xl flex flex-col space-y-4 border border-slate-200">
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 bg-green-50 rounded-2xl border border-green-200 flex items-center justify-center">
              <Award className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <h2 className="text-base font-black text-slate-900">Reward Wallet</h2>
              <p className="text-xs text-slate-400">Offline MVP Balance</p>
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

        {/* Balance Card in Sleek Interface Theme */}
        <div className="bg-slate-900 text-white rounded-2xl p-5 shadow-md flex flex-col justify-between space-y-2 border border-slate-800">
          <div className="flex justify-between items-start">
            <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400">
              Total Balance
            </span>
            <span className="bg-green-100 text-green-700 px-2.5 py-0.5 rounded-full text-[10px] font-bold">
              MVP Active
            </span>
          </div>
          <div>
            <div className="text-3xl font-black text-white">₹{stats.earnedRupees}.00</div>
            <p className="text-xs text-slate-400 mt-0.5">
              Accumulated across {stats.completedLevels.length} solved levels
            </p>
          </div>
        </div>

        {/* Reward Tiers List */}
        <div className="space-y-2">
          <h3 className="text-slate-400 uppercase tracking-widest text-xs font-bold">
            Reward Scale per Level
          </h3>
          <div className="space-y-1.5 max-h-40 overflow-y-auto pr-1">
            {tiers.map((tier, idx) => (
              <div
                key={idx}
                className={`flex items-center justify-between p-2.5 rounded-xl text-xs ${
                  tier.active
                    ? 'bg-blue-50 border border-blue-200 text-blue-900 font-bold'
                    : 'bg-slate-50 border border-slate-100 text-slate-600 font-medium'
                }`}
              >
                <div className="flex items-center space-x-2">
                  {tier.active ? (
                    <CheckCircle2 className="w-3.5 h-3.5 text-blue-600 shrink-0" />
                  ) : (
                    <div className="w-3.5 h-3.5 rounded-full border border-slate-300" />
                  )}
                  <span>{tier.range}</span>
                </div>
                <span className="font-bold text-blue-600">{tier.rate}</span>
              </div>
            ))}
          </div>
        </div>

        {/* MVP Notice */}
        <div className="bg-slate-50 rounded-2xl p-3 border border-slate-200 flex items-start space-x-2 text-left">
          <ShieldAlert className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
          <p className="text-[11px] text-slate-500 leading-snug">
            For the MVP, rewards are tracked locally on your device. Real-money withdrawals and UPI transfers are disabled in this build.
          </p>
        </div>

        {/* Close Button */}
        <button
          onClick={() => {
            sounds.playTap();
            onClose();
          }}
          className="w-full py-3 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-2xl text-xs transition-all shadow-md shadow-blue-200"
        >
          Got it
        </button>
      </div>
    </div>
  );
};
