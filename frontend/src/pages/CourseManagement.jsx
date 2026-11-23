import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  BookOpenIcon,
  PlusIcon,
  EditIcon,
  TrashIcon,
  SearchIcon,
  RefreshCwIcon,
} from '../components/Icons';

export default function CourseManagement() {
  const [courses, setCourses] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [facultyList, setFacultyList] = useState([]);
  const [studentGroups, setStudentGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedDept, setSelectedDept] = useState('');

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    courseCode: '',
    courseName: '',
    departmentId: '',
    semester: 1,
    credits: 3,
    assignedFacultyId: '',
    requiredRoomType: 'CLASSROOM',
    weeklySessions: 3,
    sessionDurationMinutes: 60,
    studentGroupIds: [],
  });
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      const [crs, depts, facs, grps] = await Promise.all([
        api.getCourses(),
        api.getDepartments(),
        api.getFaculty(),
        api.getStudentGroups(),
      ]);
      setCourses(crs);
      setDepartments(depts);
      setFacultyList(facs);
      setStudentGroups(grps);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openAddModal = () => {
    setEditingId(null);
    setFormData({
      courseCode: '',
      courseName: '',
      departmentId: departments[0]?.id || '',
      semester: 1,
      credits: 3,
      assignedFacultyId: facultyList[0]?.id || '',
      requiredRoomType: 'CLASSROOM',
      weeklySessions: 3,
      sessionDurationMinutes: 55,
      studentGroupIds: studentGroups.length > 0 ? [studentGroups[0].id] : [],
    });
    setFormError('');
    setModalOpen(true);
  };

  const openEditModal = (c) => {
    setEditingId(c.id);
    setFormData({
      courseCode: c.courseCode,
      courseName: c.courseName,
      departmentId: c.departmentId,
      semester: c.semester,
      credits: c.credits,
      assignedFacultyId: c.assignedFacultyId || '',
      requiredRoomType: c.requiredRoomType,
      weeklySessions: c.weeklySessions,
      sessionDurationMinutes: c.sessionDurationMinutes,
      studentGroupIds: c.studentGroupIds || [],
    });
    setFormError('');
    setModalOpen(true);
  };

  const handleGroupCheckbox = (groupId) => {
    setFormData((prev) => {
      const exists = prev.studentGroupIds.includes(groupId);
      return {
        ...prev,
        studentGroupIds: exists
          ? prev.studentGroupIds.filter((id) => id !== groupId)
          : [...prev.studentGroupIds, groupId],
      };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      if (editingId) {
        await api.updateCourse(editingId, formData);
      } else {
        await api.createCourse(formData);
      }
      setModalOpen(false);
      loadData();
    } catch (err) {
      setFormError(err.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Are you sure you want to delete course ${name}?`)) {
      try {
        await api.deleteCourse(id);
        loadData();
      } catch (err) {
        alert(err.message || 'Failed to delete');
      }
    }
  };

  const filtered = courses.filter((c) => {
    const matchesSearch =
      c.courseName.toLowerCase().includes(search.toLowerCase()) ||
      c.courseCode.toLowerCase().includes(search.toLowerCase()) ||
      (c.assignedFacultyName && c.assignedFacultyName.toLowerCase().includes(search.toLowerCase()));
    const matchesDept = selectedDept ? String(c.departmentId) === String(selectedDept) : true;
    return matchesSearch && matchesDept;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <BookOpenIcon className="w-5 h-5 text-blue-600" />
            Courses & Curriculum Management
          </h2>
          <p className="text-xs text-slate-500">Configure courses, theory/laboratory requirements, weekly sessions, credits, and faculty assignments</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={openAddModal} className="btn-primary text-xs">
            <PlusIcon className="w-4 h-4" />
            Add Course
          </button>
          <button onClick={loadData} className="btn-secondary text-xs" title="Refresh">
            <RefreshCwIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="app-card p-4 flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:w-80">
          <SearchIcon className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
          <input
            type="text"
            placeholder="Search by code, title, or instructor..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="form-input pl-9 text-xs"
          />
        </div>
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <label className="text-xs text-slate-500 font-medium">Department:</label>
          <select
            value={selectedDept}
            onChange={(e) => setSelectedDept(e.target.value)}
            className="form-input text-xs w-48"
          >
            <option value="">All Departments</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name} ({d.code})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Courses Table */}
      <div className="app-card overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading courses...</div>
        ) : filtered.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500">No courses found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider">
                  <th className="p-3.5">Code</th>
                  <th className="p-3.5">Course Name</th>
                  <th className="p-3.5">Department</th>
                  <th className="p-3.5 text-center">Sem / Credits</th>
                  <th className="p-3.5">Assigned Faculty</th>
                  <th className="p-3.5">Room Type</th>
                  <th className="p-3.5 text-center">Sessions/Wk</th>
                  <th className="p-3.5">Enrolled Groups</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="p-3.5 font-mono font-bold text-blue-700">{c.courseCode}</td>
                    <td className="p-3.5 font-medium text-slate-900">{c.courseName}</td>
                    <td className="p-3.5">
                      <span className="badge badge-slate text-[10px]">{c.departmentCode || c.departmentName}</span>
                    </td>
                    <td className="p-3.5 text-center">
                      <span className="font-semibold text-slate-700">Sem {c.semester}</span>
                      <span className="text-slate-400 text-[11px] block">{c.credits} Credits</span>
                    </td>
                    <td className="p-3.5 text-slate-800 font-medium">
                      {c.assignedFacultyName || <span className="text-slate-400 italic">Unassigned</span>}
                    </td>
                    <td className="p-3.5">
                      <span className={`badge text-[10px] ${c.requiredRoomType === 'LABORATORY' ? 'badge-amber' : 'badge-blue'}`}>
                        {c.requiredRoomType}
                      </span>
                    </td>
                    <td className="p-3.5 text-center font-semibold text-slate-700">
                      {c.weeklySessions} × {c.sessionDurationMinutes}m
                    </td>
                    <td className="p-3.5">
                      <div className="flex flex-wrap gap-1">
                        {c.studentGroupNames?.map((gn, i) => (
                          <span key={i} className="badge badge-purple text-[10px]">{gn}</span>
                        )) || <span className="text-slate-400 text-[11px]">—</span>}
                      </div>
                    </td>
                    <td className="p-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openEditModal(c)}
                          className="p-1.5 rounded hover:bg-slate-200/80 text-slate-600 hover:text-blue-600"
                          title="Edit"
                        >
                          <EditIcon className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(c.id, c.courseName)}
                          className="p-1.5 rounded hover:bg-rose-100 text-slate-600 hover:text-rose-600"
                          title="Delete"
                        >
                          <TrashIcon className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add/Edit Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="app-card w-full max-w-lg p-6 bg-white shadow-2xl space-y-4 max-h-[90vh] overflow-y-auto">
            <h3 className="text-sm font-bold text-slate-900 border-b pb-2">
              {editingId ? 'Edit Course' : 'Add New Course'}
            </h3>

            {formError && (
              <div className="p-2.5 rounded bg-rose-50 border border-rose-200 text-rose-700 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Course Code *</label>
                  <input
                    type="text"
                    required
                    value={formData.courseCode}
                    onChange={(e) => setFormData({ ...formData, courseCode: e.target.value })}
                    className="form-input text-xs"
                    placeholder="e.g. IT701"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Department *</label>
                  <select
                    required
                    value={formData.departmentId}
                    onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                    className="form-input text-xs"
                  >
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.code})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Course Title *</label>
                <input
                  type="text"
                  required
                  value={formData.courseName}
                  onChange={(e) => setFormData({ ...formData, courseName: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. Advanced Algorithms & Complexity"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Semester *</label>
                  <input
                    type="number"
                    min="1"
                    max="10"
                    required
                    value={formData.semester}
                    onChange={(e) => setFormData({ ...formData, semester: parseInt(e.target.value) || 1 })}
                    className="form-input text-xs"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Credits *</label>
                  <input
                    type="number"
                    min="1"
                    max="10"
                    required
                    value={formData.credits}
                    onChange={(e) => setFormData({ ...formData, credits: parseInt(e.target.value) || 3 })}
                    className="form-input text-xs"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Room Type *</label>
                  <select
                    value={formData.requiredRoomType}
                    onChange={(e) => setFormData({ ...formData, requiredRoomType: e.target.value })}
                    className="form-input text-xs font-semibold"
                  >
                    <option value="CLASSROOM">CLASSROOM (Theory)</option>
                    <option value="LABORATORY">LABORATORY (Practical)</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Assigned Faculty</label>
                  <select
                    value={formData.assignedFacultyId}
                    onChange={(e) => setFormData({ ...formData, assignedFacultyId: e.target.value })}
                    className="form-input text-xs"
                  >
                    <option value="">-- Unassigned --</option>
                    {facultyList.map((f) => (
                      <option key={f.id} value={f.id}>
                        {f.name} ({f.facultyCode})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Weekly Sessions</label>
                  <input
                    type="number"
                    min="1"
                    max="10"
                    value={formData.weeklySessions}
                    onChange={(e) => setFormData({ ...formData, weeklySessions: parseInt(e.target.value) || 3 })}
                    className="form-input text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Enrolled Student Groups</label>
                <div className="border border-slate-200 rounded-lg p-2.5 max-h-32 overflow-y-auto space-y-1.5 bg-slate-50">
                  {studentGroups.map((g) => (
                    <label key={g.id} className="flex items-center gap-2 cursor-pointer hover:bg-white p-1 rounded">
                      <input
                        type="checkbox"
                        checked={formData.studentGroupIds.includes(g.id)}
                        onChange={() => handleGroupCheckbox(g.id)}
                        className="rounded text-blue-600"
                      />
                      <span className="font-medium text-slate-800">{g.groupName}</span>
                      <span className="text-[11px] text-slate-400">({g.studentCount} students)</span>
                    </label>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="btn-secondary text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="btn-primary text-xs"
                >
                  {submitting ? 'Saving...' : editingId ? 'Update Course' : 'Create Course'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
