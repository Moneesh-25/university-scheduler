import React, { useState } from 'react';
import { api } from '../api/client';
import {
  PlayIcon,
  CalendarIcon,
  CheckCircleIcon,
  AlertTriangleIcon,
  RefreshCwIcon,
  CpuIcon,
  BarChartIcon,
} from '../components/Icons';

export default function GenerateTimetable({ setActivePage }) {
  const [algorithm, setAlgorithm] = useState('BACKTRACKING');
  const [clearExisting, setClearExisting] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleGenerate = async () => {
    setGenerating(true);
    setError(null);
    setResult(null);

    try {
      const data = await api.generateTimetable(algorithm, clearExisting);
      setResult({
        count: data.length,
        algorithm: algorithm,
        timestamp: new Date().toLocaleTimeString(),
      });
    } catch (err) {
      setError(err.message || 'Generation failed. Check constraints.');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Title */}
      <div>
        <h2 className="text-xl font-bold text-slate-800 flex items-center gap-2">
          <PlayIcon className="w-6 h-6 text-blue-600" />
          Automated Course Timetable Generator
        </h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Select an academic constraint algorithm to construct an optimal, collision-free weekly university timetable.
        </p>
      </div>

      {/* Algorithm Selector Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Backtracking Option */}
        <div
          onClick={() => setAlgorithm('BACKTRACKING')}
          className={`app-card p-5 cursor-pointer border-2 transition-all ${
            algorithm === 'BACKTRACKING'
              ? 'border-blue-600 bg-blue-50/20 shadow-md'
              : 'border-slate-200 hover:border-slate-300'
          }`}
        >
          <div className="flex items-center justify-between mb-3">
            <span className="badge badge-blue text-[10px] font-bold">RECOMMENDED • EXACT CSP</span>
            <input
              type="radio"
              checked={algorithm === 'BACKTRACKING'}
              onChange={() => setAlgorithm('BACKTRACKING')}
              className="text-blue-600 focus:ring-blue-500"
            />
          </div>
          <h3 className="font-bold text-sm text-slate-900 mb-1">
            Backtracking with MRV & LCV Heuristics
          </h3>
          <p className="text-xs text-slate-600 leading-relaxed mb-3">
            Systematic search with <strong>Minimum Remaining Values (MRV)</strong>, <strong>Degree Heuristic</strong>, and <strong>Least Constraining Value (LCV)</strong> ordering.
          </p>
          <div className="space-y-1 text-[11px] text-slate-500 border-t pt-2.5">
            <div>• <strong>Completeness:</strong> Systematic exact search with forward pruning</div>
            <div>• <strong>Hard Constraints:</strong> 100% collision-free guarantee</div>
            <div>• <strong>Soft Quality Score:</strong> High (optimizes consecutive classes & gaps)</div>
          </div>
        </div>

        {/* Graph Coloring Option */}
        <div
          onClick={() => setAlgorithm('GRAPH_COLORING')}
          className={`app-card p-5 cursor-pointer border-2 transition-all ${
            algorithm === 'GRAPH_COLORING'
              ? 'border-indigo-600 bg-indigo-50/20 shadow-md'
              : 'border-slate-200 hover:border-slate-300'
          }`}
        >
          <div className="flex items-center justify-between mb-3">
            <span className="badge badge-purple text-[10px] font-bold">GREEDY HEURISTIC</span>
            <input
              type="radio"
              checked={algorithm === 'GRAPH_COLORING'}
              onChange={() => setAlgorithm('GRAPH_COLORING')}
              className="text-indigo-600 focus:ring-indigo-500"
            />
          </div>
          <h3 className="font-bold text-sm text-slate-900 mb-1">
            Greedy Welsh-Powell Graph Coloring
          </h3>
          <p className="text-xs text-slate-600 leading-relaxed mb-3">
            Models course sessions as vertices in a conflict graph and available time slots as colors. Orders vertices by descending conflict degree.
          </p>
          <div className="space-y-1 text-[11px] text-slate-500 border-t pt-2.5">
            <div>• <strong>Completeness:</strong> Polynomial-time greedy approximation O(|V| log |V|)</div>
            <div>• <strong>Hard Constraints:</strong> Vertex color incompatibility enforced</div>
            <div>• <strong>Execution Speed:</strong> Ultra fast (single pass coloring)</div>
          </div>
        </div>
      </div>

      {/* Settings & Execution Control */}
      <div className="app-card p-5 space-y-4">
        <div className="flex items-center justify-between border-b pb-3">
          <label className="flex items-center gap-2 text-xs font-medium text-slate-700 cursor-pointer">
            <input
              type="checkbox"
              checked={clearExisting}
              onChange={(e) => setClearExisting(e.target.checked)}
              className="rounded text-blue-600 focus:ring-blue-500"
            />
            <span>Clear previous timetable entries before generating new schedule</span>
          </label>
        </div>

        <div className="flex items-center justify-between gap-4 pt-2">
          <div className="text-xs text-slate-500">
            Enforces faculty overlaps, room capacities, lab requirements, student group schedules, and recess breaks.
          </div>
          <button
            onClick={handleGenerate}
            disabled={generating}
            className="btn-primary px-6 py-2.5 text-sm font-semibold shadow-lg shadow-blue-500/25 shrink-0"
          >
            {generating ? (
              <>
                <RefreshCwIcon className="w-4 h-4 animate-spin" />
                Executing Scheduling Algorithm...
              </>
            ) : (
              <>
                <PlayIcon className="w-4 h-4" />
                Run {algorithm === 'BACKTRACKING' ? 'Backtracking CSP' : 'Graph Coloring'}
              </>
            )}
          </button>
        </div>
      </div>

      {/* Success Banner */}
      {result && (
        <div className="app-card p-6 bg-emerald-50 border-emerald-200 text-emerald-900 space-y-3">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-emerald-100 rounded-full text-emerald-700">
              <CheckCircleIcon className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-bold text-sm">Timetable Generated Successfully!</h3>
              <p className="text-xs text-emerald-800">
                Scheduled <strong>{result.count}</strong> weekly lectures and laboratory sessions using{' '}
                <strong>{result.algorithm}</strong> at {result.timestamp}.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3 pt-2">
            <button
              onClick={() => setActivePage('timetable')}
              className="btn-primary text-xs bg-emerald-700 hover:bg-emerald-800 shadow-sm"
            >
              <CalendarIcon className="w-4 h-4" />
              Open Weekly Timetable Grid
            </button>
            <button
              onClick={() => setActivePage('conflicts')}
              className="btn-secondary text-xs"
            >
              Verify Conflict Scanner
            </button>
          </div>
        </div>
      )}

      {/* Error Banner */}
      {error && (
        <div className="app-card p-5 bg-rose-50 border-rose-200 text-rose-900 space-y-2">
          <div className="font-bold text-xs flex items-center gap-2">
            <AlertTriangleIcon className="w-4 h-4 text-rose-600" />
            Scheduling Conflict or Constraint Failure
          </div>
          <p className="text-xs text-rose-800">{error}</p>
        </div>
      )}
    </div>
  );
}
