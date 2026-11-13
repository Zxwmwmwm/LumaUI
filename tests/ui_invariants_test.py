#!/usr/bin/env python3
"""Static guards for rendering and gesture lifecycle; not a substitute for Android UI testing."""
from pathlib import Path
s=Path('src/LumaUI.java').read_text()
sheet=Path('src/SettingsSheet.java').read_text()
assert 'if(!snapshotValid){Canvas snapshot=new Canvas(warpBitmap)' in s
assert 'snapshotValid=true;warpBitmap.prepareToDraw()' in s
assert 'warpBitmap.recycle()' not in s
assert 'if(act==MotionEvent.ACTION_UP&&!dragged&&!modBusy)activateModAt' in s
assert 'if(act==MotionEvent.ACTION_UP&&dragged&&modLayout==0' in s
assert 'ModBridge.renderSettings' in sheet and 'ModBridge.stopSettings(container)' in sheet
assert 'modLayout",selected' in s and 'saveModOffset()' in s and 'loadModOffset()' in s
print('UI snapshot, settings and gesture lifecycle guards passed')
