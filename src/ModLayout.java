package luma.ui;
/** Scroll geometry in density-independent panel units. */
final class ModLayout {
  static final int PAGES=0,VERTICAL=1,HORIZONTAL=2;
  static final float ROW=44f,CARD=210f,HEIGHT=176f;
  static float max(int mode,int count,float viewportWidth){
    if(count<=0||mode==PAGES)return 0;
    return Math.max(0,(mode==VERTICAL?count*ROW-HEIGHT:count*CARD-Math.max(0,viewportWidth)));
  }
  static float clamp(float value,int mode,int count,float viewportWidth){return Math.max(0,Math.min(max(mode,count,viewportWidth),value));}
  static int index(int mode,float local,float offset,int page,int count){
    float step=mode==HORIZONTAL?CARD:ROW;
    int i=(mode==PAGES?page*4:0)+(int)Math.floor((local+(mode==PAGES?0:offset))/step);
    return local<0||i<0||i>=count?-1:i;
  }
}
