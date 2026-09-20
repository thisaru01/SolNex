import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom"
import AppLayout from "./components/layout/AppLayout"
import Dashboard from "./pages/Dashboard"
import Stations from "./pages/Stations"
import CreateStation from "./pages/CreateStation"
import StationDetails from "./pages/StationDetails"
import EditStation from "./pages/EditStation"
import StationSchedule from "./pages/StationSchedule"
import StationAvailability from "./pages/StationAvailability"
import StationMap from "./pages/StationMap"
import ProtectedRoute from "./components/layout/ProtectedRoute"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="stations" element={<Stations />} />
          <Route path="stations/create" element={<ProtectedRoute allowedRoles={["Backoffice"]}><CreateStation /></ProtectedRoute>} />
          <Route path="stations/map" element={<ProtectedRoute allowedRoles={["GridOperator"]}><StationMap /></ProtectedRoute>} />
          <Route path="stations/:id" element={<StationDetails />} />
          <Route path="stations/:id/edit" element={<ProtectedRoute allowedRoles={["Backoffice"]}><EditStation /></ProtectedRoute>} />
          <Route path="stations/:id/schedule" element={<ProtectedRoute allowedRoles={["Backoffice"]}><StationSchedule /></ProtectedRoute>} />
          <Route path="stations/:id/availability" element={<ProtectedRoute allowedRoles={["GridOperator"]}><StationAvailability /></ProtectedRoute>} />
          
          {/* Placeholders for future routes */}
          <Route path="history" element={<div className="p-4">Operational History Coming Soon</div>} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
