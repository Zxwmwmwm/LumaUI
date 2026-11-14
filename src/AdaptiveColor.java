package luma.ui;
/** On-device color selection, independent of Android. No frame is retained. */
final class AdaptiveColor {
  static int average(int[] pixels){long r=0,g=0,b=0,n=0;for(int c:pixels){if((c>>>24)<64)continue;r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;n++;}if(n==0)return 0;return 0xff000000|((int)(r/n)<<16)|((int)(g/n)<<8)|(int)(b/n);}
  static int panel(int rgb){return 0xff000000|(component(rgb,16,.27f,9)<<16)|(component(rgb,8,.27f,9)<<8)|component(rgb,0,.27f,11);}
  static int accent(int rgb){return 0xff000000|(component(rgb,16,.62f,92)<<16)|(component(rgb,8,.62f,92)<<8)|component(rgb,0,.62f,102);}
  private static int component(int c,int shift,float scale,int offset){return Math.min(255,Math.round(((c>>shift)&255)*scale)+offset);}
}
