const BASE_URL = (typeof window !== 'undefined' && window.location.port === '5173')
  ? 'http://localhost:8081/api'
  : '/api';

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const response = await fetch(url, config);
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
    const json = await response.json();
    return json.data;
  } catch (err) {
    console.error(`API Error on ${endpoint}:`, err);
    throw err;
  }
}

export const api = {
  // Dashboard
  getDashboardStats: () => request('/dashboard/stats'),

  // Departments
  getDepartments: () => request('/departments'),
  createDepartment: (data) => request('/departments', { method: 'POST', body: JSON.stringify(data) }),
  updateDepartment: (id, data) => request(`/departments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteDepartment: (id) => request(`/departments/${id}`, { method: 'DELETE' }),

  // Faculty
  getFaculty: () => request('/faculty'),
  getFacultyById: (id) => request(`/faculty/${id}`),
  createFaculty: (data) => request('/faculty', { method: 'POST', body: JSON.stringify(data) }),
  updateFaculty: (id, data) => request(`/faculty/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteFaculty: (id) => request(`/faculty/${id}`, { method: 'DELETE' }),

  // Classrooms
  getClassrooms: (type) => request(`/classrooms${type ? `?type=${type}` : ''}`),
  createClassroom: (data) => request('/classrooms', { method: 'POST', body: JSON.stringify(data) }),
  updateClassroom: (id, data) => request(`/classrooms/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteClassroom: (id) => request(`/classrooms/${id}`, { method: 'DELETE' }),

  // Student Groups
  getStudentGroups: () => request('/student-groups'),
  createStudentGroup: (data) => request('/student-groups', { method: 'POST', body: JSON.stringify(data) }),
  updateStudentGroup: (id, data) => request(`/student-groups/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteStudentGroup: (id) => request(`/student-groups/${id}`, { method: 'DELETE' }),

  // Courses
  getCourses: () => request('/courses'),
  createCourse: (data) => request('/courses', { method: 'POST', body: JSON.stringify(data) }),
  updateCourse: (id, data) => request(`/courses/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCourse: (id) => request(`/courses/${id}`, { method: 'DELETE' }),

  // Time Slots
  getTimeSlots: () => request('/time-slots'),
  createTimeSlot: (data) => request('/time-slots', { method: 'POST', body: JSON.stringify(data) }),
  updateTimeSlot: (id, data) => request(`/time-slots/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteTimeSlot: (id) => request(`/time-slots/${id}`, { method: 'DELETE' }),
  generateAcademicSlots: (days) => request('/time-slots/generate-academic', { method: 'POST', body: JSON.stringify(days || []) }),

  // Timetable Generation & Query
  generateTimetable: (algorithm = 'BACKTRACKING', clearExisting = true) =>
    request(`/timetable/generate?algorithm=${algorithm}&clearExisting=${clearExisting}`, { method: 'POST' }),
  undoTimetable: () => request('/timetable/undo', { method: 'POST' }),
  getTimetableEntries: (filters = {}) => {
    const params = new URLSearchParams();
    if (filters.studentGroupId) params.append('studentGroupId', filters.studentGroupId);
    if (filters.facultyId) params.append('facultyId', filters.facultyId);
    if (filters.classroomId) params.append('classroomId', filters.classroomId);
    if (filters.departmentId) params.append('departmentId', filters.departmentId);
    if (filters.semester) params.append('semester', filters.semester);
    const query = params.toString();
    return request(`/timetable/entries${query ? `?${query}` : ''}`);
  },
  clearTimetableEntries: () => request('/timetable/entries', { method: 'DELETE' }),
  getCsvExportUrl: () => `${BASE_URL}/timetable/export/csv`,

  // Conflicts
  scanConflicts: () => request('/conflicts'),

  // Examinations
  getExaminations: (filters = {}) => {
    const params = new URLSearchParams();
    if (filters.date) params.append('date', filters.date);
    if (filters.semester) params.append('semester', filters.semester);
    if (filters.studentGroupId) params.append('studentGroupId', filters.studentGroupId);
    if (filters.invigilatorId) params.append('invigilatorId', filters.invigilatorId);
    const query = params.toString();
    return request(`/examinations${query ? `?${query}` : ''}`);
  },
  createExamination: (data) => request('/examinations', { method: 'POST', body: JSON.stringify(data) }),
  updateExamination: (id, data) => request(`/examinations/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteExamination: (id) => request(`/examinations/${id}`, { method: 'DELETE' }),
  clearExaminations: () => request('/examinations', { method: 'DELETE' }),
  generateExaminations: (startDate, minDaysSpacing = 1, clearExisting = true) => {
    let url = `/examinations/generate?minDaysSpacing=${minDaysSpacing}&clearExisting=${clearExisting}`;
    if (startDate) url += `&startDate=${startDate}`;
    return request(url, { method: 'POST' });
  },

  // Algorithm Benchmark
  runBenchmark: () => request('/benchmark/compare'),

  // Concurrency Simulation
  runConcurrencyDemo: (producers = 4, consumers = 2, itemsPerProducer = 50, queueCapacity = 20) =>
    request(`/concurrency/run?producers=${producers}&consumers=${consumers}&itemsPerProducer=${itemsPerProducer}&queueCapacity=${queueCapacity}`, {
      method: 'POST',
    }),
};
