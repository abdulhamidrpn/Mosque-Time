
-- Mosques table
CREATE TABLE public.mosques (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  owner_uid UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  city TEXT,
  country TEXT,
  jumua_time TIME,
  bottom_message TEXT DEFAULT '',
  data_version INTEGER NOT NULL DEFAULT 1,
  image TEXT,
  is_active BOOLEAN NOT NULL DEFAULT true,
  lat_lon TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Prayer times table
CREATE TABLE public.prayer_times (
  mosque_id UUID NOT NULL REFERENCES public.mosques(id) ON DELETE CASCADE,
  date DATE NOT NULL,
  fajr TIME,
  dhuhr TIME,
  asr TIME,
  maghrib TIME,
  isha TIME,
  sunrise TIME,
  PRIMARY KEY (mosque_id, date)
);

-- Mosque slides table
CREATE TABLE public.mosque_slides (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  mosque_id UUID NOT NULL REFERENCES public.mosques(id) ON DELETE CASCADE,
  image_url TEXT NOT NULL,
  display_order INTEGER NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.mosques ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.prayer_times ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- Mosques policies: owners only
CREATE POLICY "Users can view their own mosques" ON public.mosques
  FOR SELECT TO authenticated USING (auth.uid() = owner_uid);

CREATE POLICY "Users can create their own mosque" ON public.mosques
  FOR INSERT TO authenticated WITH CHECK (auth.uid() = owner_uid);

CREATE POLICY "Users can update their own mosque" ON public.mosques
  FOR UPDATE TO authenticated USING (auth.uid() = owner_uid);

CREATE POLICY "Users can delete their own mosque" ON public.mosques
  FOR DELETE TO authenticated USING (auth.uid() = owner_uid);

-- Prayer times policies: via mosque ownership
CREATE POLICY "Users can view prayer times for their mosques" ON public.prayer_times
  FOR SELECT TO authenticated USING (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

CREATE POLICY "Users can insert prayer times for their mosques" ON public.prayer_times
  FOR INSERT TO authenticated WITH CHECK (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

CREATE POLICY "Users can update prayer times for their mosques" ON public.prayer_times
  FOR UPDATE TO authenticated USING (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

CREATE POLICY "Users can delete prayer times for their mosques" ON public.prayer_times
  FOR DELETE TO authenticated USING (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

-- Mosque slides policies: via mosque ownership
CREATE POLICY "Users can view slides for their mosques" ON public.mosque_slides
  FOR SELECT TO authenticated USING (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

CREATE POLICY "Users can insert slides for their mosques" ON public.mosque_slides
  FOR INSERT TO authenticated WITH CHECK (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

CREATE POLICY "Users can update slides for their mosques" ON public.mosque_slides
  FOR UPDATE TO authenticated USING (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

CREATE POLICY "Users can delete slides for their mosques" ON public.mosque_slides
  FOR DELETE TO authenticated USING (
    EXISTS (SELECT 1 FROM public.mosques WHERE id = mosque_id AND owner_uid = auth.uid())
  );

-- Auto-update updated_at trigger
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SET search_path = public;

CREATE TRIGGER update_mosques_updated_at
  BEFORE UPDATE ON public.mosques
  FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Auto-increment data_version on any related change
CREATE OR REPLACE FUNCTION public.increment_data_version()
RETURNS TRIGGER AS $$
BEGIN
  NEW.data_version = OLD.data_version + 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SET search_path = public;

CREATE TRIGGER increment_mosques_data_version
  BEFORE UPDATE ON public.mosques
  FOR EACH ROW EXECUTE FUNCTION public.increment_data_version();

-- Storage buckets
INSERT INTO storage.buckets (id, name, public) VALUES ('mosque-images', 'mosque-images', true);
INSERT INTO storage.buckets (id, name, public) VALUES ('mosque-slides', 'mosque-slides', true);

-- Storage policies
CREATE POLICY "Users can upload mosque images" ON storage.objects
  FOR INSERT TO authenticated WITH CHECK (bucket_id = 'mosque-images');

CREATE POLICY "Anyone can view mosque images" ON storage.objects
  FOR SELECT USING (bucket_id = 'mosque-images');

CREATE POLICY "Users can update their mosque images" ON storage.objects
  FOR UPDATE TO authenticated USING (bucket_id = 'mosque-images');

CREATE POLICY "Users can delete their mosque images" ON storage.objects
  FOR DELETE TO authenticated USING (bucket_id = 'mosque-images');

CREATE POLICY "Users can upload mosque slides" ON storage.objects
  FOR INSERT TO authenticated WITH CHECK (bucket_id = 'mosque-slides');

CREATE POLICY "Anyone can view mosque slides" ON storage.objects
  FOR SELECT USING (bucket_id = 'mosque-slides');

CREATE POLICY "Users can update their mosque slides" ON storage.objects
  FOR UPDATE TO authenticated USING (bucket_id = 'mosque-slides');

CREATE POLICY "Users can delete their mosque slides" ON storage.objects
  FOR DELETE TO authenticated USING (bucket_id = 'mosque-slides');
