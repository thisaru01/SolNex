import { Outlet } from "react-router-dom"
import { SidebarProvider, SidebarTrigger, SidebarInset } from "@/components/ui/sidebar"
import { AppSidebar } from "./AppSidebar"
import { Separator } from "@/components/ui/separator"

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("solnex_user") || "null")
  } catch {
    return null
  }
}

export default function AppLayout() {
  const user = getStoredUser()
  const role = user?.role || "Guest"
  const initials = role === "Backoffice" ? "BO" : role === "GridOperator" ? "GO" : "PS"

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center gap-2 border-b bg-background px-4">
          <SidebarTrigger className="-ml-1" />
          <Separator orientation="vertical" className="mr-2 h-4" />
          <div className="flex-1 flex justify-between items-center">
            <h1 className="text-lg font-semibold tracking-tight">Microgrid Management System</h1>
            <div className="flex items-center gap-4">
              <span className="text-sm text-muted-foreground hidden sm:block">Role: {role}</span>
              <div className="h-8 w-8 rounded-full bg-primary flex items-center justify-center font-bold text-primary-foreground text-xs">
                {initials}
              </div>
            </div>
          </div>
        </header>
        <main className="p-4 md:p-6 flex-1 overflow-auto bg-muted/20">
          <div className="mx-auto max-w-7xl">
            <Outlet />
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}
