import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { stationApi } from "../../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ArrowLeft } from "lucide-react"
import { GoogleMap, useJsApiLoader, Marker } from "@react-google-maps/api"
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
import { toast } from "sonner"

const containerStyle = {
  width: '100%',
  height: '100%'
}
const defaultCenter = {
  lat: 7.8731, 
  lng: 80.7718
}

export default function CreateStation() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [alertOpen, setAlertOpen] = useState(false)
  
  const [formData, setFormData] = useState({
    stationName: "",
    latitude: "",
    longitude: "",
    capacityKw: "",
    totalBatterySlots: "",
  })

  const [nextStationId, setNextStationId] = useState("Loading...")

  useEffect(() => {
    const fetchId = async () => {
      try {
        const result = await stationApi.getNextStationId();
        if (result && result.nextId) {
          setNextStationId(result.nextId);
        } else {
          setNextStationId("Auto-generated");
        }
      } catch (err) {
        console.error("Failed to fetch next station ID:", err);
        setNextStationId("Auto-generated");
      }
    };
    fetchId();
  }, []);

  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "", 
  })

  const handleMapClick = (e) => {
    setFormData(prev => ({
      ...prev,
      latitude: e.latLng.lat().toFixed(6),
      longitude: e.latLng.lng().toFixed(6)
    }))
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handlePreSubmit = (e) => {
    e.preventDefault()
    setAlertOpen(true)
  }

  const handleSubmit = async () => {
    setAlertOpen(false)
    setLoading(true)
    
    try {
      // Convert numeric fields
      const payload = {
        ...formData,
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        capacityKw: parseFloat(formData.capacityKw),
        totalBatterySlots: parseInt(formData.totalBatterySlots, 10),
      }
      
      const newStation = await stationApi.createStation(payload)
      toast.success("Station created successfully")
      navigate(`/stations/${newStation.id || newStation.stationId}`)
    } catch (err) {
      toast.error(err.message || "Failed to create station")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate("/stations")}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Create New Station</h1>
          <p className="text-muted-foreground mt-1">Register a new microgrid station to the network.</p>
        </div>
      </div>

      <Card className="max-w-3xl mx-auto">
        <CardHeader>
          <CardTitle>Station Details</CardTitle>
          <CardDescription>Fill out the basic information for the new station.</CardDescription>
        </CardHeader>
        <CardContent>
          
          <form onSubmit={handlePreSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="stationId">Station ID</Label>
                <Input 
                  id="stationId" 
                  name="stationId" 
                  value={nextStationId} 
                  disabled 
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="stationName">Station Name <span className="text-destructive">*</span></Label>
                <Input 
                  id="stationName" 
                  name="stationName" 
                  placeholder="e.g. Kurunegala Hub" 
                  required 
                  value={formData.stationName} 
                  onChange={handleChange} 
                />
              </div>
            </div>

            <div className="space-y-3">
              <Label>Location Selection <span className="text-muted-foreground font-normal">(Click on the map or enter coordinates below)</span></Label>
              <div className="h-[300px] w-full rounded-md border overflow-hidden relative bg-muted/20">
                {!isLoaded ? (
                  <div className="w-full h-full bg-muted animate-pulse" />
                ) : (
                  <GoogleMap
                    mapContainerStyle={containerStyle}
                    center={
                      formData.latitude && formData.longitude 
                        ? { lat: parseFloat(formData.latitude), lng: parseFloat(formData.longitude) } 
                        : defaultCenter
                    }
                    zoom={7}
                    onClick={handleMapClick}
                    options={{ disableDefaultUI: false, zoomControl: true, streetViewControl: false }}
                  >
                    {formData.latitude && formData.longitude && (
                      <Marker 
                        position={{ lat: parseFloat(formData.latitude), lng: parseFloat(formData.longitude) }} 
                        icon={{ url: "http://maps.google.com/mapfiles/ms/icons/green-dot.png" }}
                      />
                    )}
                  </GoogleMap>
                )}
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="latitude">Latitude <span className="text-destructive">*</span></Label>
                <Input 
                  id="latitude" 
                  name="latitude" 
                  type="number" 
                  step="any" 
                  required 
                  value={formData.latitude} 
                  onChange={handleChange} 
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="longitude">Longitude <span className="text-destructive">*</span></Label>
                <Input 
                  id="longitude" 
                  name="longitude" 
                  type="number" 
                  step="any" 
                  required 
                  value={formData.longitude} 
                  onChange={handleChange} 
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="capacityKw">Capacity (kW) <span className="text-destructive">*</span></Label>
                <Input 
                  id="capacityKw" 
                  name="capacityKw" 
                  type="number" 
                  step="0.1" 
                  min="0"
                  required 
                  value={formData.capacityKw} 
                  onChange={handleChange} 
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="totalBatterySlots">Total Battery Slots <span className="text-destructive">*</span></Label>
                <Input 
                  id="totalBatterySlots" 
                  name="totalBatterySlots" 
                  type="number" 
                  min="0"
                  required 
                  value={formData.totalBatterySlots} 
                  onChange={handleChange} 
                />
              </div>
            </div>
            
            <div className="text-sm text-muted-foreground pt-2">
              Note: The new station will be created with an "Active" status and all its battery slots will be fully available by default.
            </div>

            <div className="pt-4 flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/stations")}>Cancel</Button>
              <Button type="submit" disabled={loading}>
                {loading ? "Creating..." : "Create Station"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
      
      <AlertDialog open={alertOpen} onOpenChange={setAlertOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Create Station?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to create this new microgrid station? 
              It will be available for reservations immediately.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleSubmit}>Create</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
