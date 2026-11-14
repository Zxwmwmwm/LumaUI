package luma.ui;
/** Spatial, locally sampled theme. Rows and columns correspond to the panel's backdrop. */
final class AdaptiveGrid {
 static final int WIDTH=24,HEIGHT=16,SIZE=WIDTH*HEIGHT;
 private final float[] current=new float[SIZE*3],goal=new float[SIZE*3];
 private final int[] colors=new int[SIZE];
 void reset(int initial){for(int i=0;i<SIZE;i++)for(int c=0;c<3;c++){int v=(initial>>(16-c*8))&255;current[i*3+c]=goal[i*3+c]=v;}pack();}
 void accept(int[] source){if(source.length!=SIZE)throw new IllegalArgumentException("grid size");
   for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
     int r=0,g=0,b=0,n=0;
     for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){int xx=x+dx,yy=y+dy;if(xx<0||xx>=WIDTH||yy<0||yy>=HEIGHT)continue;int c=source[yy*WIDTH+xx];if((c>>>24)<64)continue;r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;n++;}
     if(n==0)continue;int c=AdaptiveColor.panel(0xff000000|((r/n)<<16)|((g/n)<<8)|(b/n));int i=(y*WIDTH+x)*3;
     goal[i]=(c>>16)&255;goal[i+1]=(c>>8)&255;goal[i+2]=c&255;
   }
 }
 boolean advance(float dt){float k=1f-(float)Math.exp(-Math.max(0,Math.min(.1f,dt))*2.1f);boolean active=false;
   for(int i=0;i<current.length;i++){current[i]+=(goal[i]-current[i])*k;if(Math.abs(current[i]-goal[i])<.25f)current[i]=goal[i];else active=true;}pack();return active;}
 private void pack(){for(int i=0;i<SIZE;i++)colors[i]=0xff000000|(Math.round(current[i*3])<<16)|(Math.round(current[i*3+1])<<8)|Math.round(current[i*3+2]);}
 int[] pixels(){return colors;}
}
