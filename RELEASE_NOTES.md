## LumaUI 1.4.0 · Spatial color & motion refinements

- **Spatial adaptive color:** the panel now samples a low-resolution grid behind its position, so differently colored parts of the background tint corresponding parts of the panel. The grid is blurred and smoothly transitions; accents still use the overall color.
- **HUD editor:** moved the toolbar into a touchable, non-modal application panel above the game. Use − / + on the selected launcher HUD element, drag the toolbar title to move it, and tap Done to exit. The launcher’s own HUD elements remain draggable.
- **Elastic opening:** keeps rendering the live panel through the opening instead of switching from a frozen mesh at the end; the opening stretch is monotonic. This targets the reported jerk.
- **Press feedback:** choose **Off**, **Soft**, or **Water** under **Button → Button press feedback**. Controls compress or squash and rebound when pressed, including the complete mod toggle. This is separate from touch ripples.
- **Scatter:** each opening picks fresh directions and staggered entrances. Text reveals letter by letter as sections and controls settle into place.

Adaptive sampling needs a compatible, accessible game SurfaceView and uses no recording or upload; unsupported surfaces fall back to manual colors. HUD editing depends on launcher internal APIs. Build and host/facade tests passed, but Android device testing is still needed to verify window layering, sampling alignment and animation feel.
