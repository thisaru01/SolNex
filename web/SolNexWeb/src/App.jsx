import { useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:5097'

function App() {
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [status, setStatus] = useState({ type: '', message: '' })
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setStatus({ type: '', message: '' })
    setIsSubmitting(true)

    try {
      const response = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identifier, password }),
      })
      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'Unable to sign in. Please try again.')
      }

      localStorage.setItem('solnex_token', data.token)
      localStorage.setItem('solnex_user', JSON.stringify(data.user))
      setStatus({ type: 'success', message: `Welcome back, ${data.user.fullName}.` })
      setPassword('')
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="login-shell">
      <section className="brand-panel" aria-label="SolNex introduction">
        <div className="brand-mark">S</div>
        <p className="eyebrow">SOLAR MICROGRID CONTROL</p>
        <h1>Powering a smarter exchange of energy.</h1>
        <p className="brand-copy">
          One secure workspace for the people who keep every solar connection moving.
        </p>
        <div className="network-line" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>
      </section>

      <section className="form-panel">
        <div className="form-content">
          <p className="eyebrow">WELCOME BACK</p>
          <h2>Sign in to SolNex</h2>
          <p className="form-intro">Use your NIC or registered email to access the control centre.</p>

          <form onSubmit={handleSubmit}>
            <label htmlFor="identifier">NIC or email</label>
            <input
              id="identifier"
              type="text"
              value={identifier}
              onChange={(event) => setIdentifier(event.target.value)}
              placeholder="e.g. 199012345678 or name@solnex.lk"
              autoComplete="username"
              required
            />

            <div className="label-row">
              <label htmlFor="password">Password</label>
              <span>Secure access</span>
            </div>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Enter your password"
              autoComplete="current-password"
              required
            />

            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Signing in...' : 'Sign in'}
              <span aria-hidden="true">-&gt;</span>
            </button>
          </form>

          {status.message && (
            <p className={`status-message ${status.type}`} role="status">
              {status.message}
            </p>
          )}
        </div>
        <p className="support-text">Need access? Contact your SolNex backoffice administrator.</p>
      </section>
    </main>
  )
}

export default App
