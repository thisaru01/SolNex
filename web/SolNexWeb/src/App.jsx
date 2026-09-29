import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom"
import AppLayout from "./components/layout/AppLayout"
import Dashboard from "./pages/Dashboard"
import Stations from "./pages/stations/Stations"
import CreateStation from "./pages/stations/CreateStation"
import StationDetails from "./pages/stations/StationDetails"
import EditStation from "./pages/stations/EditStation"
import StationSchedule from "./pages/stations/StationSchedule"
import StationMap from "./pages/stations/StationMap"
import ProtectedRoute from "./components/layout/ProtectedRoute"
import Login from "./pages/auth/Login"
import Register from "./pages/auth/Register"
import Transactions from "./pages/transactions/Transactions"
import TransactionDetails from "./pages/transactions/TransactionDetails"
import OperationalHistory from "./pages/transactions/OperationalHistory"
import Users from "./pages/user/Users"
import CreateUser from "./pages/user/CreateUser"
import DeactivationRequests from "./pages/user/DeactivationRequests"
import Prosumers from "./pages/prosumer/Prosumers"
import ProsumerDetails from "./pages/prosumer/ProsumerDetails"
import EditProsumer from "./pages/prosumer/EditProsumer"
import CreateProsumer from "./pages/prosumer/CreateProsumer"
import ReservationList from "./pages/reservations/ReservationList"
import ReservationHistory from "./pages/reservations/ReservationHistory"
import BookingSlots from "./pages/reservations/BookingSlots"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public / Standalone Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Main Application Routes (with Sidebar Layout) */}
        <Route path="/" element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<ProtectedRoute allowedRoles={["Backoffice", "GridOperator"]}><Dashboard /></ProtectedRoute>} />
          <Route path="users" element={<ProtectedRoute allowedRoles={["Backoffice"]}><Users /></ProtectedRoute>} />
          <Route path="users/create" element={<ProtectedRoute allowedRoles={["Backoffice"]}><CreateUser /></ProtectedRoute>} />
          <Route path="deactivation-requests" element={<ProtectedRoute allowedRoles={["Backoffice"]}><DeactivationRequests /></ProtectedRoute>} />
          <Route path="prosumers" element={<ProtectedRoute allowedRoles={["Backoffice"]}><Prosumers /></ProtectedRoute>} />
          <Route path="prosumers/create" element={<ProtectedRoute allowedRoles={["Backoffice"]}><CreateProsumer /></ProtectedRoute>} />
          <Route path="prosumers/:nic" element={<ProtectedRoute allowedRoles={["Backoffice"]}><ProsumerDetails /></ProtectedRoute>} />
          <Route path="prosumers/:nic/edit" element={<ProtectedRoute allowedRoles={["Backoffice"]}><EditProsumer /></ProtectedRoute>} />
          <Route path="stations" element={<ProtectedRoute allowedRoles={["Backoffice", "GridOperator"]}><Stations /></ProtectedRoute>} />
          <Route path="stations/create" element={<ProtectedRoute allowedRoles={["Backoffice"]}><CreateStation /></ProtectedRoute>} />
          <Route path="stations/map" element={<ProtectedRoute allowedRoles={["Backoffice", "GridOperator"]}><StationMap /></ProtectedRoute>} />
          <Route path="stations/:id" element={<ProtectedRoute allowedRoles={["Backoffice", "GridOperator"]}><StationDetails /></ProtectedRoute>} />
          <Route path="stations/:id/edit" element={<ProtectedRoute allowedRoles={["Backoffice"]}><EditStation /></ProtectedRoute>} />
          <Route path="stations/:id/schedule" element={<ProtectedRoute allowedRoles={["Backoffice"]}><StationSchedule /></ProtectedRoute>} />
          <Route path="stations/:id/availability" element={<Navigate to="../" relative="path" replace />} />
          <Route path="reservations" element={<ProtectedRoute allowedRoles={["Backoffice", "GridOperator"]}><ReservationList /></ProtectedRoute>} />
          <Route path="reservations/history" element={<ProtectedRoute allowedRoles={["Backoffice", "GridOperator"]}><ReservationHistory /></ProtectedRoute>} />
          <Route path="reservations/slots" element={<ProtectedRoute allowedRoles={["Backoffice", "GridOperator"]}><BookingSlots /></ProtectedRoute>} />
          <Route path="transactions" element={<ProtectedRoute allowedRoles={["Backoffice","GridOperator"]}><Transactions /></ProtectedRoute>}/>
          <Route path="transactions/:id" element={<ProtectedRoute allowedRoles={["Backoffice","GridOperator"]}><TransactionDetails /></ProtectedRoute>}/>
          <Route path="history" element={<ProtectedRoute allowedRoles={["Backoffice","GridOperator"]}><OperationalHistory /></ProtectedRoute>}/>
          

          {/* Placeholders for future routes */}
          {/* <Route path="history" element={<div className="p-4">Operational History Coming Soon</div>} /> */}
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
