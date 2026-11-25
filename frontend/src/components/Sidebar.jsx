import React from 'react';
import {
  DashboardIcon,
  UserIcon,
  BookOpenIcon,
  BuildingIcon,
  UsersIcon,
  ClockIcon,
  PlayIcon,
  CalendarIcon,
  LayerIcon,
  AlertTriangleIcon,
  BarChartIcon,
  CpuIcon,
  DownloadIcon,
} from './Icons';

export default function Sidebar({ activePage, setActivePage, conflictCount = 0, currentRole = 'ADMIN' }) {
  let navSections = [];

  if (currentRole === 'FACULTY') {
    navSections = [
      {
        title: 'OVERVIEW',
        items: [
          { id: 'dashboard', label: 'Faculty Dashboard', icon: DashboardIcon },
        ],
      },
      {
        title: 'TEACHING & SCHEDULE',
        items: [
          { id: 'timetable', label: 'My Teaching Schedule', icon: CalendarIcon, highlight: true },
          { id: 'exams', label: 'Invigilation Duties', icon: LayerIcon },
        ],
      },
      {
        title: 'ACADEMIC REPORTS',
        items: [
          { id: 'reports', label: 'Reports & Export', icon: DownloadIcon },
        ],
      },
    ];
  } else if (currentRole === 'STUDENT') {
    navSections = [
      {
        title: 'STUDENT PORTAL',
        items: [
          { id: 'dashboard', label: 'Student Home', icon: DashboardIcon },
        ],
      },
      {
        title: 'MY SCHEDULE',
        items: [
          { id: 'timetable', label: 'Weekly Class Timetable', icon: CalendarIcon, highlight: true },
          { id: 'exams', label: 'Examination Schedule', icon: LayerIcon },
        ],
      },
      {
        title: 'RESOURCES',
        items: [
          { id: 'reports', label: 'Download Schedule / PDF', icon: DownloadIcon },
        ],
      },
    ];
  } else {
    // ADMIN (Full Control)
    navSections = [
      {
        title: 'OVERVIEW',
        items: [
          { id: 'dashboard', label: 'Dashboard', icon: DashboardIcon },
        ],
      },
      {
        title: 'ACADEMIC MANAGEMENT',
        items: [
          { id: 'faculty', label: 'Faculty Members', icon: UserIcon },
          { id: 'courses', label: 'Courses & Subjects', icon: BookOpenIcon },
          { id: 'classrooms', label: 'Classrooms & Labs', icon: BuildingIcon },
          { id: 'groups', label: 'Student Groups', icon: UsersIcon },
          { id: 'slots', label: 'Time Slot Config', icon: ClockIcon },
        ],
      },
      {
        title: 'SCHEDULING ENGINE',
        items: [
          { id: 'generate', label: 'Generate Timetable', icon: PlayIcon, highlight: true },
          { id: 'timetable', label: 'Weekly Timetable Grid', icon: CalendarIcon },
          { id: 'exams', label: 'Exam Scheduler', icon: LayerIcon },
          {
            id: 'conflicts',
            label: 'Conflict Detection',
            icon: AlertTriangleIcon,
            badge: conflictCount > 0 ? conflictCount : null,
            badgeColor: 'bg-rose-500 text-white',
          },
        ],
      },
      {
        title: 'ANALYTICS & ENGINE',
        items: [
          { id: 'benchmark', label: 'Algorithm Analytics', icon: BarChartIcon },
          { id: 'concurrency', label: 'Concurrency Engine', icon: CpuIcon },
          { id: 'reports', label: 'Reports & Export', icon: DownloadIcon },
        ],
      },
    ];
  }

  const roleMeta = {
    ADMIN: { label: 'Administrator', desc: 'Full System Control', badgeBg: 'bg-blue-500/20 text-blue-300 border-blue-500/30', dot: 'bg-blue-400' },
    FACULTY: { label: 'Faculty Member', desc: 'Workload & Schedule View', badgeBg: 'bg-amber-500/20 text-amber-300 border-amber-500/30', dot: 'bg-amber-400' },
    STUDENT: { label: 'Enrolled Student', desc: 'Class & Exam View', badgeBg: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30', dot: 'bg-emerald-400' },
  }[currentRole] || { label: 'User', desc: 'Standard Access', badgeBg: 'bg-slate-700 text-slate-300', dot: 'bg-slate-400' };

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between shrink-0 min-h-[calc(100vh-61px)] select-none">
      <div className="p-4 space-y-6">
        {/* Active Persona Pill in Sidebar */}
        <div className={`p-3 rounded-xl border ${roleMeta.badgeBg} space-y-1`}>
          <div className="text-[10px] uppercase font-bold tracking-wider text-slate-400">Active Persona</div>
          <div className="text-xs font-bold text-white flex items-center gap-2">
            <span className={`w-2 h-2 rounded-full ${roleMeta.dot} animate-pulse`} />
            <span>{roleMeta.label}</span>
          </div>
          <div className="text-[10px] text-slate-300">{roleMeta.desc}</div>
        </div>

        {navSections.map((section, idx) => (
          <div key={idx}>
            <div className="text-[10px] font-bold text-slate-400 tracking-wider mb-2 px-3">
              {section.title}
            </div>
            <nav className="space-y-1">
              {section.items.map((item) => {
                const Icon = item.icon;
                const isActive = activePage === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => setActivePage(item.id)}
                    className={`w-full flex items-center justify-between px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                      isActive
                        ? 'bg-blue-600 text-white shadow-sm shadow-blue-500/20'
                        : item.highlight
                        ? 'text-blue-400 hover:bg-slate-800/80 hover:text-blue-300'
                        : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <Icon className={`w-4 h-4 ${isActive ? 'text-white' : item.highlight ? 'text-blue-400' : 'text-slate-400'}`} />
                      <span>{item.label}</span>
                    </div>
                    {item.badge && (
                      <span className={`px-1.5 py-0.5 rounded-full text-[10px] font-bold ${item.badgeColor || 'bg-blue-500 text-white'}`}>
                        {item.badge}
                      </span>
                    )}
                  </button>
                );
              })}
            </nav>
          </div>
        ))}
      </div>

      <div className="p-4 border-t border-slate-800/80 text-[11px] text-slate-400 space-y-1">
        <div className="font-semibold text-slate-300">Campus Scheduler Enterprise</div>
        <div className="text-slate-400">Automated Timetabling System</div>
        <div className="text-[10px] text-slate-500">v1.0.0 Production Build</div>
      </div>
    </aside>
  );
}
