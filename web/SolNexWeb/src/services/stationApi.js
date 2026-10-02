const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5097') + '/api';

async function fetchWithConfig(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const token = localStorage.getItem("solnex_token");
  const defaultHeaders = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);
    if (response.status === 401) { localStorage.removeItem('solnex_token'); localStorage.removeItem('solnex_user'); window.location.href = '/login'; return null; }
  if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      const validationMessage = errorData.errors
        ? Object.values(errorData.errors).flat().join(" ")
        : (errorData.message || errorData.title || `API Error: ${response.status} ${response.statusText}`);
      throw new Error(validationMessage);
    }
    // Handle 204 No Content or empty responses
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  } catch (error) {
    console.error(`Error fetching ${url}:`, error);
    throw error;
  }
}

export const stationApi = {
  getStations: (search = '', status = '') => {
    const query = new URLSearchParams()
    if (search) query.append('search', search)
    if (status && status !== 'All') query.append('status', status)
    const queryString = query.toString() ? `?${query.toString()}` : ''
    return fetchWithConfig(`/stations${queryString}`)
  },
  getDashboardData: () => fetchWithConfig('/stations/dashboard'),
  getNextStationId: () => fetchWithConfig('/stations/next-id'),
  
  getStationById: (id) => fetchWithConfig(`/stations/${id}`),
  
  getStationAvailability: (id) => fetchWithConfig(`/stations/${id}/availability`),
  
  getStationSchedule: (id) => fetchWithConfig(`/stations/${id}/schedule`),
  
  createStation: (data) => fetchWithConfig('/stations', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  
  updateStation: (id, data) => fetchWithConfig(`/stations/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
  
  updateStationSchedule: (id, schedule) => fetchWithConfig(`/stations/${id}/schedule`, {
    method: 'PUT',
    body: JSON.stringify({ schedule }),
  }),
  
  activateStation: (id) => fetchWithConfig(`/stations/${id}/activate`, {
    method: 'PUT',
  }),
  
  deactivateStation: (id) => fetchWithConfig(`/stations/${id}/deactivate`, {
    method: 'PUT',
  }),
  
  deleteStation: (id) => fetchWithConfig(`/stations/${id}`, {
    method: 'DELETE',
  }),
};
