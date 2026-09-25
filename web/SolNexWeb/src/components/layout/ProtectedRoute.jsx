import { Navigate } from "react-router-dom"

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("solnex_user") || "null")
  } catch {
    return null
  }
}

export default function ProtectedRoute({ allowedRoles, children }) {
  const user = getStoredUser()
  const role = user?.role || "Guest"

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (!allowedRoles.includes(role)) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}
