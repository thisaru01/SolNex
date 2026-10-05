import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { ArrowLeft, Check, X, UserX } from "lucide-react"
import { getDeactivationRequests, approveDeactivation, rejectDeactivation, activateUser } from "../../services/userApi"

/**
 * Deactivation requests page for Backoffice users.
 * Allows reviewing and approving/rejecting prosumer account deactivation requests.
 */
export default function DeactivationRequests() {
  const navigate = useNavigate()
  const [requests, setRequests] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")
  const [processing, setProcessing] = useState(null)

  useEffect(() => {
    loadRequests()
  }, [])

  // Load deactivation requests from API
  async function loadRequests() {
    setIsLoading(true)
    setError("")
    try {
      const data = await getDeactivationRequests()
      setRequests(data)
    } catch (err) {
      setError(err.message || "Failed to load deactivation requests")
    } finally {
      setIsLoading(false)
    }
  }

  // Approve deactivation request and deactivate account
  async function handleApprove(nic) {
    setProcessing(nic)
    setError("")
    try {
      await approveDeactivation(nic)
      await loadRequests()
    } catch (err) {
      setError(err.message || "Failed to approve deactivation")
    } finally {
      setProcessing(null)
    }
  }

  // Reject deactivation request and reactivate account
  async function handleReject(nic) {
    setProcessing(nic)
    setError("")
    try {
      await rejectDeactivation(nic)
      await loadRequests()
    } catch (err) {
      setError(err.message || "Failed to reject deactivation request")
    } finally {
      setProcessing(null)
    }
  }

  if (isLoading) {
    return (
      <section className="space-y-6 p-4 sm:p-6">
        <div className="flex items-center gap-4">
          <button
            type="button"
            onClick={() => navigate("/dashboard")}
            className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="size-4" />
            Back to dashboard
          </button>
        </div>
        <p className="text-muted-foreground">Loading deactivation requests...</p>
      </section>
    )
  }

  return (
    <section className="space-y-6 p-4 sm:p-6">
      <header className="flex items-center gap-4">
        <button
          type="button"
          onClick={() => navigate("/dashboard")}
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Back to dashboard
        </button>
      </header>

      <div>
        <div className="mb-6">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">Backoffice</p>
          <h1 className="text-3xl font-bold tracking-tight">Deactivation Requests</h1>
          <p className="mt-1 text-muted-foreground">Review and approve prosumer account deactivation requests.</p>
        </div>

        {error && (
          <p role="alert" className="mb-4 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
            {error}
          </p>
        )}

        {requests.length === 0 ? (
          <div className="rounded-lg border bg-muted/50 p-8 text-center">
            <UserX className="mx-auto size-12 text-muted-foreground/50" />
            <p className="mt-4 text-muted-foreground">No pending deactivation requests</p>
          </div>
        ) : (
          <div className="rounded-md border">
            <table className="w-full text-sm">
              <thead className="border-b bg-muted/50">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">NIC</th>
                  <th className="px-4 py-3 text-left font-medium">Full Name</th>
                  <th className="px-4 py-3 text-left font-medium">Email</th>
                  <th className="px-4 py-3 text-left font-medium">Phone</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((request) => (
                  <tr key={request.nic} className="border-b last:border-b-0">
                    <td className="px-4 py-3 font-medium">{request.nic}</td>
                    <td className="px-4 py-3">{request.fullName}</td>
                    <td className="px-4 py-3">{request.email}</td>
                    <td className="px-4 py-3">{request.phone}</td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => handleApprove(request.nic)}
                          disabled={processing === request.nic}
                          className="inline-flex h-8 items-center gap-1.5 rounded-md bg-destructive px-3 text-sm font-medium text-destructive-foreground hover:bg-destructive/90 disabled:cursor-wait disabled:opacity-60"
                          title="Approve deactivation"
                        >
                          <Check className="size-4" />
                          Approve
                        </button>
                        <button
                          type="button"
                          onClick={() => handleReject(request.nic)}
                          disabled={processing === request.nic}
                          className="inline-flex h-8 items-center gap-1.5 rounded-md bg-primary px-3 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:cursor-wait disabled:opacity-60"
                          title="Reject request and keep account active"
                        >
                          <X className="size-4" />
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  )
}
