import React, { useState } from 'react';
import {
  CalendarIcon,
  UserIcon,
  CheckCircleIcon,
  ShieldIcon,
} from '../components/Icons';

export default function Login({ currentRole, setCurrentRole, onLoginSuccess }) {
  const [selectedRole, setSelectedRole] = useState(currentRole || 'ADMIN');
  const [username, setUsername] = useState('admin@university.edu');
  const [password, setPassword] = useState('••••••••');

  const handleRoleSelect = (role) => {
    setSelectedRole(role);
    if (role === 'ADMIN') {
      setUsername('admin@university.edu');
    } else if (role === 'FACULTY') {
      setUsername('ramanathan@university.edu');
    } else {
      setUsername('student.it@university.edu');
    }
  };

  const handleLogin = (e) => {
    e.preventDefault();
    setCurrentRole(selectedRole);
    if (onLoginSuccess) {
      onLoginSuccess();
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center p-4">
      <div className="app-card w-full max-w-md p-8 bg-white shadow-2xl space-y-6 border-slate-200">
        {/* Branding */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-blue-600 text-white flex items-center justify-center mx-auto shadow-lg shadow-blue-500/30">
            <CalendarIcon className="w-7 h-7" />
          </div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">University Scheduler Portal</h2>
          <p className="text-xs text-slate-500">Sign in to access timetables, examination duties, and scheduling engines</p>
        </div>

        {/* Role Selection Tabs */}
        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-slate-600">Select Access Persona:</label>
          <div className="grid grid-cols-3 gap-2">
            {[
              { id: 'ADMIN', label: 'Admin', desc: 'Full Control' },
              { id: 'FACULTY', label: 'Faculty', desc: 'Workload & View' },
              { id: 'STUDENT', label: 'Student', desc: 'Timetable View' },
            ].map((r) => (
              <button
                key={r.id}
                type="button"
                onClick={() => handleRoleSelect(r.id)}
                className={`p-2.5 rounded-lg border text-left transition-all ${
                  selectedRole === r.id
                    ? 'border-blue-600 bg-blue-50/50 shadow-sm'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <div className="font-bold text-xs text-slate-800">{r.label}</div>
                <div className="text-[10px] text-slate-400">{r.desc}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Credentials Form */}
        <form onSubmit={handleLogin} className="space-y-4 text-xs">
          <div>
            <label className="block text-slate-700 font-medium mb-1">User Identifier / Email</label>
            <input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="form-input text-xs"
            />
          </div>

          <div>
            <label className="block text-slate-700 font-medium mb-1">Security Credential / Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="form-input text-xs"
            />
          </div>

          <div className="p-3 rounded-lg bg-slate-50 border border-slate-200 text-[11px] text-slate-600 space-y-1">
            <div className="font-semibold text-slate-800 flex items-center gap-1.5">
              <CheckCircleIcon className="w-3.5 h-3.5 text-emerald-600" />
              Role Permissions:
            </div>
            {selectedRole === 'ADMIN' && (
              <p>Full administrative privileges: Generate timetables, modify faculty, manage courses & classrooms, scan conflicts.</p>
            )}
            {selectedRole === 'FACULTY' && (
              <p>Faculty member access: View personal lecture schedule, update availability, inspect invigilation assignments.</p>
            )}
            {selectedRole === 'STUDENT' && (
              <p>Student cohort access: Filter weekly timetable, inspect examination dates, export schedule to CSV/PDF.</p>
            )}
          </div>

          <button
            type="submit"
            className="btn-primary w-full justify-center py-2.5 text-xs font-semibold shadow-md shadow-blue-500/20"
          >
            Access System as {selectedRole === 'ADMIN' ? 'Administrator' : selectedRole === 'FACULTY' ? 'Faculty Member' : 'Student'}
          </button>
        </form>
      </div>
    </div>
  );
}
