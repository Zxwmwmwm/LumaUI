package luma.ui;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
/** Samples only the activity's game SurfaceView, never the Luma overlay. */
final class AdaptiveSampler {
  interface Result {void colors(int[] pixels);void unavailable();}
  private final Activity activity;private final View anchor;private final Result callback;private final Handler handler=new Handler(Looper.getMainLooper());
  private boolean running,pending;private int generation;private float x,y,w,h;
  AdaptiveSampler(Activity a,View anchor,Result r){activity=a;this.anchor=anchor;callback=r;}
  void start(){if(running)return;running=true;generation++;handler.post(tick);}
  void stop(){running=false;generation++;handler.removeCallbacks(tick);}
  void bounds(float x,float y,float w,float h){this.x=x;this.y=y;this.w=w;this.h=h;}
  private final Runnable tick=new Runnable(){public void run(){sample();}};
  private void later(){if(running){handler.removeCallbacks(tick);handler.postDelayed(tick,1000);}}
  private static SurfaceView largest(View v,SurfaceView best){
    if(v instanceof SurfaceView&&v.isShown()&&v.getWidth()>20&&v.getHeight()>20){SurfaceView s=(SurfaceView)v;if(best==null||(long)s.getWidth()*s.getHeight()>(long)best.getWidth()*best.getHeight())best=s;}
    if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)best=largest(g.getChildAt(i),best);}return best;
  }
  private void sample(){if(!running||pending)return;SurfaceView surface=largest(activity.getWindow().getDecorView(),null);
    if(surface==null||w<=0||h<=0||!surface.getHolder().getSurface().isValid()){callback.unavailable();later();return;}
    int[] src=new int[2],pos=new int[2];surface.getLocationOnScreen(src);anchor.getLocationOnScreen(pos);
    int left=Math.round(pos[0]+x-src[0]),top=Math.round(pos[1]+y-src[1]);
    int right=Math.round(pos[0]+x+w-src[0]),bottom=Math.round(pos[1]+y+h-src[1]);
    if(left<0||top<0||right>surface.getWidth()||bottom>surface.getHeight()||right<=left||bottom<=top){callback.unavailable();later();return;}
    final Bitmap bmp;try{bmp=Bitmap.createBitmap(AdaptiveGrid.WIDTH,AdaptiveGrid.HEIGHT,Bitmap.Config.ARGB_8888);}catch(OutOfMemoryError e){callback.unavailable();later();return;}
    final int request=generation;pending=true;
    try{PixelCopy.request(surface,new Rect(left,top,right,bottom),bmp,result->{
      pending=false;if(running&&request==generation){if(result==PixelCopy.SUCCESS){int[] pixels=new int[AdaptiveGrid.SIZE];bmp.getPixels(pixels,0,AdaptiveGrid.WIDTH,0,0,AdaptiveGrid.WIDTH,AdaptiveGrid.HEIGHT);if(AdaptiveColor.average(pixels)!=0)callback.colors(pixels);else callback.unavailable();}else callback.unavailable();}
      bmp.recycle();later();
    },handler);}catch(RuntimeException e){pending=false;bmp.recycle();if(running&&request==generation)callback.unavailable();later();}
  }
}
