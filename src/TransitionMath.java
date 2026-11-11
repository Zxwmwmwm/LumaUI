package luma.ui;

/** Pure geometry and stable motion for expanding from the floating button. */
final class TransitionMath {
  private TransitionMath() {}
  static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
  static float layoutUnit(float density,float width,float height) {
    if(density<=0f||width<=0f||height<=0f)return .1f;
    return Math.max(.01f,Math.min(density,Math.min(width/700f,height/420f)));
  }
  static float offset(float origin, float center, float progress) { return (origin-center)*(1f-clamp(progress)); }
  static float startScale(float bubbleWidth,float bubbleHeight,float menuWidth,float menuHeight) {
    if(bubbleWidth<=0f||bubbleHeight<=0f||menuWidth<=0f||menuHeight<=0f)return .08f;
    return Math.max(.01f,Math.min(1f,Math.min(bubbleWidth/menuWidth,bubbleHeight/menuHeight)));
  }
  static float scale(float start,float progress) {float p=clamp(progress),s=clamp(start);return s+(1f-s)*p;}
  static float elasticScale(float start,float progress,boolean stretchedAxis) {
    float p=clamp(progress), envelope=4f*p*(1f-p);
    return scale(start,p)*(stretchedAxis?1f+.42f*envelope:1f-.18f*envelope);
  }
  static final class Spring {
    private float value,velocity;
    void reset(float initial){value=clamp(initial);velocity=0f;}
    float value(){return value;}
    float velocity(){return velocity;}
    float step(float goal,float elapsed,float speed) {
      goal=clamp(goal);float remaining=Math.max(0f,Math.min(.05f,elapsed));
      float stiffness=115f+155f*clamp(speed),damping=2.05f*(float)Math.sqrt(stiffness);
      while(remaining>0f){float dt=Math.min(remaining,1f/120f);remaining-=dt;
        velocity+=(stiffness*(goal-value)-damping*velocity)*dt;
        value+=velocity*dt;
        if(value<0f){value=0f;if(velocity<0f)velocity=0f;}
        if(value>1f){value=1f;if(velocity>0f)velocity=0f;}
      }
      if(Math.abs(goal-value)<.0003f&&Math.abs(velocity)<.003f){value=goal;velocity=0f;}
      return value;
    }
  }
}
