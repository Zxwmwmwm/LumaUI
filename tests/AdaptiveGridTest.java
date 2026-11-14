package luma.ui;
public class AdaptiveGridTest {
 static void check(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] z){AdaptiveGrid grid=new AdaptiveGrid();grid.reset(0xff101010);int[] sample=new int[AdaptiveGrid.SIZE];
  for(int y=0;y<AdaptiveGrid.HEIGHT;y++)for(int x=0;x<AdaptiveGrid.WIDTH;x++)sample[y*AdaptiveGrid.WIDTH+x]=x<AdaptiveGrid.WIDTH/2?0xff888888:0xff007aff;
  grid.accept(sample);check(grid.advance(.05f));for(int i=0;i<200;i++)grid.advance(.05f);
  int left=grid.pixels()[8*AdaptiveGrid.WIDTH+3],right=grid.pixels()[8*AdaptiveGrid.WIDTH+20];check(((left>>16)&255)==((left>>8)&255));check((right&255)>((right>>16)&255)+30);check((left>>>24)==255&&(right>>>24)==255);
  int before=right;for(int i=0;i<sample.length;i++)sample[i]=0xff00cc44;grid.accept(sample);grid.advance(.05f);int after=grid.pixels()[8*AdaptiveGrid.WIDTH+20];check(after!=before);check(((after>>8)&255)<((AdaptiveColor.panel(0xff00cc44)>>8)&255));
  System.out.println("Spatial adaptive color and smoothing tests passed");
 }
}
