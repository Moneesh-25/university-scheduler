import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  BookOpenIcon,
  UserIcon,
  BuildingIcon,
  UsersIcon,
  CalendarIcon,
  LayerIcon,
  AlertTriangleIcon,
  CheckCircleIcon,
  PlayIcon,
  RefreshCwIcon,
  BarChartIcon,
  CpuIcon,
} from '../components/Icons';

export default function Dashboard({ setActivePage, currentRole = 'ADMIN' }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadStats = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getDashboardStats();
      setStats(data);
    } catch (err) {
      setError(err.message || 'Failed to load dashboard metrics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStats();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="flex items-center gap-3 text-slate-500 font-medium">
          <RefreshCwIcon className="w-6 h-6 animate-spin text-blue-600" />
          <span>Loading academic metrics...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 bg-rose-50 border border-rose-200 rounded-xl text-rose-800 space-y-2">
        <div className="font-semibold flex items-center gap-2">
          <AlertTriangleIcon className="w-5 h-5 text-rose-600" />
          Error Connecting to Backend
        </div>
        <p className="text-sm">{error}</p>
        <button onClick={loadStats} className="btn-secondary text-xs mt-2">
          Retry
        </button>
      </div>
    );
  }

  const statCards = [
    { label: 'Total Courses', value: stats?.totalCourses ?? 0, icon: BookOpenIcon, color: 'text-blue-600', bg: 'bg-blue-50', border: 'border-blue-100', page: 'courses' },
    { label: 'Faculty Members', value: stats?.totalFaculty ?? 0, icon: UserIcon, color: 'text-indigo-600', bg: 'bg-indigo-50', border: 'border-indigo-100', page: 'faculty' },
    { label: 'Classrooms & Labs', value: stats?.totalClassrooms ?? 0, icon: BuildingIcon, color: 'text-cyan-600', bg: 'bg-cyan-50', border: 'border-cyan-100', page: 'classrooms' },
    { label: 'Student Groups', value: stats?.totalStudentGroups ?? 0, icon: UsersIcon, color: 'text-purple-600', bg: 'bg-purple-50', border: 'border-purple-100', page: 'groups' },
    { label: 'Scheduled Classes', value: stats?.totalScheduledClasses ?? 0, icon: CalendarIcon, color: 'text-emerald-600', bg: 'bg-emerald-50', border: 'border-emerald-100', page: 'timetable' },
    { label: 'Scheduled Exams', value: stats?.totalScheduledExaminations ?? 0, icon: LayerIcon, color: 'text-amber-600', bg: 'bg-amber-50', border: 'border-amber-100', page: 'exams' },
  ];

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className={`app-card p-6 text-white flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-slate-800 transition-all ${
        currentRole === 'FACULTY'
          ? 'bg-gradient-to-r from-slate-900 via-amber-950 to-slate-900'
          : currentRole === 'STUDENT'
          ? 'bg-gradient-to-r from-slate-900 via-emerald-950 to-slate-900'
          : 'bg-gradient-to-r from-slate-900 via-blue-950 to-slate-900'
      }`}>
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold tracking-tight">
              {currentRole === 'FACULTY'
                ? 'Faculty Academic Workload & Schedule Portal'
                : currentRole === 'STUDENT'
                ? 'Student Academic Timetable & Exam Portal'
                : 'University Scheduling & Timetabling Dashboard'}
            </h2>
            <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${
              currentRole === 'FACULTY'
                ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                : currentRole === 'STUDENT'
                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                : 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
            }`}>
              {currentRole} Access
            </span>
          </div>
          <p className="text-xs text-slate-300 max-w-2xl">
            {currentRole === 'FACULTY'
              ? 'Review your teaching timetable, weekly lecture hours, room venues, and examination invigilation duties.'
              : currentRole === 'STUDENT'
              ? 'Check your cohort class schedule, laboratory session timings, room locations, and upcoming exam dates.'
              : 'Automated conflict-free course timetabling and examination scheduler powered by Constraint Backtracking (MRV + LCV) and Welsh-Powell Graph Coloring.'}
          </p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          {currentRole === 'ADMIN' ? (
            <button
              onClick={() => setActivePage('generate')}
              className="btn-primary shadow-lg shadow-blue-600/30 font-semibold"
            >
              <PlayIcon className="w-4 h-4" />
              Generate Schedule
            </button>
          ) : (
            <button
              onClick={() => setActivePage('timetable')}
              className={`px-4 py-2 rounded-lg font-semibold text-xs flex items-center gap-2 text-white shadow-lg transition-all ${
                currentRole === 'FACULTY'
                  ? 'bg-amber-600 hover:bg-amber-500 shadow-amber-600/30'
                  : 'bg-emerald-600 hover:bg-emerald-500 shadow-emerald-600/30'
              }`}
            >
              <CalendarIcon className="w-4 h-4" />
              View Timetable
            </button>
          )}
          <button
            onClick={loadStats}
            className="px-3 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
            title="Refresh"
          >
            <RefreshCwIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Metrics Cards Grid */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {statCards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <div
              key={idx}
              onClick={() => setActivePage(card.page)}
              className="app-card p-4 cursor-pointer hover:border-blue-300 transition-all group"
            >
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{card.label}</span>
                <div className={`p-2 rounded-lg ${card.bg} ${card.color} group-hover:scale-110 transition-transform`}>
                  <Icon className="w-4 h-4" />
                </div>
              </div>
              <div className="text-2xl font-bold text-slate-800">{card.value}</div>
            </div>
          );
        })}
      </div>

      {/* Conflict Status Bar */}
      <div className={`app-card p-5 border flex items-center justify-between gap-4 ${
        (stats?.schedulingConflictsCount ?? 0) === 0
          ? 'bg-emerald-50/60 border-emerald-200 text-emerald-900'
          : 'bg-rose-50/60 border-rose-200 text-rose-900'
      }`}>
        <div className="flex items-center gap-3">
          {(stats?.schedulingConflictsCount ?? 0) === 0 ? (
            <div className="p-2.5 rounded-full bg-emerald-100 text-emerald-600">
              <CheckCircleIcon className="w-6 h-6" />
            </div>
          ) : (
            <div className="p-2.5 rounded-full bg-rose-100 text-rose-600">
              <AlertTriangleIcon className="w-6 h-6" />
            </div>
          )}
          <div>
            <h3 className="text-sm font-bold">
              {(stats?.schedulingConflictsCount ?? 0) === 0
                ? 'Timetable is Conflict-Free'
                : `${stats?.schedulingConflictsCount} Scheduling Conflicts Detected`}
            </h3>
            <p className="text-xs text-slate-600">
              {(stats?.schedulingConflictsCount ?? 0) === 0
                ? 'All hard constraints (faculty, room, student group, capacity) and academic rules are satisfied.'
                : 'Review conflicting course sessions, room capacities, or faculty overlaps in the Conflict Scanner.'}
            </p>
          </div>
        </div>
        <button
          onClick={() => setActivePage('conflicts')}
          className="btn-secondary text-xs shrink-0"
        >
          View Conflicts
        </button>
      </div>

      {/* Two Column Section: Recent Scheduling Jobs & Quick Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Jobs */}
        <div className="app-card p-5 space-y-4">
          <div className="flex items-center justify-between border-b pb-3">
            <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2">
              <CalendarIcon className="w-4 h-4 text-blue-600" />
              Recent Scheduling Jobs
            </h3>
            {currentRole === 'ADMIN' && (
              <button onClick={() => setActivePage('generate')} className="text-xs text-blue-600 hover:underline">
                Run New Job
              </button>
            )}
          </div>

          {stats?.recentJobs?.length === 0 ? (
            <div className="text-center py-8 text-xs text-slate-500">
              No recent scheduling jobs recorded yet.
            </div>
          ) : (
            <div className="space-y-2.5">
              {stats?.recentJobs?.slice(0, 5).map((job) => (
                <div key={job.id} className="p-3 rounded-lg bg-slate-50 border border-slate-200/70 flex items-center justify-between text-xs">
                  <div>
                    <div className="font-semibold text-slate-800 flex items-center gap-2">
                      <span>{job.jobId}</span>
                      <span className="badge badge-blue text-[10px]">{job.algorithm}</span>
                    </div>
                    <div className="text-[11px] text-slate-500 mt-0.5">
                      Scheduled: <strong>{job.scheduledActivities}</strong> sessions • Execution: <strong>{job.executionTimeMs} ms</strong>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className="badge badge-green text-[10px]">{job.status}</span>
                    <div className="text-[10px] text-slate-400 mt-1">
                      Score: {job.softConstraintScore?.toFixed(1) ?? 'N/A'}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Academic Quick Actions & Overview */}
        <div className="app-card p-5 space-y-4">
          <div className="border-b pb-3">
            <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2">
              <PlayIcon className="w-4 h-4 text-indigo-600" />
              {currentRole === 'ADMIN' ? 'Core Academic Modules' : 'Available Portal Views'}
            </h3>
          </div>

          <div className="grid grid-cols-2 gap-3 text-xs">
            <button
              onClick={() => setActivePage('timetable')}
              className="p-3 rounded-lg border border-slate-200 hover:border-blue-400 hover:bg-blue-50/40 text-left transition-all"
            >
              <div className="font-semibold text-slate-800 flex items-center gap-1.5 mb-1">
                <CalendarIcon className="w-4 h-4 text-blue-600" />
                Weekly Timetable
              </div>
              <p className="text-[11px] text-slate-500">View matrix of days vs time slots by group or faculty</p>
            </button>

            <button
              onClick={() => setActivePage('exams')}
              className="p-3 rounded-lg border border-slate-200 hover:border-amber-400 hover:bg-amber-50/40 text-left transition-all"
            >
              <div className="font-semibold text-slate-800 flex items-center gap-1.5 mb-1">
                <LayerIcon className="w-4 h-4 text-amber-600" />
                {currentRole === 'FACULTY' ? 'Invigilation Duties' : 'Exam Schedule'}
              </div>
              <p className="text-[11px] text-slate-500">
                {currentRole === 'FACULTY' ? 'Inspect assigned examination supervision duties' : 'Inspect semester examination dates and venues'}
              </p>
            </button>

            {currentRole === 'ADMIN' ? (
              <>
                <button
                  onClick={() => setActivePage('benchmark')}
                  className="p-3 rounded-lg border border-slate-200 hover:border-purple-400 hover:bg-purple-50/40 text-left transition-all"
                >
                  <div className="font-semibold text-slate-800 flex items-center gap-1.5 mb-1">
                    <BarChartIcon className="w-4 h-4 text-purple-600" />
                    Algorithm Analytics
                  </div>
                  <p className="text-[11px] text-slate-500">Benchmark Graph Coloring vs Backtracking CSP</p>
                </button>

                <button
                  onClick={() => setActivePage('concurrency')}
                  className="p-3 rounded-lg border border-slate-200 hover:border-emerald-400 hover:bg-emerald-50/40 text-left transition-all"
                >
                  <div className="font-semibold text-slate-800 flex items-center gap-1.5 mb-1">
                    <CpuIcon className="w-4 h-4 text-emerald-600" />
                    Concurrency Engine
                  </div>
                  <p className="text-[11px] text-slate-500">Monitor high-throughput BoundedTaskQueue & ConcurrentStack</p>
                </button>
              </>
            ) : (
              <button
                onClick={() => setActivePage('reports')}
                className="col-span-2 p-3 rounded-lg border border-slate-200 hover:border-indigo-400 hover:bg-indigo-50/40 text-left transition-all"
              >
                <div className="font-semibold text-slate-800 flex items-center gap-1.5 mb-1">
                  <DownloadIcon className="w-4 h-4 text-indigo-600" />
                  Export & Reports
                </div>
                <p className="text-[11px] text-slate-500">Download formatted academic timetables and utilization summaries</p>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
