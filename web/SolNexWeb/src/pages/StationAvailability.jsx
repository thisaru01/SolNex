import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { stationApi } from "../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { ArrowLeft, Zap, BatteryCharging, Clock } from "lucide-react"

export default function StationAvailability() {
  const { id } = useParams()
  const navigate = useNavigate()
  
  const [station, setStation] = useState(null)
  const [availability, setAvailability] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [stationData, availData] = await Promise.all([
          stationApi.getStationById(id),
          stationApi.getStationAvailability(id)
        ])
        setStation(stationData)
        setAvailability(availData?.availableBatterySlots ?? stationData.availableBatterySlots)
      } catch (err) {
        setError(err.message || "Failed to load availability data")
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  if (loading) return <div className="p-8"><Skeleton className="h-64 w-full max-w-xl mx-auto" /></div>
  if (error) return <div className="p-8 text-destructive">{error}</div>
  if (!station) return <div className="p-8 text-muted-foreground">Station not found.</div>

  // Quick check for operating status (mocked for now, assumes Open if Active, could be enhanced)
  const isOperating = station.status === "Active"

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/stations/${id}`)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Station Availability</h1>
          <p className="text-muted-foreground mt-1">{station.stationName}</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Live Status</CardTitle>
          <CardDescription>Current operational and battery availability metrics.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center justify-between p-4 border rounded-lg bg-muted/10">
            <div className="flex items-center gap-3">
              <div className={`p-2 rounded-full ${station.status === 'Active' ? 'bg-emerald-100 text-emerald-600' : 'bg-destructive/10 text-destructive'}`}>
                <Zap className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Station Status</p>
                <p className="font-semibold">{station.status}</p>
              </div>
            </div>
            <Badge variant={station.status === "Active" ? "default" : "destructive"} className={station.status === "Active" ? "bg-emerald-500" : ""}>
              {station.status === "Active" ? "🟢 Active" : "🔴 Inactive"}
            </Badge>
          </div>

          <div className="flex items-center justify-between p-4 border rounded-lg bg-muted/10">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-blue-100 text-blue-600">
                <BatteryCharging className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Battery Slots</p>
                <p className="font-semibold">{availability} / {station.totalBatterySlots} Available</p>
              </div>
            </div>
            <div className="text-right">
              <div className="text-2xl font-bold text-blue-600">
                {Math.round((availability / station.totalBatterySlots) * 100) || 0}%
              </div>
            </div>
          </div>

          <div className="flex items-center justify-between p-4 border rounded-lg bg-muted/10">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-orange-100 text-orange-600">
                <Clock className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Operating Status</p>
                <p className="font-semibold">{isOperating ? "Open" : "Closed"}</p>
              </div>
            </div>
            <div className="text-right text-sm text-muted-foreground">
              {station.capacityKw} kW Capacity
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
