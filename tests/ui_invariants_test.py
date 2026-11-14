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
assert '"Off","Lava Lamp","Space","Pop","Trail","Magnet"' in s and 'playField.pointer(' in s
assert 'playField.pressed=false' in s and 'drawPlayField(c,effect,mode,fade)' in s
assert 'finishBubbleEffect();recycleWarp();scatterSeed=' in s and 'hudEditor.close(false)' in s
assert 'revealBubble(buttonReturn,target[4])' in s and 'setVisibility(View.INVISIBLE)' in s
assert '"Off","Portal","Pulse","Orbit"' in s and 'putInt("buttonReturn",buttonReturn)' in s
assert 'backgroundBlend.select(selected)' in s and 'backgroundBlend.step(dt,target[4])' in s
assert 'addOnPreDrawListener(styler)' in sheet and 'categories.get(view)!=view.getBackground()' in sheet
assert 'animateCategoryContent(list,duration)' in sheet and 'super.dispatchTouchEvent(event)' in sheet
assert 'setTrackActiveTintList' in sheet and 'blend(panel,accent' in sheet
assert 'putBoolean("glowFollow",glowFollow)' in s and 'if(glowFollow&&(act==' in s
assert 'playField.pointer((px-body)' in s and 'lava.render' in s
assert 'if(!shown)return false;' in s and 'snapshotMix(open)' in s
assert 'PixelCopy.request(surface' in Path('src/AdaptiveSampler.java').read_text()
assert 'putBoolean("adaptiveColor",adaptiveColor)' in s and 'sampler.stop()' in s
assert 'HudEditor.start(' in s and '"Mods","Appearance","Motion","Effects","HUD Editor"' in s
assert '"Fade","Portal","Elastic","Scatter"' in s
print('UI snapshot, search, background and gesture lifecycle guards passed')
