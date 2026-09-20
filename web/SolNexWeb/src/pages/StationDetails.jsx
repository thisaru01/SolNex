import { useEffect, useState } from "react"
import { useParams, useNavigate, Link } from "react-router-dom"
import { stationApi } from "../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Separator } from "@/components/ui/separator"
import { ArrowLeft, Edit, Clock, MapPin, BatteryCharging, Zap, Power, PowerOff } from "lucide-react"

export default function StationDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [station, setStation] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  
  const fetchStation = async () => {
    try {
      setLoading(true)
      const data = await stationApi.getStationById(id)
      setStation(data)
    } catch (err) {
      setError(err.message || "Failed to load station")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchStation()
  }, [id])

  const handleActivate = async () => {
    try {
      await stationApi.activateStation(id)
      fetchStation()
    } catch (err) {
      alert("Failed to activate: " + err.message)
    }
  }

  const handleDeactivate = async () => {
    try {
      await stationApi.deactivateStation(id)
      fetchStation()
    } catch (err) {
      alert("Failed to deactivate: " + err.message)
    }
  }

  if (loading) return <div className="p-8"><Skeleton className="h-96 w-full" /></div>
  if (error) return <div className="p-8 text-destructive">{error}</div>
  if (!station) return <div className="p-8 text-muted-foreground">Station not found.</div>

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate("/stations")}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">{station.stationName}</h1>
            <p className="text-muted-foreground mt-1 flex items-center gap-2">
              <Badge variant="outline">{station.stationId}</Badge>
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          {import.meta.env.VITE_USER_ROLE === "Backoffice" && (
            <>
              <Button variant="outline" onClick={() => navigate(`/stations/${id}/edit`)}>
                <Edit className="h-4 w-4 mr-2" /> Edit
              </Button>
              <Button variant="outline" onClick={() => navigate(`/stations/${id}/schedule`)}>
                <Clock className="h-4 w-4 mr-2" /> Schedule
              </Button>
              {station.status === "Active" ? (
                <Button variant="destructive" onClick={handleDeactivate}>
                  <PowerOff className="h-4 w-4 mr-2" /> Deactivate
                </Button>
              ) : (
                <Button variant="default" className="bg-emerald-600 hover:bg-emerald-700" onClick={handleActivate}>
                  <Power className="h-4 w-4 mr-2" /> Activate
                </Button>
              )}
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Status & Capacity</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-muted-foreground flex items-center"><Zap className="h-4 w-4 mr-2"/> Status</span>
              <Badge variant={station.status === "Active" ? "default" : "secondary"} className={station.status === "Active" ? "bg-emerald-500" : ""}>
                {station.status}
              </Badge>
            </div>
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-muted-foreground flex items-center"><Zap className="h-4 w-4 mr-2"/> Capacity</span>
              <span className="font-medium">{station.capacityKw} kW</span>
            </div>
            <div className="flex justify-between items-center py-2 border-b">
              <span className="text-muted-foreground flex items-center"><MapPin className="h-4 w-4 mr-2"/> Location</span>
              <span className="font-medium">{station.latitude}, {station.longitude}</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-muted-foreground flex items-center"><Clock className="h-4 w-4 mr-2"/> Created</span>
              <span className="font-medium">{new Date(station.createdAt).toLocaleDateString()}</span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Battery Storage</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between mb-2">
              <span className="text-muted-foreground">Available Slots</span>
              <span className="font-bold text-xl">{station.availableBatterySlots} <span className="text-muted-foreground text-sm font-normal">/ {station.totalBatterySlots}</span></span>
            </div>
            <div className="w-full bg-secondary h-4 rounded-full overflow-hidden">
              <div 
                className="bg-blue-500 h-full transition-all" 
                style={{ width: `${(station.availableBatterySlots / station.totalBatterySlots) * 100}%` }}
              ></div>
            </div>
            
            <div className="pt-6">
              <Button className="w-full" variant="secondary" onClick={() => navigate(`/stations/${id}/availability`)}>
                View Availability Dashboard
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">Operating Hours</CardTitle>
          </CardHeader>
          <CardContent>
            {station.schedule && Object.keys(station.schedule).length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {Object.entries(station.schedule).map(([day, hours]) => (
                  <div key={day} className="flex flex-col p-3 border rounded-lg bg-muted/20">
                    <span className="font-medium">{day}</span>
                    <span className="text-muted-foreground text-sm mt-1">{hours || "Closed"}</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-6 text-muted-foreground">
                No schedule defined. 
                {import.meta.env.VITE_USER_ROLE === "Backoffice" && (
                  <> <Link to={`/stations/${id}/schedule`} className="text-primary hover:underline">Set schedule</Link></>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
