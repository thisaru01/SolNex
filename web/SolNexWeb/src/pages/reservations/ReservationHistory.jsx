import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Clock, CheckCircle2, XCircle, AlertCircle } from "lucide-react"

export default function ReservationHistory() {
  const [activeTab, setActiveTab] = useState("pending")

  // Mock counts (you can later replace these with real data from an API)
  const stats = {
    pending: 12,
    approved: 45,
    cancelled: 3,
    rejected: 5
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">Reservation History</h1>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending</CardTitle>
            <Clock className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-amber-500">{stats.pending}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Approved</CardTitle>
            <CheckCircle2 className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-500">{stats.approved}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Cancelled</CardTitle>
            <XCircle className="h-4 w-4 text-gray-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-gray-500">{stats.cancelled}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Rejected</CardTitle>
            <AlertCircle className="h-4 w-4 text-red-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-500">{stats.rejected}</div>
          </CardContent>
        </Card>
      </div>

      {/* Custom Tabs */}
      <div className="space-y-4 pt-4">
        <div className="flex border-b border-border">
          <button
            onClick={() => setActiveTab("pending")}
            className={`px-6 py-3 border-b-2 font-medium text-sm transition-colors ${
              activeTab === "pending"
                ? "border-primary text-primary"
                : "border-transparent text-muted-foreground hover:text-foreground hover:border-muted-foreground"
            }`}
          >
            Pending
          </button>
          <button
            onClick={() => setActiveTab("history")}
            className={`px-6 py-3 border-b-2 font-medium text-sm transition-colors ${
              activeTab === "history"
                ? "border-primary text-primary"
                : "border-transparent text-muted-foreground hover:text-foreground hover:border-muted-foreground"
            }`}
          >
            History
          </button>
        </div>

        {/* Tab Content Placeholder */}
        <div className="bg-card rounded-xl border border-border p-6 shadow-sm min-h-[300px]">
          {activeTab === "pending" && (
            <div className="animate-in fade-in slide-in-from-bottom-2 duration-300">
              <h2 className="text-lg font-semibold mb-2">Pending Reservations</h2>
              <p className="text-muted-foreground">The list of pending reservations will appear here.</p>
            </div>
          )}
          {activeTab === "history" && (
            <div className="animate-in fade-in slide-in-from-bottom-2 duration-300">
              <h2 className="text-lg font-semibold mb-2">Reservation History</h2>
              <p className="text-muted-foreground">The list of approved, cancelled, and rejected reservations will appear here.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
