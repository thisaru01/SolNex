const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5097"

async function request(path, options = {}) {
  const token = localStorage.getItem("solnex_token")
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  const text = await response.text()
  let data = {}
  try {
    data = text ? JSON.parse(text) : {}
  } catch {
    data = { message: text }
  }

  if (response.status === 401) { localStorage.removeItem('solnex_token'); localStorage.removeItem('solnex_user'); window.location.href = '/login'; return null; }
  if (!response.ok) {
    const error = new Error(data.message || `API Error: ${response.status} ${response.statusText}`)
    error.status = response.status
    error.data = data
    throw error
  }
  return data
}

export const reservationApi = {
  getReservationHistory: () => request("/api/reservations/history"),
  getPendingReservations: () => request("/api/reservations/pending"),
  getReservationsByNic: (nic) => request(`/api/reservations/user/${encodeURIComponent(nic)}`),
  searchReservations: () => request("/api/reservations/search"),
  createReservation: (data) =>
    request("/api/reservations", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  deleteReservation: (id) =>
    request(`/api/reservations/${encodeURIComponent(id)}`, {
      method: "DELETE",
    }),
  updateReservationStatus: (id, status) =>
    request(`/api/reservations/${encodeURIComponent(id)}`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    }),
}
