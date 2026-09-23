import { NavLink, useLocation } from "react-router-dom"
import { BatteryCharging, LayoutDashboard, Calendar, QrCode, Users, UserSquare, History, Sun, Zap, ChevronDown, ChevronUp } from "lucide-react"

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
  SidebarMenuSub,
  SidebarMenuSubItem,
  SidebarMenuSubButton,
} from "@/components/ui/sidebar"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"

const backofficeItems = [
  { title: "Dashboard", url: "/dashboard", icon: <LayoutDashboard /> },
  { title: "Users", url: "/users", icon: <Users /> },
  { title: "Prosumers", url: "/prosumers", icon: <UserSquare /> },
  { 
    title: "Stations", url: "/stations", icon: <BatteryCharging />,
    subItems: [
      { title: "Station List", url: "/stations" },
      { title: "Station Map", url: "/stations/map" }
    ]
  },
  { title: "Reservations", url: "/reservations", icon: <Calendar /> },
  { title: "Transactions", url: "/transactions", icon: <QrCode /> },
  { title: "Operational History", url: "/history", icon: <History /> },
]

const operatorItems = [
  { title: "Dashboard", url: "/dashboard", icon: <LayoutDashboard /> },
  { 
    title: "Stations", url: "/stations", icon: <BatteryCharging />,
    subItems: [
      { title: "Station List", url: "/stations" },
      { title: "Station Map", url: "/stations/map" }
    ]
  },
  { title: "Reservations", url: "/reservations", icon: <Calendar /> },
  { title: "Transactions", url: "/transactions", icon: <QrCode /> },
  { title: "Operational History", url: "/history", icon: <History /> },
]

export function AppSidebar() {
  const role = import.meta.env.VITE_USER_ROLE || "Backoffice"
  const isBackoffice = role === "Backoffice"
  const items = isBackoffice ? backofficeItems : operatorItems
  const location = useLocation()

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
              {items.map((item) => {
                const isActiveMain = location.pathname.startsWith(item.url)
                
                if (item.subItems) {
                  return (
                    <Collapsible key={item.title} asChild defaultOpen={isActiveMain} className="group/collapsible">
                      <SidebarMenuItem>
                        <CollapsibleTrigger asChild>
                          <SidebarMenuButton tooltip={item.title}>
                            {item.icon}
                            <span>{item.title}</span>
                            <ChevronDown className="ml-auto group-data-[state=open]/collapsible:hidden" />
                            <ChevronUp className="ml-auto hidden group-data-[state=open]/collapsible:block" />
                          </SidebarMenuButton>
                        </CollapsibleTrigger>
                        <CollapsibleContent>
                          <SidebarMenuSub>
                            {item.subItems.map((sub) => (
                              <SidebarMenuSubItem key={sub.title}>
                                <SidebarMenuSubButton 
                                  isActive={location.pathname === sub.url}
                                  render={
                                    <NavLink 
                                      to={sub.url} 
                                      end 
                                      className={({ isActive }) => isActive ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground" : ""}
                                    >
                                      <span>{sub.title}</span>
                                    </NavLink>
                                  }
                                />
                              </SidebarMenuSubItem>
                            ))}
                          </SidebarMenuSub>
                        </CollapsibleContent>
                      </SidebarMenuItem>
                    </Collapsible>
                  )
                }

                return (
                  <SidebarMenuItem key={item.title}>
                    <SidebarMenuButton 
                      isActive={isActiveMain}
                      render={
                        <NavLink 
                          to={item.url} 
                          end
                          className={({ isActive }) => isActive ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground" : ""}
                        >
                          {item.icon}
                          <span>{item.title}</span>
                        </NavLink>
                      } 
                    />
                  </SidebarMenuItem>
                )
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  )
}
