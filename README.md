# LumaUI

LumaUI is a customizable GUI-only overlay for LeviLauncher on Android. Make the interface your own with layered depth, animated themes, and independent colors—all from a draggable floating button.

**Author:** Zxwmwmwm  
**Minecraft Bedrock:** 1.26.62 / 1.26.62.x  
**Platform:** Android ARM64

## Features

- **Menu transitions:** choose **Fade**, **Portal**, **Elastic**, or **Scatter** in **Motion**. Elastic bends the interface toward the floating **L** button, drawing it into a stronger black-hole-like funnel when closed and unfurling it on opening. Portal opens an expanding aperture; Scatter brings sections and controls in from newly varied directions, with staggered, letter-by-letter text. The button-to-menu effect can be turned off with Fade.
- **Mods menu:** browse launcher modules and installed mods with a choice of paged, vertically scrolling, or sideways scrolling layouts. A Luma-styled gear sheet opens available module settings with a Luma-colored palette, an animated entrance, animated category changes and setting cards, touch feedback, and a closing transition. Module switches use and sync with the launcher’s live controls; installed mod switches are saved for the next Minecraft launch (they do not unload a native library in the current session). Search by name, group, or ID, star favorites, and filter to favorites. Module favorites sync with LeviLauncher; installed-mod favorites are saved in LumaUI. Your selected tab, list layout, pages, and scroll positions are remembered.
- **Interface depth:** choose **Flat**, **Soft**, or **Deep** for layered shadows, accent glow, and a touch-reactive glass highlight.
- **Animated backgrounds:** choose **Off**, **Lava Lamp**, **Space**, or three interactive effects: **Pop** to tap bubbles, **Trail** to draw fading light, and **Magnet** to gather sparks with your finger and burst them outward when you release. Lava Lamp uses rising warm wax, cooling and sinking blobs, and soft metaball merging and splitting. Switching effects smoothly blends the outgoing background into the new one as it forms. These effects sit behind the interface and do not change gameplay.
- **Independent colors:** customize the accent, panel, and effects with presets or RGB sliders. Panel opacity is adjustable. Turn on **Appearance → Adaptive** to sample the visible game surface locally. A softly blended color grid follows where different colors appear behind the panel; the accent uses the overall sample. Manual colors are kept as a fallback.
- **HUD editor:** open **HUD Editor** to drag launcher-supported overlays, select them and adjust their size. The Luma button remains draggable. Use the separate toolbar’s − and + buttons to resize the selected launcher element; drag its title to reposition the toolbar, and tap Done to leave edit mode. Availability depends on the launcher’s internal HUD APIs.
- **Smooth motion:** spring-based opening and closing, animated section selectors, mod switches and list transitions, touch ripples, and an adjustable animation speed.
- **Interaction effects:** the **Effects** tab adds optional fluid touch ripples, a glow-intensity slider, a switch to stop the glow following touch, and Eco or Smooth background frame pacing. Separately, **Button → Button press feedback** offers Off, Soft and Water press-and-release animations for controls, including mod switches; it works independently of touch ripples.
- **Reset visuals:** restore the visual defaults with a two-tap confirmation; the floating-button position is preserved.
- **Custom floating button:** drag it wherever it suits you, choose its size and icon, and select **Off**, **Portal**, **Pulse**, or **Orbit** for its return when the menu closes. The portal forms first, reveals the button, then fades. Settings and position are saved.

LumaUI adds no gameplay features of its own. Its Mods tab controls existing launcher modules and installed mods. The translucent glass effect is a visual treatment rather than real-time background blur.

## Install and use

1. Download `LumaUI.levipack` from the [latest release](https://github.com/Zxwmwmwm/LumaUI/releases/latest) and import it in LeviLauncher's mod manager.
2. Enable LumaUI, start Minecraft, and tap the floating **L** button. Drag the button to reposition it.
3. Open **Motion** to choose Fade, Portal, Elastic, or Scatter, and adjust depth, animation speed, and the background theme. Choose **Lava Lamp** for a flowing background, or **Pop**, **Trail**, or **Magnet** and touch or drag in the panel to interact; release Magnet to scatter the sparks; the **About** tab gives you extra open space. Use **Appearance** for colors and opacity, and **Effects** for glow strength, glow-follow, touch feedback, and frame pacing.
4. Open **Mods** and choose **Modules** for live controls or **Installed** for next-launch controls. Choose **Pages**, **Vertical**, or **Sideways** to organize the list. Swipe to scroll (or change pages), tap a star to favorite a mod, and use **Favorites** to filter. Tap the search box to find a mod and use the gear button to open a module’s available settings.
5. Use **HUD Editor** to arrange supported launcher HUD elements, and **Appearance → Adaptive** to try matching the game surface. Open **Button** to choose the floating button’s size, icon, return animation, and control press feedback. Tap **×** or outside the interface to close it and see your selection.

## Compatibility

LumaUI targets Minecraft Bedrock 1.26.62 and 1.26.62.x on Android ARM64. Android 9 or newer is recommended; the launcher must provide its internal `InbuiltOverlayManager`. The Mods tab uses launcher internal APIs; availability may vary with launcher versions, and the menu reports when a provider is unavailable. Lighter panel colors can reduce text contrast. Adaptive color uses Android PixelCopy on a visible game SurfaceView only while the menu is open; it does not record, save, or transmit screenshots. If the game surface is unavailable, protected, or not exposed as a SurfaceView, it falls back to the manual palette. It estimates colors rather than detecting block types. HUD editor integration relies on internal launcher APIs. Settings animations follow Android’s system animation setting; touch feedback follows LumaUI’s Touch ripples setting.

## Versioning

The first number changes when a Minecraft update breaks compatibility, the second for new features, and the third for bug fixes. This feature release is **1.4.0**, following **1.3.0**.

## Build

Requirements: JDK, Python 3, Android NDK r28c, an Android platform `android.jar`, and an R8 jar containing D8.

```sh
ANDROID_NDK_HOME=/path/to/android-ndk-r28c \
ANDROID_JAR=/path/to/android.jar \
R8_JAR=/path/to/r8.jar bash build.sh
```

The build creates `artifacts/libLumaUI.so` and `artifacts/LumaUI.levipack`. Run the lava thermals and metaballs, spatial adaptive color, HUD toolbar facade, independent button press timing, background blending, button-motion timing, transition geometry, spring, page-memory, search filtering, interactive simulation, list-layout, UI lifecycle guard, settings-motion, and launcher-bridge facade checks with `bash tests/run.sh`.

## License

[MIT License](LICENSE) · Copyright © 2026 Zxwmwmwm
