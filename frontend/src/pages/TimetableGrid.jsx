import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  CalendarIcon,
  DownloadIcon,
  UndoIcon,
  TrashIcon,
  RefreshCwIcon,
  SearchIcon,
} from '../components/Icons';

export default function TimetableGrid({ currentRole = 'ADMIN' }) {
  const [entries, setEntries] = useState([]);
  const [slots, setSlots] = useState([]);
  const [studentGroups, setStudentGroups] = useState([]);
  const [facultyList, setFacultyList] = useState([]);
  const [classrooms, setClassrooms] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [selectedGroup, setSelectedGroup] = useState('');
  const [selectedFaculty, setSelectedFaculty] = useState('');
  const [selectedRoom, setSelectedRoom] = useState('');
  const [selectedDept, setSelectedDept] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      const [allEntries, allSlots, grps, facs, rms, depts] = await Promise.all([
        api.getTimetableEntries(),
        api.getTimeSlots(),
        api.getStudentGroups(),
        api.getFaculty(),
        api.getClassrooms(),
        api.getDepartments(),
      ]);
      setEntries(allEntries);
      setSlots(allSlots);
      setStudentGroups(grps);
      setFacultyList(facs);
      setClassrooms(rms);
      setDepartments(depts);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUndo = async () => {
    if (window.confirm('Roll back the most recently generated schedule batch using the Concurrent Undo Stack?')) {
      try {
        const res = await api.undoTimetable();
        alert(res ? 'Successfully rolled back last schedule batch.' : 'No previous batch to undo.');
        loadData();
      } catch (err) {
        alert(err.message || 'Undo failed');
      }
    }
  };

  const handleClear = async () => {
    if (window.confirm('Are you sure you want to clear all timetable entries?')) {
      try {
        await api.clearTimetableEntries();
        loadData();
      } catch (err) {
        alert(err.message || 'Failed to clear');
      }
    }
  };

  const handlePrint = () => {
    window.print();
  };

  // Filter entries
  const filteredEntries = entries.filter((e) => {
    if (selectedGroup && String(e.studentGroupId) !== String(selectedGroup)) return false;
    if (selectedFaculty && String(e.facultyId) !== String(selectedFaculty)) return false;
    if (selectedRoom && String(e.classroomId) !== String(selectedRoom)) return false;
    return true;
  });

  const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];

  // Distinct time bands (e.g. 9:00 - 9:55, 9:55 - 10:50, etc.)
  const distinctTimeBands = [];
  const seenTimes = new Set();
  slots
    .filter((s) => s.dayOfWeek === 'MONDAY')
    .sort((a, b) => (a.slotOrder || 0) - (b.slotOrder || 0))
    .forEach((s) => {
      const timeStr = `${s.startTime?.slice(0, 5)} - ${s.endTime?.slice(0, 5)}`;
      if (!seenTimes.has(timeStr)) {
        seenTimes.add(timeStr);
        distinctTimeBands.push({
          timeStr,
          slotOrder: s.slotOrder,
          isBreak: s.break,
          isLunch: s.lunch,
        });
      }
    });

  // Map entries by Day + TimeStr
  const entryMap = {};
  filteredEntries.forEach((e) => {
    const timeStr = `${e.startTime?.slice(0, 5)} - ${e.endTime?.slice(0, 5)}`;
    const key = `${e.dayOfWeek}:${timeStr}`;
    if (!entryMap[key]) {
      entryMap[key] = [];
    }
    entryMap[key].push(e);
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 no-print">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <CalendarIcon className="w-5 h-5 text-blue-600" />
            Weekly Academic Timetable Matrix
          </h2>
          <p className="text-xs text-slate-500">
            {filteredEntries.length} sessions displayed • Synchronized with MySQL & Concurrent Stack
          </p>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          <a
            href={api.getCsvExportUrl()}
            download="university_timetable.csv"
            className="btn-secondary text-xs"
          >
            <DownloadIcon className="w-4 h-4" />
            Export CSV
          </a>
          <button onClick={handlePrint} className="btn-secondary text-xs">
            Print / PDF
          </button>
          {currentRole === 'ADMIN' && (
            <>
              <button onClick={handleUndo} className="btn-secondary text-xs text-amber-700 hover:bg-amber-50">
                <UndoIcon className="w-4 h-4" />
                Undo Batch
              </button>
              <button onClick={handleClear} className="btn-danger text-xs">
                <TrashIcon className="w-4 h-4" />
                Clear
              </button>
            </>
          )}
          <button onClick={loadData} className="btn-secondary text-xs" title="Refresh">
            <RefreshCwIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Role Tip Banner */}
      {currentRole === 'FACULTY' && (
        <div className="p-3 bg-amber-50/80 border border-amber-200 rounded-lg text-xs text-amber-900 flex items-center gap-2 shadow-sm">
          <span className="text-base">👨‍🏫</span>
          <span>Faculty Mode: Filter by your name in the <strong>Filter by Faculty</strong> dropdown below to view your personalized weekly lecture schedule.</span>
        </div>
      )}
      {currentRole === 'STUDENT' && (
        <div className="p-3 bg-emerald-50/80 border border-emerald-200 rounded-lg text-xs text-emerald-900 flex items-center gap-2 shadow-sm">
          <span className="text-base">🎓</span>
          <span>Student Mode: Filter by your cohort (e.g. <strong>B.Tech IT - Sem 5</strong>) in the <strong>Filter by Student Group</strong> dropdown to view your class schedule.</span>
        </div>
      )}

      {/* Filter Bar */}
      <div className="app-card p-4 grid grid-cols-1 sm:grid-cols-3 gap-3 no-print text-xs">
        <div>
          <label className="block text-slate-500 font-medium mb-1">Filter by Student Group:</label>
          <select
            value={selectedGroup}
            onChange={(e) => setSelectedGroup(e.target.value)}
            className="form-input text-xs"
          >
            <option value="">All Student Groups ({studentGroups.length})</option>
            {studentGroups.map((g) => (
              <option key={g.id} value={g.id}>
                {g.groupName} ({g.groupCode})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-slate-500 font-medium mb-1">Filter by Faculty:</label>
          <select
            value={selectedFaculty}
            onChange={(e) => setSelectedFaculty(e.target.value)}
            className="form-input text-xs"
          >
            <option value="">All Faculty Members ({facultyList.length})</option>
            {facultyList.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name} ({f.departmentCode})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-slate-500 font-medium mb-1">Filter by Classroom / Lab:</label>
          <select
            value={selectedRoom}
            onChange={(e) => setSelectedRoom(e.target.value)}
            className="form-input text-xs"
          >
            <option value="">All Rooms ({classrooms.length})</option>
            {classrooms.map((r) => (
              <option key={r.id} value={r.id}>
                {r.roomNumber} ({r.roomType} - {r.capacity} seats)
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Weekly Matrix Grid */}
      <div className="app-card overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-xs text-slate-500">Loading timetable grid...</div>
        ) : entries.length === 0 ? (
          <div className="p-12 text-center space-y-3">
            <CalendarIcon className="w-8 h-8 text-slate-300 mx-auto" />
            <div className="text-sm font-semibold text-slate-700">No Timetable Generated Yet</div>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Use the Timetable Generator to run Graph Coloring or Backtracking CSP to populate the weekly schedule.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs border-collapse">
              <thead>
                <tr className="bg-slate-900 text-white border-b border-slate-800">
                  <th className="p-3 w-28 text-left font-bold uppercase tracking-wider text-[11px]">Time Slot</th>
                  {days.map((day) => (
                    <th key={day} className="p-3 text-center font-bold uppercase tracking-wider text-[11px] border-l border-slate-800">
                      {day}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {distinctTimeBands.map((band, idx) => {
                  const isBreakOrLunch = band.isBreak || band.isLunch;
                  return (
                    <tr
                      key={idx}
                      className={
                        band.isLunch
                          ? 'bg-amber-50/70 border-y border-amber-200'
                          : band.isBreak
                          ? 'bg-indigo-50/70 border-y border-indigo-200'
                          : 'hover:bg-slate-50/60'
                      }
                    >
                      {/* Time Column */}
                      <td className="p-2.5 font-mono text-[11px] font-bold text-slate-700 whitespace-nowrap bg-slate-50 border-r border-slate-200">
                        <div>{band.timeStr}</div>
                        <div className="text-[10px] text-slate-400 font-normal">
                          {band.isLunch ? 'Lunch Break' : band.isBreak ? 'Tea Break' : `Period ${band.slotOrder}`}
                        </div>
                      </td>

                      {/* Days Columns */}
                      {days.map((day) => {
                        if (isBreakOrLunch) {
                          return (
                            <td key={day} className="p-2 text-center border-l border-slate-200">
                              <span className="text-[11px] font-medium text-slate-500 italic">
                                {band.isLunch ? '🍱 Recess / Lunch Period' : '☕ Morning Tea Break'}
                              </span>
                            </td>
                          );
                        }

                        const cellKey = `${day}:${band.timeStr}`;
                        const cellEntries = entryMap[cellKey] || [];

                        return (
                          <td key={day} className="p-2 align-top border-l border-slate-200 min-w-[170px]">
                            {cellEntries.length === 0 ? (
                              <div className="h-full min-h-[55px] rounded border border-dashed border-slate-200/80 bg-slate-50/30 flex items-center justify-center text-[10px] text-slate-300">
                                Free
                              </div>
                            ) : (
                              <div className="space-y-1.5">
                                {cellEntries.map((e) => {
                                  const isLab = e.roomType === 'LABORATORY';
                                  return (
                                    <div
                                      key={e.id}
                                      className={`p-2 rounded-lg border text-left shadow-sm transition-transform hover:scale-[1.02] ${
                                        isLab
                                          ? 'bg-amber-50 border-amber-300 text-amber-950'
                                          : 'bg-blue-50/80 border-blue-200 text-blue-950'
                                      }`}
                                    >
                                      <div className="flex items-center justify-between gap-1">
                                        <span className="font-bold text-[11px] tracking-tight">{e.courseCode}</span>
                                        <span className={`text-[9px] px-1 py-0.2 rounded font-semibold ${isLab ? 'bg-amber-200 text-amber-900' : 'bg-blue-200 text-blue-900'}`}>
                                          {isLab ? 'LAB' : 'ROOM'} {e.roomNumber}
                                        </span>
                                      </div>
                                      <div className="text-[10px] font-medium truncate mt-0.5" title={e.courseName}>
                                        {e.courseName}
                                      </div>
                                      <div className="text-[10px] text-slate-600 mt-1 flex items-center justify-between">
                                        <span className="truncate">{e.facultyName?.split(' ').slice(-1)[0]}</span>
                                        <span className="font-mono text-[9px] text-purple-700 bg-purple-100/70 px-1 rounded">
                                          {e.groupCode}
                                        </span>
                                      </div>
                                    </div>
                                  );
                                })}
                              </div>
                            )}
                          </td>
                        );
                      })}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
