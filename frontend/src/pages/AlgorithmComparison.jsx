import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  BarChartIcon,
  PlayIcon,
  RefreshCwIcon,
  CheckCircleIcon,
  CpuIcon,
} from '../components/Icons';

export default function AlgorithmComparison() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasRun, setHasRun] = useState(false);

  const runBenchmark = async () => {
    try {
      setLoading(true);
      const data = await api.runBenchmark();
      setResults(data);
      setHasRun(true);
    } catch (err) {
      alert(err.message || 'Benchmark execution failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    runBenchmark();
  }, []);

  const graphResult = results.find((r) => r.algorithmName.includes('Graph')) || results[0];
  const backtrackResult = results.find((r) => r.algorithmName.includes('Backtracking')) || results[1];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <BarChartIcon className="w-5 h-5 text-purple-600" />
            Algorithm Performance & Benchmark Analytics
          </h2>
          <p className="text-xs text-slate-500">
            Empirical comparative analysis of Welsh-Powell Graph Coloring vs. Backtracking Constraint Satisfaction Engine
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={runBenchmark}
            disabled={loading}
            className="btn-primary text-xs bg-purple-600 hover:bg-purple-700 shadow-sm"
          >
            <RefreshCwIcon className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            {loading ? 'Running Benchmark...' : 'Re-run Benchmark'}
          </button>
        </div>
      </div>

      {/* Comparison Cards */}
      {results.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Greedy Graph Coloring */}
          <div className="app-card p-6 border-t-4 border-t-indigo-500 space-y-4">
            <div className="flex items-center justify-between">
              <span className="badge badge-purple text-[10px] font-bold">APPROXIMATION ALGORITHM</span>
              <span className="font-mono text-xs font-bold text-slate-500">O(|V| log |V|)</span>
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">{graphResult?.algorithmName}</h3>
              <p className="text-xs text-slate-500 mt-0.5">Greedy Welsh-Powell Vertex Degree Ordering</p>
            </div>

            <div className="grid grid-cols-2 gap-3 pt-2">
              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Execution Time</div>
                <div className="text-xl font-mono font-bold text-indigo-700">{graphResult?.executionTimeMs} ms</div>
              </div>

              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Soft Quality Score</div>
                <div className="text-xl font-mono font-bold text-slate-800">{graphResult?.softConstraintScore?.toFixed(1)} / 100</div>
              </div>

              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Sessions Scheduled</div>
                <div className="text-xl font-mono font-bold text-emerald-700">
                  {graphResult?.scheduledActivities} / {graphResult?.totalActivities}
                </div>
              </div>

              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Conflicts / Backtracks</div>
                <div className="text-xl font-mono font-bold text-slate-700">
                  {graphResult?.conflictsCount} / {graphResult?.backtracksCount}
                </div>
              </div>
            </div>

            <div className="text-xs text-slate-600 bg-indigo-50/50 p-3 rounded-lg border border-indigo-100 space-y-1">
              <div className="font-semibold text-indigo-900">Algorithmic Behavior:</div>
              <p className="text-[11px] leading-relaxed">
                Constructs conflict graph where each session is a vertex. Sorts vertices by descending degree (number of conflicting classes) and assigns the first valid color. Very fast, but greedy choices can occasionally lead to suboptimal soft scores.
              </p>
            </div>
          </div>

          {/* Backtracking CSP */}
          <div className="app-card p-6 border-t-4 border-t-blue-600 space-y-4">
            <div className="flex items-center justify-between">
              <span className="badge badge-blue text-[10px] font-bold">SYSTEMATIC EXACT CSP</span>
              <span className="font-mono text-xs font-bold text-slate-500">Heuristic Backtracking</span>
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">{backtrackResult?.algorithmName}</h3>
              <p className="text-xs text-slate-500 mt-0.5">Constraint Satisfaction with MRV, Degree & LCV</p>
            </div>

            <div className="grid grid-cols-2 gap-3 pt-2">
              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Execution Time</div>
                <div className="text-xl font-mono font-bold text-blue-700">{backtrackResult?.executionTimeMs} ms</div>
              </div>

              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Soft Quality Score</div>
                <div className="text-xl font-mono font-bold text-emerald-700 font-bold">{backtrackResult?.softConstraintScore?.toFixed(1)} / 100</div>
              </div>

              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Sessions Scheduled</div>
                <div className="text-xl font-mono font-bold text-emerald-700">
                  {backtrackResult?.scheduledActivities} / {backtrackResult?.totalActivities}
                </div>
              </div>

              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                <div className="text-[10px] uppercase font-bold text-slate-400">Backtracks Performed</div>
                <div className="text-xl font-mono font-bold text-blue-800">
                  {backtrackResult?.backtracksCount}
                </div>
              </div>
            </div>

            <div className="text-xs text-slate-600 bg-blue-50/50 p-3 rounded-lg border border-blue-100 space-y-1">
              <div className="font-semibold text-blue-900">Algorithmic Behavior:</div>
              <p className="text-[11px] leading-relaxed">
                Applies <strong>MRV</strong> to select the session with fewest remaining slot options, breaks ties with the <strong>Degree Heuristic</strong>, and tries candidate slots in <strong>LCV</strong> order. Forward checks prune unfeasible branches early.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Analytical Comparison Table */}
      <div className="app-card overflow-hidden">
        <div className="p-4 bg-slate-50 border-b font-bold text-xs text-slate-800 uppercase tracking-wider">
          Comparative Performance Matrix
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-xs border-collapse">
            <thead>
              <tr className="border-b text-slate-600 font-semibold">
                <th className="p-3 text-left">Evaluation Metric</th>
                <th className="p-3 text-left">Greedy Graph Coloring</th>
                <th className="p-3 text-left">Backtracking CSP (MRV+LCV)</th>
                <th className="p-3 text-left">Academic Assessment</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-800">
              <tr>
                <td className="p-3 font-semibold text-slate-700">Computational Complexity</td>
                <td className="p-3 font-mono">O(|V| log |V| + |V|·|C|)</td>
                <td className="p-3 font-mono">O(d^n) worst case, pruned by MRV</td>
                <td className="p-3 text-slate-600">Graph Coloring has strictly polynomial runtime</td>
              </tr>
              <tr>
                <td className="p-3 font-semibold text-slate-700">Execution Speed</td>
                <td className="p-3 font-mono font-bold text-indigo-700">{graphResult?.executionTimeMs} ms</td>
                <td className="p-3 font-mono font-bold text-blue-700">{backtrackResult?.executionTimeMs} ms</td>
                <td className="p-3 text-slate-600">Graph coloring is faster due to non-backtracking nature</td>
              </tr>
              <tr>
                <td className="p-3 font-semibold text-slate-700">Schedule Quality (Soft Score)</td>
                <td className="p-3 font-mono">{graphResult?.softConstraintScore?.toFixed(1)} / 100</td>
                <td className="p-3 font-mono font-bold text-emerald-700">{backtrackResult?.softConstraintScore?.toFixed(1)} / 100</td>
                <td className="p-3 text-slate-600">Backtracking delivers higher compact score and lower student fatigue</td>
              </tr>
              <tr>
                <td className="p-3 font-semibold text-slate-700">Hard Constraint Enforcement</td>
                <td className="p-3 text-emerald-700 font-semibold">100% Enforced</td>
                <td className="p-3 text-emerald-700 font-semibold">100% Enforced</td>
                <td className="p-3 text-slate-600">Both algorithms prevent all double-bookings</td>
              </tr>
              <tr>
                <td className="p-3 font-semibold text-slate-700">Search Completeness</td>
                <td className="p-3 text-slate-600">Incomplete (Approximation)</td>
                <td className="p-3 text-blue-700 font-semibold">Complete (Systematic Exact)</td>
                <td className="p-3 text-slate-600">Backtracking explores the state tree completely</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
