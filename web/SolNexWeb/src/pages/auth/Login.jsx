import { useState } from "react"
import { ArrowRight, Eye, EyeOff, LockKeyhole, ShieldCheck, Sun } from "lucide-react"
import { Link, useLocation, useNavigate } from "react-router-dom"
import { login } from "../../services/authApi"

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const [identifier, setIdentifier] = useState("")
  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const notice = location.state?.message

  async function handleSubmit(event) {
    event.preventDefault()
    setError("")
    setIsSubmitting(true)

    try {
      const response = await login({ identifier: identifier.trim(), password })
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
    <main className="min-h-screen bg-[#f4f7f2] text-[#17261f] lg:grid lg:grid-cols-[minmax(0,1.05fr)_minmax(440px,.95fr)]">
      <section className="relative hidden min-h-screen overflow-hidden bg-[#173b2c] p-10 text-[#f4f7f2] lg:flex lg:flex-col lg:justify-between xl:p-16">
        <div className="absolute -right-40 -top-40 size-[34rem] rounded-full border border-[#b9dc93]/20" />
        <div className="absolute -right-8 top-16 size-[20rem] rounded-full border border-[#b9dc93]/15" />
        <div className="relative flex items-center gap-3"><span className="grid size-10 place-items-center rounded-full bg-[#d1efa4] text-lg font-bold text-[#173b2c]">S</span><span className="font-semibold tracking-tight">SolNex</span></div>
        <div className="relative max-w-xl space-y-7"><p className="text-xs font-bold uppercase tracking-[0.24em] text-[#9bc69b]">Solar microgrid control</p><h1 className="text-5xl font-semibold leading-[1.02] tracking-[-0.05em] xl:text-7xl">Energy moves better when everyone is connected.</h1><p className="max-w-md text-base leading-7 text-[#bdd0c0]">A single workspace for the people managing clean energy, trusted access, and every connection in between.</p><div className="flex items-center gap-3 pt-4 text-sm text-[#d1efa4]"><span className="h-1 w-16 bg-[#d1efa4]" /><span className="h-1 w-6 bg-[#d1efa4]/50" /><span className="h-1 w-2 bg-[#d1efa4]/25" /></div></div>
        <div className="relative flex items-center gap-2 text-xs text-[#a9c2ad]"><ShieldCheck className="size-4 text-[#d1efa4]" /> Secure access for every SolNex role</div>
      </section>

      <section className="flex min-h-screen items-center justify-center px-5 py-10 sm:px-10 lg:px-14 xl:px-24">
        <div className="w-full max-w-md">
          <div className="mb-10 flex items-center gap-3 lg:hidden"><span className="grid size-10 place-items-center rounded-full bg-[#173b2c] text-lg font-bold text-[#d1efa4]">S</span><span className="font-semibold tracking-tight">SolNex</span></div>
          <div className="mb-8 space-y-3"><div className="flex size-11 items-center justify-center rounded-xl bg-[#e3f1d4] text-[#356545]"><Sun className="size-5" /></div><p className="pt-3 text-xs font-bold uppercase tracking-[0.2em] text-[#4b8661]">Welcome back</p><h2 className="text-4xl font-semibold tracking-[-0.045em] text-[#173b2c]">Sign in to SolNex</h2><p className="text-sm leading-6 text-[#6c7e72]">Use your NIC or registered email to access the control centre.</p></div>
          {location.state?.message && <p role="status" className="mb-5 rounded-lg border border-[#b9d8b7] bg-[#edf7e8] px-4 py-3 text-sm leading-5 text-[#356545]">{location.state.message}</p>}
          {error && <p role="alert" className="mb-5 rounded-lg border border-[#efc8bd] bg-[#fff1ed] px-4 py-3 text-sm leading-5 text-[#9a493d]">{error}</p>}
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2"><label htmlFor="identifier" className="text-sm font-semibold text-[#274534]">NIC or email</label><input id="identifier" type="text" value={identifier} onChange={(event) => setIdentifier(event.target.value)} required autoComplete="username" placeholder="Enter your NIC or email" className="h-12 w-full rounded-lg border border-[#d7e1d4] bg-white px-4 text-sm text-[#173b2c] outline-none transition focus:border-[#4b8661] focus:ring-4 focus:ring-[#4b8661]/10" /></div>
            <div className="space-y-2"><label htmlFor="password" className="text-sm font-semibold text-[#274534]">Password</label><div className="relative"><input id="password" type={showPassword ? "text" : "password"} value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" placeholder="Enter your password" className="h-12 w-full rounded-lg border border-[#d7e1d4] bg-white px-4 pr-12 text-sm text-[#173b2c] outline-none transition focus:border-[#4b8661] focus:ring-4 focus:ring-[#4b8661]/10" /><button type="button" onClick={() => setShowPassword((current) => !current)} aria-label={showPassword ? "Hide password" : "Show password"} className="absolute right-3 top-1/2 -translate-y-1/2 p-2 text-[#809287] hover:text-[#274534]">{showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}</button></div></div>
            <button type="submit" disabled={isSubmitting} className="flex h-12 w-full items-center justify-between rounded-lg bg-[#173b2c] px-5 text-sm font-semibold text-white transition hover:bg-[#24563f] disabled:cursor-wait disabled:opacity-60">{isSubmitting ? "Signing in..." : "Sign in"}<ArrowRight className="size-4" /></button>
          </form>
          <div className="mt-8 flex items-center gap-2 border-t border-[#dfe7dc] pt-6 text-sm text-[#718176]"><LockKeyhole className="size-4 text-[#4b8661]" /><span>Accounts are protected by role-based access.</span></div>
          <p className="mt-6 text-center text-sm text-[#718176]">New prosumer? <Link to="/register" className="font-semibold text-[#356545] hover:underline">Create an account</Link></p>
        </div>
      </section>
    </main>
  )
}
