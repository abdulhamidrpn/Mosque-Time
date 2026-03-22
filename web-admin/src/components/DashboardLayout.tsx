import { Link, useLocation } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";
import { useMosque } from "@/hooks/use-mosque-data";
import {
  LayoutDashboard,
  Clock,
  Image,
  Settings,
  LogOut,
  Moon as MosqueIcon,
  Menu,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { cn } from "@/lib/utils";

const navItems = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard },
  { to: "/prayer-times", label: "Prayer Times", icon: Clock },
  { to: "/slides", label: "Slides", icon: Image },
  { to: "/profile", label: "Profile", icon: Settings },
];

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { signOut } = useAuth();
  const { data: mosque } = useMosque();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <div className="min-h-screen flex bg-background">
      {/* Sidebar */}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 w-64 bg-sidebar flex flex-col transition-transform duration-300 lg:translate-x-0",
          mobileOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div className="p-6 border-b border-sidebar-border">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-sidebar-primary flex items-center justify-center">
              <MosqueIcon className="w-5 h-5 text-sidebar-primary-foreground" />
            </div>
            <div className="min-w-0">
              <h2 className="font-heading font-bold text-sidebar-foreground truncate text-sm">
                {mosque?.name || "Mosque Manager"}
              </h2>
              <p className="text-xs text-sidebar-foreground/50 truncate">
                {mosque?.city ? `${mosque.city}, ${mosque.country}` : "Admin Panel"}
              </p>
            </div>
          </div>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {navItems.map((item) => {
            const active = location.pathname === item.to;
            return (
              <Link
                key={item.to}
                to={item.to}
                onClick={() => setMobileOpen(false)}
                className={cn(
                  "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                  active
                    ? "bg-sidebar-accent text-sidebar-primary"
                    : "text-sidebar-foreground/70 hover:text-sidebar-foreground hover:bg-sidebar-accent/50"
                )}
              >
                <item.icon className="w-4 h-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-sidebar-border">
          <Button
            variant="ghost"
            className="w-full justify-start text-sidebar-foreground/60 hover:text-sidebar-foreground hover:bg-sidebar-accent/50"
            onClick={signOut}
          >
            <LogOut className="w-4 h-4 mr-2" />
            Sign Out
          </Button>
        </div>
      </aside>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 bg-foreground/20 z-40 lg:hidden" onClick={() => setMobileOpen(false)} />
      )}

      {/* Main */}
      <div className="flex-1 lg:ml-64">
        {/* Mobile header */}
        <header className="sticky top-0 z-30 bg-background/80 backdrop-blur border-b lg:hidden">
          <div className="flex items-center justify-between px-4 h-14">
            <button onClick={() => setMobileOpen(true)}>
              <Menu className="w-5 h-5" />
            </button>
            <h1 className="font-heading font-bold text-lg">Mosque Manager</h1>
            <div className="w-5" />
          </div>
        </header>

        <main className="p-4 md:p-8 max-w-6xl mx-auto animate-fade-in">
          {children}
        </main>
      </div>
    </div>
  );
}
