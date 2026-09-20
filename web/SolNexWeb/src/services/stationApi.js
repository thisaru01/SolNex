const API_BASE_URL = 'http://localhost:5097/api';

async function fetchWithConfig(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const defaultHeaders = {
    'Content-Type': 'application/json',
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
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `API Error: ${response.status} ${response.statusText}`);
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
  getStations: () => fetchWithConfig('/stations'),
  
  getStationById: (id) => fetchWithConfig(`/stations/${id}`),
  
  getNearbyStations: (lat, lon, radius = 10.0) => 
    fetchWithConfig(`/stations/nearby?lat=${lat}&lon=${lon}&radius=${radius}`),
    
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
};
