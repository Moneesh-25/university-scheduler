import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  UserIcon,
  PlusIcon,
  EditIcon,
  TrashIcon,
  SearchIcon,
  RefreshCwIcon,
} from '../components/Icons';

export default function FacultyManagement() {
  const [facultyList, setFacultyList] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedDept, setSelectedDept] = useState('');

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    facultyCode: '',
    name: '',
    email: '',
    departmentId: '',
    specialization: '',
    maxWeeklyHours: 18,
  });
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      const [facs, depts] = await Promise.all([api.getFaculty(), api.getDepartments()]);
      setFacultyList(facs);
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
      facultyCode: `FAC-${String(facultyList.length + 1).padStart(3, '0')}`,
      name: '',
      email: '',
      departmentId: departments[0]?.id || '',
      specialization: '',
      maxWeeklyHours: 18,
    });
    setFormError('');
    setModalOpen(true);
  };

  const openEditModal = (fac) => {
    setEditingId(fac.id);
    setFormData({
      facultyCode: fac.facultyCode,
      name: fac.name,
      email: fac.email,
      departmentId: fac.departmentId,
      specialization: fac.specialization || '',
      maxWeeklyHours: fac.maxWeeklyHours || 18,
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
        await api.updateFaculty(editingId, formData);
      } else {
        await api.createFaculty(formData);
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
    if (window.confirm(`Are you sure you want to delete faculty member ${name}?`)) {
      try {
        await api.deleteFaculty(id);
        loadData();
      } catch (err) {
        alert(err.message || 'Failed to delete');
      }
    }
  };

  const filtered = facultyList.filter((f) => {
    const matchesSearch =
      f.name.toLowerCase().includes(search.toLowerCase()) ||
      f.facultyCode.toLowerCase().includes(search.toLowerCase()) ||
      (f.specialization && f.specialization.toLowerCase().includes(search.toLowerCase()));
    const matchesDept = selectedDept ? String(f.departmentId) === String(selectedDept) : true;
    return matchesSearch && matchesDept;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <UserIcon className="w-5 h-5 text-blue-600" />
            Faculty Members Management
          </h2>
          <p className="text-xs text-slate-500">Manage academic professors, instructors, workloads, and department affiliations</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={openAddModal} className="btn-primary text-xs">
            <PlusIcon className="w-4 h-4" />
            Add Faculty Member
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
            placeholder="Search by name, ID, or specialization..."
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

      {/* Faculty Table */}
      <div className="app-card overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading faculty list...</div>
        ) : filtered.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500">No faculty members found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider">
                  <th className="p-3.5">ID</th>
                  <th className="p-3.5">Faculty Name</th>
                  <th className="p-3.5">Department</th>
                  <th className="p-3.5">Email</th>
                  <th className="p-3.5">Specialization</th>
                  <th className="p-3.5 text-center">Max Hours</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((fac) => (
                  <tr key={fac.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="p-3.5 font-mono font-medium text-slate-700">{fac.facultyCode}</td>
                    <td className="p-3.5 font-medium text-slate-900">{fac.name}</td>
                    <td className="p-3.5">
                      <span className="badge badge-blue text-[10px]">{fac.departmentCode || fac.departmentName}</span>
                    </td>
                    <td className="p-3.5 text-slate-600">{fac.email}</td>
                    <td className="p-3.5 text-slate-500 max-w-xs truncate">{fac.specialization || '—'}</td>
                    <td className="p-3.5 text-center font-semibold text-slate-700">{fac.maxWeeklyHours} hrs</td>
                    <td className="p-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openEditModal(fac)}
                          className="p-1.5 rounded hover:bg-slate-200/80 text-slate-600 hover:text-blue-600"
                          title="Edit"
                        >
                          <EditIcon className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(fac.id, fac.name)}
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
              {editingId ? 'Edit Faculty Member' : 'Add New Faculty Member'}
            </h3>

            {formError && (
              <div className="p-2.5 rounded bg-rose-50 border border-rose-200 text-rose-700 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-700 font-medium mb-1">Faculty ID *</label>
                <input
                  type="text"
                  required
                  value={formData.facultyCode}
                  onChange={(e) => setFormData({ ...formData, facultyCode: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. FAC-101"
                />
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Full Name *</label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. Dr. Jane Doe"
                />
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Email Address *</label>
                <input
                  type="email"
                  required
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. jane.doe@university.edu"
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

              <div>
                <label className="block text-slate-700 font-medium mb-1">Specialization</label>
                <input
                  type="text"
                  value={formData.specialization}
                  onChange={(e) => setFormData({ ...formData, specialization: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. Deep Learning, Cloud Computing"
                />
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Max Weekly Hours</label>
                <input
                  type="number"
                  min="4"
                  max="40"
                  value={formData.maxWeeklyHours}
                  onChange={(e) => setFormData({ ...formData, maxWeeklyHours: parseInt(e.target.value) || 18 })}
                  className="form-input text-xs"
                />
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
                  {submitting ? 'Saving...' : editingId ? 'Update Faculty' : 'Create Faculty'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
