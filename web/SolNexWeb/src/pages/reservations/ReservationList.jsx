import { useState, useEffect } from "react"
import { reservationApi } from "../../services/reservationApi"
import { getUsers } from "../../services/userApi"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Clock, CheckCircle2, AlertCircle, Search } from "lucide-react"
import BackofficeReservationActions from "../../components/reservations/BackofficeReservationActions"

export default function ReservationList() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("All")
  const [users, setUsers] = useState({})
  const [selectedReservation, setSelectedReservation] = useState(null)
  const [actionLoading, setActionLoading] = useState(false)
  const [actionError, setActionError] = useState(null)

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

        try {
          const userList = await getUsers()
          const userMap = {}
          if (Array.isArray(userList)) {
            userList.forEach(u => { userMap[u.nic] = u.fullName || "Unknown" })
          }
          setUsers(userMap)
        } catch (err) {
          console.error("Failed to fetch users for owner names", err)
        }
      } catch (err) {
        setError(err.message || "Failed to load reservations")
      } finally {
        setLoading(false)
      }
    }
    fetchReservations()
  }, [])


  const handleApproveCancel = async (res) => {
    try {
      setActionLoading(true)
      setActionError(null)
      await reservationApi.updateReservationStatus(res.reservationId, "Cancelled")
      setReservations(prev => prev.map(r => r.reservationId === res.reservationId ? { ...r, status: "Cancelled" } : r))
      setSelectedReservation(null)
    } catch (err) {
      setActionError(err.message || "Failed to approve cancellation.")
    } finally {
      setActionLoading(false)
    }
  }

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
              <TableHead>Owner</TableHead>
              <TableHead>Prosumer NIC</TableHead>
              <TableHead>Station</TableHead>
              <TableHead>Date</TableHead>
              <TableHead>Time</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                  Loading reservations...
                </TableCell>
              </TableRow>
            ) : error ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-red-500">
                  {error}
                </TableCell>
              </TableRow>
            ) : filteredReservations.length > 0 ? (
              filteredReservations.map((res) => {
                const displayStatus = res.status === "CancellationRequested" ? "Cancel Request" : res.status
                return (
                  <TableRow 
                    key={res.id || res.reservationId}
                    className="cursor-pointer transition-colors hover:bg-muted/50"

                     onClick={() => {
                       setActionError(null)
                       setSelectedReservation(res)
                    }}
                  >
                    <TableCell className="font-medium">{res.reservationId}</TableCell>
                    <TableCell>{users[res.nic] || "Unknown"}</TableCell>
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
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                  No reservations found matching your criteria.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {selectedReservation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={() => setSelectedReservation(null)}>
          <div className={`bg-background w-full ${selectedReservation.status === "Pending" ? "max-w-md" : "max-w-lg"} rounded-xl border border-border shadow-xl`} onClick={e => e.stopPropagation()}>
            {selectedReservation.status === "Pending" ? (
              <div className="p-6 sm:p-7">
                <div className="mb-6">
                  <Badge variant="secondary" className="mb-3 border-none bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
                    Pending review
                  </Badge>
                  <h2 className="text-xl font-bold tracking-tight">Reservation actions</h2>
                </div>
                <BackofficeReservationActions
                  reservation={selectedReservation}
                  onCancel={() => setSelectedReservation(null)}
                  onApproved={() => {
                    setReservations(previous => previous.map(item =>
                      item.reservationId === selectedReservation.reservationId
                        ? { ...item, status: "Approved" }
                        : item
                    ))
                    setSelectedReservation(null)
                  }}
                  onRejected={() => {
                    setReservations(previous => previous.filter(item =>
                      item.reservationId !== selectedReservation.reservationId
                    ))
                    setSelectedReservation(null)
                  }}
                />
              </div>
            ) : (
            <div className="p-6">
              <h2 className="text-xl font-bold mb-4">Reservation Details</h2>
              <div className="space-y-3 mb-6">
                <div className="grid grid-cols-3 border-b border-border pb-2">
                  <span className="text-muted-foreground font-medium">Res ID:</span>
                  <span className="col-span-2 font-medium">{selectedReservation.reservationId}</span>
                </div>
                <div className="grid grid-cols-3 border-b border-border pb-2">
                  <span className="text-muted-foreground font-medium">Owner Name:</span>
                  <span className="col-span-2">{users[selectedReservation.nic] || "Unknown"}</span>
                </div>
                <div className="grid grid-cols-3 border-b border-border pb-2">
                  <span className="text-muted-foreground font-medium">NIC:</span>
                  <span className="col-span-2">{selectedReservation.nic}</span>
                </div>
                <div className="grid grid-cols-3 border-b border-border pb-2">
                  <span className="text-muted-foreground font-medium">Station ID:</span>
                  <span className="col-span-2">{selectedReservation.stationId}</span>
                </div>
                <div className="grid grid-cols-3 border-b border-border pb-2">
                  <span className="text-muted-foreground font-medium">Energy:</span>
                  <span className="col-span-2">{selectedReservation.energyAmountKwh} kWh</span>
                </div>
                <div className="grid grid-cols-3 border-b border-border pb-2">
                  <span className="text-muted-foreground font-medium">Date:</span>
                  <span className="col-span-2">{selectedReservation.reservationDate ? new Date(selectedReservation.reservationDate).toLocaleDateString() : ""}</span>
                </div>
                <div className="grid grid-cols-3 border-b border-border pb-2">
                  <span className="text-muted-foreground font-medium">Time:</span>
                  <span className="col-span-2">{selectedReservation.startTime} - {selectedReservation.endTime}</span>
                </div>
                <div className="grid grid-cols-3 pb-2">
                  <span className="text-muted-foreground font-medium">Status:</span>
                  <span className="col-span-2">
                    <Badge variant="secondary" className={`border-none ${getStatusColor(selectedReservation.status === "CancellationRequested" ? "Cancel Request" : selectedReservation.status)}`}>
                      {selectedReservation.status === "CancellationRequested" ? "Cancel Request" : selectedReservation.status}
                    </Badge>
                  </span>
                </div>
              </div>
              {actionError && (
                <div role="alert" className="mb-4 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/40 dark:text-red-300">
                  {actionError}
                </div>
              )}
              <div className="flex justify-end space-x-3">
                <button
                  onClick={() => setSelectedReservation(null)}
                  className="px-4 py-2 border border-border rounded-md hover:bg-muted transition-colors font-medium"
                >
                  Close
                </button>
{selectedReservation.status === "CancellationRequested" && (

  <button
    onClick={() =>
      handleApproveCancel(
        selectedReservation
      )
    }
    disabled={
      actionLoading
    }
    className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors disabled:opacity-50 font-medium"
  >
    {actionLoading
      ? "Processing..."
      : "Approve Cancellation"}
  </button>

)}
              </div>
            </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
