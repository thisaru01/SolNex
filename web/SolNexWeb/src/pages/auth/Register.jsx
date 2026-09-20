import { Link } from "react-router-dom"

export default function Register() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40">
      <div className="w-full max-w-md p-8 space-y-6 bg-background rounded-xl border shadow-sm text-center">
        <h1 className="text-3xl font-bold tracking-tight">Register</h1>
        <p className="text-muted-foreground">This is a dummy registration page.</p>
        
        <div className="pt-4">
          <Link to="/dashboard" className="text-primary hover:underline font-medium">
            Bypass Registration (Go to Dashboard)
          </Link>
        </div>
        <div className="pt-2 text-sm text-muted-foreground">
          Already have an account? <Link to="/login" className="text-primary hover:underline">Login</Link>
        </div>
      </div>
    </div>
  )
}
