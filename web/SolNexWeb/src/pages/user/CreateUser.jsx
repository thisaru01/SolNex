import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { ArrowLeft, UserPlus } from "lucide-react"
import { registerWebUser } from "../../services/userApi"

export default function CreateUser() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    nic: "",
    fullName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    role: "GridOperator"
  })
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  function handleChange(event) {
    setFormData(prev => ({
      ...prev,
      [event.target.name]: event.target.value
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError("")

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match")
      return
    }

    if (formData.password.length < 8) {
      setError("Password must be at least 8 characters")
      return
    }

    setIsSubmitting(true)
    try {
      await registerWebUser({
        nic: formData.nic.trim(),
        fullName: formData.fullName.trim(),
        email: formData.email.trim(),
        phone: formData.phone.trim(),
        password: formData.password,
        role: formData.role
      })
      navigate("/users", { state: { message: "User created successfully" } })
    } catch (err) {
      setError(err.message || "Failed to create user")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="space-y-6 p-4 sm:p-6">
      <header className="flex items-center gap-4">
        <button
          type="button"
          onClick={() => navigate("/users")}
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Back to users
        </button>
      </header>

      <div className="max-w-2xl">
        <div className="mb-6">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">Backoffice</p>
          <h1 className="text-3xl font-bold tracking-tight">Create web user</h1>
          <p className="mt-1 text-muted-foreground">Add a new Backoffice or Grid Operator user to the system.</p>
        </div>

        {error && (
          <p role="alert" className="mb-4 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <label htmlFor="nic" className="text-sm font-medium">NIC</label>
              <input
                id="nic"
                name="nic"
                type="text"
                value={formData.nic}
                onChange={handleChange}
                required
                minLength={5}
                maxLength={20}
                placeholder="Enter NIC"
                className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="role" className="text-sm font-medium">Role</label>
              <select
                id="role"
                name="role"
                value={formData.role}
                onChange={handleChange}
                required
                className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <option value="Backoffice">Backoffice</option>
                <option value="GridOperator">Grid Operator</option>
                <option value="Prosumer">Prosumer</option>
              </select>
            </div>
          </div>

          <div className="space-y-2">
            <label htmlFor="fullName" className="text-sm font-medium">Full name</label>
            <input
              id="fullName"
              name="fullName"
              type="text"
              value={formData.fullName}
              onChange={handleChange}
              required
              minLength={2}
              maxLength={120}
              placeholder="Enter full name"
              className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-medium">Email</label>
              <input
                id="email"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                required
                placeholder="Enter email"
                className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="phone" className="text-sm font-medium">Phone</label>
              <input
                id="phone"
                name="phone"
                type="tel"
                value={formData.phone}
                onChange={handleChange}
                required
                placeholder="Enter phone number"
                className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <label htmlFor="password" className="text-sm font-medium">Password</label>
              <input
                id="password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                required
                minLength={8}
                placeholder="Enter password"
                className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="confirmPassword" className="text-sm font-medium">Confirm password</label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
                minLength={8}
                placeholder="Confirm password"
                className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={() => navigate("/users")}
              className="h-10 rounded-md border px-4 text-sm font-medium hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex h-10 items-center gap-2 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:cursor-wait disabled:opacity-60"
            >
              <UserPlus className="size-4" />
              {isSubmitting ? "Creating..." : "Create user"}
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}
