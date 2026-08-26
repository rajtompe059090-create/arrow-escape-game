import React, { useState } from 'react';
import {
  X,
  User,
  Trophy,
  Award,
  Zap,
  RotateCcw,
  Copy,
  Check,
  Edit2,
  Save,
  ShieldCheck,
  QrCode,
  Share2,
  Wallet,
  KeyRound,
  Lock,
  UserPlus,
  LogIn,
} from 'lucide-react';
import { UserStats } from '../types/game';
import { sounds } from '../utils/audio';

interface ProfileModalProps {
  stats: UserStats;
  onClose: () => void;
  onUpdateStats: (updated: Partial<UserStats>) => void;
  onResetProgress: () => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({
  stats,
  onClose,
  onUpdateStats,
  onResetProgress,
}) => {
  const [isEditingUpi, setIsEditingUpi] = useState(false);
  const [upiInput, setUpiInput] = useState(stats.upiId || '');
  const [isEditingName, setIsEditingName] = useState(false);
  const [nameInput, setNameInput] = useState(stats.displayName || 'Player One');

  const [copiedUid, setCopiedUid] = useState(false);
  const [copiedRef, setCopiedRef] = useState(false);

  const [showAuthForm, setShowAuthForm] = useState(false);
  const [authMode, setAuthMode] = useState<'REGISTER' | 'LOGIN'>('REGISTER');
  const [authUsername, setAuthUsername] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authError, setAuthError] = useState('');
  const [authSuccess, setAuthSuccess] = useState('');

  const uid = stats.uid || 'AE-849201';
  const username = stats.username || 'player_0590';
  const displayName = stats.displayName || 'Player One';
  const referralCode = stats.referralCode || 'ESC-849201';
  const isRegistered = Boolean(stats.isRegistered);

