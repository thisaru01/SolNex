import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { ArrowLeft, UserCheck, UserRoundX, Calendar, Mail, Phone, Shield } from "lucide-react"
import { getUserByNic, activateUser, deactivateUser } from "../../services/userApi"

function statusClass(status) {
  return {
    Active: "bg-emerald-100 text-emerald-700",
    Pending: "bg-amber-100 text-amber-700",
    Inactive: "bg-slate-100 text-slate-600",
  }[status] || "bg-muted text-muted-foreground"
}

export default function ProsumerDetails() {
  const navigate = useNavigate()
  const { nic } = useParams()
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")
  const [isWorking, setIsWorking] = useState(false)

  useEffect(() => {
    async function loadUser() {
      setIsLoading(true)
      setError("")
      try {
        const userData = await getUserByNic(nic)
        setUser(userData)
      } catch (err) {
        setError(err.message || "Failed to load prosumer details")
      } finally {
        setIsLoading(false)
      }
    }
    loadUser()
  }, [nic])

  async function handleStatusChange() {
    if (!user) return
    setIsWorking(true)
    setError("")
    try {
      if (user.accountStatus === "Active") {
        await deactivateUser(nic)
      } else {
        await activateUser(nic)
      }
      const updated = await getUserByNic(nic)
      setUser(updated)
    } catch (err) {
      setError(err.message || "Failed to update status")
    } finally {
      setIsWorking(false)
    }
  }

  if (isLoading) {
    return (
      <section className="space-y-6 p-4 sm:p-6">
        <div className="flex items-center gap-4">
          <button type="button" onClick={() => navigate("/prosumers")} className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
            <ArrowLeft className="size-4" />
            Back to prosumers
          </button>
        </div>
        <p className="text-muted-foreground">Loading prosumer details...</p>
      </section>
    )
  }

  if (error || !user) {
    return (
      <section className="space-y-6 p-4 sm:p-6">
        <div className="flex items-center gap-4">
          <button type="button" onClick={() => navigate("/prosumers")} className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
            <ArrowLeft className="size-4" />
            Back to prosumers
          </button>
        </div>
        {error && <p role="alert" className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>}
        {!user && <p className="text-muted-foreground">Prosumer not found</p>}
      </section>
    )
  }

  return (
    <section className="space-y-6 p-4 sm:p-6">
      <header className="flex items-center gap-4">
        <button
          type="button"
          onClick={() => navigate("/prosumers")}
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Back to prosumers
        </button>
      </header>

      {error && <p role="alert" className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>}

      <div className="grid gap-6 md:grid-cols-2">
        <div className="space-y-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">Backoffice</p>
            <h1 className="text-3xl font-bold tracking-tight">Prosumer details</h1>
          </div>

          <div className="rounded-lg border bg-card p-6 space-y-4">
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-2xl font-semibold">{user.fullName}</h2>
                <p className="text-muted-foreground">{user.email}</p>
              </div>
              <span className={`rounded-full px-3 py-1 text-sm font-medium ${statusClass(user.accountStatus)}`}>
                {user.accountStatus}
              </span>
            </div>

            <div className="space-y-3 pt-4">
              <div className="flex items-center gap-3">
                <Shield className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-xs text-muted-foreground">NIC (Primary Key)</p>
                  <p className="font-mono text-sm">{user.nic}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Phone className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-xs text-muted-foreground">Phone</p>
                  <p className="text-sm">{user.phone}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Mail className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-xs text-muted-foreground">Email</p>
                  <p className="text-sm">{user.email}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Calendar className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-xs text-muted-foreground">Account created</p>
                  <p className="text-sm">{new Date(user.createdAt).toLocaleDateString()}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Calendar className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-xs text-muted-foreground">Last updated</p>
                  <p className="text-sm">{new Date(user.updatedAt).toLocaleDateString()}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <h2 className="text-lg font-semibold">Account actions</h2>

          <div className="rounded-lg border bg-card p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium">Account status</p>
                <p className="text-sm text-muted-foreground">
                  {user.accountStatus === "Active" 
                    ? "Account is active and can use the system" 
                    : user.accountStatus === "Pending"
                    ? "Account is pending activation by Backoffice"
                    : "Account is deactivated and cannot access the system"}
                </p>
              </div>
              <button
                type="button"
                onClick={handleStatusChange}
                disabled={isWorking}
                className="inline-flex items-center gap-2 rounded-md border px-4 py-2 text-sm font-medium hover:bg-muted disabled:cursor-wait disabled:opacity-50"
              >
                {isWorking ? "Processing..." : user.accountStatus === "Active" ? (
                  <>
                    <UserRoundX className="size-4" />
                    Deactivate
                  </>
                ) : (
                  <>
                    <UserCheck className="size-4" />
                    Activate
                  </>
                )}
              </button>
            </div>

            <div className="pt-4 border-t">
              <p className="text-xs text-muted-foreground">
                <strong>Note:</strong> Deactivated accounts can only be reactivated by a Backoffice officer.
                Prosumers can request deactivation through the mobile app.
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={() => navigate(`/prosumers/${nic}/edit`)}
            className="w-full rounded-md border px-4 py-2 text-sm font-medium hover:bg-muted"
          >
            Edit profile information
          </button>
        </div>
      </div>
    </section>
  )
}
