## LumaUI 1.3.0 · Adaptive themes & HUD editor

- **Adaptive panel:** optionally sample the game SurfaceView locally while the menu is open and smoothly tint the panel, accent and effects. No screen recording or upload; unavailable or protected surfaces fall back to manual colors. It estimates visible colors, not block identities.
- **HUD Editor:** move and resize launcher-supported HUD elements using the launcher’s actual editing mode; drag Luma’s floating button too. Its position and launcher HUD changes are saved.
- **Lava Lamp:** replaces Stretch with independently moving, rotating wax shapes, heat/cooling motion and soft metaball merging. Inspired by the lava-lamp example, independently implemented for Android Canvas.
- **Motion:** Portal opens an expanding aperture, while new **Scatter** brings interface elements in from varied directions. Touch ripples now form organic water-like waves, with a brief control expansion. Floating-button returns have a softer scale.
- **Fixes:** blend Elastic’s frozen transition snapshot into the live panel to reduce the end-of-opening jerk; pass touches through while closing rather than blocking gameplay until the animation finishes. Mods now appears first in the navigation. The gear sheet has a refreshed gradient, accent and header.

**Compatibility:** Adaptive color requires an accessible game SurfaceView; it may not work with every launcher, renderer or protected surface. HUD editing depends on the launcher’s internal APIs. These changes passed host tests and compile checks but have not been tested on an Android device.
