package luma.ui;

/** Pure geometry for expanding the menu from the floating button. */
final class TransitionMath {
  private TransitionMath() {}
  static float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
  static float offset(float origin, float center, float progress) {
    return (origin-center)*(1f-clamp(progress));
  }
  static float startScale(float bubbleWidth,float bubbleHeight,float menuWidth,float menuHeight) {
    if(bubbleWidth<=0f||bubbleHeight<=0f||menuWidth<=0f||menuHeight<=0f)return .08f;
    return Math.max(.01f,Math.min(1f,Math.min(bubbleWidth/menuWidth,bubbleHeight/menuHeight)));
  }
  static float scale(float start,float progress) {
    float safe=clamp(start), p=clamp(progress);
    return safe+(1f-safe)*p;
  }
}
