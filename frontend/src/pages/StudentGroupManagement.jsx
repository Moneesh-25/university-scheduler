import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  UsersIcon,
  PlusIcon,
  EditIcon,
  TrashIcon,
  SearchIcon,
  RefreshCwIcon,
} from '../components/Icons';

export default function StudentGroupManagement() {
  const [groups, setGroups] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    groupCode: '',
    groupName: '',
    departmentId: '',
    semester: 1,
    studentCount: 30,
  });
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      const [grps, depts] = await Promise.all([api.getStudentGroups(), api.getDepartments()]);
      setGroups(grps);
      setDepartments(depts);
      if (depts.length > 0 && !formData.departmentId) {
        setFormData((prev) => ({ ...prev, departmentId: depts[0].id }));
      }
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
      groupCode: '',
      groupName: '',
      departmentId: departments[0]?.id || '',
      semester: 1,
      studentCount: 30,
    });
    setFormError('');
    setModalOpen(true);
  };

  const openEditModal = (g) => {
    setEditingId(g.id);
    setFormData({
      groupCode: g.groupCode,
      groupName: g.groupName,
      departmentId: g.departmentId,
      semester: g.semester,
      studentCount: g.studentCount,
    });
    setFormError('');
    setModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      if (editingId) {
        await api.updateStudentGroup(editingId, formData);
      } else {
        await api.createStudentGroup(formData);
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
    if (window.confirm(`Are you sure you want to delete student group ${name}?`)) {
      try {
        await api.deleteStudentGroup(id);
        loadData();
      } catch (err) {
        alert(err.message || 'Failed to delete');
      }
    }
  };

  const filtered = groups.filter(
    (g) =>
      g.groupName.toLowerCase().includes(search.toLowerCase()) ||
      g.groupCode.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <UsersIcon className="w-5 h-5 text-blue-600" />
            Student Groups & Cohorts Management
          </h2>
          <p className="text-xs text-slate-500">Manage academic cohorts, batch headcounts, departments, and semester levels</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={openAddModal} className="btn-primary text-xs">
            <PlusIcon className="w-4 h-4" />
            Add Student Group
          </button>
          <button onClick={loadData} className="btn-secondary text-xs" title="Refresh">
            <RefreshCwIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div className="app-card p-4 flex gap-3 items-center justify-between">
        <div className="relative w-full sm:w-80">
          <SearchIcon className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
          <input
            type="text"
            placeholder="Search by cohort name or code..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="form-input pl-9 text-xs"
          />
        </div>
      </div>

      {/* Groups Table */}
      <div className="app-card overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading student cohorts...</div>
        ) : filtered.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500">No student groups found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider">
                  <th className="p-3.5">Cohort Code</th>
                  <th className="p-3.5">Cohort / Batch Name</th>
                  <th className="p-3.5">Department</th>
                  <th className="p-3.5 text-center">Semester</th>
                  <th className="p-3.5 text-center">Student Count</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((g) => (
                  <tr key={g.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="p-3.5 font-mono font-bold text-purple-700">{g.groupCode}</td>
                    <td className="p-3.5 font-medium text-slate-900">{g.groupName}</td>
                    <td className="p-3.5">
                      <span className="badge badge-blue text-[10px]">{g.departmentCode || g.departmentName}</span>
                    </td>
                    <td className="p-3.5 text-center font-semibold text-slate-700">Sem {g.semester}</td>
                    <td className="p-3.5 text-center font-bold text-slate-800">{g.studentCount} students</td>
                    <td className="p-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openEditModal(g)}
                          className="p-1.5 rounded hover:bg-slate-200/80 text-slate-600 hover:text-blue-600"
                          title="Edit"
                        >
                          <EditIcon className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(g.id, g.groupName)}
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
          <div className="app-card w-full max-w-md p-6 bg-white shadow-2xl space-y-4">
            <h3 className="text-sm font-bold text-slate-900 border-b pb-2">
              {editingId ? 'Edit Student Group' : 'Add New Student Group'}
            </h3>

            {formError && (
              <div className="p-2.5 rounded bg-rose-50 border border-rose-200 text-rose-700 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-700 font-medium mb-1">Group Code *</label>
                <input
                  type="text"
                  required
                  value={formData.groupCode}
                  onChange={(e) => setFormData({ ...formData, groupCode: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. MTECH-IT-Y1"
                />
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Group Title *</label>
                <input
                  type="text"
                  required
                  value={formData.groupName}
                  onChange={(e) => setFormData({ ...formData, groupName: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. M.Tech Information Technology - Year 1"
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

              <div className="grid grid-cols-2 gap-3">
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
                  <label className="block text-slate-700 font-medium mb-1">Student Count *</label>
                  <input
                    type="number"
                    min="1"
                    max="300"
                    required
                    value={formData.studentCount}
                    onChange={(e) => setFormData({ ...formData, studentCount: parseInt(e.target.value) || 30 })}
                    className="form-input text-xs"
                  />
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
                  {submitting ? 'Saving...' : editingId ? 'Update Group' : 'Create Group'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
