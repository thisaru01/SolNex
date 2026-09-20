import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom"
import AppLayout from "./components/layout/AppLayout"
import Dashboard from "./pages/Dashboard"
import Stations from "./pages/Stations"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="stations" element={<Stations />} />
          {/* Placeholders for future routes */}
          <Route path="users" element={<div className="p-4">Users Management Coming Soon</div>} />
          <Route path="prosumers" element={<div className="p-4">Prosumers Management Coming Soon</div>} />
          <Route path="reservations" element={<div className="p-4">Reservations Coming Soon</div>} />
          <Route path="transactions" element={<div className="p-4">Transactions Coming Soon</div>} />
          <Route path="history" element={<div className="p-4">Operational History Coming Soon</div>} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
