import { useState } from "react";
import { useAuth } from "@/hooks/use-auth";
import { useCreateMosque } from "@/hooks/use-mosque-data";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "@/hooks/use-toast";
import { useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";

export default function OnboardingPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const createMosque = useCreateMosque();
  const [name, setName] = useState("");
  const [address, setAddress] = useState("");
  const [bottomMessage, setBottomMessage] = useState("Please silence your phones during prayer.");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    try {
      await createMosque.mutateAsync({
        name,
        address: address || null,
        bottom_message: bottomMessage,
      });
      toast({ title: "Mosque Created!", description: "Your mosque profile has been set up." });
      // Wait for mosque query to refetch before navigating to dashboard
      await queryClient.refetchQueries({ queryKey: ["mosque"] });
      navigate("/");
    } catch (err: any) {
      toast({ title: "Error", description: err.message, variant: "destructive" });
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-background">
      <Card className="w-full max-w-lg animate-fade-in">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-heading">Set Up Your Mosque</CardTitle>
          <CardDescription>Let's get your mosque profile ready. You can update these details anytime.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Mosque Name *</Label>
              <Input id="name" value={name} onChange={(e) => setName(e.target.value)} required placeholder="Al-Noor Mosque" />
            </div>
            <div className="space-y-2">
              <Label htmlFor="address">Address</Label>
              <Input id="address" value={address} onChange={(e) => setAddress(e.target.value)} placeholder="123 Main Street, London, UK" />
            </div>
            <div className="space-y-2">
              <Label htmlFor="message">Bottom TV Message</Label>
              <Textarea
                id="message"
                value={bottomMessage}
                onChange={(e) => setBottomMessage(e.target.value)}
                placeholder="Scrolling message for digital signage"
                rows={2}
              />
            </div>
            <Button type="submit" className="w-full" disabled={createMosque.isPending}>
              {createMosque.isPending ? "Creating..." : "Create Mosque Profile"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
