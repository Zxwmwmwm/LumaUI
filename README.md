# LumaUI

A customizable GUI-only native mod for LeviLauncher on Android. **Author: Zxwmwmwm.**

## Features

- A draggable floating **L** button opens the interface. Its position is saved.
- In **Motion**, choose **Off**, **Stretch** (squishy drifting bubbles), or **Space** (twinkling stars with an orbiting black hole). The animation runs inside the menu, not in the game world.
- Adjust transition and background animation speed.
- In **Appearance**, choose **Accent**, **Panel**, or **Effects** to customize each color separately using presets or RGB sliders. Opacity is adjustable too. Very light panel colors may reduce text contrast.
- Settings persist in Android app preferences. No gameplay-changing features or game hooks are included. The glass look is translucent, not a real-time blur.

Version remains **1.0.0** at the author's request. The manifest retains `"hf": true` for the earlier floating-button fix; that field is informational and may not be displayed by the launcher.

## Install

1. Download [`LumaUI.levipack`](artifacts/LumaUI.levipack), import it in LeviLauncher's mod manager, and enable it.
2. Start Minecraft and tap the floating **L** button. Drag it to reposition it.
3. Tap the X or outside the menu to close it. With the menu closed, touches outside the button should pass through to the game.

## Compatibility and testing

Targets Minecraft Bedrock 1.26.62 and 1.26.62.x on Android ARM64. Android 9 or later is recommended. The native library and package have been compiled and checked, but the new animations and color controls have not been tested on an Android device. The overlay relies on the launcher's internal `InbuiltOverlayManager`; compatibility with a particular launcher version is not guaranteed. If it doesn't work, collect your launcher version and logs. This UI-only build does not hook or reverse engineer `libminecraftpe.so`.

## Rebuild

Install a JDK, Python 3, Android NDK r28c, an Android platform `android.jar`, and an R8 jar containing D8. On Linux:

```sh
ANDROID_NDK_HOME=/path/to/android-ndk-r28c \
ANDROID_JAR=/path/to/android.jar \
R8_JAR=/path/to/r8.jar ./build.sh
```

The build produces `artifacts/libLumaUI.so` and `artifacts/LumaUI.levipack`. This build used R8 8.3.37, the Android API 16 SDK stub jar, and Android NDK r28c with API 26. Runtime DEX loading requires API 26 or later.

## License

No open-source license has been selected. A public repository alone does not grant reuse rights.
