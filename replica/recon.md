# Replica Recon Map: OH-Scaler
**App Name**: OH-Scaler  
**Target Package**: `id.xms.ohscaler.egg`  
**Platform**: Android 12+ (minSdk 31, targetSdk 35)  
**Recon Objective**: Clean-room reconstruction of modern offline AI image upscaler utilities (solving high subscription fees, cloud latency, privacy concerns, and intrusive ads).

---

## 1. Scope & Core Loop
- **Core Loop**: Pick Photo -> Choose Scale Factor (x2 / x4) -> Hardware-Accelerated Local Inference (Tile-based Chunking with Overlap) -> Interactive 60fps Split-Screen Comparison -> Save to Gallery via Scoped Storage.
- **Audience**: Photographers, anime/digital art enthusiasts, mobile creators wanting fast, private on-device image enhancement.
- **Technical Pillars**:
  1. 100% offline inference (ONNX Runtime Mobile + NNAPI / XNNPACK).
  2. Zero dangerous permissions (Photo Picker + MediaStore Scoped Storage).
  3. Interactive dual-viewport split comparison slider with synchronized zoom/pan.

---

## 2. Screen Inventory (S01 - S04)

| ID | Screen Name | Route / Navigation | Purpose | Key Components | States |
|---|---|---|---|---|---|
| **S01** | Home / Selection | App Start | Select input image and configure upscale parameters | Hero badge, Photo Picker trigger, Scale selector (2x, 4x), Image preview card | Empty, Loaded |
| **S02** | Configuration | Image Loaded | Confirm resolution, estimated memory, and upscale ratio | `ResolutionBadge`, Scale selector chips, "Upscale Now" primary CTA | Ready, Extreme Res Warning |
| **S03** | Processing Overlay | Upscale Triggered | Real-time progress feedback across pipeline stages | `ProcessingProgressCard`, Stage breadcrumbs (Reading -> AI Inferencing -> Stitching -> Complete), Cancel CTA | Step 1-4, Cancelling, Error |
| **S04** | Result Comparison | Processing Done | Inspect pixel sharpness and export | `ComparisonSlider` (drag 60 FPS, 48dp handle, haptic tick), ZoomableContainer, Save to Gallery CTA, Share CTA | Interactive, Saved Toast |

---

## 3. User Flows

### F01: Happy Path Upscaling
1. S01: User taps "Select Image" -> System Photo Picker appears.
2. User selects an image -> Returns to S01 with image loaded.
3. S02: User verifies source resolution (e.g. 1080x1920) and picks 2x (Target: 2160x3840).
4. User taps "Upscale Now".
5. S03: Processing overlay shows:
   - Step 1: Reading Pixels & EXIF metadata.
   - Step 2: Running AI Neural Inference (Tile X of Y, animated progress bar).
   - Step 3: Stitching & Seam Blending.
6. S04: Upscaled image opens with `ComparisonSlider` in center. User drags left/right to compare before vs after.
7. User taps "Save to Gallery" -> Image saved to `Pictures/OH-Scaler` -> Success snackbar.

### F02: Cancellation & Edge Handling
1. During Step 2 (Tile inference), user taps "Cancel".
2. Coroutine job cancels immediately (`ensureActive()`), intermediate buffers recycled.
3. App returns cleanly to S01 without memory leak.

---

## 4. Inferred Data Model

| Entity | Fields | Description |
|---|---|---|
| `UpscaleConfig` | `scaleFactor: Int (2, 4)`, `tileSize: Int`, `overlapPadding: Int` | Configuration parameters for tile processor |
| `ImageMetadata` | `uri: Uri`, `width: Int`, `height: Int`, `mimeType: String` | Input image attributes |
| `UpscaleProgress` | `stage: PipelineStage`, `currentTile: Int`, `totalTiles: Int`, `percentage: Float` | State tracking model for UI progress card |
| `UpscaleResult` | `sourceBitmap: Bitmap`, `upscaledBitmap: Bitmap`, `durationMs: Long` | Final payload delivered to comparison view |

---

## 5. Clean-Room Boundary
- All code, shaders, UI components, and state machines are written completely from scratch.
- No proprietary cloud APIs, tracking SDKs, or subscription paywalls.
- 100% open-source compliant architecture.
