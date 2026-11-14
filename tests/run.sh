#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
w=$(mktemp -d);trap 'rm -rf "$w"' EXIT
javac -encoding UTF-8 -d "$w" src/TransitionMath.java src/AdaptiveColor.java src/AdaptiveGrid.java src/BackgroundBlend.java src/ButtonMotion.java src/PressMotion.java src/ModBridge.java src/ModSearch.java src/PlayField.java src/LavaField.java src/ModPages.java src/ModLayout.java src/SettingsMotion.java tests/TransitionMathTest.java tests/AdaptiveColorTest.java tests/AdaptiveGridTest.java tests/BackgroundBlendTest.java tests/ButtonMotionTest.java tests/PressMotionTest.java tests/ModPagesTest.java tests/ModSearchTest.java tests/PlayFieldTest.java tests/LavaFieldTest.java tests/ModLayoutTest.java tests/SettingsMotionTest.java
java -cp "$w" luma.ui.TransitionMathTest
java -cp "$w" luma.ui.AdaptiveColorTest
java -cp "$w" luma.ui.AdaptiveGridTest
java -cp "$w" luma.ui.BackgroundBlendTest
java -cp "$w" luma.ui.ButtonMotionTest
java -cp "$w" luma.ui.PressMotionTest
java -cp "$w" luma.ui.ModPagesTest
java -cp "$w" luma.ui.ModSearchTest
java -cp "$w" luma.ui.PlayFieldTest
java -cp "$w" luma.ui.LavaFieldTest
java -cp "$w" luma.ui.ModLayoutTest
java -cp "$w" luma.ui.SettingsMotionTest
python3 tests/mod_bridge_test.py
python3 tests/hud_editor_test.py

python3 tests/ui_invariants_test.py
