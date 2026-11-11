# LumaUI

LumaUI is a customizable GUI-only overlay for LeviLauncher on Android. Make the interface your own with layered depth, animated themes, and independent colors—all from a draggable floating button.

**Author:** Zxwmwmwm  
**Minecraft Bedrock:** 1.26.62 / 1.26.62.x  
**Platform:** Android ARM64

## Features

- **Menu transitions:** choose **Fade**, **Portal**, or **Elastic** in **Motion**. Elastic stretches the interface out of the floating **L** button and draws it back in when closed. The button-to-menu effect can be turned off with Fade.
- **Interface depth:** choose **Flat**, **Soft**, or **Deep** for layered shadows, accent glow, and a touch-reactive glass highlight.
- **Animated backgrounds:** choose **Off**, **Stretch** for drifting elastic bubbles, or **Space** for stars, streaks, and a glowing black hole.
- **Independent colors:** customize the accent, panel, and effects with presets or RGB sliders. Panel opacity is adjustable.
- **Smooth motion:** spring-based opening and closing, animated tabs and sliders, and an adjustable animation speed.
- **Interaction effects:** the **Effects** tab adds optional touch ripples, a glow-intensity slider, and Eco or Smooth background frame pacing.
- **Reset visuals:** restore the visual defaults with a two-tap confirmation; the floating-button position is preserved.
- **Draggable button:** move the floating button wherever it suits you; its position and your settings are saved.

LumaUI is an interface-only mod. The translucent glass effect is a visual treatment rather than real-time background blur, and the mod does not change gameplay.

## Install and use

1. Download `LumaUI.levipack` from the [latest release](https://github.com/Zxwmwmwm/LumaUI/releases/latest) and import it in LeviLauncher's mod manager.
2. Enable LumaUI, start Minecraft, and tap the floating **L** button. Drag the button to reposition it.
3. Open **Motion** to choose Fade, Portal, or Elastic, and adjust depth, animation speed, and the background theme. Use **Appearance** for colors and opacity, and **Effects** for glow, touch feedback, and frame pacing.
4. Tap **×** or outside the interface to close it.

## Compatibility

LumaUI targets Minecraft Bedrock 1.26.62 and 1.26.62.x on Android ARM64. Android 9 or newer is recommended; the launcher must provide its internal `InbuiltOverlayManager`. Lighter panel colors can reduce text contrast.

## Build

Requirements: JDK, Python 3, Android NDK r28c, an Android platform `android.jar`, and an R8 jar containing D8.

```sh
ANDROID_NDK_HOME=/path/to/android-ndk-r28c \
ANDROID_JAR=/path/to/android.jar \
R8_JAR=/path/to/r8.jar bash build.sh
```

The build creates `artifacts/libLumaUI.so` and `artifacts/LumaUI.levipack`. Run the transition geometry and spring checks with `bash tests/run.sh`.

## License

[MIT License](LICENSE) · Copyright © 2026 Zxwmwmwm
