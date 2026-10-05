import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { ArrowLeft, Save } from "lucide-react"
import { getUserByNic, updateUser } from "../../services/userApi"
import { toast } from "sonner"

/**
 * Edit prosumer page for Backoffice users.
 * Allows updating prosumer information using NIC as primary key.
 */
export default function EditProsumer() {
  const navigate = useNavigate()
  const { nic } = useParams()
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phone: ""
  })
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState("")

  // Format phone number with spaces for display
  function formatPhoneNumber(value) {
    const digits = value.replace(/\D/g, '')
    if (digits.startsWith('94') && digits.length >= 11) {
      return `+94 ${digits.substring(2, 5)} ${digits.substring(5, 8)} ${digits.substring(8)}`
    }
    if (digits.startsWith('0') && digits.length >= 10) {
      return `${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}`
    }
    return value
  }

  useEffect(() => {
    // Load prosumer data on component mount
    async function loadUser() {
      setIsLoading(true)
      setError("")
      try {
        const userData = await getUserByNic(nic)
        setFormData({
          fullName: userData.fullName,
          email: userData.email,
          phone: formatPhoneNumber(userData.phone)
        })
      } catch (err) {
        setError(err.message || "Failed to load prosumer details")
      } finally {
        setIsLoading(false)
      }
    }
    loadUser()
  }, [nic])

  // Handle form input changes
  function handleChange(event) {
    const { name, value } = event.target
    if (name === 'phone') {
      setFormData(prev => ({ ...prev, [name]: formatPhoneNumber(value) }))
    } else {
      setFormData(prev => ({ ...prev, [name]: value }))
    }
  }

  // Handle form submission to update prosumer information
  async function handleSubmit(event) {
    event.preventDefault()
    setError("")

    const phoneDigits = formData.phone.replace(/\D/g, '')
    if (phoneDigits.startsWith('94') && phoneDigits.length !== 11) {
      setError("Phone must be +94 followed by 9 digits")
      return
    }
    if (phoneDigits.startsWith('0') && phoneDigits.length !== 10) {
      setError("Phone must be 0 followed by 9 digits")
      return
    }
    if (!phoneDigits.startsWith('94') && !phoneDigits.startsWith('0')) {
      setError("Phone must start with +94 or 0")
      return
    }

    setIsSubmitting(true)

    try {
      await updateUser(nic, { ...formData, phone: phoneDigits })
      toast.success("Prosumer profile updated successfully")
      navigate(`/prosumers/${nic}`, { state: { message: "Profile updated successfully" } })
    } catch (err) {
      setError(err.message || "Failed to update profile")
      toast.error(err.message || "Failed to update profile")
    } finally {
      setIsSubmitting(false)
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

  return (
    <section className="space-y-6 p-4 sm:p-6">
      <header className="flex items-center gap-4">
        <button
          type="button"
          onClick={() => navigate(`/prosumers/${nic}`)}
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Back to details
        </button>
      </header>

      <div className="max-w-2xl">
        <div className="mb-6">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">Backoffice</p>
          <h1 className="text-3xl font-bold tracking-tight">Edit prosumer profile</h1>
          <p className="mt-1 text-muted-foreground">Update prosumer information using NIC as primary key.</p>
        </div>

        {error && (
          <p role="alert" className="mb-4 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <label htmlFor="nic" className="text-sm font-medium">NIC (Primary Key)</label>
            <input
              id="nic"
              name="nic"
              type="text"
              value={nic}
              disabled
              className="h-10 w-full rounded-md border bg-muted px-3 text-sm outline-none opacity-50"
            />
            <p className="text-xs text-muted-foreground">NIC cannot be changed</p>
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
              placeholder="+94 7XX XXX XXX or 0XX XXX XXXX"
              className="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={() => navigate(`/prosumers/${nic}`)}
              className="h-10 rounded-md border px-4 text-sm font-medium hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex h-10 items-center gap-2 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:cursor-wait disabled:opacity-60"
            >
              <Save className="size-4" />
              {isSubmitting ? "Saving..." : "Save changes"}
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}
