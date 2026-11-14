package luma.ui;
/** Direct, interruptible control feedback independent of touch ripples. */
final class PressMotion {
 static final int OFF=0,SOFT=1,WATER=2;
 float x,y,elapsed=1,intensity,releaseIntensity;boolean held;
 void down(float x,float y){this.x=x;this.y=y;held=true;elapsed=0;intensity=.12f;}
 void release(){if(held){held=false;elapsed=0;releaseIntensity=intensity;}}
 void cancel(){held=false;elapsed=1;intensity=releaseIntensity=0;}
 boolean step(float dt){dt=Math.max(0,Math.min(.05f,dt));if(held)intensity=Math.min(1,intensity+dt/.12f);else if(elapsed<.85f)elapsed=Math.min(.85f,elapsed+dt);return held||elapsed<.85f;}
 boolean active(){return held||elapsed<.85f;}
 private float wave(){return held?intensity:(float)(releaseIntensity*Math.exp(-5.5f*elapsed)*Math.cos(14*elapsed));}
 float scaleX(int style){if(style==OFF||!active())return 1;float v=wave();return style==WATER?1+.07f*v:1-.09f*v;}
 float scaleY(int style){if(style==OFF||!active())return 1;return 1-.09f*wave();}
}
