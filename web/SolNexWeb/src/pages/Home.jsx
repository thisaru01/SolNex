import { Link } from 'react-router-dom';
import { ArrowRight, Zap, Shield, Globe } from 'lucide-react';

export default function Home() {
  return (
    <div className="min-h-screen bg-[#f4f7f2] text-[#173b2c] overflow-hidden relative">
      {/* Background Gradients */}
      <div className="absolute top-0 -left-1/4 w-1/2 h-1/2 bg-[#d1efa4]/30 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-0 -right-1/4 w-1/2 h-1/2 bg-[#e3f1d4]/60 rounded-full blur-[120px] pointer-events-none" />

      {/* Navigation */}
      <nav className="relative z-10 flex items-center justify-between p-4 sm:p-6 lg:px-12 backdrop-blur-md border-b border-[#d7e1d4] bg-[#f4f7f2]/80">
        <div className="flex items-center gap-3">
          <Zap className="w-6 h-6 sm:w-8 sm:h-8 text-emerald-400" />
          <span className="text-xl sm:text-2xl font-semibold tracking-tight text-[#173b2c]">SolNex</span>
        </div>
        <div className="flex items-center gap-1 sm:gap-4">
          <Link to="/login" className="px-3 sm:px-5 py-2 text-xs sm:text-sm font-medium text-[#4b8661] hover:text-[#173b2c] transition-colors">
            Sign In
          </Link>
          <Link to="/register" className="px-4 sm:px-5 py-2 text-xs sm:text-sm font-medium bg-[#173b2c] hover:bg-[#24563f] text-[#f4f7f2] rounded-full transition-all shadow-[0_0_15px_rgba(23,59,44,0.1)] hover:shadow-[0_0_25px_rgba(23,59,44,0.2)] flex items-center gap-1 sm:gap-2">
            Sign Up <ArrowRight className="hidden sm:block w-4 h-4" />
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="relative z-10 flex flex-col items-center justify-center min-h-[calc(100vh-80px)] px-6 text-center">

        <h1 className="mt-10 text-5xl md:text-7xl font-semibold tracking-[-0.05em] mb-6 max-w-4xl leading-[1.02] text-[#173b2c]">
          Energy moves better <br />
          <span className="text-[#4b8661]">
            when everyone is connected.
          </span>
        </h1>
        
        <p className="text-lg md:text-xl text-[#6c7e72] max-w-2xl mb-12 leading-7">
          A single workspace for the people managing clean energy, trusted access, and every connection in between. Join the decentralized grid.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 mb-20 w-full sm:w-auto">
          <Link to="/register" className="px-8 py-4 text-base font-semibold bg-[#173b2c] text-[#f4f7f2] rounded-full hover:bg-[#24563f] transition-all shadow-[0_0_20px_rgba(23,59,44,0.1)] hover:scale-105 flex items-center justify-center gap-2">
            Create an Account <ArrowRight className="w-5 h-5" />
          </Link>
          <Link to="/login" className="px-8 py-4 text-base font-semibold border border-[#d7e1d4] bg-white hover:bg-[#f4f7f2] text-[#173b2c] rounded-full transition-all hover:scale-105 flex items-center justify-center shadow-sm">
            Sign In to Dashboard
          </Link>
        </div>

        {/* Feature Cards / Glassmorphism */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-5xl w-full pb-12">
          <div className="p-6 rounded-2xl bg-white/60 border border-[#d7e1d4] backdrop-blur-md hover:bg-white transition-colors text-left group shadow-sm">
            <div className="w-12 h-12 rounded-xl bg-[#e3f1d4] flex items-center justify-center mb-4 text-[#356545] group-hover:scale-110 transition-transform">
              <Shield className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-semibold mb-2 text-[#173b2c]">Secure Trading</h3>
            <p className="text-[#6c7e72] text-sm leading-relaxed">Accounts are protected by role-based access ensuring your energy transactions are transparent and immutable.</p>
          </div>
          <div className="p-6 rounded-2xl bg-white/60 border border-[#d7e1d4] backdrop-blur-md hover:bg-white transition-colors text-left group shadow-sm">
            <div className="w-12 h-12 rounded-xl bg-[#e3f1d4] flex items-center justify-center mb-4 text-[#356545] group-hover:scale-110 transition-transform">
              <Zap className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-semibold mb-2 text-[#173b2c]">Real-time Analytics</h3>
            <p className="text-[#6c7e72] text-sm leading-relaxed">Monitor your energy grid, station loads, and transactions as they happen with millisecond precision.</p>
          </div>
          <div className="p-6 rounded-2xl bg-white/60 border border-[#d7e1d4] backdrop-blur-md hover:bg-white transition-colors text-left group shadow-sm">
            <div className="w-12 h-12 rounded-xl bg-[#e3f1d4] flex items-center justify-center mb-4 text-[#356545] group-hover:scale-110 transition-transform">
              <Globe className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-semibold mb-2 text-[#173b2c]">Global Network</h3>
            <p className="text-[#6c7e72] text-sm leading-relaxed">Connect with prosumers across the globe. Buy and sell energy freely in a decentralized marketplace.</p>
          </div>
        </div>
      </main>
    </div>
  );
}
