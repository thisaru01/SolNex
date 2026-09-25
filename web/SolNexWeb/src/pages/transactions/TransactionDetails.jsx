import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { ArrowLeft } from "lucide-react"
import { transactionApi } from "../../services/transactionApi"

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : "Not yet"
}

export default function TransactionDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [transaction, setTransaction] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    async function load() {
      try {
        setLoading(true)
        setError("")
        setTransaction(await transactionApi.getById(id))
      } catch (err) {
        setError(err.message || "Failed to load transaction details.")
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  if (loading) return <p className="py-10 text-center text-muted-foreground">Loading transaction...</p>
  if (error) return <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>
  if (!transaction) return null

  const fields = [
    ["Transaction ID", transaction.transactionId],
    ["Reservation ID", transaction.reservationId],
    ["Prosumer NIC", transaction.nic],
    ["Station ID", transaction.stationId],
    ["Energy Amount", `${transaction.energyAmountKwh} kWh`],
    ["Status", transaction.status],
    ["Verified At", formatDate(transaction.verifiedAt)],
    ["Completed At", formatDate(transaction.completedAt)],
    ["Operator NIC", transaction.operatorNic || "Not assigned"],
  ]

  return (
    <div className="space-y-6">
      <button type="button" onClick={() => navigate(-1)} className="inline-flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back
      </button>

      <div>
        <h1 className="text-3xl font-bold tracking-tight">Transaction Details</h1>
        <p className="text-muted-foreground">Server-side status of this energy transfer.</p>
      </div>

      <div className="grid gap-4 rounded-xl border bg-background p-6 shadow-sm md:grid-cols-2">
        {fields.map(([label, value]) => (
          <div key={label} className="rounded-lg border bg-muted/20 p-4">
            <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</p>
            <p className="mt-1 break-words font-semibold">{value}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
