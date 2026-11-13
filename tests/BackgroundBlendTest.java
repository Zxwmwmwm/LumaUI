package luma.ui;
public final class BackgroundBlendTest {
  static void check(boolean c){if(!c)throw new AssertionError();}
  static void near(float a,float b){if(Math.abs(a-b)>.0001f)throw new AssertionError(a+" "+b);}
  public static void main(String[] args){
    BackgroundBlend b=new BackgroundBlend(1);near(b.weight(1),1);b.select(2);near(b.weight(1),1);near(b.weight(2),0);
    for(int i=0;i<5;i++)b.step(.05f,.5f);float old=b.weight(1),incoming=b.weight(2);check(old>0&&incoming>0);near(old+incoming,1);
    b.select(4);near(old,b.weight(1));near(incoming,b.weight(2));near(0,b.weight(4));for(int i=0;i<30;i++)b.step(.05f,.5f);check(!b.active());near(b.weight(4),1);
    b.select(0);for(int i=0;i<30;i++)b.step(.05f,.5f);near(b.weight(0),1);near(b.weight(4),0);check(BackgroundBlend.formation(0)<BackgroundBlend.formation(1));
    for(int mode=0;mode<6;mode++){b.select(mode);for(int j=0;j<5;j++){b.step(.05f,.5f);float sum=0;for(int n=0;n<6;n++){float w=b.weight(n);check(Float.isFinite(w)&&w>=0&&w<=1);sum+=w;}near(sum,1);}}
    System.out.println("Background blend and interruption tests passed");
  }
}
