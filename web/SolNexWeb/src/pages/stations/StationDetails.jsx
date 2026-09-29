import { useEffect, useState } from "react"
import { useParams, useNavigate, Link } from "react-router-dom"
import { stationApi } from "../../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Separator } from "@/components/ui/separator"
import { ArrowLeft, Edit, Clock, MapPin, BatteryCharging, Zap, Power, PowerOff, Trash2 } from "lucide-react"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("solnex_user") || "null")
  } catch {
    return null
  }
}

export default function StationDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [station, setStation] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [deactivateAlertOpen, setDeactivateAlertOpen] = useState(false)
  const [activateAlertOpen, setActivateAlertOpen] = useState(false)
  const [deleteAlertOpen, setDeleteAlertOpen] = useState(false)
  
  const currentUser = getStoredUser()
  const isBackoffice = currentUser?.role === "Backoffice"
  
  const fetchStation = async () => {
    try {
      setLoading(true)
      const [stationData, availData] = await Promise.all([
        stationApi.getStationById(id),
        stationApi.getStationAvailability(id).catch(() => null)
      ])
      if (availData?.availableBatterySlots !== undefined && availData?.availableBatterySlots !== null) {
        stationData.availableBatterySlots = availData.availableBatterySlots
      }
      setStation(stationData)
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

  const handleDelete = async () => {
    try {
      await stationApi.deleteStation(id)
      navigate("/stations")
    } catch (err) {
      alert("Failed to delete: " + err.message)
    }
  }

  if (loading) return <div className="p-8"><Skeleton className="h-96 w-full" /></div>
  if (error) return <div className="p-8 text-destructive">{error}</div>
  if (!station) return <div className="p-8 text-muted-foreground">Station not found.</div>

  const totalBatterySlots = station.totalBatterySlots || 0
  const availableBatterySlots = station.availableBatterySlots ?? 0
  const availabilityPercent = totalBatterySlots > 0 ? Math.round((availableBatterySlots / totalBatterySlots) * 100) : 0
  const isOperating = station.status === "Active"

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate("/stations")}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">{station.stationName}</h1>
            <p className="text-muted-foreground mt-1 flex items-center gap-2">
              <Badge variant="outline">{station.stationId}</Badge>
            </p>
          </div>
        </div>
        <div className="flex flex-wrap gap-2 w-full sm:w-auto">
          {isBackoffice && (
            <>
              <Button variant="outline" className="flex-1 sm:flex-none" onClick={() => navigate(`/stations/${id}/edit`)}>
                <Edit className="h-4 w-4 mr-2" /> Edit
              </Button>
              <Button variant="outline" className="flex-1 sm:flex-none" onClick={() => navigate(`/stations/${id}/schedule`)}>
                <Clock className="h-4 w-4 mr-2" /> Schedule
              </Button>
              {station.status === "Active" ? (
                <Button variant="destructive" className="flex-1 sm:flex-none" onClick={() => setDeactivateAlertOpen(true)}>
                  <PowerOff className="h-4 w-4 mr-2" /> Deactivate
                </Button>
              ) : (
                <>
                  <Button variant="default" className="flex-1 sm:flex-none bg-emerald-600 hover:bg-emerald-700" onClick={() => setActivateAlertOpen(true)}>
                    <Power className="h-4 w-4 mr-2" /> Activate
                  </Button>
                  <Button variant="destructive" className="flex-1 sm:flex-none" onClick={() => setDeleteAlertOpen(true)}>
                    <Trash2 className="h-4 w-4 mr-2" /> Delete
                  </Button>
                </>
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

        <Card className="flex flex-col justify-between">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg">Battery Storage</CardTitle>
              <Badge variant="outline" className="bg-blue-50 text-blue-700 dark:bg-blue-950 dark:text-blue-300 border-blue-200">
                {availabilityPercent}% Available
              </Badge>
            </div>
            <CardDescription>Live operational and battery availability metrics.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-muted-foreground">Available Slots</span>
                <span className="font-bold text-xl">
                  {availableBatterySlots} <span className="text-muted-foreground text-sm font-normal">/ {totalBatterySlots}</span>
                </span>
              </div>
              <div className="w-full bg-secondary h-3.5 rounded-full overflow-hidden">
                <div 
                  className="bg-blue-500 h-full rounded-full transition-all duration-300" 
                  style={{ width: `${availabilityPercent}%` }}
                ></div>
              </div>
            </div>

            <div className="space-y-2.5 pt-1">
              <div className="flex items-center justify-between p-3 border rounded-lg bg-muted/10">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-full bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-300">
                    <BatteryCharging className="h-4 w-4" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">Battery Slots</p>
                    <p className="text-sm font-semibold">{availableBatterySlots} / {totalBatterySlots} Available</p>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-base font-bold text-blue-600 dark:text-blue-400">
                    {availabilityPercent}%
                  </span>
                </div>
              </div>

              <div className="flex items-center justify-between p-3 border rounded-lg bg-muted/10">
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-full bg-orange-100 dark:bg-orange-900/50 text-orange-600 dark:text-orange-300">
                    <Clock className="h-4 w-4" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">Operating Status</p>
                    <p className="text-sm font-semibold">{isOperating ? "Open" : "Closed"}</p>
                  </div>
                </div>
                <div className="text-right text-xs text-muted-foreground font-medium">
                  {station.capacityKw} kW Capacity
                </div>
              </div>
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
                {isBackoffice && (
                  <> <Link to={`/stations/${id}/schedule`} className="text-primary hover:underline">Set schedule</Link></>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <AlertDialog open={deactivateAlertOpen} onOpenChange={setDeactivateAlertOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Deactivate Station?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to deactivate {station.stationName}? This will prevent any further interactions until it is reactivated.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={() => { setDeactivateAlertOpen(false); handleDeactivate(); }} className="bg-destructive hover:bg-destructive/90 text-destructive-foreground">Deactivate</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={activateAlertOpen} onOpenChange={setActivateAlertOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Activate Station?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to activate {station.stationName}? It will become available to the network.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={() => { setActivateAlertOpen(false); handleActivate(); }} className="bg-emerald-600 hover:bg-emerald-700 text-white">Activate</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={deleteAlertOpen} onOpenChange={setDeleteAlertOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Station?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to permanently delete {station.stationName}? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={() => { setDeleteAlertOpen(false); handleDelete(); }} className="bg-destructive hover:bg-destructive/90 text-destructive-foreground">Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
