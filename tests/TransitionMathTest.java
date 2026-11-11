package luma.ui;

public final class TransitionMathTest {
  private static void near(float a,float b){if(Math.abs(a-b)>.0001f)throw new AssertionError(a+" != "+b);}
  public static void main(String[] args){
    near(TransitionMath.startScale(48,48,760,440),48f/760f);near(TransitionMath.startScale(0,48,760,440),.08f);
    for(float[] screen:new float[][]{{1920,1080,3},{720,360,2},{320,180,1},{1080,2400,3}}){
      float u=TransitionMath.layoutUnit(screen[2],screen[0],screen[1]);
      float h=Math.min(screen[1]-24*u,440*u),w=Math.min(screen[0]-24*u,760*u);
      if(h<390*u||w<650*u)throw new AssertionError("controls clipped");
    }
    for(int i=0;i<=100;i++){float p=i/100f,x=TransitionMath.elasticScale(.06f,p,true),y=TransitionMath.elasticScale(.06f,p,false);if(x<0||y<0||x>1.1f||y>1.1f)throw new AssertionError("elastic bounds");}
    for(float origin:new float[]{0,48,320,1024})for(float center:new float[]{100,500}){
      near(center+TransitionMath.offset(origin,center,0),origin);near(center+TransitionMath.offset(origin,center,1),center);
      near(center+TransitionMath.offset(origin,center,.5f),(origin+center)/2f);
    }
    near(TransitionMath.scale(.05f,0),.05f);near(TransitionMath.scale(.05f,1),1);
    near(TransitionMath.elasticScale(.05f,0,true),.05f);near(TransitionMath.elasticScale(.05f,1,true),1);
    near(TransitionMath.elasticScale(.05f,0,false),.05f);near(TransitionMath.elasticScale(.05f,1,false),1);
    if(TransitionMath.elasticScale(.05f,.5f,true)<=TransitionMath.elasticScale(.05f,.5f,false))throw new AssertionError("stretch");
    for(float speed:new float[]{0f,.5f,1f}){
      TransitionMath.Spring s=new TransitionMath.Spring();float last=0;
      for(int i=0;i<180;i++){float v=s.step(1f,1f/60f,speed);if(v<last-.0001f||v<0||v>1)throw new AssertionError("open");last=v;}
      near(s.value(),1);last=1;
      for(int i=0;i<180;i++){float v=s.step(0f,1f/60f,speed);if(v>last+.0001f||v<0||v>1)throw new AssertionError("close");last=v;}
      near(s.value(),0);
      s.reset(.3f);for(int i=0;i<60;i++){float v=s.step(i%2, .05f,speed);if(v<0||v>1||Float.isNaN(v))throw new AssertionError("reversal");}
    }
    TransitionMath.Spring slow=new TransitionMath.Spring(), fast=new TransitionMath.Spring();
    for(int i=0;i<30;i++)slow.step(1f,1f/30f,.5f);
    for(int i=0;i<60;i++)fast.step(1f,1f/60f,.5f);
    if(Math.abs(slow.value()-fast.value())>.01f)throw new AssertionError("frame rate");
    System.out.println("Transition geometry and spring tests passed");
  }
}
