import { useMosque, useMosqueSlides, useUpdateSlideOrder, useDeleteSlide, checkDuplicateImage } from "@/hooks/use-mosque-data";
import { supabase } from "@/integrations/supabase/client";
import { resizeImage, generateImageHash, blobToFile, formatFileSize } from "@/lib/image-utils";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "@/hooks/use-toast";
import { Trash2, Upload, ImagePlus, GripVertical, AlertCircle } from "lucide-react";
import { useState, useRef } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Progress } from "@/components/ui/progress";
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  rectSortingStrategy,
  useSortable,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import type { MosqueSlide } from "@/hooks/use-mosque-data";

function SortableSlide({
  slide,
  onDelete,
}: {
  slide: MosqueSlide;
  onDelete: (slide: MosqueSlide) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: slide.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
    zIndex: isDragging ? 10 : undefined,
  };

  return (
    <div ref={setNodeRef} style={style} className="relative group rounded-xl overflow-hidden border bg-card">
      <div
        {...attributes}
        {...listeners}
        className="absolute top-2 left-2 z-10 cursor-grab active:cursor-grabbing bg-background/80 rounded-md p-1 hover:bg-background transition-colors"
      >
        <GripVertical className="w-4 h-4 text-muted-foreground" />
      </div>
      <img
        src={slide.image_url}
        alt={`Slide ${slide.display_order + 1}`}
        className="w-full aspect-video object-cover"
      />
      <div className="absolute inset-0 bg-foreground/0 group-hover:bg-foreground/30 transition-colors flex items-center justify-center">
        <Button
          variant="destructive"
          size="icon"
          className="opacity-0 group-hover:opacity-100 transition-opacity"
          onClick={() => onDelete(slide)}
        >
          <Trash2 className="w-4 h-4" />
        </Button>
      </div>
      <div className="absolute bottom-2 left-2">
        <span className="text-xs bg-background/80 px-2 py-0.5 rounded-md">
          #{slide.display_order + 1}
        </span>
      </div>
    </div>
  );
}

