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
    float[] point=new float[2];
    for(float ax:new float[]{0,120,800})for(float ay:new float[]{0,220,440})for(float px:new float[]{0,300,800})for(float py:new float[]{0,200,440}){
      TransitionMath.warpPoint(px,py,ax,ay,800,440,0,point,0);near(point[0],ax);near(point[1],ay);
      TransitionMath.warpPoint(px,py,ax,ay,800,440,1,point,0);near(point[0],px);near(point[1],py);
      TransitionMath.warpPoint(px,py,ax,ay,800,440,.5f,point,0);if(!Float.isFinite(point[0])||!Float.isFinite(point[1]))throw new AssertionError("warp finite");
    }
    float[] nearPoint=new float[2],farPoint=new float[2];TransitionMath.warpPoint(100,0,0,0,800,440,.5f,nearPoint,0);TransitionMath.warpPoint(800,0,0,0,800,440,.5f,farPoint,0);
    if(nearPoint[0]/100f>=farPoint[0]/800f)throw new AssertionError("warp must bend, not just scale");
    // A noticeably curved funnel while retaining positive mesh orientation.
    TransitionMath.warpPoint(100,0,0,0,800,440,.5f,point,0);if(point[1]<8f)throw new AssertionError("bend too subtle");
    for(float ax:new float[]{0,400,800})for(float ay:new float[]{0,220,440})for(float progress:new float[]{.05f,.2f,.5f,.8f,.95f}){
      float[] a=new float[2],b=new float[2],c=new float[2];
      for(int ix=0;ix<40;ix++)for(int iy=0;iy<24;iy++){
        float px=ix*20f,py=iy*440f/24f;
        TransitionMath.warpPoint(px,py,ax,ay,800,440,progress,a,0);TransitionMath.warpPoint(px+20,py,ax,ay,800,440,progress,b,0);TransitionMath.warpPoint(px,py+440f/24f,ax,ay,800,440,progress,c,0);
        if((b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])<=0)throw new AssertionError("folded mesh");
      }
    }
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
