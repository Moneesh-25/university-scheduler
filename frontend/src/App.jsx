import React, { useState, useEffect } from 'react';
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

  const checkConflicts = async () => {
    try {
      const conflicts = await api.scanConflicts();
      setConflictCount(conflicts.length);
    } catch (e) {
      // ignore
    }
  };

  useEffect(() => {
    checkConflicts();
    const interval = setInterval(checkConflicts, 30000);
    return () => clearInterval(interval);
  }, [activePage]);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Navbar
        currentRole={currentRole}
        setCurrentRole={setCurrentRole}
        activePage={activePage}
      />

      <div className="flex flex-1">
        <Sidebar
          activePage={activePage}
          setActivePage={setActivePage}
          conflictCount={conflictCount}
        />

        <main className="flex-1 p-6 max-w-7xl mx-auto w-full">
          {activePage === 'dashboard' && <Dashboard setActivePage={setActivePage} />}
          {activePage === 'faculty' && <FacultyManagement />}
          {activePage === 'courses' && <CourseManagement />}
          {activePage === 'classrooms' && <ClassroomManagement />}
          {activePage === 'groups' && <StudentGroupManagement />}
          {activePage === 'slots' && <TimeSlotConfig />}
          {activePage === 'generate' && <GenerateTimetable setActivePage={setActivePage} />}
          {activePage === 'timetable' && <TimetableGrid />}
          {activePage === 'exams' && <ExaminationScheduler />}
          {activePage === 'conflicts' && <ConflictDetection setActivePage={setActivePage} />}
          {activePage === 'benchmark' && <AlgorithmComparison />}
          {activePage === 'concurrency' && <ConcurrencyDemo />}
          {activePage === 'reports' && <ReportsExport />}
          {activePage === 'login' && (
            <Login
              currentRole={currentRole}
              setCurrentRole={setCurrentRole}
              onLoginSuccess={() => setActivePage('dashboard')}
            />
          )}
        </main>
      </div>
    </div>
  );
}
