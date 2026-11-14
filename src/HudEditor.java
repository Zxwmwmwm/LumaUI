package luma.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Compact Luma controls around the launcher's real HUD editor. */
final class HudEditor {
  private final Activity activity;private final FrameLayout host;private final Object manager;private final Method setMode,getSize,setSize;private final Runnable done;private final LinearLayout tools;private final TextView size;private boolean closed;
  private int dp(float n){return Math.round(n*activity.getResources().getDisplayMetrics().density);}
  private GradientDrawable shape(int color,int border,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(border!=0)d.setStroke(dp(1),border);return d;}
  private TextView label(String s,int width){TextView v=new TextView(activity);v.setText(s);v.setTextSize(14);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER);v.setContentDescription(s);tools.addView(v,new LinearLayout.LayoutParams(dp(width),dp(46)));return v;}
  static HudEditor start(Activity a,FrameLayout root,int accent,Runnable done)throws Exception{
    Class<?> c=Class.forName("org.levimc.launcher.core.mods.inbuilt.overlay.InbuiltOverlayManager");Object m=c.getMethod("getInstance").invoke(null);if(m==null)throw new IllegalStateException("HUD editor unavailable");
    try{Field f=c.getDeclaredField("hudEditorMode");f.setAccessible(true);if(f.getBoolean(m))throw new IllegalStateException("Another HUD editor is already open");}catch(NoSuchFieldException ignored){}
    Method mode=c.getMethod("setHudEditorMode",boolean.class),get=c.getMethod("getSelectedHudEditorButtonSize"),set=c.getMethod("setSelectedHudEditorButtonSize",int.class);
    HudEditor editor=new HudEditor(a,root,m,mode,get,set,accent,done);
    try{mode.invoke(m,true);editor.show();return editor;}catch(Exception e){try{mode.invoke(m,false);}catch(Exception ignored){}editor.remove();throw e;}
  }
  private HudEditor(Activity a,FrameLayout root,Object manager,Method mode,Method get,Method set,int accent,Runnable done){
    activity=a;host=root;this.manager=manager;setMode=mode;getSize=get;setSize=set;this.done=done;
    tools=new LinearLayout(a);tools.setGravity(Gravity.CENTER_VERTICAL);tools.setPadding(dp(7),dp(3),dp(7),dp(3));tools.setBackground(shape(0xf71a1b29,(accent&0xffffff)|0xbb000000,15));tools.setElevation(dp(12));
    TextView title=label("HUD EDITOR",100);title.setTextSize(11);title.setTextColor(accent);
    TextView minus=label("−",42);minus.setBackground(shape(0xff383949,0,9));minus.setOnClickListener(v->resize(-4));
    size=label("Select HUD",94);size.setTextSize(11);
    TextView plus=label("+",42);plus.setBackground(shape(0xff383949,0,9));plus.setOnClickListener(v->resize(4));
    TextView finish=label("Done",70);finish.setBackground(shape((accent&0xffffff)|0xff000000,0,9));finish.setOnClickListener(v->close(true));
  }
  private final Runnable refresh=new Runnable(){public void run(){if(closed)return;try{int n=((Number)getSize.invoke(manager)).intValue();size.setText(n>0?n+" dp":"Select HUD");}catch(Exception ignored){size.setText("Unavailable");}tools.postDelayed(this,350);}};
  private void show(){FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);lp.topMargin=dp(10);host.addView(tools,lp);refresh.run();}
  private void resize(int delta){try{int n=((Number)getSize.invoke(manager)).intValue();if(n>0)setSize.invoke(manager,Math.max(24,Math.min(160,n+delta)));}catch(Exception ignored){}}
  private void remove(){tools.removeCallbacks(refresh);if(tools.getParent() instanceof ViewGroup)((ViewGroup)tools.getParent()).removeView(tools);}
  void close(boolean notify){if(closed)return;closed=true;remove();try{setMode.invoke(manager,false);}catch(Exception ignored){}if(notify&&done!=null)done.run();}
}
