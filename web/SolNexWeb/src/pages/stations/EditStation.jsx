import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { stationApi } from "../../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Skeleton } from "@/components/ui/skeleton"
import { ArrowLeft } from "lucide-react"

export default function EditStation() {
  const { id } = useParams()
  const navigate = useNavigate()
  
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  
  const [formData, setFormData] = useState({
    stationName: "",
    latitude: "",
    longitude: "",
    capacityKw: "",
    totalBatterySlots: "",
    availableBatterySlots: "",
  })

  useEffect(() => {
    const fetchStation = async () => {
      try {
        const data = await stationApi.getStationById(id)
        setFormData({
          stationName: data.stationName,
          latitude: data.latitude,
          longitude: data.longitude,
          capacityKw: data.capacityKw,
          totalBatterySlots: data.totalBatterySlots,
          availableBatterySlots: data.availableBatterySlots,
        })
      } catch (err) {
        setError(err.message || "Failed to load station")
      } finally {
        setLoading(false)
      }
    }
    fetchStation()
  }, [id])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    
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
      navigate(`/stations/${id}`)
    } catch (err) {
      setError(err.message || "Failed to update station")
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="p-8"><Skeleton className="h-96 w-full" /></div>
  if (error && !saving) return <div className="p-8 text-destructive">{error}</div>

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/stations/${id}`)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Edit Station</h1>
          <p className="text-muted-foreground mt-1">Update station details and capacity limits.</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Station Details</CardTitle>
          <CardDescription>Modify the properties below and save your changes.</CardDescription>
        </CardHeader>
        <CardContent>
          {error && <div className="p-3 mb-4 text-sm text-destructive bg-destructive/10 rounded-md">{error}</div>}
          
          <form onSubmit={handleSubmit} className="space-y-4">
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

            <div className="grid grid-cols-2 gap-4">
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

            <div className="grid grid-cols-3 gap-4">
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
    </div>
  )
}
