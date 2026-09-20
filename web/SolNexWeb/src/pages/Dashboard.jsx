import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { stationApi } from "../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { BatteryCharging, Battery, Power, PowerOff, LayoutDashboard } from "lucide-react"

export default function Dashboard() {
  const [stations, setStations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchStations = async () => {
      try {
        setLoading(true)
        const data = await stationApi.getStations()
        setStations(data)
      } catch (err) {
        setError(err.message || "Failed to load stations")
      } finally {
        setLoading(false)
      }
    }

    fetchStations()
  }, [])

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64 space-y-4">
        <p className="text-destructive font-medium">Error: {error}</p>
        <Button onClick={() => window.location.reload()}>Retry</Button>
      </div>
    )
  }

  // Calculate metrics
  const totalStations = stations.length
  const activeStations = stations.filter(s => s.status === "Active").length
  const inactiveStations = totalStations - activeStations
  const totalSlots = stations.reduce((acc, s) => acc + (s.totalBatterySlots || 0), 0)
  const availableSlots = stations.reduce((acc, s) => acc + (s.availableBatterySlots || 0), 0)

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Microgrid Dashboard</h1>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate("/stations")}>
            View All Stations
          </Button>
          <Button disabled>
            Create Station
          </Button>
        </div>
      </div>

      {loading ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map(i => (
            <Card key={i}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-4 rounded-full" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-16 mt-2" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Stations</CardTitle>
              <LayoutDashboard className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{totalStations}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Active Stations</CardTitle>
              <Power className="h-4 w-4 text-emerald-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-emerald-600">{activeStations}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Inactive Stations</CardTitle>
              <PowerOff className="h-4 w-4 text-destructive" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-destructive">{inactiveStations}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Battery Availability</CardTitle>
              <Battery className="h-4 w-4 text-blue-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{availableSlots} / {totalSlots}</div>
              <p className="text-xs text-muted-foreground mt-1">Available Slots</p>
            </CardContent>
          </Card>
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Recent Stations</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="space-y-2">
              {[1, 2, 3].map(i => <Skeleton key={i} className="h-12 w-full" />)}
            </div>
          ) : stations.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No stations found. Create one to get started.
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Capacity</TableHead>
                    <TableHead>Availability</TableHead>
                    <TableHead className="text-right">Action</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {stations.slice(0, 5).map((station) => (
                    <TableRow key={station.id}>
                      <TableCell className="font-medium">{station.stationId}</TableCell>
                      <TableCell>{station.stationName}</TableCell>
                      <TableCell>
                        <Badge variant={station.status === "Active" ? "default" : "secondary"}
                               className={station.status === "Active" ? "bg-emerald-500 hover:bg-emerald-600" : ""}>
                          {station.status}
                        </Badge>
                      </TableCell>
                      <TableCell>{station.capacityKw} kW</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <BatteryCharging className="h-4 w-4 text-muted-foreground" />
                          <span>{station.availableBatterySlots} / {station.totalBatterySlots}</span>
                        </div>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm" onClick={() => navigate("/stations")}>
                          View
                        </Button>
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
