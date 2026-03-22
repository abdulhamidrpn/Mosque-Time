import { useState } from "react";
import { useMosque, useUpdateMosque } from "@/hooks/use-mosque-data";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "@/hooks/use-toast";
import { Upload } from "lucide-react";

export default function ProfilePage() {
  const { data: mosque, isLoading } = useMosque();
  const updateMosque = useUpdateMosque();
  const [uploading, setUploading] = useState(false);

  const [form, setForm] = useState<Record<string, string>>({});

  const getValue = (field: string) => form[field] ?? (mosque as any)?.[field] ?? "";

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!mosque) return;
    try {
      await updateMosque.mutateAsync({
        id: mosque.id,
        name: getValue("name"),
        address: getValue("address") || null,
        jumua_time: getValue("jumua_time") || null,
        bottom_message: getValue("bottom_message"),
        lat_lon: getValue("lat_lon") || null,
      });
      toast({ title: "Profile Updated", description: "Your mosque profile has been saved." });
      setForm({});
    } catch (err: any) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    }
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !mosque) return;
    setUploading(true);
    try {
      const ext = file.name.split(".").pop();
      const path = `${mosque.id}/profile.${ext}`;
      const { error: uploadError } = await supabase.storage.from("mosque-images").upload(path, file, { upsert: true });
      if (uploadError) throw uploadError;
      const { data: { publicUrl } } = supabase.storage.from("mosque-images").getPublicUrl(path);
      await updateMosque.mutateAsync({ id: mosque.id, image: publicUrl });
      toast({ title: "Image Uploaded", description: "Profile image updated successfully." });
    } catch (err: any) {
      toast({ title: "Upload Error", description: err.message, variant: "destructive" });
    } finally {
      setUploading(false);
    }
  };

  if (isLoading) return <div className="space-y-4"><Skeleton className="h-8 w-48" /><Skeleton className="h-64" /></div>;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-heading font-bold">Mosque Profile</h1>
        <p className="text-muted-foreground mt-1">Update your mosque information</p>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        {/* Image upload */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Profile Image</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {mosque?.image ? (
              <img src={mosque.image} alt="Mosque" className="w-full aspect-square object-cover rounded-xl" />
            ) : (
              <div className="w-full aspect-square rounded-xl bg-muted flex items-center justify-center">
                <Upload className="w-8 h-8 text-muted-foreground" />
              </div>
            )}
            <label className="block">
              <Button variant="outline" className="w-full" disabled={uploading} asChild>
                <span>{uploading ? "Uploading..." : "Upload Image"}</span>
              </Button>
              <input type="file" accept="image/*" className="hidden" onChange={handleImageUpload} />
            </label>
          </CardContent>
        </Card>

        {/* Profile form */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">Mosque Details</CardTitle>
            <CardDescription>This information is used on your digital signage.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSave} className="space-y-4">
              <div className="space-y-2">
                <Label>Mosque Name *</Label>
                <Input value={getValue("name")} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
              </div>
              <div className="space-y-2">
                <Label>Address</Label>
                <Input 
                  value={getValue("address")} 
                  onChange={(e) => setForm({ ...form, address: e.target.value })} 
                  placeholder="e.g., Kumira, Chittagong, Bangladesh"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Jumu'ah Time</Label>
                  <Input type="time" value={getValue("jumua_time")} onChange={(e) => setForm({ ...form, jumua_time: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <Label>Lat/Lon</Label>
                  <Input value={getValue("lat_lon")} onChange={(e) => setForm({ ...form, lat_lon: e.target.value })} placeholder="51.5074,-0.1278" />
                </div>
              </div>
              <div className="space-y-2">
                <Label>Bottom TV Message</Label>
                <Textarea
                  value={getValue("bottom_message")}
                  onChange={(e) => setForm({ ...form, bottom_message: e.target.value })}
                  placeholder="Scrolling marquee message for your digital signage"
                  rows={3}
                />
              </div>
              <Button type="submit" disabled={updateMosque.isPending}>
                {updateMosque.isPending ? "Saving..." : "Save Changes"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
