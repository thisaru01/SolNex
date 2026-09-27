import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { Search, UserCheck, UserRoundX, UserPlus, Edit, Eye } from "lucide-react"
import {
  activateUser,
  deactivateUser,
  getUsers,
} from "../../services/userApi"

function statusClass(status) {
  return {
    Active: "bg-emerald-100 text-emerald-700",
    Pending: "bg-amber-100 text-amber-700",
    Inactive: "bg-slate-100 text-slate-600",
  }[status] || "bg-muted text-muted-foreground"
}

export default function Prosumers() {
  const navigate = useNavigate()
  const [users, setUsers] = useState([])
  const [search, setSearch] = useState("")
  const [filter, setFilter] = useState("all")
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")
  const [workingNic, setWorkingNic] = useState("")

  async function loadUsers() {
    setIsLoading(true)
    setError("")
    try {
      const allUsers = await getUsers(search)
      const prosumers = allUsers.filter(user => user.role === "Prosumer")
      
      if (filter === "pending") {
        setUsers(prosumers.filter(user => user.accountStatus === "Pending"))
      } else if (filter === "active") {
        setUsers(prosumers.filter(user => user.accountStatus === "Active"))
      } else if (filter === "inactive") {
        setUsers(prosumers.filter(user => user.accountStatus === "Inactive"))
      } else {
        setUsers(prosumers)
      }
    } catch (loadError) {
      setError(loadError.message)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    let isCurrent = true
    const load = async () => {
      setIsLoading(true)
      setError("")
      try {
        const allUsers = await getUsers()
        const prosumers = allUsers.filter(user => user.role === "Prosumer")
        
        let nextUsers
        if (filter === "pending") {
          nextUsers = prosumers.filter(user => user.accountStatus === "Pending")
        } else if (filter === "active") {
          nextUsers = prosumers.filter(user => user.accountStatus === "Active")
        } else if (filter === "inactive") {
          nextUsers = prosumers.filter(user => user.accountStatus === "Inactive")
        } else {
          nextUsers = prosumers
        }
        
        if (isCurrent) setUsers(nextUsers)
      } catch (loadError) {
        if (isCurrent) {
          setError(loadError.status === 401
            ? "Backoffice sign-in is required to view prosumer management. Please sign in again."
            : loadError.message)
        }
      } finally {
        if (isCurrent) setIsLoading(false)
      }
    }
    load()
    return () => { isCurrent = false }
  }, [filter])

  async function changeStatus(user) {
    setWorkingNic(user.nic)
    try {
      if (user.accountStatus === "Active") await deactivateUser(user.nic)
      else await activateUser(user.nic)
      await loadUsers()
    } catch (actionError) {
      setError(actionError.status === 401 || actionError.status === 403
        ? "Only a Backoffice user can activate or deactivate accounts. Sign in as Backoffice first."
        : actionError.message)
    } finally {
      setWorkingNic("")
    }
  }

  return (
    <section className="space-y-6 p-4 sm:p-6">
      <header className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">Backoffice</p>
          <h1 className="text-3xl font-bold tracking-tight">Prosumer management</h1>
          <p className="mt-1 text-muted-foreground">Create, update, and deactivate prosumer profiles using NIC as primary key.</p>
        </div>
        <div className="flex gap-2">
          <button type="button" onClick={() => navigate("/prosumers/create")} className="inline-flex h-10 items-center gap-2 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90">
            <UserPlus className="size-4" />
            Create prosumer
          </button>
          <div className="flex rounded-md border">
            <button type="button" onClick={() => setFilter("all")} className={`h-10 px-4 text-sm font-medium hover:bg-muted ${filter === "all" ? "bg-muted" : ""}`}>
              All
            </button>
            <button type="button" onClick={() => setFilter("pending")} className={`h-10 px-4 text-sm font-medium hover:bg-muted border-l ${filter === "pending" ? "bg-muted" : ""}`}>
              Pending
            </button>
            <button type="button" onClick={() => setFilter("active")} className={`h-10 px-4 text-sm font-medium hover:bg-muted border-l ${filter === "active" ? "bg-muted" : ""}`}>
              Active
            </button>
            <button type="button" onClick={() => setFilter("inactive")} className={`h-10 px-4 text-sm font-medium hover:bg-muted border-l ${filter === "inactive" ? "bg-muted" : ""}`}>
              Inactive
            </button>
          </div>
        </div>
      </header>

      <div className="flex max-w-xl gap-2">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-2.5 size-5 text-muted-foreground" aria-hidden="true" />
          <input value={search} onChange={(event) => setSearch(event.target.value)} onKeyDown={(event) => event.key === "Enter" && loadUsers()} placeholder="Search by NIC, name, or email" className="h-10 w-full rounded-md border bg-background pl-10 pr-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring" />
        </div>
        <button type="button" onClick={loadUsers} className="h-10 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90">Search</button>
      </div>

      {error && <p role="alert" className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>}

      <div className="overflow-x-auto rounded-lg border bg-background">
        <table className="w-full min-w-[900px] text-left text-sm">
          <thead className="border-b bg-muted/50 text-xs uppercase tracking-wide text-muted-foreground">
            <tr><th className="px-4 py-3">User</th><th className="px-4 py-3">NIC</th><th className="px-4 py-3">Phone</th><th className="px-4 py-3">Role</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Created</th><th className="px-4 py-3 text-right">Actions</th></tr>
          </thead>
          <tbody className="divide-y">
            {isLoading && <tr><td colSpan="7" className="px-4 py-10 text-center text-muted-foreground">Loading prosumers...</td></tr>}
            {!isLoading && users.length === 0 && <tr><td colSpan="7" className="px-4 py-10 text-center text-muted-foreground">No prosumers found.</td></tr>}
            {!isLoading && users.map((user) => (
              <tr key={user.nic} className="hover:bg-muted/30">
                <td className="px-4 py-3"><div className="font-medium">{user.fullName}</div><div className="text-muted-foreground">{user.email}</div></td>
                <td className="px-4 py-3 font-mono text-xs">{user.nic}</td>
                <td className="px-4 py-3">{user.phone}</td>
                <td className="px-4 py-3"><span className="rounded-full px-2.5 py-1 text-xs font-medium bg-blue-100 text-blue-700">{user.role}</span></td>
                <td className="px-4 py-3"><span className={`rounded-full px-2.5 py-1 text-xs font-medium ${statusClass(user.accountStatus)}`}>{user.accountStatus}</span></td>
                <td className="px-4 py-3 text-xs text-muted-foreground">{new Date(user.createdAt).toLocaleDateString()}</td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-1">
                    <button type="button" title="View details" onClick={() => navigate(`/prosumers/${user.nic}`)} className="inline-flex size-8 items-center justify-center rounded-md border hover:bg-muted">
                      <Eye className="size-4" />
                    </button>
                    <button type="button" title="Edit profile" onClick={() => navigate(`/prosumers/${user.nic}/edit`)} className="inline-flex size-8 items-center justify-center rounded-md border hover:bg-muted">
                      <Edit className="size-4" />
                    </button>
                    <button type="button" title={user.accountStatus === "Active" ? "Deactivate user" : "Activate user"} onClick={() => changeStatus(user)} disabled={workingNic === user.nic} className="inline-flex size-8 items-center justify-center rounded-md border hover:bg-muted disabled:opacity-50">
                      {user.accountStatus === "Active" ? <UserRoundX className="size-4" /> : <UserCheck className="size-4" />}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
