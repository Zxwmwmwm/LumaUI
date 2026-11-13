#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
w=$(mktemp -d);trap 'rm -rf "$w"' EXIT
javac -encoding UTF-8 -d "$w" src/TransitionMath.java src/ModPages.java tests/TransitionMathTest.java tests/ModPagesTest.java
java -cp "$w" luma.ui.TransitionMathTest
java -cp "$w" luma.ui.ModPagesTest
python3 tests/mod_bridge_test.py
