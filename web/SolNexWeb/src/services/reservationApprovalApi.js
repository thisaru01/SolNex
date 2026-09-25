const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5097"

function getAuthHeaders() {
  const token = localStorage.getItem("solnex_token")
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function request(endpoint, options = {}) {
  const response = await fetch(`${API_URL}/api${endpoint}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
      ...(options.headers || {}),
    },
  })

  const text = await response.text()
  let data = null

  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = { message: text }
    }
  }

  if (!response.ok) {
    const validationMessage = data?.errors
      ? Object.values(data.errors).flat().join(" ")
      : data?.message

    throw new Error(validationMessage || `Request failed with status ${response.status}.`)
  }

  return data
}

export const reservationApprovalApi = {
  approveReservation: (id, operatorNic) =>
    request(`/reservations/${encodeURIComponent(id)}/approve`, {
      method: "PUT",
      body: JSON.stringify({ operatorNic }),
    }),

  rejectReservation: (id, operatorNic, reason) =>
    request(`/reservations/${encodeURIComponent(id)}/reject`, {
      method: "PUT",
      body: JSON.stringify({ operatorNic, reason }),
    }),
}
