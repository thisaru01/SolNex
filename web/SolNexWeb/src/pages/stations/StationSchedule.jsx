import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { stationApi } from "../../services/stationApi"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import { Switch } from "@/components/ui/switch"
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
import { toast } from "sonner"

const DAYS_OF_WEEK = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

// Page component for managing the operating schedule of a specific station.
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

  // Trigger side effects like fetching initial station data on mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch both the basic station details and the operational schedule concurrently
        const [stationData, scheduleData] = await Promise.all([
          stationApi.getStationById(id),
          stationApi.getStationSchedule(id)
        ])
        
        // Save the station name for display purposes in the header
        setStationName(stationData.stationName)
        
        // If a schedule exists on the server, merge it into our default schedule state
        if (scheduleData) {
          setSchedule(prev => ({ ...prev, ...scheduleData }))
        }
      } catch (err) {
        // Capture any error messages to show in the UI
        setError(err.message || "Failed to load schedule")
      } finally {
        // Dismiss loading spinner regardless of success or failure
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleScheduleChange = (day, value) => {
    setSchedule(prev => ({ ...prev, [day]: value }))
  }
  
  // Prevent default form submission and trigger confirmation dialog instead
  const handlePreSubmit = (e) => {
    e.preventDefault()
    setError(null)

    for (const day of DAYS_OF_WEEK) {
      const val = schedule[day]
      if (val && val !== "Closed") {
        const parts = val.split(" - ")
        if (parts.length === 2) {
          const [start, end] = parts
          if (!start || !end) {
            toast.error(`Please specify both opening and closing times for ${day}.`)
            return
          }
          if (start >= end) {
            toast.error(`Start time must be earlier than end time for ${day} (${start} - ${end}).`)
            return
          }
        }
      }
    }

    setAlertOpen(true)
  }

  // Executes the actual update/create logic after user confirms action
  const handleSubmit = async () => {
    setAlertOpen(false)
    setSaving(true)
    setError(null)
    
    try {
      await stationApi.updateStationSchedule(id, schedule)
      toast.success("Schedule updated successfully")
      navigate(`/stations/${id}`)
    } catch (err) {
      toast.error(err.message || "Failed to update schedule")
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
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight">Station Schedule</h1>
          <p className="text-muted-foreground mt-1">{stationName}</p>
        </div>
      </div>

      <Card className="max-w-3xl mx-auto">
        <CardHeader>
          <CardTitle>Operating Hours</CardTitle>
          <CardDescription>Toggle days and select exact opening and closing times.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handlePreSubmit} className="space-y-0">
            <div className="max-w-xl mx-auto">
              {DAYS_OF_WEEK.map((day) => {
                const isOpen = schedule[day] !== "Closed" && schedule[day] !== "" && schedule[day] !== undefined
                const times = isOpen ? schedule[day].split(" - ") : ["08:00", "18:00"]
                const startTime = times[0] || "08:00"
                const endTime = times[1] || "18:00"

                return (
                  <div key={day} className="flex flex-col sm:flex-row sm:items-center gap-4 sm:gap-8 py-4 sm:py-3.5 border-b last:border-0">
                    <div className="flex items-center justify-between sm:justify-start w-full sm:w-auto gap-4">
                      <div className="w-28 font-medium flex-shrink-0">{day}</div>
                      
                      <div className="flex items-center justify-end sm:justify-start gap-3 flex-shrink-0 w-24">
                        <Switch 
                          checked={isOpen} 
                          onCheckedChange={(c) => handleScheduleChange(day, c ? "08:00 - 18:00" : "Closed")} 
                        />
                        <span className="text-sm text-muted-foreground">
                          {isOpen ? "Open" : "Closed"}
                        </span>
                      </div>
                    </div>
                    
                    <div className="flex-1 w-full sm:w-auto">
                      {isOpen ? (
                        <div className="flex items-center gap-2 sm:gap-4 animate-in fade-in zoom-in-95 duration-200">
                          <Input 
                            type="time" 
                            value={startTime}
                            onChange={(e) => handleScheduleChange(day, `${e.target.value} - ${endTime}`)}
                            className="flex-1 sm:flex-none sm:w-32"
                            required
                          />
                          <span className="text-muted-foreground font-medium text-sm">to</span>
                          <Input 
                            type="time" 
                            value={endTime}
                            onChange={(e) => handleScheduleChange(day, `${startTime} - ${e.target.value}`)}
                            className="flex-1 sm:flex-none sm:w-32"
                            required
                          />
                        </div>
                      ) : (
                        <span className="text-xs text-muted-foreground italic">Closed all day</span>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>

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
