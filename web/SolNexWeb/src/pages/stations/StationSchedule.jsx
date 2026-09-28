import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { stationApi } from "../../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import { ArrowLeft } from "lucide-react"
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

const DAYS_OF_WEEK = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

export default function StationSchedule() {
  const { id } = useParams()
  const navigate = useNavigate()
  
  const [stationName, setStationName] = useState("")
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [alertOpen, setAlertOpen] = useState(false)
  
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

  const handlePreSubmit = (e) => {
    e.preventDefault()
    setAlertOpen(true)
  }

  const handleSubmit = async () => {
    setAlertOpen(false)
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

  if (loading) return <div className="p-8"><Skeleton className="h-96 w-full" /></div>
  
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/stations/${id}`)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Station Schedule</h1>
          <p className="text-muted-foreground mt-1">{stationName}</p>
        </div>
      </div>

      <Card className="max-w-3xl mx-auto">
        <CardHeader>
          <CardTitle>Operating Hours</CardTitle>
          <CardDescription>Enter time ranges like "08:00 - 18:00" or type "Closed".</CardDescription>
        </CardHeader>
        <CardContent>
          {error && <div className="p-3 mb-4 text-sm text-destructive bg-destructive/10 rounded-md">{error}</div>}
          
          <form onSubmit={handlePreSubmit} className="space-y-4">
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

      <AlertDialog open={alertOpen} onOpenChange={setAlertOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Save Schedule?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to update the operating hours for this station? 
              This will affect when grid operators and prosumers can interact with it.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleSubmit}>Save</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
