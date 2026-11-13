package luma.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.RadialGradient;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/** Overlay and controls for the launcher’s existing mods. */
public final class LumaUI {
  private static final Handler MAIN = new Handler(Looper.getMainLooper());
  private static boolean enabled;
  private static Activity attached;
  private static FrameLayout root;
  private static Panel panel;
  private static View bubble;
  private static final Runnable monitor = new Runnable() {
    public void run() {
      if (!enabled) return;
      try {
        Class<?> managerClass = Class.forName("org.levimc.launcher.core.mods.inbuilt.overlay.InbuiltOverlayManager");
        Method get = managerClass.getMethod("getInstance");
        Object manager = get.invoke(null);
        if (manager != null) {
          Field field = managerClass.getDeclaredField("activity");
          field.setAccessible(true);
          Activity a = (Activity)field.get(manager);
          if (a != null && !a.isFinishing() && a != attached) attach(a);
        }
      } catch (Throwable ignored) { }
      MAIN.postDelayed(this, 1000);
    }
  };
  public static void start() { MAIN.post(new Runnable(){ public void run(){ if (!enabled) { enabled=true; monitor.run(); } }}); }
  public static void stop() { MAIN.post(new Runnable(){ public void run(){ enabled=false; MAIN.removeCallbacks(monitor); detach(); }}); }
  private static void detach() {
    if (panel != null) panel.release();
    if (root != null && root.getParent() instanceof ViewGroup) ((ViewGroup)root.getParent()).removeView(root);
    root=null; panel=null; bubble=null; attached=null;
  }
  private static int dp(Context c, float n) { return (int)(n*c.getResources().getDisplayMetrics().density+0.5f); }
  private static void attach(Activity a) {
    detach(); attached=a;
    root=new FrameLayout(a); root.setClipChildren(false);
    panel=new Panel(a); panel.setVisibility(View.GONE);
    root.addView(panel, new FrameLayout.LayoutParams(-1,-1));
    bubble=new Bubble(a);
    FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(dp(a,48),dp(a,48), Gravity.RIGHT|Gravity.CENTER_VERTICAL);
    p.rightMargin=dp(a,12); root.addView(bubble,p);
    final FrameLayout host=root; final View button=bubble; final Panel sheet=panel;
    final SharedPreferences position=a.getSharedPreferences("lumaui_settings",0);
    button.setOnTouchListener(new View.OnTouchListener(){
      float downX,downY,startX,startY; boolean dragging;
      public boolean onTouch(View v,MotionEvent e){
        switch(e.getActionMasked()){
          case MotionEvent.ACTION_DOWN:
            downX=e.getRawX();downY=e.getRawY();startX=v.getX();startY=v.getY();dragging=false;return true;
          case MotionEvent.ACTION_MOVE:
            float dx=e.getRawX()-downX,dy=e.getRawY()-downY;
            if(!dragging && dx*dx+dy*dy>dp(v.getContext(),8)*dp(v.getContext(),8)) dragging=true;
            if(dragging){v.setX(Math.max(0,Math.min(host.getWidth()-v.getWidth(),startX+dx)));v.setY(Math.max(0,Math.min(host.getHeight()-v.getHeight(),startY+dy)));}
            return true;
          case MotionEvent.ACTION_UP:
            if(dragging){float maxX=Math.max(1,host.getWidth()-v.getWidth()),maxY=Math.max(1,host.getHeight()-v.getHeight()); position.edit().putFloat("buttonX",v.getX()/maxX).putFloat("buttonY",v.getY()/maxY).apply();}
            else v.performClick(); return true;
          case MotionEvent.ACTION_CANCEL: return true;
          default: return true;
        }
      }
    });
    button.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ sheet.open(); }});
    a.addContentView(root,new ViewGroup.LayoutParams(-1,-1));
    host.post(new Runnable(){public void run(){if(position.contains("buttonX")){button.setX(Math.max(0,Math.min(1,position.getFloat("buttonX",1)))*Math.max(0,host.getWidth()-button.getWidth()));button.setY(Math.max(0,Math.min(1,position.getFloat("buttonY",.5f)))*Math.max(0,host.getHeight()-button.getHeight()));}}});
  }
  private static final class Bubble extends View {
    final Paint p=new Paint(3); Bubble(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;SharedPreferences prefs=getContext().getSharedPreferences("lumaui_settings",0);
      int ac=prefs.getInt("accentColor",Color.rgb((int)(prefs.getFloat("red",.57f)*255),(int)(prefs.getFloat("green",.50f)*255),(int)(prefs.getFloat("blue",1f)*255)));
      float glow=Math.max(0f,Math.min(1f,prefs.getFloat("glowStrength",.8f)));
      p.setStyle(Paint.Style.FILL);p.setColor(0xff171925);p.setShadowLayer(w*.16f,0,0,(ac&0x00ffffff)|((int)(170*glow)<<24));c.drawCircle(w/2,h/2,w*.43f,p);p.clearShadowLayer();
      p.setShader(new RadialGradient(w*.34f,h*.24f,w*.8f,new int[]{(ac&0x00ffffff)|0x88000000,0x11171925,0x00171925},null,Shader.TileMode.CLAMP));c.drawCircle(w/2,h/2,w*.43f,p);p.setShader(null);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w*.025f);p.setColor((ac&0x00ffffff)|0xdd000000);c.drawCircle(w/2,h/2,w*.43f,p);p.setStyle(Paint.Style.FILL);p.setColor(-1);p.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0));p.setTextSize(w*.40f);p.setTextAlign(Paint.Align.CENTER);c.drawText("L",w/2,h*.65f,p);}
  }
  private static final class Panel extends View {
    final Paint p=new Paint(3), meshPaint=new Paint(3); final SharedPreferences prefs;
    static final int MESH_X=40,MESH_Y=24;final float[] mesh=new float[(MESH_X+1)*(MESH_Y+1)*2]; Bitmap warpBitmap;
    List<ModBridge.Row> modRows=new ArrayList<>(); boolean modPackages=false,modBusy=false,modReleased=false; int modPage=0,modRequest=0; String modStatus="";
    float open=0, tabMotion=0; boolean shown=false, touchRipple=true, smoothBackground=true; int tab=0, active=-1, motionMode=1, colorSlot=0, depthMode=2, transitionStyle=2;
    final TransitionMath.Spring spring=new TransitionMath.Spring(); float tapAge=1f,tapX,tapY; long resetArmedUntil;
    final int[] palette=new int[3]; float phaseTime=0, lightX=.82f, lightY=.12f, targetLightX=.82f, targetLightY=.12f;
    float[] target=new float[6], shownValue=new float[6];
    float x,y,w,h,side,body,unit,anchorX,anchorY,anchorWidth,anchorHeight; boolean haveAnchor; long last;
    Panel(Context c){ super(c); setClickable(true); setLayerType(View.LAYER_TYPE_SOFTWARE,null); prefs=c.getSharedPreferences("lumaui_settings",0);
      palette[0]=prefs.getInt("accentColor",Color.rgb((int)(prefs.getFloat("red",.57f)*255),(int)(prefs.getFloat("green",.50f)*255),(int)(prefs.getFloat("blue",1f)*255)));
      palette[1]=prefs.getInt("panelColor",0xff12141f); palette[2]=prefs.getInt("effectColor",0xff9280ff);
      motionMode=Math.max(0,Math.min(2,prefs.getInt("motionMode",prefs.getBoolean("bubbles",true)?1:0)));
      transitionStyle=Math.max(0,Math.min(2,prefs.getInt("transitionStyle",prefs.getBoolean("portalTransition",true)?2:0)));
      depthMode=Math.max(0,Math.min(2,prefs.getInt("depthMode",2)));touchRipple=prefs.getBoolean("touchRipple",true);smoothBackground=prefs.getBoolean("smoothBackground",true);
      loadColor(0);target[3]=prefs.getFloat("opacity",.92f);target[4]=prefs.getFloat("motion",.5f);target[5]=prefs.getFloat("glowStrength",.8f);System.arraycopy(target,0,shownValue,0,6);
    }
    void release(){modReleased=true;modRequest++;if(warpBitmap!=null){warpBitmap.recycle();warpBitmap=null;}}
    boolean prepareWarp(float width,float height){
      float factor=Math.min(1f,1536f/Math.max(width,height));int bw=Math.max(1,Math.round(width*factor)),bh=Math.max(1,Math.round(height*factor));
      if(warpBitmap!=null&&(warpBitmap.getWidth()!=bw||warpBitmap.getHeight()!=bh))release();
      if(warpBitmap==null)try{warpBitmap=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);}catch(OutOfMemoryError ignored){return false;}
      return true;
    }
    void drawWarp(Canvas target,float width,float height,float ax,float ay,float progress){
      int i=0;for(int row=0;row<=MESH_Y;row++)for(int col=0;col<=MESH_X;col++){
        TransitionMath.warpPoint(width*col/MESH_X,height*row/MESH_Y,ax,ay,width,height,progress,mesh,i);i+=2;
      }
      target.drawBitmapMesh(warpBitmap,MESH_X,MESH_Y,mesh,0,null,0,meshPaint);
    }
    void open(){shown=true;if(bubble!=null){anchorWidth=bubble.getWidth();anchorHeight=bubble.getHeight();anchorX=bubble.getX()+anchorWidth/2f;anchorY=bubble.getY()+anchorHeight/2f;haveAnchor=anchorWidth>0&&anchorHeight>0;bubble.setVisibility(View.GONE);}setVisibility(View.VISIBLE);if(tab==3)refreshMods();invalidate();}
    void refreshMods(){
      final int request=++modRequest;modBusy=true;modStatus="Loading…";invalidate();
      if(!modPackages){applyMods(request,ModBridge.modules(getContext()));return;}
      new Thread(new Runnable(){public void run(){final ModBridge.Result r=ModBridge.packages();post(new Runnable(){public void run(){applyMods(request,r);}});}},"LumaUI-mod-list").start();
    }
    void applyMods(int request,ModBridge.Result result){
      if(modReleased||request!=modRequest)return;modRows=result.rows;modBusy=false;
      modStatus=result.available?(result.partial?result.message:(modRows.isEmpty()?"Nothing to show yet":"")):result.message;
      modPage=Math.max(0,Math.min(modPage,Math.max(0,(modRows.size()-1)/4)));
      setContentDescription("Mods. "+(modPackages?"Installed mods, changes apply next launch. ":"Live modules. ")+modRows.size()+" items. "+modStatus);invalidate();
    }
    void changeMod(final ModBridge.Row row){
      if(modBusy)return;modBusy=true;modStatus="Updating…";invalidate();final int request=++modRequest;
      if(!row.packageMod){boolean ok=ModBridge.toggle(row);refreshMods();if(!ok){modStatus="Could not update module";announceForAccessibility(modStatus);}return;}
      new Thread(new Runnable(){public void run(){final boolean ok=ModBridge.toggle(row);final ModBridge.Result result=ModBridge.packages();post(new Runnable(){public void run(){if(modReleased||request!=modRequest)return;applyMods(request,result);if(!ok){modStatus="Could not save change";invalidate();}announceForAccessibility(ok?"Saved for next launch":modStatus);}});}},"LumaUI-mod-toggle").start();
    }
    void close(){shown=false;active=-1;invalidate();}
    void loadColor(int slot){colorSlot=slot;int color=palette[slot];target[0]=Color.red(color)/255f;target[1]=Color.green(color)/255f;target[2]=Color.blue(color)/255f;}
    void recordColor(){palette[colorSlot]=Color.rgb((int)(target[0]*255),(int)(target[1]*255),(int)(target[2]*255));}
    void save(){recordColor();prefs.edit().putInt("accentColor",palette[0]).putInt("panelColor",palette[1]).putInt("effectColor",palette[2]).putFloat("opacity",target[3]).putFloat("motion",target[4]).putInt("motionMode",motionMode).putBoolean("portalTransition",transitionStyle!=0).putInt("transitionStyle",transitionStyle).putInt("depthMode",depthMode).putBoolean("touchRipple",touchRipple).putBoolean("smoothBackground",smoothBackground).putFloat("glowStrength",target[5]).apply();if(bubble!=null)bubble.invalidate();}
    int accent(){return colorSlot==0?Color.rgb((int)(shownValue[0]*255),(int)(shownValue[1]*255),(int)(shownValue[2]*255)):palette[0];}
    int alpha(int color,int a){return (color&0xffffff)|(Math.max(0,Math.min(255,a))<<24);}
    void resetVisuals(){palette[0]=0xff9280ff;palette[1]=0xff12141f;palette[2]=0xff9280ff;loadColor(0);target[3]=.92f;target[4]=.5f;target[5]=.8f;motionMode=1;depthMode=2;transitionStyle=2;touchRipple=true;smoothBackground=true;resetArmedUntil=0;save();invalidate();}
    void round(Canvas c,float l,float t,float r,float b,float radius,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(new RectF(l,t,r,b),radius,radius,p);}
    void text(Canvas c,String s,float a,float b,float size,int col,boolean bold){p.setColor(col);p.setStyle(Paint.Style.FILL);p.setTypeface(android.graphics.Typeface.create(bold?"sans-serif-medium":"sans-serif",0));p.setTextAlign(Paint.Align.LEFT);p.setTextSize(size);c.drawText(s,a,b,p);}
    void depthShadow(Canvas c,int ac){
      if(depthMode==0)return;
      int layers=depthMode==2?5:2;
      for(int i=layers;i>0;i--){float offset=i*(depthMode==2?1.6f:1.1f)*unit;
        round(c,x+offset*.35f,y+offset,x+w-offset*.35f,y+h+offset,20*unit,alpha(0xff03040a,(int)((26+i*6)*open)));}
      p.setStyle(Paint.Style.FILL);p.setColor(alpha(0xff0a0a18,(int)(110*open)));
      p.setShadowLayer((depthMode==2?22:11)*unit,0,(depthMode==2?8:4)*unit,alpha(ac,(int)((depthMode==2?60:30)*shownValue[5]*open)));
      c.drawRoundRect(new RectF(x,y,x+w,y+h),20*unit,20*unit,p);p.clearShadowLayer();
    }
    void depthLight(Canvas c,int ac){
      if(depthMode==0)return;
      c.save();Path clip=new Path();clip.addRoundRect(new RectF(x,y,x+w,y+h),20*unit,20*unit,Path.Direction.CW);c.clipPath(clip);
      float strength=(depthMode==2?1f:.55f)*shownValue[5];
      float drift=motionMode==0?0:(float)Math.sin(phaseTime*.55f)*18*unit;
      p.setStyle(Paint.Style.FILL);
      p.setShader(new RadialGradient(x+w*lightX+drift,y+h*lightY,Math.max(1,w*.72f),
        new int[]{alpha(ac,(int)(48*strength*open)),alpha(ac,(int)(12*strength*open)),0x00000000},null,Shader.TileMode.CLAMP));
      c.drawRect(x,y,x+w,y+h,p);p.setShader(null);
      p.setShader(new LinearGradient(x,y,x,y+h*.30f,alpha(0xffffffff,(int)(18*strength*open)),0x00ffffff,Shader.TileMode.CLAMP));
      c.drawRoundRect(new RectF(x+unit,y+unit,x+w-unit,y+Math.min(h*.35f,90*unit)),19*unit,19*unit,p);p.setShader(null);
      c.restore();
    }
    protected void onDraw(Canvas c){
      long now=android.os.SystemClock.uptimeMillis();float dt=last==0?.016f:Math.min(.05f,(now-last)/1000f);last=now;
      phaseTime+=dt*(.35f+target[4]*1.3f);float speed=10f+target[4]*14f;float k=1f-(float)Math.exp(-dt*speed);
      open=spring.step(shown?1f:0f,dt,target[4]);tabMotion+=(tab-tabMotion)*k;
      if(tapAge<1f)tapAge=Math.min(1f,tapAge+dt*2.7f);
      boolean anim=Math.abs((shown?1:0)-open)>.002f||Math.abs(spring.velocity())>.01f||Math.abs(tab-tabMotion)>.002f||(touchRipple&&tapAge<1f);
      lightX+=(targetLightX-lightX)*k;lightY+=(targetLightY-lightY)*k;
      anim|=depthMode!=0&&(Math.abs(targetLightX-lightX)>.002f||Math.abs(targetLightY-lightY)>.002f);
      for(int i=0;i<6;i++){shownValue[i]+=(target[i]-shownValue[i])*k;anim|=Math.abs(target[i]-shownValue[i])>.002f;}
      if(!shown&&open<.01f){open=0;spring.reset(0);setVisibility(View.GONE);if(bubble!=null)bubble.setVisibility(View.VISIBLE);return;}
      float W=getWidth(),H=getHeight(),density=getResources().getDisplayMetrics().density;if(W<=0||H<=0)return;unit=TransitionMath.layoutUnit(density,W,H);
      w=Math.min(W-24*unit,760*unit);h=Math.min(H-24*unit,440*unit);x=(W-w)/2;y=(H-h)/2;side=Math.min(185*unit,w*.29f);body=x+side;int ac=accent();
      c.drawColor(Color.argb((int)(125*open),0,0,0));
      float originX=haveAnchor?anchorX:W/2,originY=haveAnchor?anchorY:H/2;
      if(transitionStyle!=0 && open<.995f){
        float flash=4f*open*(1f-open);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((1.2f+2f*flash)*unit);
        p.setColor(alpha(ac,(int)(145*flash)));c.drawCircle(originX,originY,(20+45*open)*unit,p);
        p.setStrokeWidth(unit);p.setColor(alpha(0xffffffff,(int)(60*flash)));c.drawCircle(originX,originY,(14+25*open)*unit,p);
        if(transitionStyle==2){float dx=W/2-originX,dy=H/2-originY,cx=originX+dx*open,cy=originY+dy*open;
          for(int i=-1;i<=1;i+=2){Path trail=new Path();trail.moveTo(originX,originY);trail.quadTo(originX+dx*.48f-i*dy*.10f,originY+dy*.48f+i*dx*.10f,cx,cy);
            p.setStrokeWidth((i==1?1.6f:.9f)*unit);p.setColor(alpha(ac,(int)(90*flash)));c.drawPath(trail,p);}}
        p.setStyle(Paint.Style.FILL);
      }
      Canvas visible=c;boolean warped=transitionStyle==2&&open<.995f&&prepareWarp(W,H);
      if(warped){c=new Canvas(warpBitmap);c.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);c.scale((float)warpBitmap.getWidth()/W,(float)warpBitmap.getHeight()/H);}
      c.save();
      if(transitionStyle!=0&&!warped){float initial=TransitionMath.startScale(anchorWidth,anchorHeight,w,h);
        float sx=TransitionMath.scale(initial,open),sy=sx;
        if(transitionStyle==2){boolean horizontal=Math.abs(originX-W/2)>=Math.abs(originY-H/2);sx=TransitionMath.elasticScale(initial,open,horizontal);sy=TransitionMath.elasticScale(initial,open,!horizontal);}
        c.translate(TransitionMath.offset(originX,W/2,open),TransitionMath.offset(originY,H/2,open));c.scale(sx,sy,W/2,H/2);}
      else if(transitionStyle==0)c.scale(.94f+.06f*open,.94f+.06f*open,W/2,H/2);
      depthShadow(c,ac);
      int bg=(int)((.68f+.31f*shownValue[3])*255*open);round(c,x,y,x+w,y+h,20*unit,Color.argb(bg,Color.red(palette[1]),Color.green(palette[1]),Color.blue(palette[1])));
      depthLight(c,ac);
      if(motionMode!=0){
        c.save(); Path clip=new Path(); clip.addRoundRect(new RectF(x,y,x+w,y+h),20*unit,20*unit,Path.Direction.CW); c.clipPath(clip);
        int effect=palette[2]; float seconds=phaseTime;
        if(motionMode==1){
          for(int i=0;i<13;i++){
            float phase=i*1.73f, radius=(5+(i%4)*4)*unit;
            float bx=x+side+(w-side)*(.07f+((i*37)%86)/100f)+(float)Math.sin(seconds*.6f+phase)*9*unit;
            float travel=(seconds*(8+i%4)*unit+i*47*unit)%(h+2*radius);
            float by=y+h+radius-travel;
            float stretch=1.05f+.35f*(.5f+.5f*(float)Math.sin(seconds*1.8f+phase));
            RectF oval=new RectF(bx-radius/stretch,by-radius*stretch,bx+radius/stretch,by+radius*stretch);
            p.setStyle(Paint.Style.FILL);p.setColor(alpha(effect,(int)((12+(i%3)*6)*open)));c.drawOval(oval,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit);p.setColor(alpha(effect,(int)(38*open)));c.drawOval(oval,p);
          }
        } else {
          for(int i=0;i<48;i++){
            float sx=body+((i*79)%997)/997f*(w-side), sy=y+((i*131)%991)/991f*h;
            float pulse=.45f+.55f*(float)(.5+.5*Math.sin(seconds*1.5+i*2.1));
            p.setColor(alpha(i%3==0?effect:0xffe7e8ff,(int)(70*pulse*open)));p.setStyle(Paint.Style.FILL);c.drawCircle(sx,sy,(i%5==0?1.6f:.8f)*unit,p);
          }
          for(int i=0;i<2;i++){float t=(seconds*.09f+i*.53f)%1f;float sx=body+(w-side)*(1f-t),sy=y+(.14f+.32f*i)*h+t*25*unit;
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((i==0?1.3f:.8f)*unit);p.setColor(alpha(effect,(int)(65*Math.sin(Math.PI*t)*open)));
            c.drawLine(sx,sy,sx-26*unit,sy+9*unit,p);p.setStyle(Paint.Style.FILL);}
          float cx=x+w-110*unit,cy=y+h-85*unit;
          c.save(); c.translate(cx,cy); c.rotate(-18);
          p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(0,0,68*unit,new int[]{alpha(effect,(int)(60*shownValue[5]*open)),alpha(effect,(int)(18*shownValue[5]*open)),0x00000000},null,Shader.TileMode.CLAMP));
          c.drawCircle(0,0,68*unit,p);p.setShader(null);
          for(int i=4;i>=0;i--){float r=(30+i*6)*unit;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((i==0?3:1.2f)*unit);p.setColor(alpha(effect,(int)((28-i*4)*open)));c.drawOval(new RectF(-r,-r*.34f,r,r*.34f),p);}
          p.setStyle(Paint.Style.FILL);p.setColor(alpha(0xff050610,(int)(220*open)));c.drawCircle(0,0,22*unit,p);
          p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*unit);p.setColor(alpha(effect,(int)(90*open)));c.drawCircle(0,0,22*unit,p);
          for(int i=0;i<9;i++){double theta=seconds*.8+i*6.2831853/9;float r=(38+(i%3)*5)*unit;p.setStyle(Paint.Style.FILL);p.setColor(alpha(effect,(int)(90*open)));c.drawCircle((float)Math.cos(theta)*r,(float)Math.sin(theta)*r*.34f,2*unit,p);}
          c.restore();
        }
        p.setStyle(Paint.Style.FILL);c.restore();
      }
      p.setColor(alpha(ac,(int)(65*open)));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit);c.drawRoundRect(new RectF(x,y,x+w,y+h),20*unit,20*unit,p);p.setStyle(Paint.Style.FILL);
      if(depthMode!=0){round(c,x+8*unit,y+8*unit,body-8*unit,y+h-8*unit,15*unit,alpha(0xff0b0c14,(int)((depthMode==2?62:32)*open)));
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit);p.setColor(alpha(0xffffffff,(int)((depthMode==2?28:14)*open)));c.drawRoundRect(new RectF(x+2*unit,y+2*unit,x+w-2*unit,y+h-2*unit),18*unit,18*unit,p);p.setStyle(Paint.Style.FILL);}
      round(c,x+16*unit,y+18*unit,x+48*unit,y+50*unit,10*unit,alpha(ac,(int)(210*open)));text(c,"L",x+26*unit,y+41*unit,20*unit,alpha(Color.WHITE,(int)(255*open)),true);
      text(c,"LumaUI",x+57*unit,y+34*unit,19*unit,alpha(Color.WHITE,(int)(255*open)),true);text(c,"YOUR SPACE",x+57*unit,y+48*unit,8*unit,alpha(0xffa7a8bd,(int)(220*open)),true);
      float menuY=y+83*unit; round(c,x+12*unit,menuY+tabMotion*44*unit,x+side-12*unit,menuY+37*unit+tabMotion*44*unit,10*unit,alpha(ac,(int)(45*open)));
      round(c,x+12*unit,menuY+tabMotion*44*unit+8*unit,x+15*unit,menuY+tabMotion*44*unit+29*unit,2*unit,alpha(ac,(int)(220*open)));
      String[] tabs={"Appearance","Motion","Effects","Mods","About"}; for(int i=0;i<5;i++){int co=i==tab?0xfff5f4ff:0xff979aaa;text(c,tabs[i],x+24*unit,menuY+(i*44+24)*unit,13*unit,alpha(co,(int)(255*open)),i==tab);}
      text(c,"GUI ONLY  •  v1.0.1",x+20*unit,y+h-20*unit,9*unit,alpha(0xff85899e,(int)(220*open)),false);
      p.setColor(alpha(0xff6f7080,(int)(65*open)));p.setStrokeWidth(unit);c.drawLine(body,y+18*unit,body,y+h-18*unit,p);
      text(c,tab==0?"Appearance":tab==1?"Motion":tab==2?"Effects":tab==3?"Mods":"About",body+24*unit,y+38*unit,21*unit,alpha(Color.WHITE,(int)(255*open)),true);
      round(c,x+w-48*unit,y+14*unit,x+w-15*unit,y+47*unit,10*unit,alpha(0xff777b94,(int)(38*open)));
      text(c,"×",x+w-36*unit,y+38*unit,25*unit,alpha(0xffe5e6f2,(int)(255*open)),false);
      float transition=1f-Math.min(.75f,Math.abs(tab-tabMotion)*.65f);
      c.saveLayerAlpha(body,y,x+w,y+h,(int)(255*transition),Canvas.ALL_SAVE_FLAG);
      c.translate((tabMotion-tab)*8*unit,0);
      if(tab==0){text(c,"MAKE IT YOURS",body+24*unit,y+61*unit,9*unit,alpha(ac,(int)(255*open)),true);
        float segLeft=body+24*unit,segW=(x+w-32*unit-segLeft)/3f;
        String[] groups={"Accent","Panel","Effects"};
        for(int i=0;i<3;i++){float sl=segLeft+i*segW;round(c,sl,y+75*unit,sl+segW-5*unit,y+102*unit,8*unit,alpha(i==colorSlot?ac:0xff505365,(int)((i==colorSlot?85:45)*open)));text(c,groups[i],sl+9*unit,y+93*unit,10*unit,alpha(Color.WHITE,(int)(230*open)),i==colorSlot);}
        text(c,"Choose a preset or mix your own.",body+24*unit,y+119*unit,10*unit,alpha(0xffa4a7bb,(int)(255*open)),false);
        float by=y+132*unit;int[] cs={0xff9280ff,0xff54d7d2,0xfffd87a5,0xffffbe69,0xff12141f};for(int i=0;i<5;i++)round(c,body+(24+i*34)*unit,by,body+(48+i*34)*unit,by+24*unit,8*unit,alpha(cs[i],(int)(255*open)));
        for(int i=0;i<4;i++) slider(c,i,y+(184+i*51)*unit,open);
      } else if(tab==1){text(c,"SMOOTH BY DESIGN",body+24*unit,y+62*unit,9*unit,alpha(ac,(int)(255*open)),true);text(c,"Animation speed",body+24*unit,y+98*unit,14*unit,alpha(Color.WHITE,(int)(255*open)),true);text(c,"Controls transitions and background motion.",body+24*unit,y+118*unit,10*unit,alpha(0xffa4a7bb,(int)(255*open)),false);slider(c,4,y+163*unit,open);
        text(c,"Background animation",body+24*unit,y+222*unit,12*unit,alpha(0xffe4e5f0,(int)(255*open)),true);
        float segLeft=body+24*unit,segW=(x+w-32*unit-segLeft)/3f;String[] modes={"Off","Stretch","Space"};
        for(int i=0;i<3;i++){float sl=segLeft+i*segW;round(c,sl,y+236*unit,sl+segW-5*unit,y+268*unit,9*unit,alpha(i==motionMode?ac:0xff505365,(int)((i==motionMode?100:50)*open)));text(c,modes[i],sl+9*unit,y+256*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),i==motionMode);}
        text(c,"Menu transition",body+24*unit,y+296*unit,11*unit,alpha(0xffe4e5f0,(int)(255*open)),true);
        float transitionLeft=body+24*unit,transitionWidth=(x+w-32*unit-transitionLeft)/3f;String[] styles={"Fade","Portal","Elastic"};
        for(int i=0;i<3;i++){float sl=transitionLeft+i*transitionWidth;round(c,sl,y+304*unit,sl+transitionWidth-5*unit,y+333*unit,9*unit,alpha(i==transitionStyle?ac:0xff505365,(int)((i==transitionStyle?100:50)*open)));text(c,styles[i],sl+9*unit,y+323*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),i==transitionStyle);}
        text(c,"Interface depth",body+24*unit,y+352*unit,11*unit,alpha(0xffe4e5f0,(int)(255*open)),true);
        float depthLeft=body+24*unit,depthWidth=(x+w-32*unit-depthLeft)/3f;String[] depths={"Flat","Soft","Deep"};
        for(int i=0;i<3;i++){float sl=depthLeft+i*depthWidth;round(c,sl,y+361*unit,sl+depthWidth-5*unit,y+390*unit,9*unit,alpha(i==depthMode?ac:0xff505365,(int)((i==depthMode?100:50)*open)));text(c,depths[i],sl+9*unit,y+380*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),i==depthMode);}
      } else if(tab==2){
        text(c,"INTERACTION LAB",body+24*unit,y+62*unit,9*unit,alpha(ac,(int)(255*open)),true);
        text(c,"Tune glow, touch, and motion performance.",body+24*unit,y+93*unit,11*unit,alpha(0xffa4a7bb,(int)(255*open)),false);
        slider(c,5,y+133*unit,open);
        text(c,"Touch ripples",body+24*unit,y+200*unit,12*unit,alpha(0xffe4e5f0,(int)(255*open)),true);
        float right=x+w-32*unit;round(c,right-56*unit,y+181*unit,right,y+208*unit,9*unit,alpha(touchRipple?ac:0xff505365,(int)((touchRipple?125:70)*open)));
        text(c,touchRipple?"ON":"OFF",right-43*unit,y+199*unit,10*unit,alpha(Color.WHITE,(int)(255*open)),true);
        text(c,"Background frame pacing",body+24*unit,y+241*unit,12*unit,alpha(0xffe4e5f0,(int)(255*open)),true);
        float left=body+24*unit,half=(right-left)/2f;String[] frames={"Eco","Smooth"};
        for(int i=0;i<2;i++){boolean selected=(i==1)==smoothBackground;float sl=left+i*half;round(c,sl,y+250*unit,sl+half-5*unit,y+280*unit,9*unit,alpha(selected?ac:0xff505365,(int)((selected?100:50)*open)));text(c,frames[i],sl+9*unit,y+270*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),selected);}
        text(c,"Eco reduces background redraws; transitions stay smooth.",body+24*unit,y+301*unit,10*unit,alpha(0xffa4a7bb,(int)(255*open)),false);
        boolean armed=android.os.SystemClock.uptimeMillis()<resetArmedUntil;
        round(c,left,y+320*unit,right,y+352*unit,9*unit,alpha(armed?0xffe27878:0xff505365,(int)((armed?100:50)*open)));
        text(c,armed?"Tap again to reset visual settings":"Reset visual settings",left+10*unit,y+341*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),armed);
      } else if(tab==3){drawMods(c,ac);
      } else {text(c,"A little more you.",body+24*unit,y+92*unit,16*unit,alpha(Color.WHITE,(int)(255*open)),true);text(c,"A smooth, customizable interface.",body+24*unit,y+117*unit,11*unit,alpha(0xffa4a7bb,(int)(255*open)),false);text(c,"No gameplay features are included.",body+24*unit,y+138*unit,11*unit,alpha(0xffa4a7bb,(int)(255*open)),false);}
      c.restore();
      if(touchRipple&&tapAge<1f){c.save();Path clip=new Path();clip.addRoundRect(new RectF(x,y,x+w,y+h),20*unit,20*unit,Path.Direction.CW);c.clipPath(clip);
        float life=1f-tapAge;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((1.1f+life)*unit);p.setColor(alpha(ac,(int)(120*life*open)));c.drawCircle(tapX,tapY,(5+28*tapAge)*unit,p);
        p.setStyle(Paint.Style.FILL);p.setColor(alpha(ac,(int)(25*life*open)));c.drawCircle(tapX,tapY,(4+20*tapAge)*unit,p);c.restore();}
      c.restore();
      if(warped){c=visible;drawWarp(c,W,H,originX,originY,open);float pull=4f*open*(1f-open);
        p.setShader(new RadialGradient(originX,originY,Math.max(1f,31*unit),new int[]{alpha(0xff03040a,(int)(205*pull)),alpha(ac,(int)(100*pull)),0x00000000},null,Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);c.drawCircle(originX,originY,31*unit,p);p.setShader(null);
        p.setColor(alpha(0xff04040a,(int)(230*pull)));c.drawCircle(originX,originY,10*unit,p);
      }
      if(anim)postInvalidateOnAnimation();else if(shown&&motionMode!=0){if(smoothBackground)postInvalidateOnAnimation();else postInvalidateDelayed(33);}else last=0;
    }
    void drawMods(Canvas c,int ac){
      float left=body+24*unit,right=x+w-32*unit,half=(right-left)/2f;
      text(c,"YOUR LAUNCHER, YOUR WAY",left,y+61*unit,9*unit,alpha(ac,(int)(255*open)),true);
      for(int i=0;i<2;i++){boolean selected=(i==1)==modPackages;float a=left+i*half;
        round(c,a,y+74*unit,a+half-5*unit,y+107*unit,9*unit,alpha(selected?ac:0xff505365,(int)((selected?110:50)*open)));
        text(c,i==0?"Modules · Live":"Installed · Next launch",a+9*unit,y+95*unit,10*unit,alpha(Color.WHITE,(int)(245*open)),selected);
      }
      text(c,modPackages?"Changes apply when Minecraft starts again.":"Changes apply to in-game modules now.",left,y+127*unit,10*unit,alpha(0xffb3b5c8,(int)(255*open)),false);
      if(!modStatus.isEmpty())text(c,modStatus,left,y+145*unit,10*unit,alpha(0xffffca85,(int)(255*open)),false);
      int start=modPage*4;for(int i=0;i<4&&start+i<modRows.size();i++){
        ModBridge.Row row=modRows.get(start+i);float top=y+(153+i*52)*unit;
        round(c,left,top,right,top+46*unit,10*unit,alpha(row.enabled?0xff35304d:0xff242632,(int)(185*open)));
        round(c,left+7*unit,top+9*unit,left+10*unit,top+37*unit,2*unit,alpha(row.enabled?ac:0xff777b8b,(int)(230*open)));
        c.save();c.clipRect(left+15*unit,top,right-82*unit,top+46*unit);
        text(c,row.name,left+17*unit,top+19*unit,12*unit,alpha(0xfff3f2fa,(int)(255*open)),true);
        text(c,row.packageMod?"INSTALLED · NEXT LAUNCH":row.group,left+17*unit,top+35*unit,9*unit,alpha(0xffafb0c3,(int)(255*open)),false);c.restore();
        round(c,right-68*unit,top+10*unit,right-8*unit,top+36*unit,8*unit,alpha(row.enabled?ac:0xff606473,(int)((row.enabled?160:95)*open)));
        text(c,row.enabled?"ON":"OFF",right-50*unit,top+28*unit,10*unit,alpha(Color.WHITE,(int)(255*open)),true);
      }
      int pages=Math.max(1,(modRows.size()+3)/4);text(c,"Page "+(modPage+1)+" / "+pages,left,y+389*unit,10*unit,alpha(0xffbec0d1,(int)(255*open)),false);
      round(c,right-135*unit,y+367*unit,right-73*unit,y+399*unit,8*unit,alpha(0xff616579,(int)(65*open)));
      round(c,right-68*unit,y+367*unit,right,y+399*unit,8*unit,alpha(0xff616579,(int)(65*open)));
      text(c,"‹ Prev",right-124*unit,y+388*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),false);text(c,"Next ›",right-55*unit,y+388*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),false);
    }
    void slider(Canvas c,int i,float yy,float fade){String[] names={"Red","Green","Blue","Panel opacity","Animation speed","Glow intensity"};float left=body+24*unit,right=x+w-32*unit; text(c,names[i],left,yy,11*unit,alpha(0xffe4e5f0,(int)(255*fade)),true);text(c,""+Math.round(target[i]*100)+"%",right-30*unit,yy,10*unit,alpha(0xffa4a7bb,(int)(255*fade)),false);float sy=yy+13*unit;round(c,left,sy,right,sy+4*unit,3*unit,alpha(0xff525567,(int)(150*fade)));round(c,left,sy,left+(right-left)*shownValue[i],sy+4*unit,3*unit,alpha(accent(),(int)(255*fade)));p.setColor(alpha(0xffffffff,(int)(255*fade)));if(depthMode!=0)p.setShadowLayer(7*unit,0,0,alpha(accent(),(int)(125*shownValue[5]*fade)));c.drawCircle(left+(right-left)*shownValue[i],sy+2*unit,6*unit,p);p.clearShadowLayer();}
    public boolean onTouchEvent(MotionEvent e){float px=e.getX(),py=e.getY();int act=e.getActionMasked();float left=body+24*unit,right=x+w-32*unit;
      if(open<.98f||!shown)return true;
      if(act==MotionEvent.ACTION_DOWN&&touchRipple&&px>=x&&px<=x+w&&py>=y&&py<=y+h){tapX=px;tapY=py;tapAge=0;invalidate();}
      if((act==MotionEvent.ACTION_DOWN||act==MotionEvent.ACTION_MOVE)&&px>=x&&px<=x+w&&py>=y&&py<=y+h){targetLightX=Math.max(.1f,Math.min(.9f,(px-x)/w));targetLightY=Math.max(.05f,Math.min(.8f,(py-y)/h));if(depthMode!=0)invalidate();}
      if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL){targetLightX=.82f;targetLightY=.12f;if(depthMode!=0)invalidate();}
      if(act==MotionEvent.ACTION_DOWN){if(px>x+w-55*unit&&py>y&&py<y+55*unit){close();return true;} if(px<x||px>x+w||py<y||py>y+h){close();return true;}if(px>x+12*unit&&px<body-10*unit&&py>y+83*unit&&py<y+303*unit){int newTab=Math.min(4,(int)((py-y-83*unit)/(44*unit)));if(newTab!=tab){tab=newTab;if(tab==3){modPage=0;refreshMods();}}invalidate();return true;}
        if(tab==3&&px>=left&&px<=right){
          if(py>=y+74*unit&&py<=y+107*unit){boolean selection=px>=left+(right-left)/2; if(selection!=modPackages){modPackages=selection;modPage=0;modRows=new ArrayList<>();refreshMods();}return true;}
          if(py>=y+153*unit&&py<y+361*unit&&!modBusy){int relative=(int)((py-y-153*unit)/(52*unit));float within=(py-y-153*unit)-relative*52*unit;int index=modPage*4+relative;if(within<=46*unit&&index<modRows.size())changeMod(modRows.get(index));return true;}
          if(py>=y+367*unit&&py<=y+399*unit){if(px>=right-135*unit&&px<right-73*unit)modPage=Math.max(0,modPage-1);else if(px>=right-68*unit)modPage=Math.min(Math.max(0,(modRows.size()-1)/4),modPage+1);invalidate();return true;}
        }
        if(tab==1&&px>=left&&px<=right&&py>=y+232*unit&&py<=y+274*unit){motionMode=Math.min(2,(int)((px-left)/((right-left)/3)));save();invalidate();return true;}
        if(tab==1&&px>=left&&px<=right&&py>=y+301*unit&&py<=y+337*unit){transitionStyle=Math.min(2,(int)((px-left)/((right-left)/3)));save();invalidate();return true;}
        if(tab==1&&px>=left&&px<=right&&py>=y+358*unit&&py<=y+394*unit){depthMode=Math.min(2,(int)((px-left)/((right-left)/3)));save();invalidate();return true;}
        if(tab==2&&px>=right-65*unit&&px<=right&&py>=y+178*unit&&py<=y+212*unit){touchRipple=!touchRipple;save();invalidate();return true;}
        if(tab==2&&px>=left&&px<=right&&py>=y+247*unit&&py<=y+284*unit){smoothBackground=px>=left+(right-left)/2;save();invalidate();return true;}
        if(tab==2&&px>=left&&px<=right&&py>=y+317*unit&&py<=y+356*unit){long now=android.os.SystemClock.uptimeMillis();if(now<resetArmedUntil)resetVisuals();else{resetArmedUntil=now+3000;postInvalidateDelayed(3050);invalidate();}return true;}
        if(tab==0&&px>=left&&px<=right&&py>=y+71*unit&&py<=y+106*unit){save();loadColor(Math.min(2,(int)((px-left)/((right-left)/3))));for(int i=0;i<3;i++)shownValue[i]=target[i];invalidate();return true;}
        if(tab==0&&py>y+127*unit&&py<y+162*unit){int n=(int)((px-body-24*unit)/(34*unit));int[] colors={0xff9280ff,0xff54d7d2,0xfffd87a5,0xffffbe69,0xff12141f};if(n>=0&&n<5&&px>=body+24*unit){int col=colors[n];target[0]=Color.red(col)/255f;target[1]=Color.green(col)/255f;target[2]=Color.blue(col)/255f;save();invalidate();return true;}}
        if(px>=left-12*unit&&px<=right+12*unit){if(tab==0){for(int i=0;i<4;i++)if(Math.abs(py-(y+(197+i*51)*unit))<18*unit){active=i;break;}} else if(tab==1&&Math.abs(py-(y+176*unit))<18*unit)active=4;else if(tab==2&&Math.abs(py-(y+146*unit))<18*unit)active=5;}
      }
      if((act==MotionEvent.ACTION_DOWN||act==MotionEvent.ACTION_MOVE)&&active>=0){target[active]=Math.max(0,Math.min(1,(px-left)/(right-left)));if(active<3)recordColor();invalidate();return true;}
      if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL){if(active>=0)save();active=-1;}return true;
    }
  }
}
