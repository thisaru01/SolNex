import { Navigate } from "react-router-dom"

export default function ProtectedRoute({ allowedRoles, children }) {
  const role = import.meta.env.VITE_USER_ROLE || "Backoffice"
  
  if (!allowedRoles.includes(role)) {
    // If they don't have access, redirect them to the dashboard
    return <Navigate to="/dashboard" replace />
  }
  
  return children
}
