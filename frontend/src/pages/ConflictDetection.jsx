import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  AlertTriangleIcon,
  CheckCircleIcon,
  RefreshCwIcon,
  PlayIcon,
} from '../components/Icons';

export default function ConflictDetection({ setActivePage }) {
  const [conflicts, setConflicts] = useState([]);
  const [loading, setLoading] = useState(true);

  const scan = async () => {
    try {
      setLoading(true);
      const data = await api.scanConflicts();
      setConflicts(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    scan();
  }, []);

  const hardConflicts = conflicts.filter((c) => c.severity === 'HARD');
  const softConflicts = conflicts.filter((c) => c.severity === 'SOFT');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <AlertTriangleIcon className="w-5 h-5 text-rose-600" />
            Schedule Conflict Scanner & Resolution Advisor
          </h2>
          <p className="text-xs text-slate-500">
            Real-time validation against all University Hard Constraints (mutual exclusions, capacity, room types) and Soft Constraints
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={scan} className="btn-primary text-xs">
            <RefreshCwIcon className="w-4 h-4" />
            Re-scan Schedule
          </button>
        </div>
      </div>

      {/* Overview Status Banner */}
      {loading ? (
        <div className="app-card p-12 text-center text-xs text-slate-500">Scanning schedule for conflicts...</div>
      ) : conflicts.length === 0 ? (
        <div className="app-card p-8 bg-emerald-50/70 border-emerald-200 text-emerald-950 flex flex-col sm:flex-row items-center gap-4 text-center sm:text-left">
          <div className="p-3 bg-emerald-100 rounded-full text-emerald-600 shrink-0">
            <CheckCircleIcon className="w-8 h-8" />
          </div>
          <div className="space-y-1">
            <h3 className="text-base font-bold">100% Conflict-Free Schedule Verified!</h3>
            <p className="text-xs text-emerald-800 max-w-2xl leading-relaxed">
              No faculty double-bookings, classroom overlaps, student group clashes, capacity shortfalls, or room type mismatches exist in the current timetable.
            </p>
          </div>
          <div className="sm:ml-auto">
            <button onClick={() => setActivePage('timetable')} className="btn-primary text-xs bg-emerald-700 hover:bg-emerald-800">
              View Weekly Grid
            </button>
          </div>
        </div>
      ) : (
        <div className="app-card p-6 bg-rose-50/70 border-rose-200 text-rose-950 space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-rose-100 rounded-full text-rose-600 shrink-0">
              <AlertTriangleIcon className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-sm font-bold">
                {conflicts.length} Constraint Violations Detected ({hardConflicts.length} Hard, {softConflicts.length} Soft)
              </h3>
              <p className="text-xs text-rose-800">
                Hard constraints make the timetable physically unfeasible. Resolve these conflicts by reassigning slots or re-running the Backtracking CSP algorithm.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2 pt-1">
            <button
              onClick={() => setActivePage('generate')}
              className="btn-primary text-xs bg-rose-700 hover:bg-rose-800"
            >
              <PlayIcon className="w-4 h-4" />
              Resolve via Backtracking Generator
            </button>
          </div>
        </div>
      )}

      {/* Conflict List */}
      {conflicts.length > 0 && (
        <div className="space-y-3">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500">Detailed Conflict Reports</h3>
          {conflicts.map((item, idx) => {
            const isHard = item.severity === 'HARD';
            return (
              <div
                key={idx}
                className={`app-card p-4 border-l-4 space-y-2 ${
                  isHard ? 'border-l-rose-500 bg-white' : 'border-l-amber-500 bg-white'
                }`}
              >
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className={`badge ${isHard ? 'badge-red' : 'badge-amber'} text-[10px]`}>
                      {item.severity} CONSTRAINT
                    </span>
                    <span className="font-mono text-xs font-bold text-slate-800">{item.conflictType}</span>
                  </div>
                  {item.timeSlotInfo && (
                    <span className="text-[11px] font-mono text-slate-500 bg-slate-100 px-2 py-0.5 rounded">
                      {item.timeSlotInfo}
                    </span>
                  )}
                </div>

                <div className="text-xs text-slate-700 font-medium">{item.description}</div>

                <div className="text-[11px] text-slate-500">
                  <strong>Affected:</strong> {item.affectedEntity}
                </div>

                {item.suggestedResolution && (
                  <div className="p-2.5 rounded bg-slate-50 border border-slate-200 text-xs text-slate-700 flex items-start gap-2">
                    <span className="font-bold text-blue-700 shrink-0">💡 Suggested Action:</span>
                    <span>{item.suggestedResolution}</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
