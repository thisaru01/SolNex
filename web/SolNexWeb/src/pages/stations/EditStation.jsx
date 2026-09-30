import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { stationApi } from "../../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Skeleton } from "@/components/ui/skeleton"
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

// Page component for editing an existing solar station details.
export default function EditStation() {
  const { id } = useParams()
  const navigate = useNavigate()
  
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [alertOpen, setAlertOpen] = useState(false)
  
  const [formData, setFormData] = useState({
    stationName: "",
    latitude: "",
    longitude: "",
    capacityKw: "",
    totalBatterySlots: "",
    availableBatterySlots: "",
  })

  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "", 
  })

  // Update latitude and longitude in form data when user clicks on map
  const handleMapClick = (e) => {
    setFormData(prev => ({
      ...prev,
      latitude: e.latLng.lat().toFixed(6),
      longitude: e.latLng.lng().toFixed(6)
    }))
  }

  // Trigger side effects like fetching initial station data on mount
  useEffect(() => {
    const fetchStation = async () => {
      try {
        // Call backend API to fetch the current station info using URL param ID
        const data = await stationApi.getStationById(id)
        
        // Populate the form state with the fetched station data to allow editing
        setFormData({
          stationName: data.stationName,
          latitude: data.latitude,
          longitude: data.longitude,
          capacityKw: data.capacityKw,
          totalBatterySlots: data.totalBatterySlots,
          availableBatterySlots: data.availableBatterySlots,
        })
      } catch (err) {
        // Set error message to display in UI if API call fails
        setError(err.message || "Failed to load station")
      } finally {
        // Clear loading state so the form or error is displayed
        setLoading(false)
      }
    }
    fetchStation()
  }, [id])

  // Handle input changes dynamically by name attribute
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  // Prevent default form submission and trigger confirmation dialog instead
  const handlePreSubmit = (e) => {
    e.preventDefault()
    setAlertOpen(true)
  }

  // Executes the actual update/create logic after user confirms action
  const handleSubmit = async () => {
    setAlertOpen(false)
    setSaving(true)
    
    try {
      const payload = {
        stationName: formData.stationName,
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        capacityKw: parseFloat(formData.capacityKw),
        totalBatterySlots: parseInt(formData.totalBatterySlots, 10),
        availableBatterySlots: parseInt(formData.availableBatterySlots, 10),
      }
      
      await stationApi.updateStation(id, payload)
      toast.success("Station updated successfully")
      navigate(`/stations/${id}`)
    } catch (err) {
      toast.error(err.message || "Failed to update station")
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="p-8"><Skeleton className="h-96 w-full" /></div>
  if (error && !saving) return <div className="p-8 text-destructive">{error}</div>

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/stations/${id}`)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Edit Station</h1>
          <p className="text-muted-foreground mt-1">Update station details and capacity limits.</p>
        </div>
      </div>

      <Card className="max-w-3xl mx-auto">
        <CardHeader>
          <CardTitle>Station Details</CardTitle>
          <CardDescription>Modify the properties below and save your changes.</CardDescription>
        </CardHeader>
        <CardContent>
          
          <form onSubmit={handlePreSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="stationName">Station Name</Label>
              <Input 
                id="stationName" 
                name="stationName" 
                required 
                value={formData.stationName} 
                onChange={handleChange} 
              />
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
                <Label htmlFor="latitude">Latitude</Label>
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
                <Label htmlFor="longitude">Longitude</Label>
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

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="capacityKw">Capacity (kW)</Label>
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
                <Label htmlFor="totalBatterySlots">Total Slots</Label>
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
              <div className="space-y-2">
                <Label htmlFor="availableBatterySlots">Available Slots</Label>
                <Input 
                  id="availableBatterySlots" 
                  name="availableBatterySlots" 
                  type="number" 
                  min="0"
                  max={formData.totalBatterySlots}
                  required 
                  value={formData.availableBatterySlots} 
                  onChange={handleChange} 
                />
              </div>
            </div>

            <div className="pt-4 flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate(`/stations/${id}`)}>Cancel</Button>
              <Button type="submit" disabled={saving}>
                {saving ? "Saving..." : "Save Changes"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
      
      <AlertDialog open={alertOpen} onOpenChange={setAlertOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Save Changes?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to update this station's configuration? 
              Changes to capacity or battery slots may affect existing reservations.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleSubmit}>Save Changes</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
