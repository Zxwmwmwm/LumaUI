# LumaUI

LumaUI is a customizable GUI-only overlay for LeviLauncher on Android. Make the interface your own with layered depth, animated themes, and independent colors—all from a draggable floating button.

**Author:** Zxwmwmwm  
**Minecraft Bedrock:** 1.26.62 / 1.26.62.x  
**Platform:** Android ARM64

## Features

- **Menu transitions:** choose **Fade**, **Portal**, or **Elastic** in **Motion**. Elastic bends the interface toward the floating **L** button, drawing it into a stronger black-hole-like funnel when closed and unfurling it on opening. The button-to-menu effect can be turned off with Fade.
- **Mods menu:** browse launcher modules and installed mods with a choice of paged, vertically scrolling, or sideways scrolling layouts. A Luma-styled gear sheet opens available module settings with a soft entrance, animated setting cards, touch feedback, and a closing transition. Module switches use and sync with the launcher’s live controls; installed mod switches are saved for the next Minecraft launch (they do not unload a native library in the current session). Your selected tab, list layout, pages, and scroll positions are remembered.
- **Interface depth:** choose **Flat**, **Soft**, or **Deep** for layered shadows, accent glow, and a touch-reactive glass highlight.
- **Animated backgrounds:** choose **Off**, **Stretch** for drifting elastic bubbles, or **Space** for stars, streaks, and a glowing black hole.
- **Independent colors:** customize the accent, panel, and effects with presets or RGB sliders. Panel opacity is adjustable.
- **Smooth motion:** spring-based opening and closing, animated section selectors, mod switches and list transitions, touch ripples, and an adjustable animation speed.
- **Interaction effects:** the **Effects** tab adds optional touch ripples, a glow-intensity slider, and Eco or Smooth background frame pacing.
- **Reset visuals:** restore the visual defaults with a two-tap confirmation; the floating-button position is preserved.
- **Draggable button:** move the floating button wherever it suits you; its position and your settings are saved.

LumaUI adds no gameplay features of its own. Its Mods tab controls existing launcher modules and installed mods. The translucent glass effect is a visual treatment rather than real-time background blur.

## Install and use

1. Download `LumaUI.levipack` from the [latest release](https://github.com/Zxwmwmwm/LumaUI/releases/latest) and import it in LeviLauncher's mod manager.
2. Enable LumaUI, start Minecraft, and tap the floating **L** button. Drag the button to reposition it.
3. Open **Motion** to choose Fade, Portal, or Elastic, and adjust depth, animation speed, and the background theme. Use **Appearance** for colors and opacity, and **Effects** for glow, touch feedback, and frame pacing.
4. Open **Mods** and choose **Modules** for live controls or **Installed** for next-launch controls. Choose **Pages**, **Vertical**, or **Sideways** to organize the list. Swipe to scroll (or change pages) and use the gear button to open a module’s available settings.
5. Tap **×** or outside the interface to close it.

## Compatibility

LumaUI targets Minecraft Bedrock 1.26.62 and 1.26.62.x on Android ARM64. Android 9 or newer is recommended; the launcher must provide its internal `InbuiltOverlayManager`. The Mods tab uses launcher internal APIs; availability may vary with launcher versions, and the menu reports when a provider is unavailable. Lighter panel colors can reduce text contrast. Settings animations follow Android’s system animation setting; touch feedback follows LumaUI’s Touch ripples setting.

## Build

Requirements: JDK, Python 3, Android NDK r28c, an Android platform `android.jar`, and an R8 jar containing D8.

```sh
ANDROID_NDK_HOME=/path/to/android-ndk-r28c \
ANDROID_JAR=/path/to/android.jar \
R8_JAR=/path/to/r8.jar bash build.sh
```

The build creates `artifacts/libLumaUI.so` and `artifacts/LumaUI.levipack`. Run the transition geometry, spring, page-memory, list-layout, UI lifecycle guard, settings-motion, and launcher-bridge facade checks with `bash tests/run.sh`.

## License

[MIT License](LICENSE) · Copyright © 2026 Zxwmwmwm
