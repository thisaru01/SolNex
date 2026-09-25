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
  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(data.message || "The request could not be completed.")
    error.status = response.status
    throw error
  }
  return data
}

export function getUsers(search = "") {
  const query = search.trim() ? `?search=${encodeURIComponent(search.trim())}` : ""
  return request(`/api/users${query}`)
}

export function getPendingUsers() {
  return request("/api/users/pending")
}

export function updateUser(nic, user) {
  return request(`/api/users/${encodeURIComponent(nic)}`, {
    method: "PUT",
    body: JSON.stringify(user),
  })
}

export function updateUserRole(nic, role) {
  return request(`/api/users/${encodeURIComponent(nic)}/role`, {
    method: "PUT",
    body: JSON.stringify({ role }),
  })
}

export function activateUser(nic) {
  return request(`/api/users/${encodeURIComponent(nic)}/activate`, { method: "PUT" })
}

export function deactivateUser(nic) {
  return request(`/api/users/${encodeURIComponent(nic)}/deactivate`, { method: "PUT" })
}
