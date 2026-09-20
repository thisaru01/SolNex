import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { stationApi } from "../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ArrowLeft } from "lucide-react"

export default function CreateStation() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  
  const [formData, setFormData] = useState({
    stationId: "",
    stationName: "",
    latitude: "",
    longitude: "",
    capacityKw: "",
    totalBatterySlots: "",
  })

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    
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
      navigate(`/stations/${newStation.id || newStation.stationId}`)
    } catch (err) {
      setError(err.message || "Failed to create station")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate("/stations")}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Create New Station</h1>
          <p className="text-muted-foreground mt-1">Register a new microgrid station to the network.</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Station Details</CardTitle>
          <CardDescription>Fill out the basic information for the new station.</CardDescription>
        </CardHeader>
        <CardContent>
          {error && <div className="p-3 mb-4 text-sm text-destructive bg-destructive/10 rounded-md">{error}</div>}
          
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="stationId">Station ID <span className="text-destructive">*</span></Label>
                <Input 
                  id="stationId" 
                  name="stationId" 
                  placeholder="e.g. ST001" 
                  required 
                  value={formData.stationId} 
                  onChange={handleChange} 
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

            <div className="grid grid-cols-2 gap-4">
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

            <div className="grid grid-cols-2 gap-4">
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
    </div>
  )
}
