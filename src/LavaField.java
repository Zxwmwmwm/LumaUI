package luma.ui;
/** Independent lava simulation: warm wax rises, cool wax sinks; scalar fields merge smoothly. */
final class LavaField {
  static final int COUNT=10;
  final float[] x=new float[COUNT],y=new float[COUNT],vx=new float[COUNT],vy=new float[COUNT],heat=new float[COUNT],radius=new float[COUNT],angle=new float[COUNT];
  private int seed=0x15737;private float time,aspect=1.3f;
  private float random(){seed=seed*1664525+1013904223;return ((seed>>>8)&65535)/65535f;}
  LavaField(){for(int i=0;i<COUNT;i++){x[i]=.12f+random()*1.05f;y[i]=.1f+random()*.8f;vx[i]=(random()-.5f)*.05f;vy[i]=(random()-.5f)*.05f;heat[i]=random();radius[i]=.060f+random()*.032f;angle[i]=random()*6.28f;}}
  void step(float elapsed,float aspect){float dt=Math.max(0,Math.min(.05f,elapsed));this.aspect=Math.max(.3f,Math.min(3f,aspect));time+=dt;
    for(int i=0;i<COUNT;i++){
      float bottom=Math.max(0,(y[i]-.62f)/.38f),top=Math.max(0,(.38f-y[i])/.38f);
      heat[i]=Math.max(0,Math.min(1,heat[i]+dt*(bottom*.36f-top*.36f+(.5f-heat[i])*.025f)));
      vx[i]+=dt*(.022f*(float)Math.sin(time*.39f+i*2.11f)+.01f*(float)Math.sin(time*.9f+i));
      vy[i]+=dt*(.047f-.094f*heat[i]+.014f*(float)Math.sin(time*.36f+i));
      float damping=(float)Math.exp(-dt*.55f);vx[i]*=damping;vy[i]*=damping;
      float speed=(float)Math.sqrt(vx[i]*vx[i]+vy[i]*vy[i]);if(speed>.13f){vx[i]*=.13f/speed;vy[i]*=.13f/speed;}
      x[i]+=vx[i]*dt;y[i]+=vy[i]*dt;float edge=radius[i]*.55f;
      if(x[i]<edge){x[i]=edge;vx[i]=Math.abs(vx[i])*.65f;}if(x[i]>this.aspect-edge){x[i]=this.aspect-edge;vx[i]=-Math.abs(vx[i])*.65f;}
      if(y[i]<edge){y[i]=edge;vy[i]=Math.abs(vy[i])*.65f;}if(y[i]>1-edge){y[i]=1-edge;vy[i]=-Math.abs(vy[i])*.65f;}
      angle[i]+=dt*(.12f+.06f*(float)Math.sin(time*.31f+i));
    }
  }
  void render(int[] pixels,int width,int height,int rgb){
    float[] cos=new float[COUNT],sin=new float[COUNT],ax=new float[COUNT],ay=new float[COUNT];
    for(int i=0;i<COUNT;i++){cos[i]=(float)Math.cos(angle[i]);sin[i]=(float)Math.sin(angle[i]);float stretch=1.25f+.42f*(float)Math.sin(time*.48f+i*1.7f);ax[i]=radius[i]*stretch;ay[i]=radius[i]/stretch;}
    for(int row=0;row<height;row++)for(int col=0;col<width;col++){
      float xx=aspect*(col+.5f)/width,yy=(row+.5f)/height,field=0;
      for(int i=0;i<COUNT;i++){float dx=xx-x[i],dy=yy-y[i],rx=(dx*cos[i]+dy*sin[i])/ax[i],ry=(-dx*sin[i]+dy*cos[i])/ay[i];field+=1f/(rx*rx+ry*ry+.04f);}
      float edge=Math.max(0,Math.min(1,(field-.82f)*4.5f));int alpha=Math.round(edge*(42+Math.min(26,Math.max(0,field-1)*9)));
      pixels[row*width+col]=(alpha<<24)|(rgb&0xffffff);
    }
  }
}
