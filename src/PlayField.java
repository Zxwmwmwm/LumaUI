package luma.ui;
/** Small bounded, deterministic interface-only particle simulation. Coordinates are normalized. */
final class PlayField {
  static final int POP=3,TRAIL=4,MAGNET=5,COUNT=18,TRAIL_COUNT=36;
  final float[] x=new float[COUNT],y=new float[COUNT],vx=new float[COUNT],vy=new float[COUNT],burst=new float[COUNT];
  final float[] tx=new float[TRAIL_COUNT],ty=new float[TRAIL_COUNT],age=new float[TRAIL_COUNT];
  boolean pressed;float pointerX,pointerY;int nextTrail;private int seed=0x15a27;
  PlayField(){reset();}
  private float random(){seed=seed*1664525+1013904223;return ((seed>>>8)&0xffff)/65535f;}
  void reset(){pressed=false;nextTrail=0;for(int i=0;i<COUNT;i++){x[i]=.04f+.92f*random();y[i]=.04f+.92f*random();vx[i]=(random()-.5f)*.12f;vy[i]=(random()-.5f)*.12f;burst[i]=0;}for(int i=0;i<TRAIL_COUNT;i++)age[i]=2;}
  void pointer(float nx,float ny,boolean down,int mode,float aspect){
    if(!down&&pressed&&mode==MAGNET)release(aspect);
    pointerX=clamp(nx);pointerY=clamp(ny);pressed=down;
    if(down&&mode==TRAIL){int prev=(nextTrail+TRAIL_COUNT-1)%TRAIL_COUNT;float dx=tx[prev]-pointerX,dy=ty[prev]-pointerY;if(age[prev]>=1.5f||dx*dx+dy*dy>.00045f){tx[nextTrail]=pointerX;ty[nextTrail]=pointerY;age[nextTrail]=0;nextTrail=(nextTrail+1)%TRAIL_COUNT;}}
    if(down&&mode==POP){int hit=-1;float best=.065f*.065f;for(int i=0;i<COUNT;i++){if(burst[i]>0)continue;float dx=(x[i]-pointerX)*aspect,dy=y[i]-pointerY,d=dx*dx+dy*dy;if(d<best){best=d;hit=i;}}if(hit>=0)burst[hit]=.36f;}
  }
  private void release(float aspect){
    float ax=Math.max(.2f,aspect);
    for(int i=0;i<COUNT;i++){
      float dx=(x[i]-pointerX)*ax,dy=y[i]-pointerY;float d=(float)Math.sqrt(dx*dx+dy*dy);
      if(d<.025f){double angle=(i*2.399963)+random()*.5;dx=(float)Math.cos(angle);dy=(float)Math.sin(angle);d=1;}
      float force=.48f+random()*.22f;vx[i]=dx/d*force/ax;vy[i]=dy/d*force;
    }
  }
  void step(float elapsed,int mode){float dt=Math.max(0,Math.min(.05f,elapsed));
    for(int i=0;i<TRAIL_COUNT;i++)age[i]=Math.min(2,age[i]+dt);
    for(int i=0;i<COUNT;i++){
      if(burst[i]>0){burst[i]-=dt;if(burst[i]<=0){burst[i]=0;x[i]=.05f+.9f*random();y[i]=1.04f;}continue;}
      if(mode==POP){y[i]-=dt*(.045f+.015f*(i%4));x[i]+=(float)Math.sin(y[i]*9+i)*dt*.012f;if(y[i]<-.05f){y[i]=1.04f;x[i]=.05f+.9f*random();}}
      if(mode==MAGNET){if(pressed){float dx=pointerX-x[i],dy=pointerY-y[i];float d=(float)Math.sqrt(dx*dx+dy*dy+.004f);vx[i]+=dx/d*dt*1.1f;vy[i]+=dy/d*dt*1.1f;}
        float friction=pressed?2.8f:.32f;vx[i]*=(float)Math.exp(-dt*friction);vy[i]*=(float)Math.exp(-dt*friction);x[i]+=vx[i]*dt;y[i]+=vy[i]*dt;
        if(x[i]<0||x[i]>1){x[i]=clamp(x[i]);vx[i]=-vx[i]*.8f;}if(y[i]<0||y[i]>1){y[i]=clamp(y[i]);vy[i]=-vy[i]*.8f;}}
    }
  }
  private static float clamp(float n){return Math.max(0,Math.min(1,n));}
}
