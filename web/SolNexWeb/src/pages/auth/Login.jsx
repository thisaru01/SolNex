import { useState } from "react"
import { Link, useLocation, useNavigate } from "react-router-dom"
import { login } from "../../services/authApi"

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const [identifier, setIdentifier] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const notice = location.state?.message

  async function handleSubmit(event) {
    event.preventDefault()
    setError("")
    setIsSubmitting(true)

    try {
      const response = await login({ identifier, password })
      localStorage.setItem("solnex_token", response.token)
      localStorage.setItem("solnex_user", JSON.stringify(response))
      navigate("/dashboard", { replace: true })
    } catch (loginError) {
      setError(loginError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 px-4 py-8">
      <div className="w-full max-w-md space-y-6 rounded-xl border bg-background p-8 shadow-sm sm:p-10">
        <div className="space-y-2">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">SolNex access</p>
          <h1 className="text-3xl font-bold tracking-tight">Sign in</h1>
          <p className="text-muted-foreground">Use your NIC or registered email to continue.</p>
        </div>

        {notice && <p className="rounded-md border border-primary/30 bg-primary/10 px-3 py-2 text-sm text-primary">{notice}</p>}

        <form onSubmit={handleSubmit} className="grid gap-4">
          <div className="grid gap-2">
            <label htmlFor="identifier" className="text-sm font-medium">NIC or email</label>
            <input id="identifier" value={identifier} onChange={(event) => setIdentifier(event.target.value)} required placeholder="Enter your NIC or email" className="h-10 rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring" />
          </div>
          <div className="grid gap-2">
            <label htmlFor="password" className="text-sm font-medium">Password</label>
            <input id="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required placeholder="Enter your password" className="h-10 rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring" />
          </div>
          {error && <p role="alert" className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>}
          <button type="submit" disabled={isSubmitting} className="inline-flex h-10 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:pointer-events-none disabled:opacity-50">
            {isSubmitting ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="text-center text-sm text-muted-foreground">
          New prosumer? <Link to="/register" className="font-medium text-primary hover:underline">Create an account</Link>
        </p>
      </div>
    </div>
  )
}
