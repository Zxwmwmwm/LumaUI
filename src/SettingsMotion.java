package luma.ui;
/** Timing helpers shared by the settings sheet and host tests. */
final class SettingsMotion {
  private SettingsMotion(){}
  static long duration(float speed){return Math.round(450-170*Math.max(0,Math.min(1,speed)));}
  static float progress(long now,long start,long duration){return duration<=0?1:Math.max(0,Math.min(1,(now-start)/(float)duration));}
}
