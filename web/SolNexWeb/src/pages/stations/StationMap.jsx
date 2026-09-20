import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Map } from "lucide-react"

export default function StationMap() {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Stations Map</h1>
        <p className="text-muted-foreground mt-1">Geographical overview of all microgrid nodes.</p>
      </div>

      <Card className="h-[600px] flex flex-col">
        <CardHeader>
          <CardTitle>Network Overview</CardTitle>
          <CardDescription>Interactive map powered by Google Maps will be integrated here.</CardDescription>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col items-center justify-center border-2 border-dashed mx-6 mb-6 rounded-lg bg-muted/10">
          <Map className="h-16 w-16 text-muted-foreground mb-4 opacity-50" />
          <h3 className="text-xl font-medium text-muted-foreground">Map Integration Pending</h3>
          <p className="text-sm text-muted-foreground mt-2 text-center max-w-sm">
            This module will use the <code className="bg-muted px-1 rounded">GET /api/stations/nearby</code> API to plot active stations and their availability statuses.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
