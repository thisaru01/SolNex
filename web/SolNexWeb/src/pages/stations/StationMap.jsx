import { useState, useEffect, useCallback } from "react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { GoogleMap, useJsApiLoader, Marker, InfoWindow } from "@react-google-maps/api"
import { stationApi } from "../../services/stationApi"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { useNavigate } from "react-router-dom"
import { Skeleton } from "@/components/ui/skeleton"

const containerStyle = {
  width: '100%',
  height: '100%'
}

// Default center (e.g., Colombo, Sri Lanka, or general fallback)
const defaultCenter = {
  lat: 7.8731,
  lng: 80.7718
}

export default function StationMap() {
  const navigate = useNavigate()
  const [stations, setStations] = useState([])
  const [selectedStation, setSelectedStation] = useState(null)
  
  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "", 
  })

  useEffect(() => {
    const fetchStations = async () => {
      try {
        const data = await stationApi.getStations()
        setStations(data || [])
      } catch (err) {
        console.error("Failed to fetch stations:", err)
      }
    }
    fetchStations()
  }, [])

  const onLoad = useCallback(function callback(map) {
    if (stations && stations.length > 0) {
      const bounds = new window.google.maps.LatLngBounds()
      let hasValidCoords = false;
      stations.forEach(station => {
        if (station.latitude && station.longitude) {
          bounds.extend({ lat: station.latitude, lng: station.longitude })
          hasValidCoords = true;
        }
      })
      if (hasValidCoords) {
        map.fitBounds(bounds)
      }
    }
  }, [stations])

  const onUnmount = useCallback(function callback(map) {
    // optional unmount logic
  }, [])

  return (
    <div className="space-y-6 h-full flex flex-col">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">Stations Map</h1>
        <p className="text-muted-foreground mt-1">Geographical overview of all microgrid nodes.</p>
      </div>

      <Card className="h-[600px] flex flex-col overflow-hidden">
        <CardHeader className="shrink-0">
          <CardTitle>Network Overview</CardTitle>
          <CardDescription>Interactive map showing all active and inactive stations.</CardDescription>
        </CardHeader>
        <CardContent className="flex-1 p-0 overflow-hidden relative">
          {!isLoaded ? (
             <Skeleton className="w-full h-full" />
          ) : (
            <GoogleMap
              mapContainerStyle={containerStyle}
              center={defaultCenter}
              zoom={7}
              onLoad={onLoad}
              onUnmount={onUnmount}
              options={{ disableDefaultUI: false, zoomControl: true }}
            >
              {stations.map(station => (
                station.latitude && station.longitude && (
                  <Marker
                    key={station.id || station.stationId}
                    position={{ lat: station.latitude, lng: station.longitude }}
                    onClick={() => setSelectedStation(station)}
                    icon={{
                      url: station.status === "Active" 
                        ? "http://maps.google.com/mapfiles/ms/icons/green-dot.png" 
                        : "http://maps.google.com/mapfiles/ms/icons/red-dot.png"
                    }}
                  />
                )
              ))}

              {selectedStation && (
                <InfoWindow
                  position={{ lat: selectedStation.latitude, lng: selectedStation.longitude }}
                  onCloseClick={() => setSelectedStation(null)}
                >
                  <div className="p-2 max-w-[200px]">
                    <h3 className="font-bold text-sm mb-1">{selectedStation.stationName}</h3>
                    <div className="flex items-center gap-2 mb-2">
                      <Badge variant="outline" className="text-[10px] px-1 py-0">
                        {selectedStation.stationId}
                      </Badge>
                      <Badge 
                        variant="secondary" 
                        className={`text-[10px] px-1 py-0 ${selectedStation.status === 'Active' ? 'bg-emerald-100 text-emerald-700' : 'bg-destructive/10 text-destructive'}`}
                      >
                        {selectedStation.status}
                      </Badge>
                    </div>
                    <p className="text-xs text-muted-foreground mb-3">
                      Capacity: {selectedStation.capacityKw} kW
                    </p>
                    <Button 
                      size="sm" 
                      className="w-full h-7 text-xs" 
                      onClick={() => navigate(`/stations/${selectedStation.id || selectedStation.stationId}`)}
                    >
                      View Details
                    </Button>
                  </div>
                </InfoWindow>
              )}
            </GoogleMap>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
