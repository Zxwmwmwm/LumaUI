#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_NDK_HOME:?Set ANDROID_NDK_HOME to Android NDK r28c}"
: "${ANDROID_JAR:?Set ANDROID_JAR to an Android platform android.jar}"
: "${R8_JAR:?Set R8_JAR to an R8 jar containing D8}"
work=$(mktemp -d); trap 'rm -rf "$work"' EXIT
mkdir -p "$work/classes" "$work/dex" artifacts
javac -encoding UTF-8 -source 8 -target 8 -classpath "$ANDROID_JAR" -d "$work/classes" src/LumaUI.java src/TransitionMath.java src/ModBridge.java src/ModSearch.java src/PlayField.java src/ModPages.java src/ModLayout.java src/SettingsMotion.java src/SettingsSheet.java src/SearchSheet.java
(cd "$work/classes" && python3 -m zipfile -c "$work/ui.jar" luma)
java -cp "$R8_JAR" com.android.tools.r8.D8 --min-api 26 --lib "$ANDROID_JAR" --output "$work/dex" "$work/ui.jar"
python3 - "$work/dex/classes.dex" "$work/dex.cpp" <<'PY'
import sys
b=open(sys.argv[1],'rb').read()
with open(sys.argv[2],'w') as f:
 f.write('extern const unsigned char dex_data[] = {'+','.join(map(str,b))+'};\nextern const unsigned int dex_len = '+str(len(b))+';\n')
PY
"$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android26-clang++" -std=c++17 -fPIC -shared -O2 -Wl,-z,max-page-size=16384 src/loader.cpp "$work/dex.cpp" -llog -static-libstdc++ -o artifacts/libLumaUI.so
python3 - <<'PY'
from zipfile import ZipFile, ZIP_DEFLATED
with ZipFile('artifacts/LumaUI.levipack','w',ZIP_DEFLATED) as z:
 z.write('manifest.json','manifest.json');z.write('artifacts/libLumaUI.so','libLumaUI.so');z.write('LICENSE','LICENSE')
PY
