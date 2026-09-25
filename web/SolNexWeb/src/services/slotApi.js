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

  if (!response.ok) {
    const error = new Error(data.message || `API Error: ${response.status} ${response.statusText}`)
    error.status = response.status
    error.data = data
    throw error
  }
  return data
}

export const slotApi = {
  getAllSlots: () => request("/api/slots"),
  getAvailableSlots: () => request("/api/slots/available"),
  getSlotsByStation: (stationId) => request(`/api/slots/${stationId}`),
  createSlot: (data) =>
    request("/api/slots", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateSlotStatus: (id, status) =>
    request(`/api/slots/${id}/status`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    }),
  reserveSlot: (id) =>
    request(`/api/slots/${id}/reserve`, {
      method: "PUT",
    }),
  deleteSlot: (id) =>
    request(`/api/slots/${id}`, {
      method: "DELETE",
    }),
}
