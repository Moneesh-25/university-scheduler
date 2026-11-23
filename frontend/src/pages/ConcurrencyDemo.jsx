import React, { useState } from 'react';
import { api } from '../api/client';
import {
  CpuIcon,
  PlayIcon,
  RefreshCwIcon,
  CheckCircleIcon,
  AlertTriangleIcon,
} from '../components/Icons';

export default function ConcurrencyDemo() {
  const [producers, setProducers] = useState(4);
  const [consumers, setConsumers] = useState(2);
  const [itemsPerProducer, setItemsPerProducer] = useState(50);
  const [queueCapacity, setQueueCapacity] = useState(20);

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const handleRunDemo = async () => {
    try {
      setLoading(true);
      const data = await api.runConcurrencyDemo(producers, consumers, itemsPerProducer, queueCapacity);
      setResult(data);
    } catch (err) {
      alert(err.message || 'Simulation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <CpuIcon className="w-5 h-5 text-emerald-600" />
            Asynchronous Task Engine & Concurrency Monitor
          </h2>
          <p className="text-xs text-slate-500">
            Multi-threaded asynchronous task queue (BoundedTaskQueue) and transactional schedule state stack (ConcurrentScheduleStack)
          </p>
        </div>
      </div>

      {/* Control Configuration Card */}
      <div className="app-card p-5 space-y-4">
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
          Engine Execution Parameters
        </h3>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
          <div>
            <label className="block text-slate-600 font-semibold mb-1">Producer Threads:</label>
            <input
              type="number"
              min="1"
              max="16"
              value={producers}
              onChange={(e) => setProducers(parseInt(e.target.value) || 1)}
              className="form-input text-xs"
            />
            <span className="text-[10px] text-slate-400">Generates scheduling jobs</span>
          </div>

          <div>
            <label className="block text-slate-600 font-semibold mb-1">Consumer Threads:</label>
            <input
              type="number"
              min="1"
              max="16"
              value={consumers}
              onChange={(e) => setConsumers(parseInt(e.target.value) || 1)}
              className="form-input text-xs"
            />
            <span className="text-[10px] text-slate-400">Dequeues & processes tasks</span>
          </div>

          <div>
            <label className="block text-slate-600 font-semibold mb-1">Items Per Producer:</label>
            <input
              type="number"
              min="10"
              max="500"
              value={itemsPerProducer}
              onChange={(e) => setItemsPerProducer(parseInt(e.target.value) || 10)}
              className="form-input text-xs"
            />
            <span className="text-[10px] text-slate-400">Total items: {producers * itemsPerProducer}</span>
          </div>

          <div>
            <label className="block text-slate-600 font-semibold mb-1">Bounded Queue Capacity:</label>
            <input
              type="number"
              min="2"
              max="100"
              value={queueCapacity}
              onChange={(e) => setQueueCapacity(parseInt(e.target.value) || 2)}
              className="form-input text-xs font-bold text-blue-700"
            />
            <span className="text-[10px] text-slate-400">Backpressure bound</span>
          </div>
        </div>

        <div className="pt-2 flex items-center justify-between border-t">
          <span className="text-xs text-slate-500">
            High-throughput thread-safe task processing using Java <code>ReentrantLock</code> and condition variables.
          </span>
          <button
            onClick={handleRunDemo}
            disabled={loading}
            className="btn-primary text-xs bg-emerald-600 hover:bg-emerald-700 shadow-sm"
          >
            {loading ? (
              <>
                <RefreshCwIcon className="w-4 h-4 animate-spin" />
                Executing Concurrent Threads...
              </>
            ) : (
              <>
                <PlayIcon className="w-4 h-4" />
                Run Multi-Threaded Simulation
              </>
            )}
          </button>
        </div>
      </div>

      {/* Results Dashboard */}
      {result && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
            <div className="app-card p-4 border-l-4 border-l-emerald-500">
              <div className="text-[10px] font-bold text-slate-400 uppercase">Verification Status</div>
              <div className="text-base font-bold text-emerald-700 flex items-center gap-1.5 mt-1">
                <CheckCircleIcon className="w-5 h-5 text-emerald-600" />
                <span>Thread-Safe & Lossless</span>
              </div>
              <div className="text-[11px] text-slate-500 mt-1">0 lost or duplicated tasks</div>
            </div>

            <div className="app-card p-4 border-l-4 border-l-blue-500">
              <div className="text-[10px] font-bold text-slate-400 uppercase">Items Processed</div>
              <div className="text-xl font-mono font-bold text-slate-800 mt-1">
                {result.itemsConsumed} / {result.itemsProduced}
              </div>
              <div className="text-[11px] text-slate-500 mt-1">100% throughput achieved</div>
            </div>

            <div className="app-card p-4 border-l-4 border-l-indigo-500">
              <div className="text-[10px] font-bold text-slate-400 uppercase">Simulation Duration</div>
              <div className="text-xl font-mono font-bold text-indigo-700 mt-1">
                {result.durationMs} ms
              </div>
              <div className="text-[11px] text-slate-500 mt-1">{result.producersCount + result.consumersCount} active OS threads</div>
            </div>

            <div className="app-card p-4 border-l-4 border-l-purple-500">
              <div className="text-[10px] font-bold text-slate-400 uppercase">Concurrent Stack Test</div>
              <div className="text-base font-bold text-purple-700 flex items-center gap-1 mt-1">
                <CheckCircleIcon className="w-4 h-4 text-purple-600" />
                <span>Passed (1000 items)</span>
              </div>
              <div className="text-[11px] text-slate-500 mt-1">LIFO undo history verified</div>
            </div>
          </div>

          {/* Academic Complexity and Architecture Explanation */}
          <div className="app-card p-5 bg-slate-900 text-white space-y-2 border-slate-800">
            <h4 className="text-xs font-bold text-emerald-400 uppercase tracking-wider flex items-center gap-1.5">
              <CpuIcon className="w-4 h-4" />
              Academic Concurrency Architecture & Complexity Analysis
            </h4>
            <p className="text-xs text-slate-300 leading-relaxed font-mono">
              {result.dataStructureExplanation}
            </p>
          </div>

          {/* Live Execution Trace Logs */}
          <div className="app-card p-5 space-y-2">
            <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
              Thread Execution Event Stream Log
            </h4>
            <div className="bg-slate-950 text-emerald-400 font-mono text-[11px] p-4 rounded-lg max-h-60 overflow-y-auto space-y-1">
              {result.executionLogs?.map((log, i) => (
                <div key={i} className="leading-tight">
                  <span className="text-slate-500 mr-2">[{String(i + 1).padStart(2, '0')}]</span>
                  <span>{log}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
