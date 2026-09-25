import { useState } from "react"
import { ArrowRight, Eye, EyeOff, LockKeyhole, ShieldCheck, Sun } from "lucide-react"
import { Link, useNavigate } from "react-router-dom"
import { registerProsumer } from "../../services/authApi"

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
  const [showPassword, setShowPassword] = useState(false)

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError("")
    setIsSubmitting(true)

    try {
      await registerProsumer(form)
      navigate("/login", {
        replace: true,
        state: {
          message: "Registration submitted. A Backoffice officer must activate your account before you can sign in.",
        },
      })
    } catch (submissionError) {
      setError(submissionError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="min-h-screen bg-[#f4f7f2] text-[#17261f] lg:grid lg:grid-cols-[minmax(0,1.05fr)_minmax(440px,.95fr)]">
      <section className="relative hidden min-h-screen overflow-hidden bg-[#173b2c] p-10 text-[#f4f7f2] lg:flex lg:flex-col lg:justify-between xl:p-16">
        <div className="absolute -right-40 -top-40 size-[34rem] rounded-full border border-[#b9dc93]/20" />
        <div className="absolute -right-8 top-16 size-[20rem] rounded-full border border-[#b9dc93]/15" />
        <div className="relative flex items-center gap-3"><span className="grid size-10 place-items-center rounded-full bg-[#d1efa4] text-lg font-bold text-[#173b2c]">S</span><span className="font-semibold tracking-tight">SolNex</span></div>
        <div className="relative max-w-xl space-y-7"><p className="text-xs font-bold uppercase tracking-[0.24em] text-[#9bc69b]">Join the energy exchange</p><h1 className="text-5xl font-semibold leading-[1.02] tracking-[-0.05em] xl:text-7xl">Your energy can go further.</h1><p className="max-w-md text-base leading-7 text-[#bdd0c0]">Register as a prosumer and become part of a cleaner, smarter local energy network.</p><div className="flex items-center gap-3 pt-4"><span className="h-1 w-16 bg-[#d1efa4]" /><span className="h-1 w-6 bg-[#d1efa4]/50" /><span className="h-1 w-2 bg-[#d1efa4]/25" /></div></div>
        <div className="relative flex items-center gap-2 text-xs text-[#a9c2ad]"><ShieldCheck className="size-4 text-[#d1efa4]" /> Your account stays protected and under your control</div>
      </section>

      <section className="flex min-h-screen items-center justify-center px-5 py-10 sm:px-10 lg:px-14 xl:px-20">
        <div className="w-full max-w-lg">
          <div className="mb-8 flex items-center gap-3 lg:hidden"><span className="grid size-10 place-items-center rounded-full bg-[#173b2c] text-lg font-bold text-[#d1efa4]">S</span><span className="font-semibold tracking-tight">SolNex</span></div>
          <div className="mb-7 space-y-3"><div className="flex size-11 items-center justify-center rounded-xl bg-[#e3f1d4] text-[#356545]"><Sun className="size-5" /></div><p className="pt-3 text-xs font-bold uppercase tracking-[0.2em] text-[#4b8661]">Solar prosumer</p><h2 className="text-4xl font-semibold tracking-[-0.045em] text-[#173b2c]">Create your account</h2><p className="text-sm leading-6 text-[#6c7e72]">Your account will be pending until a Backoffice officer activates it.</p></div>
          {error && <p role="alert" className="mb-5 rounded-lg border border-[#efc8bd] bg-[#fff1ed] px-4 py-3 text-sm leading-5 text-[#9a493d]">{error}</p>}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2"><label htmlFor="nic" className="text-sm font-semibold text-[#274534]">National Identity Card</label><input id="nic" name="nic" value={form.nic} onChange={updateField} required minLength={5} maxLength={20} placeholder="Enter your NIC" className="h-11 w-full rounded-lg border border-[#d7e1d4] bg-white px-4 text-sm text-[#173b2c] outline-none transition focus:border-[#4b8661] focus:ring-4 focus:ring-[#4b8661]/10" /></div>
            <div className="space-y-2"><label htmlFor="fullName" className="text-sm font-semibold text-[#274534]">Full name</label><input id="fullName" name="fullName" value={form.fullName} onChange={updateField} required minLength={2} maxLength={120} placeholder="Enter your full name" className="h-11 w-full rounded-lg border border-[#d7e1d4] bg-white px-4 text-sm text-[#173b2c] outline-none transition focus:border-[#4b8661] focus:ring-4 focus:ring-[#4b8661]/10" /></div>
            <div className="grid gap-4 sm:grid-cols-2"><div className="space-y-2"><label htmlFor="email" className="text-sm font-semibold text-[#274534]">Email</label><input id="email" name="email" type="email" value={form.email} onChange={updateField} required placeholder="name@example.com" className="h-11 w-full rounded-lg border border-[#d7e1d4] bg-white px-4 text-sm text-[#173b2c] outline-none transition focus:border-[#4b8661] focus:ring-4 focus:ring-[#4b8661]/10" /></div><div className="space-y-2"><label htmlFor="phone" className="text-sm font-semibold text-[#274534]">Phone</label><input id="phone" name="phone" type="tel" value={form.phone} onChange={updateField} required placeholder="+94 77 123 4567" className="h-11 w-full rounded-lg border border-[#d7e1d4] bg-white px-4 text-sm text-[#173b2c] outline-none transition focus:border-[#4b8661] focus:ring-4 focus:ring-[#4b8661]/10" /></div></div>
            <div className="space-y-2"><label htmlFor="password" className="text-sm font-semibold text-[#274534]">Password</label><div className="relative"><input id="password" name="password" type={showPassword ? "text" : "password"} value={form.password} onChange={updateField} required minLength={8} maxLength={100} placeholder="At least 8 characters" className="h-11 w-full rounded-lg border border-[#d7e1d4] bg-white px-4 pr-12 text-sm text-[#173b2c] outline-none transition focus:border-[#4b8661] focus:ring-4 focus:ring-[#4b8661]/10" /><button type="button" onClick={() => setShowPassword((current) => !current)} aria-label={showPassword ? "Hide password" : "Show password"} className="absolute right-3 top-1/2 -translate-y-1/2 p-2 text-[#809287] hover:text-[#274534]">{showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}</button></div></div>
            <button type="submit" disabled={isSubmitting} className="flex h-12 w-full items-center justify-between rounded-lg bg-[#173b2c] px-5 text-sm font-semibold text-white transition hover:bg-[#24563f] disabled:cursor-wait disabled:opacity-60">{isSubmitting ? "Creating account..." : "Create account"}<ArrowRight className="size-4" /></button>
          </form>
          <div className="mt-7 flex items-center gap-2 border-t border-[#dfe7dc] pt-5 text-sm text-[#718176]"><LockKeyhole className="size-4 text-[#4b8661]" /><span>Activation is handled securely by Backoffice.</span></div>
          <p className="mt-5 text-center text-sm text-[#718176]">Already registered? <Link to="/login" className="font-semibold text-[#356545] hover:underline">Sign in</Link></p>
        </div>
      </section>
    </main>
  )
}
