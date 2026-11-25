import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { CalendarIcon, ClockIcon } from './Icons';

export default function Navbar({ currentRole, setCurrentRole, activePage }) {
  const [backendOnline, setBackendOnline] = useState(true);
  const [currentTime, setCurrentTime] = useState(new Date().toLocaleTimeString());

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date().toLocaleTimeString());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        await api.getDashboardStats();
        setBackendOnline(true);
      } catch (e) {
        setBackendOnline(false);
      }
    };
    checkHealth();
    const interval = setInterval(checkHealth, 15000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="bg-slate-900 text-white border-b border-slate-800 px-6 py-3 flex items-center justify-between shrink-0 z-30 shadow-md">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center text-white font-bold shadow-lg shadow-blue-500/30">
          <CalendarIcon className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-base font-bold tracking-tight text-white flex items-center gap-2">
            UniScheduler
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-300 border border-blue-500/30">
              Academic Enterprise
            </span>
          </h1>
          <p className="text-xs text-slate-400">Automated Course Timetabling & Examination Management System</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {/* System Status indicator */}
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-800/80 border border-slate-700/60 text-xs">
          <span className={`w-2 h-2 rounded-full ${backendOnline ? 'bg-emerald-400 animate-pulse' : 'bg-rose-500'}`} />
          <span className="text-slate-300">
            {backendOnline ? 'System Online' : 'Service Disconnected'}
          </span>
        </div>

        {/* Live Clock */}
        <div className="hidden md:flex items-center gap-1.5 text-xs text-slate-400 bg-slate-800/50 px-3 py-1.5 rounded-lg border border-slate-700/40">
          <ClockIcon className="w-4 h-4 text-slate-400" />
          <span>{currentTime}</span>
        </div>

        {/* Role Switcher */}
        <div className="flex items-center gap-1.5 bg-slate-800 p-1 rounded-lg border border-slate-700 shadow-inner">
          <span className="text-xs text-slate-400 pl-2 pr-1 font-semibold">Persona:</span>
          {[
            { id: 'ADMIN', label: 'Admin', activeClass: 'bg-blue-600 text-white shadow-md shadow-blue-500/30' },
            { id: 'FACULTY', label: 'Faculty', activeClass: 'bg-amber-600 text-white shadow-md shadow-amber-500/30' },
            { id: 'STUDENT', label: 'Student', activeClass: 'bg-emerald-600 text-white shadow-md shadow-emerald-500/30' },
          ].map((r) => (
            <button
              key={r.id}
              onClick={() => setCurrentRole(r.id)}
              className={`px-3 py-1 rounded-md text-xs font-semibold transition-all ${
                currentRole === r.id
                  ? r.activeClass
                  : 'text-slate-400 hover:text-white hover:bg-slate-700/50'
              }`}
              title={`Switch access persona to ${r.label}`}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>
    </header>
  );
}
