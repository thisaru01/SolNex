import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { stationApi } from "../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import { ArrowLeft } from "lucide-react"

const DAYS_OF_WEEK = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

export default function StationSchedule() {
  const { id } = useParams()
  const navigate = useNavigate()
  
  const [stationName, setStationName] = useState("")
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  
  const [schedule, setSchedule] = useState(
    DAYS_OF_WEEK.reduce((acc, day) => ({ ...acc, [day]: "" }), {})
  )

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [stationData, scheduleData] = await Promise.all([
          stationApi.getStationById(id),
          stationApi.getStationSchedule(id)
        ])
        
        setStationName(stationData.stationName)
        
        if (scheduleData) {
          setSchedule(prev => ({ ...prev, ...scheduleData }))
        }
      } catch (err) {
        setError(err.message || "Failed to load schedule")
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleScheduleChange = (day, value) => {
    setSchedule(prev => ({ ...prev, [day]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    
    try {
      await stationApi.updateStationSchedule(id, schedule)
      navigate(`/stations/${id}`)
    } catch (err) {
      setError(err.message || "Failed to update schedule")
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="p-8"><Skeleton className="h-96 w-full max-w-2xl mx-auto" /></div>
  
  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/stations/${id}`)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Station Schedule</h1>
          <p className="text-muted-foreground mt-1">{stationName}</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Operating Hours</CardTitle>
          <CardDescription>Enter time ranges like "08:00 - 18:00" or type "Closed".</CardDescription>
        </CardHeader>
        <CardContent>
          {error && <div className="p-3 mb-4 text-sm text-destructive bg-destructive/10 rounded-md">{error}</div>}
          
          <form onSubmit={handleSubmit} className="space-y-4">
            {DAYS_OF_WEEK.map((day) => (
              <div key={day} className="flex items-center gap-4">
                <div className="w-24 font-medium">{day}</div>
                <Input 
                  placeholder="e.g. 08:00 - 18:00" 
                  value={schedule[day]}
                  onChange={(e) => handleScheduleChange(day, e.target.value)}
                  className="flex-1"
                />
              </div>
            ))}

            <div className="pt-6 flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate(`/stations/${id}`)}>Cancel</Button>
              <Button type="submit" disabled={saving}>
                {saving ? "Saving..." : "Save Schedule"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
