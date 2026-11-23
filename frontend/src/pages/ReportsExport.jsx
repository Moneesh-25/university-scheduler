import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  DownloadIcon,
  BookOpenIcon,
  UserIcon,
  BuildingIcon,
  RefreshCwIcon,
} from '../components/Icons';

export default function ReportsExport() {
  const [entries, setEntries] = useState([]);
  const [facultyList, setFacultyList] = useState([]);
  const [classrooms, setClassrooms] = useState([]);
  const [studentGroups, setStudentGroups] = useState([]);
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const [allEntries, facs, rooms, groups, crs] = await Promise.all([
        api.getTimetableEntries(),
        api.getFaculty(),
        api.getClassrooms(),
        api.getStudentGroups(),
        api.getCourses(),
      ]);
      setEntries(allEntries);
      setFacultyList(facs);
      setClassrooms(rooms);
      setStudentGroups(groups);
      setCourses(crs);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handlePrint = () => {
    window.print();
  };

  // Compute faculty workload breakdown
  const facultyWorkload = facultyList.map((f) => {
    const teachingSessions = entries.filter((e) => e.facultyId === f.id);
    const assignedCourses = courses.filter((c) => c.assignedFacultyId === f.id);
    return {
      id: f.id,
      name: f.name,
      code: f.facultyCode,
      department: f.departmentCode || f.departmentName,
      maxHours: f.maxWeeklyHours,
      assignedSessions: teachingSessions.length,
      assignedCoursesCount: assignedCourses.length,
      utilizationPercent: Math.min(100, Math.round((teachingSessions.length / (f.maxWeeklyHours || 18)) * 100)),
    };
  });

  // Compute room utilization breakdown
  const roomUtilization = classrooms.map((r) => {
    const roomSessions = entries.filter((e) => e.classroomId === r.id);
    const totalWeeklySlots = 35;
    const utilization = Math.min(100, Math.round((roomSessions.length / totalWeeklySlots) * 100));
    return {
      id: r.id,
      roomNumber: r.roomNumber,
      building: r.building,
      type: r.roomType,
      capacity: r.capacity,
      sessionsCount: roomSessions.length,
      utilization,
    };
  });

  // Compute student group course load
  const groupWorkload = studentGroups.map((g) => {
    const groupSessions = entries.filter((e) => e.studentGroupId === g.id);
    return {
      id: g.id,
      code: g.groupCode,
      name: g.groupName,
      department: g.departmentCode || g.departmentName,
      semester: g.semester,
      studentCount: g.studentCount,
      weeklyClasses: groupSessions.length,
    };
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 no-print">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <DownloadIcon className="w-5 h-5 text-blue-600" />
            Academic Reports & Timetable Data Export
          </h2>
          <p className="text-xs text-slate-500">
            Generate printable audit reports for faculty workload balancing, room occupancy metrics, and CSV timetable downloads
          </p>
        </div>
        <div className="flex items-center gap-2">
          <a
            href={api.getCsvExportUrl()}
            download="university_timetable.csv"
            className="btn-primary text-xs shadow-sm"
          >
            <DownloadIcon className="w-4 h-4" />
            Download Complete CSV
          </a>
          <button onClick={handlePrint} className="btn-secondary text-xs">
            Print Full Report (PDF)
          </button>
          <button onClick={loadData} className="btn-secondary text-xs" title="Refresh">
            <RefreshCwIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {loading ? (
        <div className="app-card p-12 text-center text-xs text-slate-500">Generating analytical reports...</div>
      ) : (
        <div className="space-y-6">
          {/* Section 1: Faculty Workload Audit Report */}
          <div className="app-card overflow-hidden">
            <div className="p-4 bg-slate-50 border-b flex items-center justify-between">
              <div className="flex items-center gap-2">
                <UserIcon className="w-4 h-4 text-indigo-600" />
                <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wider">
                  Faculty Teaching Workload & Allocation Audit
                </h3>
              </div>
              <span className="text-[11px] text-slate-500 font-medium">
                {facultyList.length} Faculty Members Evaluated
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left border-collapse">
                <thead>
                  <tr className="border-b text-slate-600 font-semibold bg-slate-50/50">
                    <th className="p-3">Faculty Code</th>
                    <th className="p-3">Professor Name</th>
                    <th className="p-3">Department</th>
                    <th className="p-3 text-center">Assigned Courses</th>
                    <th className="p-3 text-center">Weekly Classes</th>
                    <th className="p-3 text-center">Max Allowed</th>
                    <th className="p-3 text-left">Workload Capacity Bar</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {facultyWorkload.map((f) => (
                    <tr key={f.id} className="hover:bg-slate-50/60">
                      <td className="p-3 font-mono font-medium text-slate-700">{f.code}</td>
                      <td className="p-3 font-bold text-slate-900">{f.name}</td>
                      <td className="p-3">
                        <span className="badge badge-blue text-[10px]">{f.department}</span>
                      </td>
                      <td className="p-3 text-center font-medium text-slate-700">{f.assignedCoursesCount} courses</td>
                      <td className="p-3 text-center font-bold text-blue-700">{f.assignedSessions} hrs/wk</td>
                      <td className="p-3 text-center text-slate-500">{f.maxHours} hrs</td>
                      <td className="p-3 w-48">
                        <div className="space-y-1">
                          <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                            <div
                              className={`h-full rounded-full ${
                                f.utilizationPercent > 90
                                  ? 'bg-rose-500'
                                  : f.utilizationPercent > 60
                                  ? 'bg-emerald-500'
                                  : 'bg-blue-500'
                              }`}
                              style={{ width: `${f.utilizationPercent}%` }}
                            />
                          </div>
                          <div className="text-[10px] text-slate-400 font-mono text-right">
                            {f.utilizationPercent}% capacity
                          </div>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Section 2: Classroom & Laboratory Utilization */}
          <div className="app-card overflow-hidden">
            <div className="p-4 bg-slate-50 border-b flex items-center justify-between">
              <div className="flex items-center gap-2">
                <BuildingIcon className="w-4 h-4 text-cyan-600" />
                <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wider">
                  Classroom & Laboratory Utilization Metrics
                </h3>
              </div>
              <span className="text-[11px] text-slate-500 font-medium">
                {classrooms.length} Academic Rooms
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left border-collapse">
                <thead>
                  <tr className="border-b text-slate-600 font-semibold bg-slate-50/50">
                    <th className="p-3">Room</th>
                    <th className="p-3">Building</th>
                    <th className="p-3">Type</th>
                    <th className="p-3 text-center">Capacity</th>
                    <th className="p-3 text-center">Occupied Slots / Wk</th>
                    <th className="p-3 text-left">Weekly Occupancy Rate</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {roomUtilization.map((r) => (
                    <tr key={r.id} className="hover:bg-slate-50/60">
                      <td className="p-3 font-mono font-bold text-slate-800">{r.roomNumber}</td>
                      <td className="p-3 text-slate-700">{r.building}</td>
                      <td className="p-3">
                        <span className={`badge text-[10px] ${r.type === 'LABORATORY' ? 'badge-amber' : 'badge-blue'}`}>
                          {r.type}
                        </span>
                      </td>
                      <td className="p-3 text-center font-medium text-slate-700">{r.capacity} seats</td>
                      <td className="p-3 text-center font-bold text-cyan-700">{r.sessionsCount} slots</td>
                      <td className="p-3 w-48">
                        <div className="space-y-1">
                          <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-cyan-600 rounded-full"
                              style={{ width: `${r.utilization}%` }}
                            />
                          </div>
                          <div className="text-[10px] text-slate-400 font-mono text-right">
                            {r.utilization}% booked
                          </div>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Section 3: Student Cohort Academic Load */}
          <div className="app-card overflow-hidden">
            <div className="p-4 bg-slate-50 border-b flex items-center justify-between">
              <div className="flex items-center gap-2">
                <BookOpenIcon className="w-4 h-4 text-purple-600" />
                <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wider">
                  Student Cohort Academic Load Summary
                </h3>
              </div>
              <span className="text-[11px] text-slate-500 font-medium">
                {studentGroups.length} Active Cohorts
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left border-collapse">
                <thead>
                  <tr className="border-b text-slate-600 font-semibold bg-slate-50/50">
                    <th className="p-3">Cohort Code</th>
                    <th className="p-3">Cohort Title</th>
                    <th className="p-3">Department</th>
                    <th className="p-3 text-center">Semester</th>
                    <th className="p-3 text-center">Headcount</th>
                    <th className="p-3 text-center">Total Scheduled Weekly Periods</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {groupWorkload.map((g) => (
                    <tr key={g.id} className="hover:bg-slate-50/60">
                      <td className="p-3 font-mono font-bold text-purple-700">{g.code}</td>
                      <td className="p-3 font-bold text-slate-800">{g.name}</td>
                      <td className="p-3">
                        <span className="badge badge-slate text-[10px]">{g.department}</span>
                      </td>
                      <td className="p-3 text-center font-semibold text-slate-700">Sem {g.semester}</td>
                      <td className="p-3 text-center font-bold text-slate-800">{g.studentCount} students</td>
                      <td className="p-3 text-center font-mono font-bold text-emerald-700">{g.weeklyClasses} sessions/wk</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
