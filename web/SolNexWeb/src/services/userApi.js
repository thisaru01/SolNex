const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5097"

/**
 * User API service for user and prosumer management operations.
 * Provides functions for user CRUD, role management, and account status control.
 */

// Helper function for API requests with authentication
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

// Get all users with optional search filter
export function getUsers(search = "") {
  const query = search.trim() ? `?search=${encodeURIComponent(search.trim())}` : ""
  return request(`/api/users${query}`)
}

// Get users with pending status
export function getPendingUsers() {
  return request("/api/users/pending")
}

// Get users with deactivation requests
export function getDeactivationRequests() {
  return request("/api/users/deactivation-requests")
}

// Get user by NIC
export function getUserByNic(nic) {
  return request(`/api/users/${encodeURIComponent(nic)}`)
}

// Update user information
export function updateUser(nic, user) {
  return request(`/api/users/${encodeURIComponent(nic)}`, {
    method: "PUT",
    body: JSON.stringify(user),
  })
}

// Update user role
export function updateUserRole(nic, role) {
  return request(`/api/users/${encodeURIComponent(nic)}/role`, {
    method: "PUT",
    body: JSON.stringify({ role }),
  })
}

// Activate user account
export function activateUser(nic) {
  return request(`/api/users/${encodeURIComponent(nic)}/activate`, { method: "PUT" })
}

// Deactivate user account
export function deactivateUser(nic) {
  return request(`/api/users/${encodeURIComponent(nic)}/deactivate`, { method: "PUT" })
}

// Approve deactivation request
export function approveDeactivation(nic) {
  return request(`/api/users/${encodeURIComponent(nic)}/approve-deactivation`, { method: "PUT" })
}

// Reject deactivation request
export function rejectDeactivation(nic) {
  return request(`/api/users/${encodeURIComponent(nic)}/reject-deactivation`, { method: "PUT" })
}

// Register a new web user
export function registerWebUser(user) {
  return request("/api/users/register", {
    method: "POST",
    body: JSON.stringify(user),
  })
}
