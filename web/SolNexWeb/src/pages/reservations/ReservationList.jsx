import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Clock, CheckCircle2, AlertCircle, Search } from "lucide-react"

// Mock Data for demonstration
const mockReservations = [
  { id: "RES-001", prosumer: "John Doe", station: "Kurunegala Hub", date: "2026-10-15", time: "10:00 AM", status: "Pending" },
  { id: "RES-002", prosumer: "Alice Smith", station: "Colombo Hub", date: "2026-10-15", time: "11:30 AM", status: "Approved" },
  { id: "RES-003", prosumer: "Bob Johnson", station: "Kandy Hub", date: "2026-10-16", time: "09:00 AM", status: "Cancel Request" },
  { id: "RES-005", prosumer: "Mike Tyson", station: "Colombo Hub", date: "2026-10-17", time: "16:00 PM", status: "Pending" },
  { id: "RES-006", prosumer: "Emma Watson", station: "Galle Hub", date: "2026-10-17", time: "08:30 AM", status: "Approved" },
]

export default function ReservationList() {
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("All")

  const stats = {
    pending: 12,
    approved: 45,
    cancelRequests: 3
  }

  const getStatusColor = (status) => {
    switch (status) {
      case "Approved": return "bg-green-100 text-green-700 hover:bg-green-100 dark:bg-green-900/30 dark:text-green-400"
      case "Pending": return "bg-amber-100 text-amber-700 hover:bg-amber-100 dark:bg-amber-900/30 dark:text-amber-400"
      case "Cancel Request": return "bg-red-100 text-red-700 hover:bg-red-100 dark:bg-red-900/30 dark:text-red-400"
      default: return "bg-gray-100 text-gray-700"
    }
  }

  // Filter Logic
  const filteredReservations = mockReservations.filter((res) => {
    const matchesSearch = 
      res.id.toLowerCase().includes(searchTerm.toLowerCase()) || 
      res.prosumer.toLowerCase().includes(searchTerm.toLowerCase()) ||
      res.station.toLowerCase().includes(searchTerm.toLowerCase())
    
    const matchesStatus = statusFilter === "All" || res.status === statusFilter

    return matchesSearch && matchesStatus
  })

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">Reservation List</h1>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending</CardTitle>
            <Clock className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-amber-500">{stats.pending}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Approved</CardTitle>
            <CheckCircle2 className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-500">{stats.approved}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Cancel Requests</CardTitle>
            <AlertCircle className="h-4 w-4 text-red-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-500">{stats.cancelRequests}</div>
          </CardContent>
        </Card>
      </div>

      {/* Filters & Search */}
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center bg-card p-4 rounded-xl border border-border shadow-sm">
        <div className="flex space-x-2 overflow-x-auto w-full sm:w-auto pb-2 sm:pb-0">
          {["All", "Pending", "Approved", "Cancel Request"].map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                statusFilter === status 
                  ? "bg-primary text-primary-foreground" 
                  : "bg-muted text-muted-foreground hover:bg-muted/80 hover:text-foreground"
              }`}
            >
              {status}
            </button>
          ))}
        </div>
        
        <div className="relative w-full sm:w-72">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input 
            type="text" 
            placeholder="Search ID, Prosumer, Station..." 
            className="pl-9 bg-background"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* Data Table */}
      <div className="bg-card rounded-xl border border-border shadow-sm overflow-hidden">
        <Table>
          <TableHeader className="bg-muted/50">
            <TableRow>
              <TableHead>Res ID</TableHead>
              <TableHead>Prosumer</TableHead>
              <TableHead>Station</TableHead>
              <TableHead>Date</TableHead>
              <TableHead>Time</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredReservations.length > 0 ? (
              filteredReservations.map((res) => (
                <TableRow key={res.id}>
                  <TableCell className="font-medium">{res.id}</TableCell>
                  <TableCell>{res.prosumer}</TableCell>
                  <TableCell>{res.station}</TableCell>
                  <TableCell>{res.date}</TableCell>
                  <TableCell>{res.time}</TableCell>
                  <TableCell>
                    <Badge variant="secondary" className={`border-none ${getStatusColor(res.status)}`}>
                      {res.status}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                  No reservations found matching your criteria.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}
