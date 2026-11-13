package luma.ui;
public final class LavaMotionTest {
  static void check(boolean b){if(!b)throw new AssertionError();}
  public static void main(String[] args){float[] a=new float[5],b=new float[5];
    for(int i=0;i<LavaMotion.COUNT;i++)for(int t=0;t<10000;t+=13){LavaMotion.sample(i,t*.01f,a);LavaMotion.sample(i,t*.01f+.01f,b);
      for(int k=0;k<5;k++)check(Float.isFinite(a[k])&&Math.abs(a[k]-b[k])<.02f);
      check(a[0]>=0&&a[0]<=1&&a[1]>=0&&a[1]<=1&&a[2]>0&&a[3]>0);
    }
    LavaMotion.sample(0,0,a);LavaMotion.sample(1,0,b);check(Math.abs(a[0]-b[0])>.01f);System.out.println("Lava shape continuity tests passed");
  }
}
