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
    throw new Error(data?.message || `Request failed with status ${response.status}.`)
  }

  return data
}

export const transactionApi = {
  getPending: () => request("/transactions/pending"),
  getCompleted: () => request("/transactions/completed"),
  getById: (id) => request(`/transactions/${encodeURIComponent(id)}`),
}