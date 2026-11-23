import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  ClockIcon,
  PlusIcon,
  TrashIcon,
  RefreshCwIcon,
  PlayIcon,
} from '../components/Icons';

export default function TimeSlotConfig() {
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '09:55',
    slotOrder: 1,
    isBreak: false,
    isLunch: false,
  });
  const [formError, setFormError] = useState('');

  const loadSlots = async () => {
    try {
      setLoading(true);
      const data = await api.getTimeSlots();
      setSlots(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlots();
  }, []);

  const handleGenerateDefault = async () => {
    if (window.confirm('This will generate standard Monday-Friday academic timetable slots (9:00 AM - 4:35 PM with Interval & Lunch Break). Proceed?')) {
      try {
        setGenerating(true);
        await api.generateAcademicSlots(['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY']);
        loadSlots();
      } catch (err) {
        alert(err.message || 'Generation failed');
      } finally {
        setGenerating(false);
      }
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Delete this time slot?')) {
      try {
        await api.deleteTimeSlot(id);
        loadSlots();
      } catch (err) {
        alert(err.message || 'Failed to delete');
      }
    }
  };

  const handleAddSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.createTimeSlot(formData);
      setModalOpen(false);
      loadSlots();
    } catch (err) {
      setFormError(err.message || 'Failed to save slot');
    }
  };

  const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
  const groupedSlots = days.reduce((acc, d) => {
    acc[d] = slots.filter((s) => s.dayOfWeek === d);
    return acc;
  }, {});

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <ClockIcon className="w-5 h-5 text-blue-600" />
            Time Slot & Academic Period Configuration
          </h2>
          <p className="text-xs text-slate-500">Configure lecture periods, recess intervals, and lunch breaks across academic days</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handleGenerateDefault}
            disabled={generating}
            className="btn-primary text-xs bg-indigo-600 hover:bg-indigo-700 shadow-sm"
          >
            <PlayIcon className="w-4 h-4" />
            {generating ? 'Generating...' : 'Generate Academic 5-Day Schedule'}
          </button>
          <button onClick={() => setModalOpen(true)} className="btn-secondary text-xs">
            <PlusIcon className="w-4 h-4" />
            Add Custom Slot
          </button>
          <button onClick={loadSlots} className="btn-secondary text-xs" title="Refresh">
            <RefreshCwIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Grid of Days */}
      {loading ? (
        <div className="app-card p-12 text-center text-xs text-slate-500">Loading configured time slots...</div>
      ) : slots.length === 0 ? (
        <div className="app-card p-12 text-center space-y-3">
          <ClockIcon className="w-8 h-8 text-slate-300 mx-auto" />
          <div className="text-sm font-semibold text-slate-700">No Time Slots Configured</div>
          <p className="text-xs text-slate-500 max-w-md mx-auto">
            Click "Generate Academic 5-Day Schedule" to automatically create standard 9:00 AM - 4:35 PM periods with morning break and lunch.
          </p>
          <button onClick={handleGenerateDefault} className="btn-primary text-xs mx-auto">
            Generate Default Slots
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          {days.slice(0, 5).map((day) => {
            const daySlots = groupedSlots[day] || [];
            return (
              <div key={day} className="app-card p-4 space-y-3">
                <div className="flex items-center justify-between border-b pb-2">
                  <span className="font-bold text-xs text-slate-800 tracking-wider">{day}</span>
                  <span className="badge badge-slate text-[10px]">{daySlots.length} slots</span>
                </div>

                <div className="space-y-1.5">
                  {daySlots.map((slot) => {
                    const isSpecial = slot.break || slot.lunch;
                    return (
                      <div
                        key={slot.id}
                        className={`p-2.5 rounded-lg border text-xs flex items-center justify-between transition-all ${
                          slot.lunch
                            ? 'bg-amber-50 border-amber-200 text-amber-900 font-medium'
                            : slot.break
                            ? 'bg-indigo-50 border-indigo-200 text-indigo-900 font-medium'
                            : 'bg-white border-slate-200 text-slate-800 hover:border-blue-300'
                        }`}
                      >
                        <div>
                          <div className="font-mono text-[11px] font-semibold">
                            {slot.startTime?.slice(0, 5)} - {slot.endTime?.slice(0, 5)}
                          </div>
                          <div className="text-[10px] text-slate-500 mt-0.5">
                            {slot.lunch
                              ? '🍱 Lunch Break'
                              : slot.break
                              ? '☕ Tea Break'
                              : `Period ${slot.slotOrder || '•'}`}
                          </div>
                        </div>

                        <button
                          onClick={() => handleDelete(slot.id)}
                          className="p-1 rounded hover:bg-rose-100 text-slate-400 hover:text-rose-600 transition-colors"
                          title="Delete slot"
                        >
                          <TrashIcon className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Add Custom Slot Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="app-card w-full max-w-sm p-6 bg-white shadow-2xl space-y-4">
            <h3 className="text-sm font-bold text-slate-900 border-b pb-2">Add Custom Time Slot</h3>

            {formError && (
              <div className="p-2 rounded bg-rose-50 border border-rose-200 text-rose-700 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleAddSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-700 font-medium mb-1">Day of Week *</label>
                <select
                  value={formData.dayOfWeek}
                  onChange={(e) => setFormData({ ...formData, dayOfWeek: e.target.value })}
                  className="form-input text-xs"
                >
                  {days.map((d) => (
                    <option key={d} value={d}>{d}</option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Start Time *</label>
                  <input
                    type="time"
                    required
                    value={formData.startTime}
                    onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                    className="form-input text-xs font-mono"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">End Time *</label>
                  <input
                    type="time"
                    required
                    value={formData.endTime}
                    onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                    className="form-input text-xs font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Slot Order Index</label>
                <input
                  type="number"
                  min="1"
                  max="20"
                  value={formData.slotOrder}
                  onChange={(e) => setFormData({ ...formData, slotOrder: parseInt(e.target.value) || 1 })}
                  className="form-input text-xs"
                />
              </div>

              <div className="space-y-2 pt-1 border-t">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.isBreak}
                    onChange={(e) => setFormData({ ...formData, isBreak: e.target.checked, isLunch: false })}
                    className="rounded text-indigo-600"
                  />
                  <span>Mark as Recess / Tea Break (No classes scheduled)</span>
                </label>

                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.isLunch}
                    onChange={(e) => setFormData({ ...formData, isLunch: e.target.checked, isBreak: false })}
                    className="rounded text-amber-600"
                  />
                  <span>Mark as Lunch Period (No classes scheduled)</span>
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
                <button type="submit" className="btn-primary text-xs">
                  Save Slot
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
