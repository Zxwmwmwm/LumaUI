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
assert 'dispatchTouchEvent(MotionEvent event)' in sheet and 'super.dispatchTouchEvent(event)' in sheet
assert 'setClickable(false)' in sheet and 'animate&&!dialog.gestureActive' in sheet
assert 'ValueAnimator.areAnimatorsEnabled()' in sheet and 'closeAnimated(true)' in sheet
assert 'modLayout",selected' in s and 'saveModOffset()' in s and 'loadModOffset()' in s
assert 'if(filtering())return;modPages.set' in s and 'if(!filtering()&&modLayout!=0)' in s
assert 'ModSearch.filter(allModRows' in s and 'ModBridge.syncModules(getContext(),allModRows)' in s
assert '"Off","Stretch","Space","Pop","Trail","Magnet"' in s and 'playField.pointer(' in s
assert 'playField.pressed=false' in s and 'else drawPlayField(c,effect)' in s
print('UI snapshot, search, background and gesture lifecycle guards passed')
