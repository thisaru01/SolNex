import { useEffect, useState } from "react"
import { reservationApi } from "../../services/reservationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { 
  Clock, 
  CheckCircle2, 
  XCircle, 
  AlertCircle,
  Search,
  RefreshCw,
  Filter,
  MapPin,
  Calendar,
  Zap
} from "lucide-react"

export default function ReservationHistory() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  
  // Search and Filter states
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState("All")

  const fetchHistory = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await reservationApi.getReservationHistory()
      const allReservations = Array.isArray(data) ? data : []
      setReservations(allReservations.filter(r => ["Completed", "Cancelled", "Rejected"].includes(r.status)))
    } catch (err) {
      setError(err.message || "Failed to load reservation history")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchHistory()
  }, [])

  // Derived stats using actual data
  const stats = {
    completed: reservations.filter(r => r.status === "Completed").length,
    cancelled: reservations.filter(r => r.status === "Cancelled").length,
    rejected: reservations.filter(r => r.status === "Rejected").length
  }

  // Filtered list for the table
  const filteredReservations = reservations.filter((res) => {
    const q = searchQuery.toLowerCase().trim()
    const matchesSearch =
      !q ||
      (res.reservationId && res.reservationId.toLowerCase().includes(q)) ||
      (res.stationId && res.stationId.toLowerCase().includes(q)) ||
      (res.nic && res.nic.toLowerCase().includes(q)) ||
      (res.slotId && res.slotId.toLowerCase().includes(q))

    const matchesStatus =
      statusFilter === "All" ||
      (res.status && res.status.toLowerCase() === statusFilter.toLowerCase())

    return matchesSearch && matchesStatus
  })

  // Format date helper
  const formatDate = (dateString) => {
    if (!dateString) return "—"
    return new Date(dateString).toLocaleDateString()
  }

  // Format status badge color
  const getStatusBadge = (status) => {
    switch(status) {
      case "Pending":
        return <Badge variant="secondary" className="bg-amber-100 text-amber-800 border-amber-200">Pending</Badge>
      case "Approved":
        return <Badge variant="secondary" className="bg-emerald-100 text-emerald-800 border-emerald-200">Approved</Badge>
      case "Completed":
        return <Badge className="bg-emerald-500 hover:bg-emerald-600">Completed</Badge>
      case "Rejected":
        return <Badge variant="destructive">Rejected</Badge>
      case "Cancelled":
        return <Badge variant="outline" className="text-gray-500">Cancelled</Badge>
      case "CancellationRequested":
        return <Badge variant="secondary" className="bg-orange-100 text-orange-800 border-orange-200">Cancel Requested</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Reservation History</h1>
          <p className="text-muted-foreground mt-1">
            View the complete history of energy reservations across all microgrid stations.
          </p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Completed</CardTitle>
            <CheckCircle2 className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-500">{stats.completed}</div>
            <p className="text-xs text-muted-foreground mt-1">Successfully finished</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Cancelled</CardTitle>
            <XCircle className="h-4 w-4 text-gray-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-500">{stats.cancelled}</div>
            <p className="text-xs text-muted-foreground mt-1">User cancelled</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Rejected</CardTitle>
            <AlertCircle className="h-4 w-4 text-red-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-500">{stats.rejected}</div>
            <p className="text-xs text-muted-foreground mt-1">Denied by backoffice</p>
          </CardContent>
        </Card>
      </div>

      {/* Main Table Card */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle>History Directory</CardTitle>
          <CardDescription>
            Filter and view all energy reservations recorded in the system.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Controls & Filter Bar */}
          <div className="flex flex-col lg:flex-row gap-4 mb-6 justify-between">
            <div className="flex flex-col sm:flex-row gap-3 flex-1">
              <div className="relative flex-1 max-w-md">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search by ID, Station, NIC, Slot..."
                  className="pl-8"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            <div className="flex items-center gap-2">
              <div className="inline-flex items-center rounded-md border p-1 bg-muted/50 hidden md:flex">
                {["All", "Completed", "Cancelled", "Rejected"].map((status) => (
                  <Button
                    key={status}
                    variant={statusFilter === status ? "secondary" : "ghost"}
                    size="sm"
                    className="h-7 text-xs"
                    onClick={() => setStatusFilter(status)}
                  >
                    {status}
                  </Button>
                ))}
              </div>
              <div className="md:hidden">
                <select
                  className="h-9 px-3 py-1 rounded-md border border-input bg-background text-sm"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  {["All", "Completed", "Cancelled", "Rejected"].map((status) => (
                    <option key={status} value={status}>{status}</option>
                  ))}
                </select>
              </div>

              <Button
                variant="outline"
                size="icon"
                onClick={fetchHistory}
                disabled={loading}
                title="Refresh History"
              >
                <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
              </Button>
            </div>
          </div>

          {/* Table Display */}
          {error ? (
            <div className="text-center py-8 text-destructive flex flex-col items-center gap-2">
              <AlertCircle className="h-8 w-8 text-destructive" />
              <span>{error}</span>
              <Button variant="outline" size="sm" onClick={fetchHistory} className="mt-2">
                Retry Loading
              </Button>
            </div>
          ) : loading ? (
            <div className="space-y-2">
              {[1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-14 w-full" />
              ))}
            </div>
          ) : filteredReservations.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground border border-dashed rounded-lg flex flex-col items-center justify-center gap-2">
              <Filter className="h-8 w-8 text-muted-foreground/50" />
              <p className="font-medium">No reservations found matching your filters.</p>
              {(searchQuery || statusFilter !== "All") && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    setSearchQuery("")
                    setStatusFilter("All")
                  }}
                  className="text-primary mt-1"
                >
                  Clear all filters
                </Button>
              )}
            </div>
          ) : (
            <div className="rounded-md border overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Reservation ID</TableHead>
                    <TableHead>NIC</TableHead>
                    <TableHead>Station</TableHead>
                    <TableHead>Slot</TableHead>
                    <TableHead>Date</TableHead>
                    <TableHead>Energy</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredReservations.map((res) => (
                    <TableRow key={res.id || res.reservationId}>
                      <TableCell>
                        <div className="font-medium font-mono text-sm">{res.reservationId}</div>
                      </TableCell>
                      
                      <TableCell>
                        <span className="text-sm font-medium">{res.nic}</span>
                      </TableCell>

                      <TableCell>
                        <div className="flex items-center gap-1.5 text-sm">
                          <MapPin className="h-3.5 w-3.5 text-blue-500" />
                          <span className="font-medium">{res.stationId}</span>
                        </div>
                      </TableCell>

                      <TableCell>
                        <div className="flex items-center gap-1.5 text-xs font-mono text-muted-foreground">
                          {res.slotId}
                        </div>
                      </TableCell>

                      <TableCell>
                        <div className="text-sm">
                          {formatDate(res.reservationDate)}
                        </div>
                      </TableCell>

                      <TableCell>
                        <div className="flex items-center gap-1.5 text-sm font-semibold">
                          <Zap className="h-4 w-4 text-amber-500" />
                          {res.energyAmountKwh} kWh
                        </div>
                      </TableCell>

                      <TableCell>
                        {getStatusBadge(res.status)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
