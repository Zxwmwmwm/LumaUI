package luma.ui;

public final class TransitionMathTest {
  private static void near(float a,float b){if(Math.abs(a-b)>.0001f)throw new AssertionError(a+" != "+b);}
  public static void main(String[] args){
    near(TransitionMath.startScale(48,48,760,440),48f/760f);
    near(TransitionMath.startScale(0,48,760,440),.08f);
    for(float origin:new float[]{0,48,320,1024})for(float center:new float[]{100,500}){
      near(center+TransitionMath.offset(origin,center,0),origin);
      near(center+TransitionMath.offset(origin,center,1),center);
      near(center+TransitionMath.offset(origin,center,.5f),(origin+center)/2f);
    }
    near(TransitionMath.scale(.05f,0),.05f);near(TransitionMath.scale(.05f,1),1);
    near(TransitionMath.scale(.05f,-1),.05f);near(TransitionMath.scale(.05f,2),1);
    for(int i=0;i<=100;i++){float p=i/100f;float scale=TransitionMath.scale(.06f,p);if(scale<.06f||scale>1)throw new AssertionError("scale");}
    System.out.println("Transition geometry tests passed");
  }
}
