import React, { useState } from 'react';
import { X, Award, ShieldAlert, CheckCircle2, Wallet, History, ArrowUpRight, Sparkles, Gift } from 'lucide-react';
import { UserStats, EarningTransaction } from '../types/game';
import { sounds } from '../utils/audio';

interface WalletModalProps {
  stats: UserStats;
  onClose: () => void;
}

export const WalletModal: React.FC<WalletModalProps> = ({ stats, onClose }) => {
  const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'HISTORY'>('OVERVIEW');

  const tiers = [
    { range: 'Levels 1 – 100', rate: '₹0.25 / lvl', difficulty: 'Easy', active: stats.unlockedLevel <= 100 },
    { range: 'Levels 101 – 200', rate: '₹0.50 / lvl', difficulty: 'Normal', active: stats.unlockedLevel > 100 && stats.unlockedLevel <= 200 },
    { range: 'Levels 201 – 300', rate: '₹0.75 / lvl', difficulty: 'Hard', active: stats.unlockedLevel > 200 && stats.unlockedLevel <= 300 },
    { range: 'Levels 301 – 400', rate: '₹1.00 / lvl', difficulty: 'Very Hard', active: stats.unlockedLevel > 300 && stats.unlockedLevel <= 400 },
    { range: 'Levels 401 – 600', rate: '₹1.25 / lvl', difficulty: 'Master', active: stats.unlockedLevel > 400 && stats.unlockedLevel <= 600 },
    { range: 'Levels 601 – 800', rate: '₹1.50 / lvl', difficulty: 'Grandmaster', active: stats.unlockedLevel > 600 && stats.unlockedLevel <= 800 },
    { range: 'Levels 801+', rate: '₹2.00 / lvl', difficulty: 'Legendary', active: stats.unlockedLevel > 800 },
  ];

  const formatDate = (timestamp: number) => {
    const d = new Date(timestamp);
    return `${d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}, ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
  };

  const getTransactionIcon = (type: EarningTransaction['type']) => {
    switch (type) {
      case 'LEVEL_REWARD':
        return <Award className="w-3.5 h-3.5 text-blue-600" />;
      case 'DAILY_REWARD':
        return <Gift className="w-3.5 h-3.5 text-amber-600" />;
      case 'AD_BONUS':
        return <Sparkles className="w-3.5 h-3.5 text-emerald-600" />;
      default:
        return <ArrowUpRight className="w-3.5 h-3.5 text-slate-600" />;
    }
  };

  return (
    <div
      id="wallet-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-5 shadow-2xl flex flex-col space-y-3.5 border border-slate-200 max-h-[90vh] overflow-y-auto">
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 bg-emerald-50 rounded-2xl border border-emerald-200 flex items-center justify-center">
              <Wallet className="w-5 h-5 text-emerald-600" />
            </div>
            <div>
              <h2 className="text-base font-black text-slate-900">Earnings Wallet</h2>
              <p className="text-xs text-slate-400">Real ₹ Rewards & History</p>
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

        {/* Dual Balance Card */}
        <div className="bg-slate-900 text-white rounded-2xl p-4 shadow-md flex flex-col space-y-3 border border-slate-800">
          <div className="grid grid-cols-2 gap-3 border-b border-slate-800 pb-3">
            <div>
              <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400">
                Wallet Balance
              </span>
              <div className="text-2xl font-black text-emerald-400 mt-0.5">
                ₹{stats.walletBalance.toFixed(2)}
              </div>
            </div>
            <div className="text-right">
              <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400">
                Total Earned
              </span>
              <div className="text-xl font-black text-white mt-0.5">
                ₹{stats.totalEarnings.toFixed(2)}
              </div>
            </div>
          </div>

          <div className="flex items-center justify-between text-[11px] text-slate-400">
            <span>Solved Levels: <strong className="text-white">{stats.completedLevels.length}</strong></span>
            <span className="px-2 py-0.5 bg-emerald-950/80 text-emerald-400 rounded-full font-bold border border-emerald-800/60 text-[10px]">
              Offline Ledger
            </span>
          </div>
        </div>

        {/* Tab Toggle: Overview vs History */}
        <div className="grid grid-cols-2 gap-1 p-1 bg-slate-100 rounded-xl">
          <button
            onClick={() => {
              sounds.playTap();
              setActiveTab('OVERVIEW');
            }}
            className={`py-1.5 text-xs font-bold rounded-lg transition-all ${
              activeTab === 'OVERVIEW'
                ? 'bg-white text-slate-900 shadow-2xs'
                : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            Reward Tiers
          </button>
          <button
            onClick={() => {
              sounds.playTap();
              setActiveTab('HISTORY');
            }}
            className={`py-1.5 text-xs font-bold rounded-lg transition-all flex items-center justify-center space-x-1.5 ${
              activeTab === 'HISTORY'
                ? 'bg-white text-slate-900 shadow-2xs'
                : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            <History className="w-3.5 h-3.5" />
            <span>History ({stats.earningHistory.length})</span>
          </button>
        </div>

        {activeTab === 'OVERVIEW' ? (
          <div className="space-y-2">
            <h3 className="text-slate-400 uppercase tracking-widest text-[10px] font-black">
              Continuous Level Reward Rates
            </h3>
            <div className="space-y-1.5">
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
                    <span className="text-[10px] text-slate-400">({tier.difficulty})</span>
                  </div>
                  <span className="font-black text-green-700">{tier.rate}</span>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="space-y-2">
            <h3 className="text-slate-400 uppercase tracking-widest text-[10px] font-black">
              Recent Earning Transactions
            </h3>
            <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
              {stats.earningHistory.length === 0 ? (
                <div className="p-4 bg-slate-50 rounded-xl text-center text-xs text-slate-400">
                  No transactions yet. Complete levels or claim daily rewards to see earnings here!
                </div>
              ) : (
                [...stats.earningHistory].reverse().map(tx => (
                  <div
                    key={tx.id}
                    className="p-2 bg-slate-50 rounded-xl border border-slate-200/80 flex items-center justify-between text-xs"
                  >
                    <div className="flex items-center space-x-2">
                      <div className="w-7 h-7 rounded-lg bg-white border border-slate-200 flex items-center justify-center">
                        {getTransactionIcon(tx.type)}
                      </div>
                      <div>
                        <p className="font-bold text-slate-800">{tx.title}</p>
                        <p className="text-[10px] text-slate-400">{formatDate(tx.timestamp)}</p>
                      </div>
                    </div>
                    <span className="font-black text-green-700">+₹{tx.amount.toFixed(2)}</span>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {/* MVP Notice */}
        <div className="bg-slate-50 rounded-2xl p-2.5 border border-slate-200 flex items-start space-x-2 text-left">
          <ShieldAlert className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
          <p className="text-[10px] text-slate-500 leading-snug">
            Real ₹ earnings are saved locally to your device. Real-money UPI & bank payouts will be unlocked in the upcoming production release.
          </p>
        </div>

        {/* Close Button */}
        <button
          onClick={() => {
            sounds.playTap();
            onClose();
          }}
          className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-2xl text-xs transition-all shadow-md shadow-blue-200"
        >
          Close
        </button>
      </div>
    </div>
  );
};
