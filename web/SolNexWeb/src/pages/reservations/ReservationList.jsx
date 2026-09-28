import { useState, useEffect } from "react"
import { reservationApi } from "../../services/reservationApi"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Clock, CheckCircle2, AlertCircle, Search } from "lucide-react"

export default function ReservationList() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("All")

  useEffect(() => {
    const fetchReservations = async () => {
      try {
        setLoading(true)
        setError(null)
        const user = JSON.parse(localStorage.getItem("solnex_user") || "null")
        let data = []
        
        if (user?.role === "Backoffice" || user?.role === "GridOperator") {
           data = await reservationApi.searchReservations()
        } else if (user?.nic) {
           data = await reservationApi.getReservationsByNic(user.nic)
        }
        
        const activeReservations = Array.isArray(data) 
          ? data.filter(r => !["Completed", "Cancelled", "Rejected"].includes(r.status)) 
          : []
          
        setReservations(activeReservations)
      } catch (err) {
        setError(err.message || "Failed to load reservations")
      } finally {
        setLoading(false)
      }
    }
    fetchReservations()
  }, [])

  const stats = {
    pending: reservations.filter(r => r.status === "Pending").length,
    approved: reservations.filter(r => r.status === "Approved").length,
    cancelRequests: reservations.filter(r => r.status === "CancellationRequested").length
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
  const filteredReservations = reservations.filter((res) => {
    const searchString = searchTerm.toLowerCase()
    const matchesSearch = 
      (res.reservationId || "").toLowerCase().includes(searchString) || 
      (res.nic || "").toLowerCase().includes(searchString) ||
      (res.stationId || "").toLowerCase().includes(searchString)
    
    const displayStatus = res.status === "CancellationRequested" ? "Cancel Request" : res.status
    const matchesStatus = statusFilter === "All" || displayStatus === statusFilter

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
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                  Loading reservations...
                </TableCell>
              </TableRow>
            ) : error ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8 text-red-500">
                  {error}
                </TableCell>
              </TableRow>
            ) : filteredReservations.length > 0 ? (
              filteredReservations.map((res) => {
                const displayStatus = res.status === "CancellationRequested" ? "Cancel Request" : res.status
                return (
                  <TableRow key={res.id || res.reservationId}>
                    <TableCell className="font-medium">{res.reservationId}</TableCell>
                    <TableCell>{res.nic}</TableCell>
                    <TableCell>{res.stationId}</TableCell>
                    <TableCell>{res.reservationDate ? new Date(res.reservationDate).toLocaleDateString() : ""}</TableCell>
                    <TableCell>{res.startTime}</TableCell>
                    <TableCell>
                      <Badge variant="secondary" className={`border-none ${getStatusColor(displayStatus)}`}>
                        {displayStatus}
                      </Badge>
                    </TableCell>
                  </TableRow>
                )
              })
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
