package luma.ui;

/** Remembers the last page independently for live modules and installed mods. */
final class ModPages {
  private final int[] pages=new int[2];
  int get(boolean packages){return pages[packages?1:0];}
  void set(boolean packages,int page){pages[packages?1:0]=Math.max(0,page);}
  int visible(boolean packages,int count){return Math.min(get(packages),Math.max(0,(count-1)/4));}
}
