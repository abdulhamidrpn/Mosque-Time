import { supabase } from "@/integrations/supabase/client";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { Tables, TablesInsert, TablesUpdate } from "@/integrations/supabase/types";
import { useAuth } from "./use-auth";

export type Mosque = Tables<"mosques">;
export type PrayerTime = Tables<"prayer_times">;
export type MosqueSlide = Tables<"mosque_slides">;

export function useMosque() {
  const { user } = useAuth();
  return useQuery({
    queryKey: ["mosque", user?.id],
    enabled: !!user?.id,
    queryFn: async () => {
      const { data, error } = await supabase
        .from("mosques")
        .select("*")
        .eq("owner_uid", user!.id)
        .limit(1)
        .maybeSingle();
      if (error) throw error;
      return data;
    },
  });
}

export function useUpdateMosque() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, ...updates }: TablesUpdate<"mosques"> & { id: string }) => {
      const { data, error } = await supabase
        .from("mosques")
        .update(updates)
        .eq("id", id)
        .select()
        .single();
      if (error) throw error;
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["mosque"] }),
  });
}

export function useCreateMosque() {
  const qc = useQueryClient();
  const { user } = useAuth();
  return useMutation({
    mutationFn: async (mosque: Omit<TablesInsert<"mosques">, "owner_uid">) => {
      if (!user?.id) throw new Error("User not authenticated");
      const { data, error } = await supabase
        .from("mosques")
        .insert({
          ...mosque,
          owner_uid: user.id,
        })
        .select()
        .single();
      if (error) throw error;
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["mosque"] }),
  });
}

export function useTodayPrayerTimes(mosqueId: string | undefined) {
  return useQuery({
    queryKey: ["prayer-times-today", mosqueId],
    enabled: !!mosqueId,
    queryFn: async () => {
      const today = new Date().toISOString().split("T")[0];
      const { data, error } = await supabase
        .from("prayer_times")
        .select("*")
        .eq("mosque_id", mosqueId!)
        .eq("date", today)
        .maybeSingle();
      if (error) throw error;
      return data;
    },
  });
}

export function usePrayerTimesForMonth(mosqueId: string | undefined, year: number, month: number) {
  return useQuery({
    queryKey: ["prayer-times-month", mosqueId, year, month],
    enabled: !!mosqueId,
    queryFn: async () => {
      const startDate = `${year}-${String(month).padStart(2, "0")}-01`;
      const endDay = new Date(year, month, 0).getDate();
      const endDate = `${year}-${String(month).padStart(2, "0")}-${String(endDay).padStart(2, "0")}`;
      const { data, error } = await supabase
        .from("prayer_times")
        .select("*")
        .eq("mosque_id", mosqueId!)
        .gte("date", startDate)
        .lte("date", endDate)
        .order("date");
      if (error) throw error;
      return data;
    },
  });
}

export function useUpsertPrayerTime() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (pt: TablesInsert<"prayer_times">) => {
      const { data, error } = await supabase
        .from("prayer_times")
        .upsert(pt, { onConflict: "mosque_id,date" })
        .select()
        .single();
      if (error) throw error;
      return data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["prayer-times-today"] });
      qc.invalidateQueries({ queryKey: ["prayer-times-month"] });
    },
  });
}

export function useBulkUpsertPrayerTimes() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (rows: TablesInsert<"prayer_times">[]) => {
      // Upsert in batches of 50
      for (let i = 0; i < rows.length; i += 50) {
        const batch = rows.slice(i, i + 50);
        const { error } = await supabase
          .from("prayer_times")
          .upsert(batch, { onConflict: "mosque_id,date" });
        if (error) throw error;
      }
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["prayer-times-today"] });
      qc.invalidateQueries({ queryKey: ["prayer-times-month"] });
    },
  });
}

export function useUpdateSlideOrder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (slides: { id: string; display_order: number }[]) => {
      for (const s of slides) {
        const { error } = await supabase
          .from("mosque_slides")
          .update({ display_order: s.display_order })
          .eq("id", s.id);
        if (error) throw error;
      }
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["mosque-slides"] }),
  });
}

export function useMosqueSlides(mosqueId: string | undefined) {
  return useQuery({
    queryKey: ["mosque-slides", mosqueId],
    enabled: !!mosqueId,
    queryFn: async () => {
      const { data, error } = await supabase
        .from("mosque_slides")
        .select("*")
        .eq("mosque_id", mosqueId!)
        .order("display_order");
      if (error) throw error;
      return data;
    },
  });
}

export function useDeleteSlide() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, imageUrl }: { id: string; imageUrl: string }) => {
      // Delete from storage
      const url = new URL(imageUrl);
      const pathParts = url.pathname.split("/mosque-slides/");
      if (pathParts[1]) {
        await supabase.storage.from("mosque-slides").remove([decodeURIComponent(pathParts[1])]);
      }
      // Delete from database
      const { error } = await supabase.from("mosque_slides").delete().eq("id", id);
      if (error) throw error;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["mosque-slides"] }),
  });
}

/**
 * Check if image with same hash already exists in the mosque
 * Returns the existing slide if duplicate found, null otherwise
 */
export async function checkDuplicateImage(mosqueId: string, imageHash: string): Promise<MosqueSlide | null> {
  const { data, error } = await supabase
    .from("mosque_slides")
    .select("*")
    .eq("mosque_id", mosqueId)
    .eq("image_hash", imageHash)
    .maybeSingle();
  
  if (error) throw error;
  return data || null;
}
