package luma.ui;
public final class ButtonMotionTest {
  static void near(float a,float b){if(Math.abs(a-b)>.0001f)throw new AssertionError();}
  public static void main(String[] args){
    near(ButtonMotion.reveal(0),0);near(ButtonMotion.reveal(.22f),0);near(ButtonMotion.reveal(.58f),1);near(ButtonMotion.reveal(1),1);
    near(ButtonMotion.halo(0),0);near(ButtonMotion.halo(1),0);if(ButtonMotion.halo(.5f)<.99f)throw new AssertionError("halo");
    if(ButtonMotion.duration(0)<=ButtonMotion.duration(1))throw new AssertionError("speed");
    float previous=0;for(int i=0;i<=100;i++){float a=ButtonMotion.reveal(i/100f);if(a<previous||a>1)throw new AssertionError("reveal");previous=a;}
    System.out.println("Floating button motion tests passed");
  }
}
