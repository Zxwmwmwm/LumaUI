package luma.ui;
/** Continuous weighted cross-morph; interrupted selections start from the visible blend. */
final class BackgroundBlend {
  static final int COUNT=6;
  private final float[] weights=new float[COUNT],start=new float[COUNT];
  private float progress=1;private int target;
  BackgroundBlend(int mode){target=Math.max(0,Math.min(COUNT-1,mode));weights[target]=1;}
  float weight(int mode){return weights[mode];}
  boolean active(){return progress<1;}
  void select(int mode){mode=Math.max(0,Math.min(COUNT-1,mode));if(mode==target)return;System.arraycopy(weights,0,start,0,COUNT);target=mode;progress=0;}
  boolean step(float elapsed,float speed){if(!active())return false;
    float duration=.85f-.35f*Math.max(0,Math.min(1,speed));progress=Math.min(1,progress+Math.max(0,Math.min(.05f,elapsed))/duration);
    float t=progress*progress*(3-2*progress);for(int i=0;i<COUNT;i++)weights[i]=start[i]*(1-t)+(i==target?t:0);
    if(progress>=1){for(int i=0;i<COUNT;i++)weights[i]=i==target?1:0;return false;}return true;
  }
  static float formation(float weight){float v=Math.max(0,Math.min(1,weight));return .68f+.32f*(v*v*(3-2*v));}
}
