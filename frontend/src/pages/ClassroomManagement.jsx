import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  BuildingIcon,
  PlusIcon,
  EditIcon,
  TrashIcon,
  SearchIcon,
  RefreshCwIcon,
} from '../components/Icons';

export default function ClassroomManagement() {
  const [classrooms, setClassrooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedType, setSelectedType] = useState('');

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    roomNumber: '',
    building: '',
    capacity: 50,
    roomType: 'CLASSROOM',
    available: true,
  });
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await api.getClassrooms();
      setClassrooms(data);
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
      roomNumber: '',
      building: 'Main Academic Block',
      capacity: 60,
      roomType: 'CLASSROOM',
      available: true,
    });
    setFormError('');
    setModalOpen(true);
  };

  const openEditModal = (r) => {
    setEditingId(r.id);
    setFormData({
      roomNumber: r.roomNumber,
      building: r.building,
      capacity: r.capacity,
      roomType: r.roomType,
      available: r.available,
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
        await api.updateClassroom(editingId, formData);
      } else {
        await api.createClassroom(formData);
      }
      setModalOpen(false);
      loadData();
    } catch (err) {
      setFormError(err.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id, roomNumber) => {
    if (window.confirm(`Are you sure you want to delete room ${roomNumber}?`)) {
      try {
        await api.deleteClassroom(id);
        loadData();
      } catch (err) {
        alert(err.message || 'Failed to delete');
      }
    }
  };

  const filtered = classrooms.filter((r) => {
    const matchesSearch =
      r.roomNumber.toLowerCase().includes(search.toLowerCase()) ||
      r.building.toLowerCase().includes(search.toLowerCase());
    const matchesType = selectedType ? r.roomType === selectedType : true;
    return matchesSearch && matchesType;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <BuildingIcon className="w-5 h-5 text-blue-600" />
            Classrooms & Laboratories Management
          </h2>
          <p className="text-xs text-slate-500">Configure lecture halls, specialized laboratories, room capacities, and availability</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={openAddModal} className="btn-primary text-xs">
            <PlusIcon className="w-4 h-4" />
            Add Room / Lab
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
            placeholder="Search by room number or building..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="form-input pl-9 text-xs"
          />
        </div>
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <label className="text-xs text-slate-500 font-medium">Type:</label>
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="form-input text-xs w-48"
          >
            <option value="">All Room Types</option>
            <option value="CLASSROOM">CLASSROOM (Lecture Hall)</option>
            <option value="LABORATORY">LABORATORY (Lab)</option>
          </select>
        </div>
      </div>

      {/* Classroom Table */}
      <div className="app-card overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading classrooms...</div>
        ) : filtered.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500">No classrooms or laboratories found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider">
                  <th className="p-3.5">Room Number</th>
                  <th className="p-3.5">Building</th>
                  <th className="p-3.5">Type</th>
                  <th className="p-3.5 text-center">Capacity</th>
                  <th className="p-3.5 text-center">Status</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="p-3.5 font-mono font-bold text-slate-900">{r.roomNumber}</td>
                    <td className="p-3.5 text-slate-700 font-medium">{r.building}</td>
                    <td className="p-3.5">
                      <span className={`badge text-[10px] ${r.roomType === 'LABORATORY' ? 'badge-amber' : 'badge-blue'}`}>
                        {r.roomType}
                      </span>
                    </td>
                    <td className="p-3.5 text-center font-bold text-slate-800">
                      {r.capacity} seats
                    </td>
                    <td className="p-3.5 text-center">
                      <span className={`badge text-[10px] ${r.available ? 'badge-green' : 'badge-red'}`}>
                        {r.available ? 'Available' : 'Maintenance'}
                      </span>
                    </td>
                    <td className="p-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openEditModal(r)}
                          className="p-1.5 rounded hover:bg-slate-200/80 text-slate-600 hover:text-blue-600"
                          title="Edit"
                        >
                          <EditIcon className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(r.id, r.roomNumber)}
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
              {editingId ? 'Edit Classroom / Laboratory' : 'Add New Classroom / Laboratory'}
            </h3>

            {formError && (
              <div className="p-2.5 rounded bg-rose-50 border border-rose-200 text-rose-700 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-700 font-medium mb-1">Room Number *</label>
                <input
                  type="text"
                  required
                  value={formData.roomNumber}
                  onChange={(e) => setFormData({ ...formData, roomNumber: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. LH-101 or LAB-301"
                />
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Building Block *</label>
                <input
                  type="text"
                  required
                  value={formData.building}
                  onChange={(e) => setFormData({ ...formData, building: e.target.value })}
                  className="form-input text-xs"
                  placeholder="e.g. Main Academic Block"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Seating Capacity *</label>
                  <input
                    type="number"
                    min="10"
                    max="500"
                    required
                    value={formData.capacity}
                    onChange={(e) => setFormData({ ...formData, capacity: parseInt(e.target.value) || 60 })}
                    className="form-input text-xs"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Room Type *</label>
                  <select
                    value={formData.roomType}
                    onChange={(e) => setFormData({ ...formData, roomType: e.target.value })}
                    className="form-input text-xs font-semibold"
                  >
                    <option value="CLASSROOM">CLASSROOM</option>
                    <option value="LABORATORY">LABORATORY</option>
                  </select>
                </div>
              </div>

              <div className="pt-1">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.available}
                    onChange={(e) => setFormData({ ...formData, available: e.target.checked })}
                    className="rounded text-blue-600"
                  />
                  <span className="text-xs text-slate-700 font-medium">Room is active and available for scheduling</span>
                </label>
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
                  {submitting ? 'Saving...' : editingId ? 'Update Room' : 'Create Room'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
