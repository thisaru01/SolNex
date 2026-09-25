import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { RefreshCw, Search } from "lucide-react"
import { transactionApi } from "../../services/transactionApi"

function formatDate(value) {
  if (!value) return "—"
  return new Date(value).toLocaleString()
}

function StatusBadge({ status }) {
  const classes = status === "Completed"
    ? "bg-green-100 text-green-700"
    : status === "Verified"
      ? "bg-blue-100 text-blue-700"
      : "bg-amber-100 text-amber-700"

  return <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${classes}`}>{status}</span>
}

export default function Transactions() {
  const [activeTab, setActiveTab] = useState("pending")
  const [transactions, setTransactions] = useState([])
  const [search, setSearch] = useState("")
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")
  const navigate = useNavigate()

  async function loadTransactions(tab = activeTab) {
    try {
      setLoading(true)
      setError("")
      const data = tab === "completed"
        ? await transactionApi.getCompleted()
        : await transactionApi.getPending()
      setTransactions(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message || "Failed to load transactions.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTransactions(activeTab)
  }, [activeTab])

  const query = search.trim().toLowerCase()
  const filtered = transactions.filter((transaction) => {
    if (!query) return true
    return [
      transaction.transactionId,
      transaction.reservationId,
      transaction.nic,
      transaction.stationId,
      transaction.status,
    ].some((value) => String(value || "").toLowerCase().includes(query))
  })

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Energy Transactions</h1>
          <p className="text-muted-foreground">Monitor pending, verified, and completed energy transfers.</p>
        </div>
        <button
          type="button"
          onClick={() => loadTransactions()}
          className="inline-flex h-10 items-center justify-center gap-2 rounded-md border bg-background px-4 text-sm font-medium hover:bg-muted"
        >
          <RefreshCw className="h-4 w-4" /> Refresh
        </button>
      </div>

      <div className="rounded-xl border bg-background shadow-sm">
        <div className="flex flex-col gap-4 border-b p-4 md:flex-row md:items-center md:justify-between">
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setActiveTab("pending")}
              className={`rounded-md px-4 py-2 text-sm font-medium ${activeTab === "pending" ? "bg-primary text-primary-foreground" : "bg-muted hover:bg-muted/80"}`}
            >
              Pending / Verified
            </button>
            <button
              type="button"
              onClick={() => setActiveTab("completed")}
              className={`rounded-md px-4 py-2 text-sm font-medium ${activeTab === "completed" ? "bg-primary text-primary-foreground" : "bg-muted hover:bg-muted/80"}`}
            >
              Completed
            </button>
          </div>

          <div className="relative w-full md:w-80">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search transaction, reservation, NIC..."
              className="h-10 w-full rounded-md border bg-background pl-9 pr-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>
        </div>

        {error && <p className="m-4 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>}

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="border-b bg-muted/50 text-left">
              <tr>
                <th className="px-4 py-3 font-medium">Transaction</th>
                <th className="px-4 py-3 font-medium">Reservation</th>
                <th className="px-4 py-3 font-medium">Prosumer NIC</th>
                <th className="px-4 py-3 font-medium">Station</th>
                <th className="px-4 py-3 font-medium">Energy</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Updated</th>
                <th className="px-4 py-3 text-right font-medium">Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="8" className="px-4 py-10 text-center text-muted-foreground">Loading transactions...</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan="8" className="px-4 py-10 text-center text-muted-foreground">No transactions found.</td></tr>
              ) : filtered.map((transaction) => (
                <tr key={transaction.id || transaction.transactionId} className="border-b last:border-0 hover:bg-muted/30">
                  <td className="px-4 py-3 font-medium">{transaction.transactionId}</td>
                  <td className="px-4 py-3">{transaction.reservationId}</td>
                  <td className="px-4 py-3">{transaction.nic}</td>
                  <td className="px-4 py-3">{transaction.stationId}</td>
                  <td className="px-4 py-3">{transaction.energyAmountKwh} kWh</td>
                  <td className="px-4 py-3"><StatusBadge status={transaction.status} /></td>
                  <td className="px-4 py-3 text-muted-foreground">{formatDate(transaction.completedAt || transaction.verifiedAt)}</td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() => navigate(`/transactions/${encodeURIComponent(transaction.transactionId)}`)}
                      className="rounded-md border px-3 py-1.5 text-xs font-medium hover:bg-muted"
                    >
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}