  const handleCopy = (text: string, type: 'UID' | 'REF') => {
    sounds.playTap();
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text).catch(() => {});
    }
    if (type === 'UID') {
      setCopiedUid(true);
      setTimeout(() => setCopiedUid(false), 2000);
    } else {
      setCopiedRef(true);
      setTimeout(() => setCopiedRef(false), 2000);
    }
  };

  const handleSaveName = () => {
    sounds.playTap();
    const clean = nameInput.trim() || 'Player One';
    onUpdateStats({ displayName: clean });
    setIsEditingName(false);
  };

  const handleSaveUpi = () => {
    sounds.playTap();
    const clean = upiInput.trim();
    onUpdateStats({ upiId: clean });
    setIsEditingUpi(false);
  };

  const handleAuthSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    sounds.playTap();
    setAuthError('');
    setAuthSuccess('');

    if (!authUsername.trim() || authUsername.trim().length < 3) {
      setAuthError('Username must be at least 3 characters.');
      return;
    }
    if (!authPassword || authPassword.length < 4) {
      setAuthError('Password must be at least 4 characters.');
      return;
    }

    // Never store plain text password. Update profile state.
    const cleanUser = authUsername.trim().toLowerCase().replace(/\s+/g, '_');
    onUpdateStats({
      username: cleanUser,
      displayName: authUsername.trim(),
      isRegistered: true,
    });

    setAuthSuccess(
      authMode === 'REGISTER'
        ? 'Account registered successfully!'
        : 'Logged in successfully!'
    );
    setTimeout(() => {
      setShowAuthForm(false);
      setAuthSuccess('');
      setAuthPassword('');
    }, 1200);
  };

  return (
    <div
      id="profile-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-md bg-white text-slate-900 rounded-3xl p-5 shadow-2xl flex flex-col space-y-4 border border-slate-200 max-h-[92vh] overflow-y-auto">
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-11 h-11 bg-linear-to-br from-blue-500 to-indigo-600 rounded-2xl flex items-center justify-center shadow-md shadow-blue-500/20 text-white font-black text-lg">
              {displayName.charAt(0).toUpperCase()}
            </div>
            <div>
              <div className="flex items-center space-x-1.5">
                <h2 className="text-base font-black text-slate-900">{displayName}</h2>
                {isRegistered ? (
                  <span className="inline-flex items-center gap-0.5 px-1.5 py-0.5 bg-emerald-100 text-emerald-800 rounded-md text-[10px] font-bold">
                    <ShieldCheck className="w-3 h-3 text-emerald-600" />
                    Verified
                  </span>
                ) : (
                  <span className="inline-flex items-center px-1.5 py-0.5 bg-slate-100 text-slate-600 rounded-md text-[10px] font-bold">
                    Guest
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-500">@{username}</p>
            </div>
          </div>
          <button
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="w-8 h-8 bg-slate-100 hover:bg-slate-200 rounded-full flex items-center justify-center text-slate-600 transition-all cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Balance Card Banner */}
        <div className="bg-slate-900 text-white rounded-2xl p-4 shadow-md flex items-center justify-between border border-slate-800">
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
              Total Earnings
            </span>
            <div className="text-lg font-black text-white mt-0.5">
              ₹{stats.totalEarnings.toFixed(2)}
            </div>
          </div>
        </div>

        {/* User Identity Details & Codes */}
        <div className="bg-slate-50 rounded-2xl p-3.5 border border-slate-200/80 flex flex-col space-y-2.5">
          {/* Game UID */}
          <div className="flex items-center justify-between">
            <div>
              <span className="text-[10px] uppercase font-bold text-slate-400">Game UID / User Code</span>
              <div className="text-xs font-mono font-bold text-slate-800">{uid}</div>
            </div>
            <button
              onClick={() => handleCopy(uid, 'UID')}
              className="px-2.5 py-1 bg-white hover:bg-slate-100 border border-slate-200 rounded-lg flex items-center space-x-1 text-[11px] font-bold text-slate-700 transition-all cursor-pointer"
            >
              {copiedUid ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copiedUid ? 'Copied' : 'Copy'}</span>
            </button>
          </div>

          <div className="h-px bg-slate-200/60" />

          {/* Referral Code */}
          <div className="flex items-center justify-between">
            <div>
              <span className="text-[10px] uppercase font-bold text-slate-400">Referral Code</span>
              <div className="text-xs font-mono font-bold text-indigo-700">{referralCode}</div>
            </div>
            <button
              onClick={() => handleCopy(referralCode, 'REF')}
              className="px-2.5 py-1 bg-white hover:bg-slate-100 border border-slate-200 rounded-lg flex items-center space-x-1 text-[11px] font-bold text-indigo-600 transition-all cursor-pointer"
            >
              {copiedRef ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Share2 className="w-3.5 h-3.5" />}
              <span>{copiedRef ? 'Copied' : 'Share'}</span>
            </button>
          </div>

          <div className="h-px bg-slate-200/60" />

          {/* UPI ID Field */}
          <div className="flex items-center justify-between">
            <div className="flex-1 mr-2">
              <span className="text-[10px] uppercase font-bold text-slate-400">UPI ID for Payouts</span>
              {isEditingUpi ? (
                <input
                  type="text"
                  value={upiInput}
                  onChange={(e) => setUpiInput(e.target.value)}
                  placeholder="e.g. yourname@okaxis"
                  className="w-full mt-0.5 px-2.5 py-1 bg-white border border-blue-400 rounded-lg text-xs font-semibold focus:outline-hidden"
                />
              ) : (
                <div className="text-xs font-medium text-slate-800 truncate">
                  {stats.upiId ? stats.upiId : <span className="text-slate-400 italic">Not set (tap edit to add)</span>}
                </div>
              )}
            </div>
            {isEditingUpi ? (
              <button
                onClick={handleSaveUpi}
                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg flex items-center space-x-1 text-[11px] font-bold transition-all cursor-pointer"
              >
                <Save className="w-3.5 h-3.5" />
                <span>Save</span>
              </button>
            ) : (
              <button
                onClick={() => {
                  sounds.playTap();
                  setUpiInput(stats.upiId || '');
                  setIsEditingUpi(true);
                }}
                className="px-2.5 py-1 bg-white hover:bg-slate-100 border border-slate-200 rounded-lg flex items-center space-x-1 text-[11px] font-bold text-slate-700 transition-all cursor-pointer"
              >
                <Edit2 className="w-3.5 h-3.5" />
                <span>Edit</span>
              </button>
            )}
          </div>
        </div>

        {/* Register / Login CTA if not registered */}
        {!isRegistered && !showAuthForm && (
          <div className="bg-linear-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-2xl p-3.5 flex items-center justify-between">
            <div>
              <h4 className="text-xs font-black text-blue-950">Save Progress & Payouts</h4>
              <p className="text-[11px] text-blue-700">Create an account to safeguard your rewards</p>
            </div>
            <button
              onClick={() => {
                sounds.playTap();
                setShowAuthForm(true);
              }}
              className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl text-xs flex items-center space-x-1 shadow-sm transition-all cursor-pointer"
            >
              <UserPlus className="w-3.5 h-3.5" />
              <span>Register</span>
            </button>
          </div>
        )}

        {/* Auth Form (Expandable) */}
        {showAuthForm && (
          <form onSubmit={handleAuthSubmit} className="bg-slate-50 border border-slate-200 rounded-2xl p-3.5 space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-black text-slate-900">
                {authMode === 'REGISTER' ? 'Create Game Account' : 'Account Login'}
              </span>
              <div className="flex space-x-1">
                <button
                  type="button"
                  onClick={() => {
                    sounds.playTap();
                    setAuthMode('REGISTER');
                  }}
                  className={`px-2 py-0.5 rounded-md text-[10px] font-bold transition-all cursor-pointer ${
                    authMode === 'REGISTER' ? 'bg-blue-600 text-white' : 'text-slate-600 bg-slate-200'
                  }`}
                >
                  Register
                </button>
                <button
                  type="button"
                  onClick={() => {
                    sounds.playTap();
                    setAuthMode('LOGIN');
                  }}
                  className={`px-2 py-0.5 rounded-md text-[10px] font-bold transition-all cursor-pointer ${
                    authMode === 'LOGIN' ? 'bg-blue-600 text-white' : 'text-slate-600 bg-slate-200'
                  }`}
                >
                  Login
                </button>
              </div>
            </div>

            <div>
              <label className="text-[10px] font-bold uppercase text-slate-400">Username</label>
              <input
                type="text"
                value={authUsername}
                onChange={(e) => setAuthUsername(e.target.value)}
                placeholder="e.g. MasterGamer"
                className="w-full px-2.5 py-1.5 bg-white border border-slate-300 rounded-xl text-xs focus:border-blue-500 focus:outline-hidden"
              />
            </div>

            <div>
              <label className="text-[10px] font-bold uppercase text-slate-400">Password</label>
              <input
                type="password"
                value={authPassword}
                onChange={(e) => setAuthPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-2.5 py-1.5 bg-white border border-slate-300 rounded-xl text-xs focus:border-blue-500 focus:outline-hidden"
              />
            </div>

            {authError && <p className="text-[11px] text-rose-600 font-medium">{authError}</p>}
            {authSuccess && <p className="text-[11px] text-emerald-600 font-medium">{authSuccess}</p>}

            <div className="flex space-x-2 pt-1">
              <button
                type="submit"
                className="flex-1 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl text-xs transition-all cursor-pointer shadow-xs"
              >
                {authMode === 'REGISTER' ? 'Register Account' : 'Sign In'}
              </button>
              <button
                type="button"
                onClick={() => {
                  sounds.playTap();
                  setShowAuthForm(false);
                }}
                className="px-3 py-2 bg-slate-200 hover:bg-slate-300 text-slate-700 font-bold rounded-xl text-xs transition-all cursor-pointer"
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        {/* Stats Grid */}
        <div className="grid grid-cols-2 gap-2">
          <div className="bg-slate-50 p-2.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <Trophy className="w-4 h-4 text-blue-500 mb-0.5" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Cleared</span>
            <span className="text-sm font-black text-slate-900">
              {stats.completedLevels.length} Levels
            </span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <Award className="w-4 h-4 text-green-500 mb-0.5" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Total Earned</span>
            <span className="text-sm font-black text-green-700">
              ₹{stats.totalEarnings.toFixed(2)}
            </span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <Zap className="w-4 h-4 text-amber-500 mb-0.5" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Hints Remaining</span>
            <span className="text-sm font-black text-slate-900">
              {stats.hintsRemaining} Left
            </span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-2xl border border-slate-200/80 flex flex-col items-center text-center">
            <User className="w-4 h-4 text-indigo-500 mb-0.5" />
            <span className="text-[10px] uppercase font-bold text-slate-400">Current Level</span>
            <span className="text-sm font-black text-blue-600">
              Level {stats.unlockedLevel}
            </span>
          </div>
        </div>

        {/* Reset Progress Section */}
        <div className="pt-2 border-t border-slate-100 flex flex-col space-y-2">
          <button
            onClick={() => {
              if (window.confirm('Are you sure you want to reset all game progress and start over? This cannot be undone.')) {
                sounds.playTap();
                onResetProgress();
                onClose();
              }
            }}
            className="w-full py-2 px-4 bg-rose-50 hover:bg-rose-100 text-rose-700 font-bold rounded-2xl border border-rose-200 flex items-center justify-center space-x-2 text-xs transition-all cursor-pointer"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Reset Progress</span>
          </button>
        </div>

        {/* Close Button */}
        <button
          onClick={() => {
            sounds.playTap();
            onClose();
          }}
          className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-2xl text-xs transition-all shadow-md shadow-blue-200 cursor-pointer"
        >
          Close
        </button>
      </div>
    </div>
  );
};
