import { useEffect, useState } from "react"
import { slotApi } from "../../services/slotApi"
import { stationApi } from "../../services/stationApi"
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
  Calendar,
  Clock,
  Plus,
  Trash2,
  Search,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  User,
  MapPin,
  X,
  Filter,
  ShieldAlert,
} from "lucide-react"

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("solnex_user") || "null")
  } catch {
    return null
  }
}

const DAYS_OF_WEEK = [
  "Monday",
  "Tuesday",
  "Wednesday",
  "Thursday",
  "Friday",
  "Saturday",
  "Sunday",
]

export default function BookingSlots() {
  const [slots, setSlots] = useState([])
  const [stations, setStations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [toast, setToast] = useState(null) // { type: 'success' | 'error', message: string }

  // Search and Filter states
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState("All")
  const [dayFilter, setDayFilter] = useState("All")
  const [stationFilter, setStationFilter] = useState("All")

  // Create Modal state
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [createForm, setCreateForm] = useState({
    stationId: "",
    dayOfWeek: "Wednesday",
    startTime: "08:30",
    endTime: "09:30",
  })
  const [createLoading, setCreateLoading] = useState(false)
  const [createError, setCreateError] = useState(null)

  // Delete Confirmation Modal state
  const [slotToDelete, setSlotToDelete] = useState(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  const currentUser = getStoredUser()
  const isBackoffice = currentUser?.role === "Backoffice"

  const showToastNotification = (type, message) => {
    setToast({ type, message })
    setTimeout(() => setToast(null), 5000)
  }

  const fetchSlotsAndStations = async () => {
    try {
      setLoading(true)
      setError(null)
      const [slotsData, stationsData] = await Promise.all([
        slotApi.getAllSlots().catch(err => {
          console.error("Failed to load slots:", err)
          return []
        }),
        stationApi.getStations().catch(err => {
          console.error("Failed to load stations:", err)
          return []
        }),
      ])
      
      setSlots(Array.isArray(slotsData) ? slotsData : [])
      setStations(Array.isArray(stationsData) ? stationsData : [])
      
      if (stationsData.length > 0 && !createForm.stationId) {
        setCreateForm(prev => ({ ...prev, stationId: stationsData[0].stationId }))
      }
    } catch (err) {
      setError(err.message || "Failed to load slots data")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSlotsAndStations()
  }, [])

  // Create slot handler
  const handleCreateSlot = async (e) => {
    e.preventDefault()
    setCreateError(null)

    if (!createForm.stationId.trim()) {
      setCreateError("Please select or enter a Station ID.")
      return
    }
    if (!createForm.startTime || !createForm.endTime) {
      setCreateError("Please specify both Start Time and End Time.")
      return
    }

    try {
      setCreateLoading(true)
      const createdSlot = await slotApi.createSlot({
        stationId: createForm.stationId.trim(),
        dayOfWeek: createForm.dayOfWeek,
        startTime: createForm.startTime,
        endTime: createForm.endTime,
      })

      showToastNotification("success", `Booking slot '${createdSlot.slotId || createdSlot.id}' successfully created!`)
      setShowCreateModal(false)
      // Reset times
      setCreateForm(prev => ({
        ...prev,
        startTime: "08:30",
        endTime: "09:30",
      }))
      fetchSlotsAndStations()
    } catch (err) {
      setCreateError(err.message || "Failed to create slot.")
    } finally {
      setCreateLoading(false)
    }
  }

  // Delete slot handler
  const handleDeleteSlot = async () => {
    if (!slotToDelete) return
    const targetSlotId = slotToDelete.id || slotToDelete.slotId

    try {
      setDeleteLoading(true)
      await slotApi.deleteSlot(targetSlotId)
      showToastNotification("success", `Slot '${slotToDelete.slotId || targetSlotId}' was successfully deleted.`)
      setSlotToDelete(null)
      fetchSlotsAndStations()
    } catch (err) {
      showToastNotification("error", err.message || `Failed to delete slot '${slotToDelete.slotId || targetSlotId}'.`)
      setSlotToDelete(null)
    } finally {
      setDeleteLoading(false)
    }
  }

  // Filtered Slots
  const filteredSlots = slots.filter((slot) => {
    const q = searchQuery.toLowerCase().trim()
    const matchesSearch =
      !q ||
      (slot.slotId && slot.slotId.toLowerCase().includes(q)) ||
      (slot.stationId && slot.stationId.toLowerCase().includes(q)) ||
      (slot.creatorName && slot.creatorName.toLowerCase().includes(q)) ||
      (slot.createdBy && slot.createdBy.toLowerCase().includes(q)) ||
      (slot.dayOfWeek && slot.dayOfWeek.toLowerCase().includes(q))

    const matchesStatus =
      statusFilter === "All" ||
      (slot.slotStatus && slot.slotStatus.toLowerCase() === statusFilter.toLowerCase())

    const matchesDay =
      dayFilter === "All" ||
      (slot.dayOfWeek && slot.dayOfWeek.toLowerCase() === dayFilter.toLowerCase())

    const matchesStation =
      stationFilter === "All" ||
      (slot.stationId && slot.stationId === stationFilter)

    return matchesSearch && matchesStatus && matchesDay && matchesStation
  })

  // Compute stats
  const totalSlotsCount = slots.length
  const availableSlotsCount = slots.filter((s) => s.slotStatus === "Available").length
  const reservedSlotsCount = slots.filter((s) => s.slotStatus === "Reserved").length
  const uniqueStationsCount = new Set(slots.map((s) => s.stationId)).size

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Toast Notification Banner */}
      {toast && (
        <div
          className={`fixed top-4 right-4 z-50 flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg border text-sm animate-in slide-in-from-top-2 duration-300 ${
            toast.type === "success"
              ? "bg-emerald-50 border-emerald-200 text-emerald-800"
              : "bg-red-50 border-red-200 text-red-800"
          }`}
        >
          {toast.type === "success" ? (
            <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0" />
          ) : (
            <AlertCircle className="h-5 w-5 text-red-600 shrink-0" />
          )}
          <span className="font-medium">{toast.message}</span>
          <button
            onClick={() => setToast(null)}
            className="ml-auto text-muted-foreground hover:text-foreground"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Booking Slots</h1>
          <p className="text-muted-foreground mt-1">
            Manage station operating schedules and energy reservation slots.
          </p>
        </div>
        {isBackoffice && (
          <Button
            onClick={() => {
              setCreateError(null)
              setShowCreateModal(true)
            }}
            className="flex items-center gap-2"
          >
            <Plus className="h-4 w-4" /> Create Slot
          </Button>
        )}
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Slots</CardTitle>
            <Calendar className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalSlotsCount}</div>
            <p className="text-xs text-muted-foreground mt-1">Across all stations</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Available</CardTitle>
            <CheckCircle2 className="h-4 w-4 text-emerald-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-emerald-600">{availableSlotsCount}</div>
            <p className="text-xs text-muted-foreground mt-1">Ready for reservation</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Reserved</CardTitle>
            <Clock className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-amber-600">{reservedSlotsCount}</div>
            <p className="text-xs text-muted-foreground mt-1">Currently booked</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Stations</CardTitle>
            <MapPin className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">{uniqueStationsCount}</div>
            <p className="text-xs text-muted-foreground mt-1">Configured with slots</p>
          </CardContent>
        </Card>
      </div>

      {/* Main Table Card */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle>Slot Directory</CardTitle>
          <CardDescription>
            Filter and view all energy booking slots created for microgrid stations.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Controls & Filter Bar */}
          <div className="flex flex-col lg:flex-row gap-4 mb-6 justify-between">
            <div className="flex flex-col sm:flex-row gap-3 flex-1">
              {/* Search Bar */}
              <div className="relative flex-1 max-w-md">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search by Slot ID, Station, Day, Creator..."
                  className="pl-8"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>

              {/* Day Filter */}
              <select
                className="h-10 px-3 py-2 rounded-md border border-input bg-background text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring"
                value={dayFilter}
                onChange={(e) => setDayFilter(e.target.value)}
              >
                <option value="All">All Days</option>
                {DAYS_OF_WEEK.map((day) => (
                  <option key={day} value={day}>
                    {day}
                  </option>
                ))}
              </select>

              {/* Station Filter */}
              {stations.length > 0 && (
                <select
                  className="h-10 px-3 py-2 rounded-md border border-input bg-background text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring"
                  value={stationFilter}
                  onChange={(e) => setStationFilter(e.target.value)}
                >
                  <option value="All">All Stations</option>
                  {stations.map((st) => (
                    <option key={st.stationId} value={st.stationId}>
                      {st.stationName ? `${st.stationName} (${st.stationId})` : st.stationId}
                    </option>
                  ))}
                </select>
              )}
            </div>

            {/* Status Pills & Refresh */}
            <div className="flex items-center gap-2">
              <div className="inline-flex items-center rounded-md border p-1 bg-muted/50">
                {["All", "Available", "Reserved"].map((status) => (
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

              <Button
                variant="outline"
                size="icon"
                onClick={fetchSlotsAndStations}
                disabled={loading}
                title="Refresh Slots"
              >
                <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
              </Button>
            </div>
          </div>

          {/* Error display */}
          {error ? (
            <div className="text-center py-8 text-destructive flex flex-col items-center gap-2">
              <AlertCircle className="h-8 w-8 text-destructive" />
              <span>{error}</span>
              <Button variant="outline" size="sm" onClick={fetchSlotsAndStations} className="mt-2">
                Retry Loading
              </Button>
            </div>
          ) : loading ? (
            <div className="space-y-2">
              {[1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-14 w-full" />
              ))}
            </div>
          ) : filteredSlots.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground border border-dashed rounded-lg flex flex-col items-center justify-center gap-2">
              <Filter className="h-8 w-8 text-muted-foreground/50" />
              <p className="font-medium">No booking slots found matching your filters.</p>
              {searchQuery || statusFilter !== "All" || dayFilter !== "All" || stationFilter !== "All" ? (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    setSearchQuery("")
                    setStatusFilter("All")
                    setDayFilter("All")
                    setStationFilter("All")
                  }}
                  className="text-primary mt-1"
                >
                  Clear all filters
                </Button>
              ) : null}
            </div>
          ) : (
            <div className="rounded-md border overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Slot ID</TableHead>
                    <TableHead>Station ID</TableHead>
                    <TableHead>Day</TableHead>
                    <TableHead>Operating Hours</TableHead>
                    <TableHead>Slot Time</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created By</TableHead>
                    {isBackoffice && <TableHead className="text-right">Actions</TableHead>}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredSlots.map((slot) => {
                    const isReserved = slot.slotStatus === "Reserved"
                    const createdDateFormatted = slot.createdAt
                      ? new Date(slot.createdAt).toLocaleDateString()
                      : null

                    return (
                      <TableRow key={slot.id || slot.slotId}>
                        <TableCell>
                          <div className="font-medium font-mono text-sm">{slot.slotId}</div>
                        </TableCell>

                        <TableCell>
                          <Badge variant="outline" className="font-mono">
                            {slot.stationId}
                          </Badge>
                        </TableCell>

                        <TableCell>
                          <span className="font-medium text-sm">{slot.dayOfWeek || "—"}</span>
                        </TableCell>

                        <TableCell>
                          <span className="text-xs text-muted-foreground">
                            {slot.scheduleTime || "07:00 - 19:00"}
                          </span>
                        </TableCell>

                        <TableCell>
                          <div className="flex items-center gap-1.5 font-semibold text-sm">
                            <Clock className="h-3.5 w-3.5 text-primary" />
                            {slot.startTime} - {slot.endTime}
                          </div>
                        </TableCell>

                        <TableCell>
                          <Badge
                            variant={isReserved ? "secondary" : "default"}
                            className={
                              isReserved
                                ? "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400 border-amber-200"
                                : "bg-emerald-500 hover:bg-emerald-600 text-white"
                            }
                          >
                            {slot.slotStatus}
                          </Badge>
                        </TableCell>

                        <TableCell>
                          {slot.creatorName || slot.createdBy ? (
                            <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                              <User className="h-3.5 w-3.5 text-slate-400" />
                              <span>{slot.creatorName || slot.createdBy}</span>
                              {createdDateFormatted && (
                                <span className="text-muted-foreground/60">• {createdDateFormatted}</span>
                              )}
                            </div>
                          ) : (
                            <span className="text-xs text-muted-foreground">—</span>
                          )}
                        </TableCell>

                        {isBackoffice && (
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="sm"
                              disabled={isReserved}
                              title={
                                isReserved
                                  ? "Reserved slots cannot be deleted"
                                  : "Delete slot"
                              }
                              onClick={() => setSlotToDelete(slot)}
                              className={
                                isReserved
                                  ? "opacity-40 cursor-not-allowed"
                                  : "text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/20"
                              }
                            >
                              <Trash2 className="h-4 w-4 mr-1" />
                              Delete
                            </Button>
                          </TableCell>
                        )}
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* CREATE SLOT MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 animate-in fade-in duration-200">
          <div className="bg-card border border-border rounded-xl shadow-2xl max-w-lg w-full p-6 space-y-5 animate-in zoom-in-95 duration-200">
            <div className="flex justify-between items-center border-b pb-3">
              <div className="flex items-center gap-2">
                <Plus className="h-5 w-5 text-primary" />
                <h2 className="text-xl font-bold">Create Booking Slot</h2>
              </div>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-muted-foreground hover:text-foreground rounded-md p-1"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {createError && (
              <div className="p-3 bg-red-50 border border-red-200 text-red-700 rounded-md text-sm flex items-start gap-2">
                <AlertCircle className="h-4 w-4 text-red-600 shrink-0 mt-0.5" />
                <span>{createError}</span>
              </div>
            )}

            <form onSubmit={handleCreateSlot} className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">Station ID</label>
                {stations.length > 0 ? (
                  <select
                    className="w-full h-10 px-3 py-2 rounded-md border border-input bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={createForm.stationId}
                    onChange={(e) => setCreateForm({ ...createForm, stationId: e.target.value })}
                    required
                  >
                    {stations.map((st) => (
                      <option key={st.stationId} value={st.stationId}>
                        {st.stationName} ({st.stationId})
                      </option>
                    ))}
                  </select>
                ) : (
                  <Input
                    type="text"
                    placeholder="e.g. ST001"
                    value={createForm.stationId}
                    onChange={(e) => setCreateForm({ ...createForm, stationId: e.target.value })}
                    required
                  />
                )}
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Day of Week</label>
                <select
                  className="w-full h-10 px-3 py-2 rounded-md border border-input bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  value={createForm.dayOfWeek}
                  onChange={(e) => setCreateForm({ ...createForm, dayOfWeek: e.target.value })}
                  required
                >
                  {DAYS_OF_WEEK.map((day) => (
                    <option key={day} value={day}>
                      {day}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Start Time</label>
                  <Input
                    type="text"
                    placeholder="e.g. 8:30 or 08:30"
                    value={createForm.startTime}
                    onChange={(e) => setCreateForm({ ...createForm, startTime: e.target.value })}
                    required
                  />
                  <span className="text-[11px] text-muted-foreground mt-0.5 block">Format: HH:mm</span>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">End Time</label>
                  <Input
                    type="text"
                    placeholder="e.g. 9:30 or 09:30"
                    value={createForm.endTime}
                    onChange={(e) => setCreateForm({ ...createForm, endTime: e.target.value })}
                    required
                  />
                  <span className="text-[11px] text-muted-foreground mt-0.5 block">Format: HH:mm</span>
                </div>
              </div>

              <div className="pt-3 flex justify-end gap-3 border-t">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowCreateModal(false)}
                  disabled={createLoading}
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={createLoading}>
                  {createLoading ? (
                    <>
                      <RefreshCw className="h-4 w-4 mr-2 animate-spin" /> Creating...
                    </>
                  ) : (
                    "Create Slot"
                  )}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {slotToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 animate-in fade-in duration-200">
          <div className="bg-card border border-border rounded-xl shadow-2xl max-w-md w-full p-6 space-y-4 animate-in zoom-in-95 duration-200">
            <div className="flex items-center gap-3 text-red-600">
              <div className="p-2 rounded-full bg-red-100 dark:bg-red-950/40">
                <ShieldAlert className="h-6 w-6" />
              </div>
              <h2 className="text-lg font-bold text-foreground">Confirm Slot Deletion</h2>
            </div>

            <p className="text-sm text-muted-foreground">
              Are you sure you want to delete slot{" "}
              <strong className="text-foreground font-mono">{slotToDelete.slotId || slotToDelete.id}</strong>?
            </p>

            <div className="p-3 bg-muted/60 rounded-lg text-xs space-y-1">
              <div><strong>Station:</strong> {slotToDelete.stationId}</div>
              <div><strong>Day:</strong> {slotToDelete.dayOfWeek}</div>
              <div><strong>Time:</strong> {slotToDelete.startTime} - {slotToDelete.endTime}</div>
              <div><strong>Status:</strong> {slotToDelete.slotStatus}</div>
            </div>

            <div className="pt-2 flex justify-end gap-3 border-t">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setSlotToDelete(null)}
                disabled={deleteLoading}
              >
                Cancel
              </Button>
              <Button
                variant="destructive"
                size="sm"
                onClick={handleDeleteSlot}
                disabled={deleteLoading}
              >
                {deleteLoading ? (
                  <>
                    <RefreshCw className="h-3.5 w-3.5 mr-2 animate-spin" /> Deleting...
                  </>
                ) : (
                  "Delete Slot"
                )}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
