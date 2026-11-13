package luma.ui;
/** Timings for the floating button return animation. */
final class ButtonMotion {
  static final int NONE=0,PORTAL=1,PULSE=2,ORBIT=3;
  static float clamp(float x){return Math.max(0,Math.min(1,x));}
  static float ease(float x){x=clamp(x);return x*x*(3-2*x);}
  static float reveal(float t){return ease((t-.22f)/.36f);}
  static float halo(float t){t=clamp(t);return (float)Math.sin(Math.PI*t);}
  static long duration(float speed){return Math.round(850-300*clamp(speed));}
}
