import { useMosque, useTodayPrayerTimes } from "@/hooks/use-mosque-data";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Link } from "react-router-dom";
import { Clock, Image, Settings, Activity } from "lucide-react";

const quickActions = [
  { to: "/prayer-times", label: "Update Prayer Times", icon: Clock, desc: "Edit daily schedules" },
  { to: "/slides", label: "Manage Slides", icon: Image, desc: "Digital signage content" },
  { to: "/profile", label: "Edit Profile", icon: Settings, desc: "Mosque information" },
];

const prayerNames = ["fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"] as const;
const prayerLabels: Record<string, string> = {
  fajr: "Fajr",
  sunrise: "Sunrise",
  dhuhr: "Dhuhr",
  asr: "Asr",
  maghrib: "Maghrib",
  isha: "Isha",
};

export default function DashboardPage() {
  const { data: mosque, isLoading } = useMosque();
  const { data: todayTimes } = useTodayPrayerTimes(mosque?.id);

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <div className="grid md:grid-cols-3 gap-4">
          <Skeleton className="h-32" />
          <Skeleton className="h-32" />
          <Skeleton className="h-32" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-heading font-bold">Dashboard</h1>
        <p className="text-muted-foreground mt-1">Welcome to {mosque?.name}</p>
      </div>

      {/* Summary card */}
      <Card>
        <CardContent className="p-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="flex items-center gap-4">
              {mosque?.image ? (
                <img src={mosque.image} alt={mosque.name} className="w-16 h-16 rounded-xl object-cover" />
              ) : (
                <div className="w-16 h-16 rounded-xl bg-primary/10 flex items-center justify-center">
                  <Activity className="w-7 h-7 text-primary" />
                </div>
              )}
              <div>
                <h2 className="text-xl font-heading font-semibold">{mosque?.name}</h2>
                <p className="text-sm text-muted-foreground">
                  {mosque?.city && `${mosque.city}, ${mosque.country}`}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Badge variant={mosque?.is_active ? "default" : "secondary"}>
                {mosque?.is_active ? "Active" : "Inactive"}
              </Badge>
              <span className="text-xs text-muted-foreground">
                v{mosque?.data_version} · Updated {mosque?.updated_at ? new Date(mosque.updated_at).toLocaleDateString() : "—"}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Quick actions */}
      <div>
        <h3 className="text-lg font-heading font-semibold mb-4">Quick Actions</h3>
        <div className="grid sm:grid-cols-3 gap-4">
          {quickActions.map((action) => (
            <Link key={action.to} to={action.to}>
              <Card className="hover:border-primary/40 transition-colors cursor-pointer group">
                <CardContent className="p-5 flex items-center gap-4">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center group-hover:bg-primary/20 transition-colors">
                    <action.icon className="w-5 h-5 text-primary" />
                  </div>
                  <div>
                    <p className="font-medium text-sm">{action.label}</p>
                    <p className="text-xs text-muted-foreground">{action.desc}</p>
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </div>

      {/* Today's prayer times */}
      <div>
        <h3 className="text-lg font-heading font-semibold mb-4">Today's Prayer Times</h3>
        {todayTimes ? (
          <div className="grid grid-cols-3 sm:grid-cols-6 gap-3">
            {prayerNames.map((name) => (
              <Card key={name} className="text-center">
                <CardContent className="p-4">
                  <p className="text-xs text-muted-foreground mb-1">{prayerLabels[name]}</p>
                  <p className="font-heading font-bold text-lg">
                    {todayTimes[name]
                      ? String(todayTimes[name]).slice(0, 5)
                      : "—"}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <Card>
            <CardContent className="p-6 text-center text-muted-foreground">
              <p>No prayer times set for today.</p>
              <Link to="/prayer-times" className="text-primary hover:underline text-sm mt-1 inline-block">
                Add prayer times →
              </Link>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