export default function SlidesPage() {
  const { data: mosque } = useMosque();
  const { data: slides, isLoading } = useMosqueSlides(mosque?.id);
  const updateOrder = useUpdateSlideOrder();
  const deleteSlide = useDeleteSlide();
  const qc = useQueryClient();
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  const currentSlideCount = slides?.length || 0;
  const canAddMore = currentSlideCount < 10;
  const maxSlides = 10;

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files?.length || !mosque) {
      console.error("No files or mosque:", { filesLength: files?.length, mosqueId: mosque?.id });
      return;
    }

    // Validate total slides won't exceed 10
    const totalSlides = currentSlideCount + files.length;
    if (totalSlides > maxSlides) {
      toast({
        title: "Too Many Slides",
        description: `You can only have up to ${maxSlides} slides. You currently have ${currentSlideCount}. Can add ${maxSlides - currentSlideCount} more.`,
        variant: "destructive",
      });
      return;
    }

    setUploading(true);
    const uploadedSlides: MosqueSlide[] = [];
    let successCount = 0;
    let duplicateCount = 0;
    
    try {
      const maxOrder = slides?.length ? Math.max(...slides.map((s) => s.display_order)) + 1 : 0;
      console.log("Upload started - Mosque ID:", mosque.id, "Max order:", maxOrder);
      
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        console.log(`Processing file ${i + 1}/${files.length}:`, file.name, "Size:", formatFileSize(file.size), "Type:", file.type);
        
        // Validate file type
        if (!file.type.startsWith("image/")) {
          console.warn(`Skipped ${file.name} - not an image`);
          toast({
            title: "Invalid File",
            description: `${file.name} is not an image file.`,
            variant: "destructive",
          });
          continue;
        }

        try {
          // Step 1: Generate hash of original file for deduplication
          console.log("Generating image hash for deduplication...");
          const imageHash = await generateImageHash(file);
          console.log("Image hash generated:", imageHash);

          // Step 2: Check if this image already exists
          console.log("Checking for duplicate images...");
          const existingSlide = await checkDuplicateImage(mosque.id, imageHash);
          
          if (existingSlide) {
            console.log("Duplicate image found! Reusing existing slide:", existingSlide.id);
            duplicateCount++;
            toast({
              title: "Duplicate Image",
              description: `"${file.name}" is already uploaded. Using existing slide.`,
              variant: "default",
            });
            // Still add to uploaded slides but mark as reused
            uploadedSlides.push(existingSlide);
            successCount++;
            continue;
          }

          // Step 3: Resize image to optimize file size and quality
          console.log("Resizing image...");
          const resizedBlob = await resizeImage(file);
          console.log("Image resized. Original size:", formatFileSize(file.size), "→ New size:", formatFileSize(resizedBlob.size));

          // Step 4: Convert resized blob to file
          const ext = file.name.split(".").pop()?.toLowerCase() || "jpg";
          const resizedFile = blobToFile(resizedBlob, `${file.name.split(".")[0]}.jpg`);

          // Step 5: Upload resized image to storage
          const timestamp = Date.now();
          const path = `${mosque.id}/${timestamp}-${i}.jpg`;
          console.log("Storage upload path:", path);

          console.log("Starting storage upload...");
          const { data: uploadData, error: upErr } = await supabase.storage
            .from("mosque-slides")
            .upload(path, resizedFile, { upsert: false });
          
          if (upErr) {
            console.error("Storage upload failed:", upErr);
            toast({
              title: "Storage Upload Failed",
              description: `${file.name}: ${upErr.message}`,
              variant: "destructive",
            });
            continue;
          }

          console.log("Storage upload successful, file:", uploadData);

          // Step 6: Get public URL
          const { data: publicData } = supabase.storage
            .from("mosque-slides")
            .getPublicUrl(path);
          
          const publicUrl = publicData.publicUrl;
          console.log("Public URL obtained:", publicUrl);

          // Step 7: Insert into database with image hash
          console.log("Inserting into database...");
          const { data: newSlide, error: dbErr } = await supabase.from("mosque_slides").insert({
            mosque_id: mosque.id,
            image_url: publicUrl,
            image_hash: imageHash,
            display_order: maxOrder + successCount,
          }).select().single();

          if (dbErr) {
            console.error("Database insert failed:", dbErr);
            toast({
              title: "Database Error",
              description: `${file.name}: ${dbErr.message}`,
              variant: "destructive",
            });
            continue;
          }

          console.log("Database insert successful:", newSlide);
          
          if (newSlide) {
            uploadedSlides.push(newSlide);
            successCount++;
          }

        } catch (fileError: any) {
          console.error("Error processing file:", file.name, fileError);
          toast({
            title: "Processing Error",
            description: `${file.name}: ${fileError.message}`,
            variant: "destructive",
          });
        }

        // Update progress
        setUploadProgress(Math.round(((i + 1) / files.length) * 100));
      }

      console.log("All files processed. Successful uploads:", successCount, "Duplicates reused:", duplicateCount);

      // Update cache with new slides immediately
      if (uploadedSlides.length > 0) {
        const currentSlides = slides || [];
        const updatedSlides = [...currentSlides, ...uploadedSlides];
        qc.setQueryData(["mosque-slides", mosque.id], updatedSlides);
        console.log("Cache updated with new slides");
      }

      // Also invalidate to ensure fresh data
      await qc.invalidateQueries({ queryKey: ["mosque-slides", mosque.id] });
      
      if (successCount > 0) {
        const message = duplicateCount > 0 
          ? `${successCount} slide(s) processed (${duplicateCount} were duplicates)`
          : `${successCount} slide(s) uploaded successfully`;
        toast({
          title: "Slides Processed",
          description: message,
        });
      }
    } catch (err: any) {
      console.error("Upload exception:", err);
      toast({
        title: "Upload Error",
        description: err.message || "An error occurred during upload",
        variant: "destructive",
      });
    } finally {
      setUploading(false);
      setUploadProgress(0);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleDelete = async (slide: MosqueSlide) => {
    try {
      await deleteSlide.mutateAsync({
        id: slide.id,
        imageUrl: slide.image_url,
      });
      toast({ title: "Slide Deleted", description: "The slide has been removed." });
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to delete slide",
        variant: "destructive",
      });
    }
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id || !slides) return;

    const oldIndex = slides.findIndex((s) => s.id === active.id);
    const newIndex = slides.findIndex((s) => s.id === over.id);
    const reordered = arrayMove(slides, oldIndex, newIndex);

    // Optimistic update
    qc.setQueryData(["mosque-slides", mosque?.id], reordered.map((s, i) => ({ ...s, display_order: i })));

    try {
      await updateOrder.mutateAsync(reordered.map((s, i) => ({ id: s.id, display_order: i })));
      toast({ title: "Order Updated", description: "Slides have been reordered." });
    } catch (err: any) {
      qc.invalidateQueries({ queryKey: ["mosque-slides"] });
      toast({
        title: "Error",
        description: err.message || "Failed to update order",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-heading font-bold">Digital Signage</h1>
          <p className="text-muted-foreground mt-1">Upload up to 10 slides, drag to reorder</p>
        </div>
        <div>
          <Button
            disabled={uploading || !canAddMore}
            variant={!canAddMore ? "outline" : "default"}
            onClick={() => fileInputRef.current?.click()}
          >
            <Upload className="w-4 h-4 mr-2" />
            {uploading ? "Uploading..." : "Upload Slides"}
          </Button>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            multiple
            className="hidden"
            onChange={handleUpload}
            disabled={uploading}
          />
        </div>
      </div>

      {/* Slide Count Info */}
      <Card className="bg-blue-50 border-blue-200 dark:bg-blue-950 dark:border-blue-800">
        <CardContent className="pt-6">
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-sm font-medium">Slide Capacity</span>
              <span className="text-sm font-semibold">
                {currentSlideCount}/{maxSlides}
              </span>
            </div>
            <Progress value={(currentSlideCount / maxSlides) * 100} className="h-2" />
            <p className="text-xs text-muted-foreground">
              {canAddMore ? `You can add ${maxSlides - currentSlideCount} more slide(s)` : "Maximum slides reached"}
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Upload Progress */}
      {uploading && (
        <Card className="bg-amber-50 border-amber-200 dark:bg-amber-950 dark:border-amber-800">
          <CardContent className="pt-6">
            <div className="space-y-2">
              <div className="flex justify-between items-center">
                <span className="text-sm font-medium">Upload Progress</span>
                <span className="text-sm font-semibold">{uploadProgress}%</span>
              </div>
              <Progress value={uploadProgress} className="h-2" />
            </div>
          </CardContent>
        </Card>
      )}

      {/* Slides Grid */}
      {isLoading ? (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="aspect-video rounded-xl" />
          ))}
        </div>
      ) : slides?.length ? (
        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
          <SortableContext items={slides.map((s) => s.id)} strategy={rectSortingStrategy}>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {slides.map((slide) => (
                <SortableSlide key={slide.id} slide={slide} onDelete={handleDelete} />
              ))}
            </div>
          </SortableContext>
        </DndContext>
      ) : (
        <Card>
          <CardContent className="p-12 text-center text-muted-foreground">
            <ImagePlus className="w-12 h-12 mx-auto mb-4 opacity-40" />
            <p className="font-medium">No slides yet</p>
            <p className="text-sm">Upload images for your digital signage display. You can add up to 10 slides.</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
