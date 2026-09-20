import { NavLink } from "react-router-dom"
import { BatteryCharging, LayoutDashboard, Calendar, QrCode, Users, UserSquare, History, Sun, Zap } from "lucide-react"

import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"

const backofficeItems = [
  { title: "Dashboard", url: "/dashboard", icon: <LayoutDashboard /> },
  { title: "Users", url: "/users", icon: <Users /> },
  { title: "Prosumers", url: "/prosumers", icon: <UserSquare /> },
  { title: "Stations", url: "/stations", icon: <BatteryCharging /> },
  { title: "Reservations", url: "/reservations", icon: <Calendar /> },
  { title: "Transactions", url: "/transactions", icon: <QrCode /> },
  { title: "Operational History", url: "/history", icon: <History /> },
]

const operatorItems = [
  { title: "Dashboard", url: "/dashboard", icon: <LayoutDashboard /> },
  { title: "Stations", url: "/stations", icon: <BatteryCharging /> },
  { title: "Reservations", url: "/reservations", icon: <Calendar /> },
  { title: "Transactions", url: "/transactions", icon: <QrCode /> },
  { title: "Operational History", url: "/history", icon: <History /> },
]

export function AppSidebar() {
  const role = import.meta.env.VITE_USER_ROLE || "Backoffice"
  const isBackoffice = role === "Backoffice"
  const items = isBackoffice ? backofficeItems : operatorItems

  return (
    <Sidebar>
      <SidebarHeader className="p-4 border-b">
        <div className="flex items-center gap-2">
          {isBackoffice ? <Sun className="h-6 w-6 text-amber-500" /> : <Zap className="h-6 w-6 text-amber-500" />}
          <h2 className="text-xl font-bold tracking-tight">Solar Microgrid</h2>
        </div>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>{role} Menu</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {items.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton render={
                    <NavLink to={item.url} className={({ isActive }) => isActive ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground" : ""}>
                      {item.icon}
                      <span>{item.title}</span>
                    </NavLink>
                  } />
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  )
}
