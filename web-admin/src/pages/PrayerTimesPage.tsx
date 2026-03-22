import { useState, useRef } from "react";
import { useMosque, usePrayerTimesForMonth, useUpsertPrayerTime, useBulkUpsertPrayerTimes } from "@/hooks/use-mosque-data";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { toast } from "@/hooks/use-toast";
import { ChevronLeft, ChevronRight, Upload, Copy } from "lucide-react";
import { cn } from "@/lib/utils";

const prayerFields = ["fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"] as const;
const prayerLabels: Record<string, string> = {
  fajr: "Fajr", sunrise: "Sunrise", dhuhr: "Dhuhr", asr: "Asr", maghrib: "Maghrib", isha: "Isha",
};

export default function PrayerTimesPage() {
  const { data: mosque } = useMosque();
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const { data: monthTimes, isLoading } = usePrayerTimesForMonth(mosque?.id, year, month);
  const upsertPT = useUpsertPrayerTime();
  const bulkUpsert = useBulkUpsertPrayerTimes();
  const csvRef = useRef<HTMLInputElement>(null);

  const [editDate, setEditDate] = useState<string | null>(null);
  const [editForm, setEditForm] = useState<Record<string, string>>({});

  const daysInMonth = new Date(year, month, 0).getDate();
  const firstDayOfWeek = new Date(year, month - 1, 1).getDay();
  const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);
  const timesMap = new Map((monthTimes ?? []).map((t) => [t.date, t]));

  const monthName = new Date(year, month - 1).toLocaleString("default", { month: "long" });

  const prevMonth = () => {
    if (month === 1) { setMonth(12); setYear(year - 1); } else setMonth(month - 1);
  };
  const nextMonth = () => {
    if (month === 12) { setMonth(1); setYear(year + 1); } else setMonth(month + 1);
  };

  const openEdit = (day: number) => {
    const dateStr = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    setEditDate(dateStr);
    const existing = timesMap.get(dateStr);
    const form: Record<string, string> = {};
    prayerFields.forEach((f) => {
      form[f] = existing?.[f] ? String(existing[f]).slice(0, 5) : "";
    });
    setEditForm(form);
  };

  const handleSave = async () => {
    if (!mosque || !editDate) return;
    try {
      const payload: any = { mosque_id: mosque.id, date: editDate };
      prayerFields.forEach((f) => {
        payload[f] = editForm[f] ? editForm[f] + ":00" : null;
      });
      await upsertPT.mutateAsync(payload);
      toast({ title: "Saved", description: `Prayer times for ${editDate} updated.` });
      setEditDate(null);
    } catch (err: any) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    }
  };

  // Copy from previous day
  const handleCopyPrevious = () => {
    if (!editDate) return;
    const prev = new Date(editDate);
    prev.setDate(prev.getDate() - 1);
    const prevStr = prev.toISOString().split("T")[0];
    const prevData = timesMap.get(prevStr);
    if (!prevData) {
      toast({ title: "No data", description: "No prayer times found for the previous day.", variant: "destructive" });
      return;
    }
    const form: Record<string, string> = {};
    prayerFields.forEach((f) => {
      form[f] = prevData[f] ? String(prevData[f]).slice(0, 5) : "";
    });
    setEditForm(form);
    toast({ title: "Copied", description: `Times copied from ${prevStr}` });
  };

  // CSV upload handler
  const handleCSVUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !mosque) return;
    try {
      const text = await file.text();
      const lines = text.trim().split("\n");
      if (lines.length < 2) throw new Error("CSV must have a header row and at least one data row.");

      const header = lines[0].toLowerCase().split(",").map((h) => h.trim());
      const dateIdx = header.indexOf("date");
      if (dateIdx === -1) throw new Error("CSV must have a 'date' column.");

      const fieldIndices: Record<string, number> = {};
      prayerFields.forEach((f) => {
        const idx = header.indexOf(f);
        if (idx !== -1) fieldIndices[f] = idx;
      });

      const rows = lines.slice(1).filter((l) => l.trim()).map((line) => {
        const cols = line.split(",").map((c) => c.trim());
        const row: any = { mosque_id: mosque.id, date: cols[dateIdx] };
        prayerFields.forEach((f) => {
          if (fieldIndices[f] !== undefined) {
            const val = cols[fieldIndices[f]];
            if (val) {
              // Ensure HH:MM:SS format
              row[f] = val.length === 5 ? val + ":00" : val;
            } else {
              row[f] = null;
            }
          }
        });
        // Validate date format
        if (!/^\d{4}-\d{2}-\d{2}$/.test(row.date)) {
          throw new Error(`Invalid date format: ${row.date}. Use YYYY-MM-DD.`);
        }
        return row;
      });

      await bulkUpsert.mutateAsync(rows);
      toast({ title: "CSV Imported", description: `${rows.length} prayer time(s) imported successfully.` });
    } catch (err: any) {
      toast({ title: "CSV Error", description: err.message, variant: "destructive" });
    } finally {
      e.target.value = "";
    }
  };

  const todayStr = new Date().toISOString().split("T")[0];

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold">Prayer Times</h1>
          <p className="text-muted-foreground mt-1">Click on a day to edit prayer times</p>
        </div>
        <label>
          <Button variant="outline" disabled={bulkUpsert.isPending} onClick={() => csvRef.current?.click()}>
            <Upload className="w-4 h-4 mr-2" />
            {bulkUpsert.isPending ? "Importing..." : "Upload CSV"}
          </Button>
          <input
            ref={csvRef}
            type="file"
            accept=".csv"
            className="hidden"
            onChange={handleCSVUpload}
          />
        </label>
      </div>

      <Card className="text-xs text-muted-foreground">
        <CardContent className="pt-4">
          <p><strong>CSV format:</strong> <code>date,fajr,sunrise,dhuhr,asr,maghrib,isha</code></p>
          <p>Date format: <code>YYYY-MM-DD</code> · Time format: <code>HH:MM</code> or <code>HH:MM:SS</code></p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <Button variant="ghost" size="icon" onClick={prevMonth}><ChevronLeft className="w-4 h-4" /></Button>
            <CardTitle className="text-lg font-heading">{monthName} {year}</CardTitle>
            <Button variant="ghost" size="icon" onClick={nextMonth}><ChevronRight className="w-4 h-4" /></Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-64" />
          ) : (
            <>
              <div className="grid grid-cols-7 gap-1 mb-2">
                {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((d) => (
                  <div key={d} className="text-center text-xs font-medium text-muted-foreground py-1">{d}</div>
                ))}
              </div>
              <div className="grid grid-cols-7 gap-1">
                {Array.from({ length: firstDayOfWeek }).map((_, i) => (
                  <div key={`empty-${i}`} />
                ))}
                {days.map((day) => {
                  const dateStr = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
                  const hasData = timesMap.has(dateStr);
                  const isToday = dateStr === todayStr;
                  return (
                    <button
                      key={day}
                      onClick={() => openEdit(day)}
                      className={cn(
                        "aspect-square rounded-lg text-sm font-medium flex flex-col items-center justify-center transition-colors border",
                        isToday && "border-primary",
                        !isToday && "border-transparent",
                        hasData ? "bg-primary/10 text-primary hover:bg-primary/20" : "hover:bg-muted text-foreground"
                      )}
                    >
                      {day}
                      {hasData && <div className="w-1.5 h-1.5 rounded-full bg-primary mt-0.5" />}
                    </button>
                  );
                })}
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* Edit dialog */}
      <Dialog open={!!editDate} onOpenChange={() => setEditDate(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="font-heading">Edit Times — {editDate}</DialogTitle>
          </DialogHeader>
          <Button variant="outline" size="sm" onClick={handleCopyPrevious} className="w-full">
            <Copy className="w-4 h-4 mr-2" />
            Copy from previous day
          </Button>
          <div className="grid grid-cols-2 gap-4">
            {prayerFields.map((f) => (
              <div key={f} className="space-y-1">
                <Label className="text-xs">{prayerLabels[f]}</Label>
                <Input
                  type="time"
                  value={editForm[f] || ""}
                  onChange={(e) => setEditForm({ ...editForm, [f]: e.target.value })}
                />
              </div>
            ))}
          </div>
          <Button onClick={handleSave} disabled={upsertPT.isPending} className="w-full mt-2">
            {upsertPT.isPending ? "Saving..." : "Save Prayer Times"}
          </Button>
        </DialogContent>
      </Dialog>
    </div>
  );
}
