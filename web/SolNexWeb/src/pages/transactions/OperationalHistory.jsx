import { useEffect, useState } from "react"
import { RefreshCw } from "lucide-react"
import { transactionApi } from "../../services/transactionApi"

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : "—"
}

export default function OperationalHistory() {
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  async function loadHistory() {
    try {
      setLoading(true)
      setError("")
      const data = await transactionApi.getHistory()
      setTransactions(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message || "Failed to load operational history.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false

    transactionApi.getHistory()
      .then((data) => {
        if (!cancelled) setTransactions(Array.isArray(data) ? data : [])
      })
      .catch((err) => {
        if (!cancelled) setError(err.message || "Failed to load operational history.")
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Operational History</h1>
          <p className="text-muted-foreground">Verified and completed energy transfers recorded by the central service.</p>
        </div>
        <button type="button" onClick={loadHistory} className="inline-flex h-10 items-center justify-center gap-2 rounded-md border bg-background px-4 text-sm font-medium hover:bg-muted">
          <RefreshCw className="h-4 w-4" /> Refresh
        </button>
      </div>

      {error && <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>}

      <div className="overflow-x-auto rounded-xl border bg-background shadow-sm">
        <table className="w-full text-sm">
          <thead className="border-b bg-muted/50 text-left">
            <tr>
              <th className="px-4 py-3 font-medium">Activity</th>
              <th className="px-4 py-3 font-medium">Transaction</th>
              <th className="px-4 py-3 font-medium">Reservation</th>
              <th className="px-4 py-3 font-medium">Station</th>
              <th className="px-4 py-3 font-medium">Prosumer NIC</th>
              <th className="px-4 py-3 font-medium">Energy</th>
              <th className="px-4 py-3 font-medium">Operator</th>
              <th className="px-4 py-3 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="8" className="px-4 py-10 text-center text-muted-foreground">Loading history...</td></tr>
            ) : transactions.length === 0 ? (
              <tr><td colSpan="8" className="px-4 py-10 text-center text-muted-foreground">No operational history yet.</td></tr>
            ) : transactions.map((transaction) => (
              <tr key={transaction.id || transaction.transactionId} className="border-b last:border-0">
                <td className="px-4 py-3">{formatDate(transaction.completedAt || transaction.verifiedAt)}</td>
                <td className="px-4 py-3 font-medium">{transaction.transactionId}</td>
                <td className="px-4 py-3">{transaction.reservationId}</td>
                <td className="px-4 py-3">{transaction.stationId}</td>
                <td className="px-4 py-3">{transaction.nic}</td>
                <td className="px-4 py-3">{transaction.energyAmountKwh} kWh</td>
                <td className="px-4 py-3">{transaction.operatorNic || "—"}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${transaction.status === "Completed" ? "bg-green-100 text-green-700" : "bg-blue-100 text-blue-700"}`}>
                    {transaction.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}