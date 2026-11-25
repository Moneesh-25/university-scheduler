import React, { useState, useEffect, useRef } from 'react';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import FacultyManagement from './pages/FacultyManagement';
import CourseManagement from './pages/CourseManagement';
import ClassroomManagement from './pages/ClassroomManagement';
import StudentGroupManagement from './pages/StudentGroupManagement';
import TimeSlotConfig from './pages/TimeSlotConfig';
import GenerateTimetable from './pages/GenerateTimetable';
import TimetableGrid from './pages/TimetableGrid';
import ExaminationScheduler from './pages/ExaminationScheduler';
import ConflictDetection from './pages/ConflictDetection';
import AlgorithmComparison from './pages/AlgorithmComparison';
import ConcurrencyDemo from './pages/ConcurrencyDemo';
import ReportsExport from './pages/ReportsExport';
import Login from './pages/Login';
import { api } from './api/client';

export default function App() {
  const [activePage, setActivePage] = useState('dashboard');
  const [currentRole, setCurrentRole] = useState('ADMIN');
  const [conflictCount, setConflictCount] = useState(0);

  // Allowed pages per role
  const rolePermissions = {
    ADMIN: ['dashboard', 'faculty', 'courses', 'classrooms', 'groups', 'slots', 'generate', 'timetable', 'exams', 'conflicts', 'benchmark', 'concurrency', 'reports', 'login'],
    FACULTY: ['dashboard', 'timetable', 'exams', 'reports'],
    STUDENT: ['dashboard', 'timetable', 'exams', 'reports'],
  };

  const handleRoleChange = (newRole) => {
    setCurrentRole(newRole);
    const allowed = rolePermissions[newRole] || rolePermissions.ADMIN;
    if (!allowed.includes(activePage)) {
      setActivePage('timetable');
    }
  };

  const checkConflicts = async () => {
    try {
      const conflicts = await api.scanConflicts();
      setConflictCount(conflicts.length);
    } catch (e) {
      // ignore
    }
  };

  const mainRef = useRef(null);

  useEffect(() => {
    checkConflicts();
    const interval = setInterval(checkConflicts, 30000);
    return () => clearInterval(interval);
  }, [activePage]);

  // Reset scroll position to top whenever activePage or currentRole changes
  useEffect(() => {
    if (mainRef.current) {
      mainRef.current.scrollTo({ top: 0, behavior: 'instant' });
    }
  }, [activePage, currentRole]);

  return (
    <div className="h-screen flex flex-col overflow-hidden bg-slate-50 font-sans">
      <Navbar
        currentRole={currentRole}
        setCurrentRole={handleRoleChange}
        activePage={activePage}
      />

      <div className="flex flex-1 overflow-hidden">
        <Sidebar
          activePage={activePage}
          setActivePage={setActivePage}
          conflictCount={conflictCount}
          currentRole={currentRole}
        />

        <main ref={mainRef} className="flex-1 overflow-y-auto p-6 space-y-4">
          <div className="max-w-7xl mx-auto space-y-4 pb-12">
            {/* Persona Banner */}
            {currentRole !== 'ADMIN' && (
              <div className={`px-4 py-2.5 rounded-xl border flex items-center justify-between text-xs transition-all ${
                currentRole === 'FACULTY'
                  ? 'bg-amber-50/80 border-amber-200 text-amber-900'
                  : 'bg-emerald-50/80 border-emerald-200 text-emerald-900'
              }`}>
                <div className="flex items-center gap-2">
                  <span className="font-bold">
                    {currentRole === 'FACULTY' ? '👨‍🏫 Faculty View Mode' : '🎓 Student Portal Mode'}
                  </span>
                  <span className="text-slate-600 hidden sm:inline">
                    {currentRole === 'FACULTY'
                      ? '— Showing teaching schedules, classroom locations, and examination invigilation duties.'
                      : '— Showing cohort timetable, lecture hours, and semester examination dates.'}
                  </span>
                </div>
                <button
                  onClick={() => handleRoleChange('ADMIN')}
                  className="underline font-semibold text-slate-700 hover:text-black shrink-0 ml-2"
                >
                  Switch to Admin Mode
                </button>
              </div>
            )}

            {activePage === 'dashboard' && <Dashboard setActivePage={setActivePage} currentRole={currentRole} />}
            {activePage === 'faculty' && <FacultyManagement />}
            {activePage === 'courses' && <CourseManagement />}
            {activePage === 'classrooms' && <ClassroomManagement />}
            {activePage === 'groups' && <StudentGroupManagement />}
            {activePage === 'slots' && <TimeSlotConfig />}
            {activePage === 'generate' && <GenerateTimetable setActivePage={setActivePage} />}
            {activePage === 'timetable' && <TimetableGrid currentRole={currentRole} />}
            {activePage === 'exams' && <ExaminationScheduler currentRole={currentRole} />}
            {activePage === 'conflicts' && <ConflictDetection setActivePage={setActivePage} />}
            {activePage === 'benchmark' && <AlgorithmComparison />}
            {activePage === 'concurrency' && <ConcurrencyDemo />}
            {activePage === 'reports' && <ReportsExport currentRole={currentRole} />}
            {activePage === 'login' && (
              <Login
                currentRole={currentRole}
                setCurrentRole={handleRoleChange}
                onLoginSuccess={() => setActivePage('dashboard')}
              />
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
