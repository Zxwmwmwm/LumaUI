#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
w=$(mktemp -d);trap 'rm -rf "$w"' EXIT
javac -encoding UTF-8 -d "$w" src/TransitionMath.java src/ModPages.java src/ModLayout.java tests/TransitionMathTest.java tests/ModPagesTest.java tests/ModLayoutTest.java
java -cp "$w" luma.ui.TransitionMathTest
java -cp "$w" luma.ui.ModPagesTest
java -cp "$w" luma.ui.ModLayoutTest
python3 tests/mod_bridge_test.py

python3 tests/ui_invariants_test.py
