# LumaUI

LumaUI is a customizable GUI-only overlay for LeviLauncher on Android. Make the interface your own with layered depth, animated themes, and independent colors—all from a draggable floating button.

**Author:** Zxwmwmwm  
**Minecraft Bedrock:** 1.26.62 / 1.26.62.x  
**Platform:** Android ARM64

## Features

- **Button-to-menu transition:** the interface expands out of the floating **L** button and returns to it when closed. Turn this effect on or off in **Motion**.
- **Interface depth:** choose **Flat**, **Soft**, or **Deep** for layered shadows, accent glow, and a touch-reactive glass highlight.
- **Animated backgrounds:** choose **Off**, **Stretch** for drifting elastic bubbles, or **Space** for stars, streaks, and a glowing black hole.
- **Independent colors:** customize the accent, panel, and effects with presets or RGB sliders. Panel opacity is adjustable.
- **Motion speed:** tune the speed of interface transitions and background animation.
- **Draggable button:** move the floating button wherever it suits you; its position and your settings are saved.

LumaUI is an interface-only mod. The translucent glass effect is a visual treatment rather than real-time background blur, and the mod does not change gameplay.

## Install and use

1. Download `LumaUI.levipack` from the [latest release](https://github.com/Zxwmwmwm/LumaUI/releases/latest) and import it in LeviLauncher's mod manager.
2. Enable LumaUI, start Minecraft, and tap the floating **L** button. Drag the button to reposition it.
3. Open **Motion** to control the button-to-menu transition, depth, animation speed, and background theme. Use **Appearance** to customize colors and opacity.
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

The build creates `artifacts/libLumaUI.so` and `artifacts/LumaUI.levipack`. Run the transition geometry checks with `bash tests/run.sh`.

## License

[MIT License](LICENSE) · Copyright © 2026 Zxwmwmwm
