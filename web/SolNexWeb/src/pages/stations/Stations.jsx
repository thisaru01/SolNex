import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
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
import { BatteryCharging, Search, RefreshCw, Power, PowerOff } from "lucide-react"

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("solnex_user") || "null")
  } catch {
    return null
  }
}

export default function Stations() {
  const navigate = useNavigate()
  const [stations, setStations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState("All")
  const currentUser = getStoredUser()
  const isBackoffice = currentUser?.role === "Backoffice"

  const fetchStations = async () => {
    try {
      setLoading(true)
      const data = await stationApi.getStations()
      setStations(data)
      setError(null)
    } catch (err) {
      setError(err.message || "Failed to load stations")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchStations()
  }, [])

  const handleActivate = async (id) => {
    try {
      await stationApi.activateStation(id)
      fetchStations()
    } catch (err) {
      alert("Failed to activate: " + err.message)
    }
  }

  const handleDeactivate = async (id) => {
    try {
      await stationApi.deactivateStation(id)
      fetchStations()
    } catch (err) {
      alert("Failed to deactivate: " + err.message)
    }
  }

  // Filter logic
  const filteredStations = stations.filter(station => {
    const matchesSearch = 
      station.stationId.toLowerCase().includes(searchQuery.toLowerCase()) || 
      station.stationName.toLowerCase().includes(searchQuery.toLowerCase())
    
    const matchesStatus = statusFilter === "All" || station.status === statusFilter
    
    return matchesSearch && matchesStatus
  })

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Stations Management</h1>
          <p className="text-muted-foreground mt-1">Manage and monitor all solar microgrid stations.</p>
        </div>
        {isBackoffice && (
          <Button onClick={() => navigate("/stations/create")}>
            Create Station
          </Button>
        )}
      </div>

      <Card>
        <CardHeader className="pb-3">
          <CardTitle>Station Directory</CardTitle>
          <CardDescription>View all registered stations in the microgrid network.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="search"
                placeholder="Search by ID or Name..."
                className="pl-8"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="flex gap-2">
              <div className="inline-flex items-center rounded-md border p-1 bg-muted/50">
                {["All", "Active", "Inactive"].map(status => (
                  <Button
                    key={status}
                    variant={statusFilter === status ? "secondary" : "ghost"}
                    size="sm"
                    className="h-7"
                    onClick={() => setStatusFilter(status)}
                  >
                    {status}
                  </Button>
                ))}
              </div>
              <Button variant="outline" size="icon" onClick={fetchStations} disabled={loading} title="Refresh">
                <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
              </Button>
            </div>
          </div>

          {error ? (
            <div className="text-center py-8 text-destructive">{error}</div>
          ) : loading ? (
            <div className="space-y-2">
              {[1, 2, 3, 4, 5].map(i => <Skeleton key={i} className="h-16 w-full" />)}
            </div>
          ) : filteredStations.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground border border-dashed rounded-lg">
              No stations found matching your criteria.
            </div>
          ) : (
            <div className="rounded-md border overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Station</TableHead>
                    <TableHead>Location</TableHead>
                    <TableHead>Capacity</TableHead>
                    <TableHead>Slots</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredStations.map((station) => (
                    <TableRow key={station.id}>
                      <TableCell>
                        <div className="font-medium">{station.stationName}</div>
                        <div className="text-xs text-muted-foreground">{station.stationId}</div>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm">{station.latitude.toFixed(4)}, {station.longitude.toFixed(4)}</div>
                      </TableCell>
                      <TableCell>{station.capacityKw} kW</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1.5">
                          <BatteryCharging className="h-4 w-4 text-blue-500" />
                          <span className="font-medium">{station.availableBatterySlots}</span>
                          <span className="text-muted-foreground text-xs">/ {station.totalBatterySlots}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant={station.status === "Active" ? "default" : "secondary"}
                               className={station.status === "Active" ? "bg-emerald-500 hover:bg-emerald-600" : ""}>
                          {station.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button variant="ghost" size="sm" onClick={() => navigate(`/stations/${station.id}`)}>View</Button>
                          
                          {isBackoffice && (
                            <>
                              <Button variant="outline" size="sm" onClick={() => navigate(`/stations/${station.id}/edit`)}>Edit</Button>
                              {station.status === "Active" ? (
                                <Button variant="secondary" size="sm" onClick={() => handleDeactivate(station.id)} className="text-destructive hover:text-destructive">
                                  <PowerOff className="h-3.5 w-3.5 mr-1" /> Deactivate
                                </Button>
                              ) : (
                                <Button variant="outline" size="sm" onClick={() => handleActivate(station.id)} className="text-emerald-600 border-emerald-200 hover:bg-emerald-50">
                                  <Power className="h-3.5 w-3.5 mr-1" /> Activate
                                </Button>
                              )}
                            </>
                          )}
                        </div>
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
