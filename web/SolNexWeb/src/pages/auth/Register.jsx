import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:5097"

const initialForm = {
  nic: "",
  fullName: "",
  email: "",
  phone: "",
  password: "",
}

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError("")
    setIsSubmitting(true)

    try {
      const response = await fetch(`${API_URL}/api/users/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      })
      const data = await response.json()

      if (!response.ok) {
        const validationMessage = data.errors
          ? Object.values(data.errors).flat().join(" ")
          : data.message
        throw new Error(validationMessage || "Registration could not be completed.")
      }

      navigate("/login", {
        replace: true,
        state: { message: "Registration submitted. A Backoffice officer must activate your account before you can sign in." },
      })
    } catch (submissionError) {
      setError(submissionError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 px-4 py-8">
      <div className="w-full max-w-lg space-y-6 rounded-xl border bg-background p-8 shadow-sm sm:p-10">
        <div className="space-y-2">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">Solar prosumer</p>
          <h1 className="text-3xl font-bold tracking-tight">Create your account</h1>
          <p className="text-muted-foreground">Your account will remain pending until a Backoffice officer activates it.</p>
        </div>

        <form onSubmit={handleSubmit} className="grid gap-4">
          <div className="grid gap-2">
            <label htmlFor="nic" className="text-sm font-medium">National Identity Card</label>
            <input id="nic" name="nic" value={form.nic} onChange={updateField} required minLength={5} maxLength={20} placeholder="Enter your NIC" className="h-10 rounded-md border bg-background px-3 text-sm outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring" />
          </div>

          <div className="grid gap-2">
            <label htmlFor="fullName" className="text-sm font-medium">Full name</label>
            <input id="fullName" name="fullName" value={form.fullName} onChange={updateField} required minLength={2} maxLength={120} placeholder="Enter your full name" className="h-10 rounded-md border bg-background px-3 text-sm outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring" />
          </div>

          <div className="grid gap-2 sm:grid-cols-2 sm:gap-4">
            <div className="grid gap-2">
              <label htmlFor="email" className="text-sm font-medium">Email</label>
              <input id="email" name="email" type="email" value={form.email} onChange={updateField} required placeholder="name@example.com" className="h-10 rounded-md border bg-background px-3 text-sm outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring" />
            </div>
            <div className="grid gap-2">
              <label htmlFor="phone" className="text-sm font-medium">Phone</label>
              <input id="phone" name="phone" type="tel" value={form.phone} onChange={updateField} required placeholder="+94 77 123 4567" className="h-10 rounded-md border bg-background px-3 text-sm outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring" />
            </div>
          </div>

          <div className="grid gap-2">
            <label htmlFor="password" className="text-sm font-medium">Password</label>
            <input id="password" name="password" type="password" value={form.password} onChange={updateField} required minLength={8} placeholder="At least 8 characters" className="h-10 rounded-md border bg-background px-3 text-sm outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring" />
          </div>

          {error && <p role="alert" className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>}

          <button type="submit" disabled={isSubmitting} className="inline-flex h-10 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90 disabled:pointer-events-none disabled:opacity-50">
            {isSubmitting ? "Submitting..." : "Submit registration"}
          </button>
        </form>

        <p className="text-center text-sm text-muted-foreground">
          Already have an account? <Link to="/login" className="font-medium text-primary hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
