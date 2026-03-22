import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@/hooks/use-auth";
import { useMosque } from "@/hooks/use-mosque-data";
import DashboardLayout from "@/components/DashboardLayout";
import AuthPage from "@/pages/AuthPage";
import OnboardingPage from "@/pages/OnboardingPage";
import DashboardPage from "@/pages/DashboardPage";
import ProfilePage from "@/pages/ProfilePage";
import PrayerTimesPage from "@/pages/PrayerTimesPage";
import SlidesPage from "@/pages/SlidesPage";
import NotFound from "@/pages/NotFound";

const queryClient = new QueryClient();

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="min-h-screen flex items-center justify-center"><div className="animate-pulse font-heading text-xl text-muted-foreground">Loading...</div></div>;
  if (!user) return <Navigate to="/auth" replace />;
  return <>{children}</>;
}

function MosqueGuard({ children }: { children: React.ReactNode }) {
  const { data: mosque, isLoading } = useMosque();
  if (isLoading) return <div className="min-h-screen flex items-center justify-center"><div className="animate-pulse font-heading text-xl text-muted-foreground">Loading...</div></div>;
  if (!mosque) return <Navigate to="/onboarding" replace />;
  return <DashboardLayout>{children}</DashboardLayout>;
}

function AppRoutes() {
  const { user, loading } = useAuth();

  if (loading) return null;

  return (
    <Routes>
      <Route path="/auth" element={user ? <Navigate to="/" replace /> : <AuthPage />} />
      <Route
        path="/onboarding"
        element={
          <ProtectedRoute>
            <OnboardingPage />
          </ProtectedRoute>
        }
      />
      <Route path="/" element={<ProtectedRoute><MosqueGuard><DashboardPage /></MosqueGuard></ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute><MosqueGuard><ProfilePage /></MosqueGuard></ProtectedRoute>} />
      <Route path="/prayer-times" element={<ProtectedRoute><MosqueGuard><PrayerTimesPage /></MosqueGuard></ProtectedRoute>} />
      <Route path="/slides" element={<ProtectedRoute><MosqueGuard><SlidesPage /></MosqueGuard></ProtectedRoute>} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
