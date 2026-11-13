package luma.ui;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.VelocityTracker;
import android.widget.FrameLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
    static final int MESH_X=40,MESH_Y=24;final float[] mesh=new float[(MESH_X+1)*(MESH_Y+1)*2]; Bitmap warpBitmap; boolean snapshotValid;
    List<ModBridge.Row> modRows=new ArrayList<>(),allModRows=new ArrayList<>(); Set<String> moduleFavorites=new HashSet<>(),packageFavorites=new HashSet<>(); boolean favoritesKnown=false,favoritesOnly=false;String modQuery=""; Dialog searchDialog; boolean modPackages=false,modBusy=false,modReleased=false,modSyncBusy=false,modAvailable=false; int modPage=0,modRequest=0; String modStatus="";
    final ModPages modPages=new ModPages(); final PlayField playField=new PlayField(); Dialog settingsDialog;
    int modLayout=0;float modOffset=0,modAnim=0,gestureX,gestureY,gestureOffset,flingVelocity,categoryMotion,layoutMotion;boolean modGesture,modDragging;VelocityTracker modVelocity;
    final Map<String,Float> switchValues=new HashMap<>();
    final Runnable syncRunnable=new Runnable(){public void run(){if(modReleased||!shown||tab!=3)return;
      if(!modBusy){if(modPackages)refreshPackagesQuietly();else syncModuleRows();}postDelayed(this,modPackages?4000:1200);}};
    float open=0, tabMotion=0; boolean shown=false, touchRipple=true, smoothBackground=true; int tab=0, active=-1, motionMode=1, colorSlot=0, depthMode=2, transitionStyle=2;
    final TransitionMath.Spring spring=new TransitionMath.Spring(); float tapAge=1f,tapX,tapY; long resetArmedUntil;
    final int[] palette=new int[3]; float phaseTime=0, lightX=.82f, lightY=.12f, targetLightX=.82f, targetLightY=.12f;
    float[] target=new float[6], shownValue=new float[6];
    float x,y,w,h,side,body,unit,anchorX,anchorY,anchorWidth,anchorHeight; boolean haveAnchor; long last;
    Panel(Context c){ super(c); setClickable(true); prefs=c.getSharedPreferences("lumaui_settings",0);
      palette[0]=prefs.getInt("accentColor",Color.rgb((int)(prefs.getFloat("red",.57f)*255),(int)(prefs.getFloat("green",.50f)*255),(int)(prefs.getFloat("blue",1f)*255)));
      palette[1]=prefs.getInt("panelColor",0xff12141f); palette[2]=prefs.getInt("effectColor",0xff9280ff);
      motionMode=Math.max(0,Math.min(5,prefs.getInt("motionMode",prefs.getBoolean("bubbles",true)?1:0)));
      transitionStyle=Math.max(0,Math.min(2,prefs.getInt("transitionStyle",prefs.getBoolean("portalTransition",true)?2:0)));
      depthMode=Math.max(0,Math.min(2,prefs.getInt("depthMode",2)));touchRipple=prefs.getBoolean("touchRipple",true);smoothBackground=prefs.getBoolean("smoothBackground",true);
      tab=Math.max(0,Math.min(4,prefs.getInt("lastTab",0)));tabMotion=tab;modPackages=prefs.getBoolean("modPackages",false);
      modPages.set(false,prefs.getInt("modulePage",0));modPages.set(true,prefs.getInt("packagePage",0));modPage=modPages.get(modPackages);
      packageFavorites=new HashSet<>(prefs.getStringSet("packageFavorites",new HashSet<String>()));
      modLayout=Math.max(0,Math.min(2,prefs.getInt("modLayout",0)));categoryMotion=modPackages?1:0;layoutMotion=modLayout;loadModOffset();
      loadColor(0);target[3]=prefs.getFloat("opacity",.92f);target[4]=prefs.getFloat("motion",.5f);target[5]=prefs.getFloat("glowStrength",.8f);System.arraycopy(target,0,shownValue,0,6);
    }
    void recycleWarp(){warpBitmap=null;snapshotValid=false;}
    void release(){modReleased=true;modRequest++;stopGesture();removeCallbacks(syncRunnable);if(settingsDialog!=null)settingsDialog.dismiss();if(searchDialog!=null)searchDialog.dismiss();recycleWarp();}
    boolean prepareWarp(float width,float height){
      float factor=Math.min(1f,1536f/Math.max(width,height));int bw=Math.max(1,Math.round(width*factor)),bh=Math.max(1,Math.round(height*factor));
      if(warpBitmap!=null&&(warpBitmap.getWidth()!=bw||warpBitmap.getHeight()!=bh))recycleWarp();
      if(warpBitmap==null)try{warpBitmap=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);}catch(OutOfMemoryError ignored){return false;}
      return true;
    }
    void drawWarp(Canvas target,float width,float height,float ax,float ay,float progress){
      TransitionMath.warpMesh(width,height,ax,ay,progress,MESH_X,MESH_Y,mesh);
      target.drawBitmapMesh(warpBitmap,MESH_X,MESH_Y,mesh,0,null,0,meshPaint);
    }
    void open(){recycleWarp();shown=true;if(bubble!=null){anchorWidth=bubble.getWidth();anchorHeight=bubble.getHeight();anchorX=bubble.getX()+anchorWidth/2f;anchorY=bubble.getY()+anchorHeight/2f;haveAnchor=anchorWidth>0&&anchorHeight>0;bubble.setVisibility(View.GONE);}setVisibility(View.VISIBLE);if(tab==3){ensureMods();startSync();}invalidate();}
    void ensureMods(){if(!modPackages&&!allModRows.isEmpty()&&!modBusy)syncModuleRows();else refreshMods();}
    String offsetKey(){return (modPackages?"package":"module")+(modLayout==1?"VerticalOffset":"HorizontalOffset");}
    void loadModOffset(){modOffset=modLayout==0?0:Math.max(0,prefs.getFloat(offsetKey(),0));}
    void saveModOffset(){if(!filtering()&&modLayout!=0)prefs.edit().putFloat(offsetKey(),modOffset).apply();}
    float viewportUnits(){return Math.max(0,(x+w-32*unit-(body+24*unit))/Math.max(.01f,unit));}
    void clampModOffset(){modOffset=ModLayout.clamp(modOffset,modLayout,modRows.size(),viewportUnits());}
    void startListAnimation(){modAnim=1f;invalidate();}
    void startSync(){removeCallbacks(syncRunnable);postDelayed(syncRunnable,1400);}
    void saveModPage(){if(filtering())return;modPages.set(modPackages,modPage);prefs.edit().putInt(modPackages?"packagePage":"modulePage",modPage).apply();}
    boolean filtering(){return favoritesOnly||!modQuery.trim().isEmpty();}
    Set<String> favoriteSet(){return modPackages?packageFavorites:moduleFavorites;}
    void rebuildFilter(){modRows=ModSearch.filter(allModRows,modQuery,favoritesOnly,favoriteSet());modPage=Math.max(0,Math.min(modPage,Math.max(0,(modRows.size()-1)/4)));if(modAvailable)clampModOffset();invalidate();}
    void changeFilter(String query,boolean favorites){
      boolean was=filtering();if(!was){saveModPage();saveModOffset();}modQuery=query;favoritesOnly=favorites;boolean now=filtering();
      if(now){modPage=0;modOffset=0;}else{modPage=modPages.get(modPackages);loadModOffset();}stopGesture();rebuildFilter();startListAnimation();
    }
    void showSearch(){if(searchDialog!=null)return;try{searchDialog=SearchSheet.show((Activity)getContext(),modQuery,accent(),new SearchSheet.Listener(){
      public void changed(String q){changeFilter(q,favoritesOnly);}public void closed(){searchDialog=null;invalidate();}});
    }catch(Throwable e){modStatus="Search unavailable";invalidate();}}
    void favorite(ModBridge.Row row){boolean current=favoriteSet().contains(row.stableKey),next=!current;
      if(row.packageMod){Set<String> copy=new HashSet<>(packageFavorites);if(next)copy.add(row.stableKey);else copy.remove(row.stableKey);packageFavorites=copy;prefs.edit().putStringSet("packageFavorites",new HashSet<>(copy)).apply();}
      else{if(!favoritesKnown||!ModBridge.setFavorite(getContext(),row,next)){modStatus="Launcher favorites unavailable";invalidate();return;}Set<String> fresh=ModBridge.favorites(getContext());if(fresh==null){modStatus="Launcher favorites unavailable";invalidate();return;}moduleFavorites=fresh;}
      rebuildFilter();startListAnimation();announceForAccessibility(next?"Added to favorites":"Removed from favorites");
    }
    boolean syncModuleRows(){List<ModBridge.Row> fresh=ModBridge.syncModules(getContext(),allModRows);if(fresh==null){modStatus="Live sync unavailable";invalidate();return false;}
      Set<String> fav=ModBridge.favorites(getContext());favoritesKnown=fav!=null;boolean changed=fav!=null&&!fav.equals(moduleFavorites);if(favoritesKnown&&"Launcher favorites unavailable".equals(modStatus)){modStatus="";changed=true;}if(fav!=null)moduleFavorites=fav;
      for(int i=0;i<fresh.size();i++)if(fresh.get(i).enabled!=allModRows.get(i).enabled){changed=true;break;}
      if("Live sync unavailable".equals(modStatus)){modStatus="";changed=true;}
      if(changed){allModRows=fresh;rebuildFilter();}return true;}
    void refreshPackagesQuietly(){if(modSyncBusy)return;modSyncBusy=true;final int request=modRequest;new Thread(new Runnable(){public void run(){final ModBridge.Result result=ModBridge.packages();post(new Runnable(){public void run(){
      modSyncBusy=false;if(modReleased||!shown||tab!=3||!modPackages||modBusy||request!=modRequest||!result.available)return;
      boolean changed=result.rows.size()!=allModRows.size();if(!changed)for(int i=0;i<allModRows.size();i++){ModBridge.Row a=allModRows.get(i),b=result.rows.get(i);if(!a.id.equals(b.id)||a.enabled!=b.enabled||!a.name.equals(b.name)){changed=true;break;}}
      if(changed)applyMods(request,result);
    }});}},"LumaUI-mod-sync").start();}
    void refreshMods(){
      final int request=++modRequest;modBusy=true;modStatus="Loading…";invalidate();
      if(!modPackages){applyMods(request,ModBridge.modules(getContext()));return;}
      new Thread(new Runnable(){public void run(){final ModBridge.Result r=ModBridge.packages();post(new Runnable(){public void run(){applyMods(request,r);}});}},"LumaUI-mod-list").start();
    }
    void applyMods(int request,ModBridge.Result result){
      if(modReleased||request!=modRequest)return;allModRows=result.rows;modBusy=false;modAvailable=result.available;
      if(!modPackages){Set<String> fav=ModBridge.favorites(getContext());favoritesKnown=fav!=null;if(fav!=null)moduleFavorites=fav;}
      modStatus=result.available?(result.partial?result.message:(allModRows.isEmpty()?"Nothing to show yet":"")):result.message;
      modRows=ModSearch.filter(allModRows,modQuery,favoritesOnly,favoriteSet());
      if(result.available){if(!filtering())modPage=modPages.visible(modPackages,modRows.size());else modPage=Math.min(modPage,Math.max(0,(modRows.size()-1)/4));if(!result.partial)saveModPage();}
      setContentDescription("Mods. "+(modPackages?"Installed mods, changes apply next launch. ":"Live modules. ")+modRows.size()+" items. "+modStatus);invalidate();
    }
    void changeMod(final ModBridge.Row row){
      if(modBusy)return;modBusy=true;modStatus="Updating…";invalidate();final int request=++modRequest;
      if(!row.packageMod){boolean ok=ModBridge.toggle(row);modBusy=false;modStatus=ok?"":"Could not update module";syncModuleRows();invalidate();if(!ok)announceForAccessibility(modStatus);return;}
      new Thread(new Runnable(){public void run(){final boolean ok=ModBridge.toggle(row);final ModBridge.Result result=ModBridge.packages();post(new Runnable(){public void run(){if(modReleased||request!=modRequest)return;applyMods(request,result);if(!ok){modStatus="Could not save change";invalidate();}announceForAccessibility(ok?"Saved for next launch":modStatus);}});}},"LumaUI-mod-toggle").start();
    }
    void openSettings(ModBridge.Row selected){
      if(!selected.hasSettings||selected.packageMod)return;
      // The launcher constructs config values on load; pick up edits made in its own menu.
      ModBridge.Result latest=ModBridge.modules(getContext());ModBridge.Row found=null;
      for(ModBridge.Row candidate:latest.rows)if(candidate.id.equals(selected.id)&&candidate.source.equals(selected.source)){found=candidate;break;}
      if(found==null){modStatus="Settings unavailable in this launcher";invalidate();return;}
      final ModBridge.Row row=found;
      try{
        if(ModBridge.openCustomSettings(row)){close();return;}
        final Activity activity=(Activity)getContext();
        settingsDialog=SettingsSheet.show(activity,row,accent(),palette[1],target[4],touchRipple,new Runnable(){public void run(){settingsDialog=null;if(!modReleased&&shown&&tab==3)syncModuleRows();}});
      }catch(Throwable e){modStatus="Settings unavailable in this launcher";announceForAccessibility(modStatus);invalidate();}
    }
    void stopGesture(){modGesture=false;flingVelocity=0;if(modVelocity!=null){modVelocity.recycle();modVelocity=null;}}
    void close(){recycleWarp();shown=false;playField.pressed=false;stopGesture();saveModOffset();active=-1;removeCallbacks(syncRunnable);invalidate();}
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
      // Layered translucent geometry gives depth without an expensive full-panel blur.
      p.setStyle(Paint.Style.FILL);p.setColor(alpha(0xff0a0a18,(int)(90*open)));
      c.drawRoundRect(new RectF(x,y,x+w,y+h),20*unit,20*unit,p);
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
      if(shown&&motionMode>=3)playField.step(dt*(.6f+target[4]),motionMode);
      open=spring.step(shown?1f:0f,dt,target[4]);tabMotion+=(tab-tabMotion)*k;modAnim=Math.max(0,modAnim-dt*3.8f);
      categoryMotion+=((modPackages?1f:0f)-categoryMotion)*k;layoutMotion+=(modLayout-layoutMotion)*k;
      if(Math.abs(flingVelocity)>1f&&!modGesture&&shown&&tab==3&&modLayout!=0){float before=modOffset;modOffset=ModLayout.clamp(modOffset+flingVelocity*dt,modLayout,modRows.size(),viewportUnits());
        flingVelocity*=(float)Math.exp(-5.5f*dt);if(Math.abs(flingVelocity)<8f||modOffset==before){flingVelocity=0;saveModOffset();}}
      boolean switchesAnimating=false;float mix=1f-(float)Math.exp(-dt*12f);
      for(ModBridge.Row row:modRows){String id=(row.packageMod?"P:":row.source+":")+row.id;Float v=switchValues.get(id);float goal=row.enabled?1f:0f;float value=v==null?goal:v+(goal-v)*mix;if(Math.abs(goal-value)<.005f)value=goal;else switchesAnimating=true;switchValues.put(id,value);}
      if(tapAge<1f)tapAge=Math.min(1f,tapAge+dt*2.7f);
      boolean anim=modAnim>0||switchesAnimating||Math.abs(flingVelocity)>1f||Math.abs(categoryMotion-(modPackages?1f:0f))>.002f||Math.abs(layoutMotion-modLayout)>.002f||Math.abs((shown?1:0)-open)>.002f||Math.abs(spring.velocity())>.01f||Math.abs(tab-tabMotion)>.002f||(touchRipple&&tapAge<1f);
      lightX+=(targetLightX-lightX)*k;lightY+=(targetLightY-lightY)*k;
      anim|=depthMode!=0&&(Math.abs(targetLightX-lightX)>.002f||Math.abs(targetLightY-lightY)>.002f);
      for(int i=0;i<6;i++){shownValue[i]+=(target[i]-shownValue[i])*k;anim|=Math.abs(target[i]-shownValue[i])>.002f;}
      if(!shown&&open<.01f){recycleWarp();open=0;spring.reset(0);setVisibility(View.GONE);if(bubble!=null)bubble.setVisibility(View.VISIBLE);return;}
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
      boolean warped=transitionStyle==2&&open<.995f&&prepareWarp(W,H);
      if(warped){
        if(!snapshotValid){Canvas snapshot=new Canvas(warpBitmap);snapshot.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);snapshot.scale((float)warpBitmap.getWidth()/W,(float)warpBitmap.getHeight()/H);
          float original=open;try{open=1f;drawPanel(snapshot,W,H,originX,originY,ac,false);}finally{open=original;}snapshotValid=true;warpBitmap.prepareToDraw();}
        meshPaint.setAlpha(Math.min(255,Math.max(0,(int)(255*Math.min(1f,open*5f)))));drawWarp(c,W,H,originX,originY,open);meshPaint.setAlpha(255);
        float pull=4f*open*(1f-open);
        p.setShader(new RadialGradient(originX,originY,Math.max(1f,31*unit),new int[]{alpha(0xff03040a,(int)(205*pull)),alpha(ac,(int)(100*pull)),0x00000000},null,Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);c.drawCircle(originX,originY,31*unit,p);p.setShader(null);
        p.setColor(alpha(0xff04040a,(int)(230*pull)));c.drawCircle(originX,originY,10*unit,p);
      }else{if(warpBitmap!=null)recycleWarp();drawPanel(c,W,H,originX,originY,ac,true);}
      if(anim)postInvalidateOnAnimation();else if(shown&&motionMode!=0){if(smoothBackground)postInvalidateOnAnimation();else postInvalidateDelayed(33);}else last=0;
    }
    // Transition snapshots are immutable until the animation ends. Reusing a
    // mutable bitmap while Android's render thread is reading it can flicker.
    void drawPanel(Canvas c,float W,float H,float originX,float originY,int ac,boolean transform){
      c.save();
      if(transform&&transitionStyle!=0){float initial=TransitionMath.startScale(anchorWidth,anchorHeight,w,h);
        float sx=TransitionMath.scale(initial,open),sy=sx;
        if(transitionStyle==2){boolean horizontal=Math.abs(originX-W/2)>=Math.abs(originY-H/2);sx=TransitionMath.elasticScale(initial,open,horizontal);sy=TransitionMath.elasticScale(initial,open,!horizontal);}
        c.translate(TransitionMath.offset(originX,W/2,open),TransitionMath.offset(originY,H/2,open));c.scale(sx,sy,W/2,H/2);}
      else if(transform&&transitionStyle==0)c.scale(.94f+.06f*open,.94f+.06f*open,W/2,H/2);
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
        } else if(motionMode==2) {
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
        }else drawPlayField(c,effect);
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
      if(transition<.995f)c.saveLayerAlpha(body,y,x+w,y+h,(int)(255*transition),Canvas.ALL_SAVE_FLAG);else c.save();
      c.translate((tabMotion-tab)*18*unit,0);
      if(tab==0){text(c,"MAKE IT YOURS",body+24*unit,y+61*unit,9*unit,alpha(ac,(int)(255*open)),true);
        float segLeft=body+24*unit,segW=(x+w-32*unit-segLeft)/3f;
        String[] groups={"Accent","Panel","Effects"};
        for(int i=0;i<3;i++){float sl=segLeft+i*segW;round(c,sl,y+75*unit,sl+segW-5*unit,y+102*unit,8*unit,alpha(i==colorSlot?ac:0xff505365,(int)((i==colorSlot?85:45)*open)));text(c,groups[i],sl+9*unit,y+93*unit,10*unit,alpha(Color.WHITE,(int)(230*open)),i==colorSlot);}
        text(c,"Choose a preset or mix your own.",body+24*unit,y+119*unit,10*unit,alpha(0xffa4a7bb,(int)(255*open)),false);
        float by=y+132*unit;int[] cs={0xff9280ff,0xff54d7d2,0xfffd87a5,0xffffbe69,0xff12141f};for(int i=0;i<5;i++)round(c,body+(24+i*34)*unit,by,body+(48+i*34)*unit,by+24*unit,8*unit,alpha(cs[i],(int)(255*open)));
        for(int i=0;i<4;i++) slider(c,i,y+(184+i*51)*unit,open);
      } else if(tab==1){text(c,"SMOOTH BY DESIGN",body+24*unit,y+62*unit,9*unit,alpha(ac,(int)(255*open)),true);text(c,"Animation speed",body+24*unit,y+98*unit,14*unit,alpha(Color.WHITE,(int)(255*open)),true);text(c,"Controls transitions and background motion.",body+24*unit,y+118*unit,10*unit,alpha(0xffa4a7bb,(int)(255*open)),false);slider(c,4,y+163*unit,open);
        text(c,"Background animation",body+24*unit,y+222*unit,12*unit,alpha(0xffe4e5f0,(int)(255*open)),true);
        float segLeft=body+24*unit,segW=(x+w-32*unit-segLeft)/3f;String[] modes={"Off","Stretch","Space","Pop","Trail","Magnet"};
        for(int i=0;i<6;i++){float sl=segLeft+(i%3)*segW,top=y+(232+(i/3)*27)*unit;round(c,sl,top,sl+segW-5*unit,top+24*unit,8*unit,alpha(i==motionMode?ac:0xff505365,(int)((i==motionMode?100:50)*open)));text(c,modes[i],sl+9*unit,top+16*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),i==motionMode);}
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
      } else {text(c,"A little more you.",body+24*unit,y+92*unit,16*unit,alpha(Color.WHITE,(int)(255*open)),true);text(c,"A smooth, customizable interface.",body+24*unit,y+117*unit,11*unit,alpha(0xffa4a7bb,(int)(255*open)),false);text(c,"No gameplay features are included.",body+24*unit,y+138*unit,11*unit,alpha(0xffa4a7bb,(int)(255*open)),false);if(motionMode>=3)text(c,motionMode==3?"Tap the bubbles to pop them.":motionMode==4?"Draw a fading trail with your finger.":"Touch and drag to attract the sparks.",body+24*unit,y+170*unit,10*unit,alpha(ac,(int)(240*open)),false);}
      c.restore();
      if(touchRipple&&tapAge<1f){c.save();Path clip=new Path();clip.addRoundRect(new RectF(x,y,x+w,y+h),20*unit,20*unit,Path.Direction.CW);c.clipPath(clip);
        float life=1f-tapAge;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((1.1f+life)*unit);p.setColor(alpha(ac,(int)(120*life*open)));c.drawCircle(tapX,tapY,(5+28*tapAge)*unit,p);
        p.setStyle(Paint.Style.FILL);p.setColor(alpha(ac,(int)(25*life*open)));c.drawCircle(tapX,tapY,(4+20*tapAge)*unit,p);c.restore();}
      c.restore();
    }
    void drawPlayField(Canvas c,int effect){
      float bw=x+w-body,bh=h;
      if(motionMode==PlayField.POP){for(int i=0;i<PlayField.COUNT;i++){
        float cx=body+playField.x[i]*bw,cy=y+playField.y[i]*bh,r=(7+i%4*2)*unit,burst=playField.burst[i];
        if(burst>0){float t=1-burst/.36f;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f*unit);p.setColor(alpha(effect,(int)(150*(1-t)*open)));c.drawCircle(cx,cy,r*(1+t*2.2f),p);}
        else{p.setStyle(Paint.Style.FILL);p.setColor(alpha(effect,(int)(28*open)));c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit);p.setColor(alpha(effect,(int)(120*open)));c.drawCircle(cx,cy,r,p);}
      }}else if(motionMode==PlayField.TRAIL){
        for(int i=0;i<PlayField.TRAIL_COUNT;i++){int j=(i+1)%PlayField.TRAIL_COUNT;float life=Math.max(0,1-playField.age[i]/1.5f);if(life<=0)continue;
          float cx=body+playField.tx[i]*bw,cy=y+playField.ty[i]*bh;p.setStyle(Paint.Style.FILL);p.setColor(alpha(effect,(int)(135*life*open)));c.drawCircle(cx,cy,(2+3*life)*unit,p);
          if(playField.age[j]<playField.age[i]&&playField.age[j]<1.5f){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((1+2*life)*unit);p.setColor(alpha(effect,(int)(100*life*open)));c.drawLine(cx,cy,body+playField.tx[j]*bw,y+playField.ty[j]*bh,p);}}
      }else if(motionMode==PlayField.MAGNET){for(int i=0;i<PlayField.COUNT;i++){
        float cx=body+playField.x[i]*bw,cy=y+playField.y[i]*bh;
        if(playField.pressed){float dx=(playField.x[i]-playField.pointerX),dy=playField.y[i]-playField.pointerY;if(dx*dx+dy*dy<.08f){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.7f*unit);p.setColor(alpha(effect,(int)(55*open)));c.drawLine(cx,cy,body+playField.pointerX*bw,y+playField.pointerY*bh,p);}}
        p.setStyle(Paint.Style.FILL);p.setColor(alpha(effect,(int)(140*open)));c.drawCircle(cx,cy,(2+(i%3))*unit,p);
      }}p.setStyle(Paint.Style.FILL);
    }
    float switchValue(ModBridge.Row row){Float v=switchValues.get((row.packageMod?"P:":row.source+":")+row.id);return v==null?(row.enabled?1:0):v;}
    int mixColor(int a,int b,float t){return Color.rgb((int)(Color.red(a)+(Color.red(b)-Color.red(a))*t),(int)(Color.green(a)+(Color.green(b)-Color.green(a))*t),(int)(Color.blue(a)+(Color.blue(b)-Color.blue(a))*t));}
    void drawToggle(Canvas c,float left,float top,float right,float bottom,ModBridge.Row row,int ac){
      float value=switchValue(row);round(c,left,top,right,bottom,9*unit,alpha(mixColor(0xff606473,ac,value),(int)(175*open)));
      p.setStyle(Paint.Style.FILL);p.setColor(alpha(0xfffafaff,(int)(255*open)));float r=(bottom-top)*.31f;c.drawCircle(left+r+4*unit+value*(right-left-2*r-8*unit),(top+bottom)/2,r,p);
    }
    void drawMods(Canvas c,int ac){
      float left=body+24*unit,right=x+w-32*unit,half=(right-left)/2f;
      text(c,"YOUR LAUNCHER, YOUR WAY",left,y+61*unit,9*unit,alpha(ac,(int)(255*open)),true);
      for(int i=0;i<2;i++){float a=left+i*half;round(c,a,y+70*unit,a+half-5*unit,y+96*unit,9*unit,alpha(0xff505365,(int)(50*open)));}
      float activeCategory=left+categoryMotion*half;round(c,activeCategory,y+70*unit,activeCategory+half-5*unit,y+96*unit,9*unit,alpha(ac,(int)(110*open)));
      for(int i=0;i<2;i++){boolean selected=(i==1)==modPackages;float a=left+i*half;text(c,i==0?"Modules · Live":"Installed · Next launch",a+9*unit,y+88*unit,10*unit,alpha(Color.WHITE,(int)(245*open)),selected);}
      round(c,left,y+102*unit,right-100*unit,y+129*unit,8*unit,alpha(0xff5a5d70,(int)(48*open)));
      c.save();c.clipRect(left+8*unit,y+102*unit,right-104*unit,y+129*unit);text(c,modQuery.trim().isEmpty()?"⌕  Search mods…":"⌕  "+modQuery,left+9*unit,y+120*unit,10*unit,alpha(0xffeeeffa,(int)(240*open)),false);c.restore();
      round(c,right-94*unit,y+102*unit,right,y+129*unit,8*unit,alpha(favoritesOnly?ac:0xff505365,(int)((favoritesOnly?115:45)*open)));
      text(c,favoritesOnly?"★ Favorites":"☆ Favorites",right-88*unit,y+120*unit,10*unit,alpha(Color.WHITE,(int)(245*open)),favoritesOnly);
      String[] layouts={"Pages","Vertical","Sideways"};float third=(right-left)/3f;
      for(int i=0;i<3;i++){float a=left+i*third;round(c,a,y+133*unit,a+third-5*unit,y+159*unit,8*unit,alpha(0xff505365,(int)(40*open)));}
      float chosen=left+layoutMotion*third;round(c,chosen,y+133*unit,chosen+third-5*unit,y+159*unit,8*unit,alpha(ac,(int)(105*open)));
      for(int i=0;i<3;i++){float a=left+i*third;text(c,layouts[i],a+8*unit,y+151*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),i==modLayout);}
      float top=y+166*unit,bottom=top+ModLayout.HEIGHT*unit;if(modAvailable&&!modBusy)clampModOffset();
      c.save();c.clipRect(left,top,right,bottom);c.translate((modLayout==ModLayout.HORIZONTAL?12:0)*unit*modAnim,(modLayout==ModLayout.HORIZONTAL?0:8)*unit*modAnim);
      int start=modLayout==0?modPage*4:(int)(modOffset/(modLayout==1?ModLayout.ROW:ModLayout.CARD));
      int end=modLayout==0?Math.min(modRows.size(),start+4):Math.min(modRows.size(),(int)Math.ceil((modOffset+(modLayout==1?ModLayout.HEIGHT:viewportUnits()))/(modLayout==1?ModLayout.ROW:ModLayout.CARD))+1);
      for(int i=start;i<end;i++){
        ModBridge.Row row=modRows.get(i);float l=left,r=right,t=top+(i-modPage*4)*ModLayout.ROW*unit;
        if(modLayout==ModLayout.VERTICAL)t=top+(i*ModLayout.ROW-modOffset)*unit;
        if(modLayout==ModLayout.HORIZONTAL){l=left+(i*ModLayout.CARD-modOffset)*unit;r=l+200*unit;t=top;}
        float b=t+(modLayout==ModLayout.HORIZONTAL?168:40)*unit;
        if((modLayout==ModLayout.HORIZONTAL&&(r<left||l>right))||(modLayout!=ModLayout.HORIZONTAL&&(b<top||t>bottom)))continue;
        float state=switchValue(row);round(c,l,t,r,b,10*unit,alpha(mixColor(0xff242632,0xff35304d,state),(int)(195*open)));
        round(c,l+6*unit,t+7*unit,l+9*unit,Math.min(b-7*unit,t+33*unit),2*unit,alpha(mixColor(0xff777b8b,ac,state),(int)(230*open)));
        c.save();c.clipRect(l+12*unit,t,modLayout==2?r-10*unit:r-(row.hasSettings?157:114)*unit,b);
        text(c,row.name,l+15*unit,t+17*unit,12*unit,alpha(0xfff3f2fa,(int)(255*open)),true);
        text(c,row.packageMod?"NEXT LAUNCH":row.group,l+15*unit,t+32*unit,9*unit,alpha(0xffafb0c3,(int)(255*open)),false);
        if(modLayout==2)text(c,row.enabled?"ENABLED":"DISABLED",l+15*unit,t+72*unit,10*unit,alpha(row.enabled?ac:0xffa1a2b1,(int)(255*open)),true);c.restore();
        float controls=modLayout==2?b-39*unit:t+7*unit;
        float starRight=r-(row.hasSettings?110:67)*unit;boolean starred=favoriteSet().contains(row.stableKey);
        round(c,starRight-35*unit,controls,starRight-2*unit,controls+26*unit,8*unit,alpha(starred?ac:0xff6e7187,(int)((starred?105:55)*open)));
        text(c,starred?"★":"☆",starRight-28*unit,controls+19*unit,16*unit,alpha(starred?0xffffd77d:0xffd4d5e2,(int)(255*open)),true);
        if(row.hasSettings){round(c,r-106*unit,controls,r-69*unit,controls+26*unit,8*unit,alpha(0xff6e7187,(int)(85*open)));text(c,"⚙",r-97*unit,controls+20*unit,17*unit,alpha(0xfff2f1ff,(int)(255*open)),true);}
        drawToggle(c,r-62*unit,controls,r-7*unit,controls+26*unit,row,ac);
      }c.restore();
      if(modRows.isEmpty()&&modAvailable&&filtering())text(c,favoritesOnly?"No favorites match":"No matches found",left+8*unit,top+35*unit,12*unit,alpha(0xffbec0d1,(int)(255*open)),false);
      if(!modStatus.isEmpty())text(c,modStatus,left,y+363*unit,10*unit,alpha(0xffffca85,(int)(255*open)),false);
      if(modLayout==0){int pages=Math.max(1,(modRows.size()+3)/4);text(c,"Page "+(modPage+1)+" / "+pages,left,y+393*unit,10*unit,alpha(0xffbec0d1,(int)(255*open)),false);
        round(c,right-135*unit,y+367*unit,right-73*unit,y+399*unit,8*unit,alpha(0xff616579,(int)(65*open)));round(c,right-68*unit,y+367*unit,right,y+399*unit,8*unit,alpha(0xff616579,(int)(65*open)));
        text(c,"‹ Prev",right-124*unit,y+388*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),false);text(c,"Next ›",right-55*unit,y+388*unit,10*unit,alpha(Color.WHITE,(int)(240*open)),false);
      }else text(c,modLayout==1?"Swipe up or down to browse":"Swipe left or right to browse",left,y+393*unit,10*unit,alpha(0xffbec0d1,(int)(255*open)),false);
    }
    void slider(Canvas c,int i,float yy,float fade){String[] names={"Red","Green","Blue","Panel opacity","Animation speed","Glow intensity"};float left=body+24*unit,right=x+w-32*unit; text(c,names[i],left,yy,11*unit,alpha(0xffe4e5f0,(int)(255*fade)),true);text(c,""+Math.round(target[i]*100)+"%",right-30*unit,yy,10*unit,alpha(0xffa4a7bb,(int)(255*fade)),false);float sy=yy+13*unit;round(c,left,sy,right,sy+4*unit,3*unit,alpha(0xff525567,(int)(150*fade)));round(c,left,sy,left+(right-left)*shownValue[i],sy+4*unit,3*unit,alpha(accent(),(int)(255*fade)));p.setColor(alpha(0xffffffff,(int)(255*fade)));if(depthMode!=0)p.setShadowLayer(7*unit,0,0,alpha(accent(),(int)(125*shownValue[5]*fade)));c.drawCircle(left+(right-left)*shownValue[i],sy+2*unit,6*unit,p);p.clearShadowLayer();}
    void activateModAt(float px,float py){
      float left=body+24*unit,right=x+w-32*unit,top=y+166*unit;
      if(px<left||px>right||py<top||py>top+ModLayout.HEIGHT*unit)return;
      float local=modLayout==ModLayout.HORIZONTAL?(px-left)/unit:(py-top)/unit;
      int index=ModLayout.index(modLayout,local,modOffset,modPage,modRows.size());if(index<0)return;
      float within=(local+(modLayout==0?0:modOffset))%(modLayout==2?ModLayout.CARD:ModLayout.ROW);
      if((modLayout==2&&within>200)||(modLayout!=2&&within>40))return;
      ModBridge.Row original=modRows.get(index);if(!modPackages&&!syncModuleRows()){refreshMods();return;}
      ModBridge.Row row=null;for(ModBridge.Row candidate:modRows)if(candidate.stableKey.equals(original.stableKey)){row=candidate;break;}if(row==null)return;
      float rowRight=modLayout==2?left+(index*ModLayout.CARD-modOffset+200)*unit:right;
      boolean controls=modLayout!=2||py>=top+120*unit;float starRight=rowRight-(row.hasSettings?110:67)*unit;
      boolean star=controls&&px>=starRight-40*unit&&px<starRight;
      boolean gear=controls&&row.hasSettings&&px>=rowRight-110*unit&&px<rowRight-67*unit;
      if(star)favorite(row);else if(gear)openSettings(row);else changeMod(row);
    }
    public boolean onTouchEvent(MotionEvent e){float px=e.getX(),py=e.getY();int act=e.getActionMasked();float left=body+24*unit,right=x+w-32*unit;
      if(open<.98f||!shown)return true;
      if(motionMode>=3){boolean inside=px>=body&&px<=x+w&&py>=y&&py<=y+h;
        if((act==MotionEvent.ACTION_DOWN&&inside)||(act==MotionEvent.ACTION_MOVE&&playField.pressed)){
          float bw=Math.max(1,x+w-body);playField.pointer((px-body)/bw,(py-y)/Math.max(1,h),true,motionMode,bw/Math.max(1,h));invalidate();
        }else if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL){playField.pressed=false;}}
      if(act==MotionEvent.ACTION_DOWN&&touchRipple&&px>=x&&px<=x+w&&py>=y&&py<=y+h){tapX=px;tapY=py;tapAge=0;invalidate();}
      if((act==MotionEvent.ACTION_DOWN||act==MotionEvent.ACTION_MOVE)&&px>=x&&px<=x+w&&py>=y&&py<=y+h){targetLightX=Math.max(.1f,Math.min(.9f,(px-x)/w));targetLightY=Math.max(.05f,Math.min(.8f,(py-y)/h));if(depthMode!=0)invalidate();}
      if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL){targetLightX=.82f;targetLightY=.12f;if(depthMode!=0)invalidate();}
      if(modGesture){
        if(modVelocity!=null)modVelocity.addMovement(e);
        float dx=(px-gestureX)/unit,dy=(py-gestureY)/unit;
        if(act==MotionEvent.ACTION_MOVE){if(Math.abs(dx)>8||Math.abs(dy)>8)modDragging=true;
          if(modDragging&&modLayout!=0){float delta=modLayout==ModLayout.HORIZONTAL?dx:dy;modOffset=ModLayout.clamp(gestureOffset-delta,modLayout,modRows.size(),viewportUnits());invalidate();}return true;}
        if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL){boolean dragged=modDragging||Math.abs(dx)>8||Math.abs(dy)>8;modGesture=false;
          if(act==MotionEvent.ACTION_UP&&dragged&&modLayout!=0&&modVelocity!=null){modVelocity.computeCurrentVelocity(1000);float speed=modLayout==2?modVelocity.getXVelocity():modVelocity.getYVelocity();flingVelocity=Math.max(-1800,Math.min(1800,-speed/unit));if(Math.abs(flingVelocity)<80)flingVelocity=0;}
          if(modVelocity!=null){modVelocity.recycle();modVelocity=null;}
          if(act==MotionEvent.ACTION_UP&&dragged&&modLayout==0&&Math.abs(dx)>35&&Math.abs(dx)>Math.abs(dy)){modPage=Math.max(0,Math.min(Math.max(0,(modRows.size()-1)/4),modPage+(dx<0?1:-1)));saveModPage();startListAnimation();}
          else if(act==MotionEvent.ACTION_UP&&!dragged&&!modBusy)activateModAt(px,py);
          if(dragged&&modLayout!=0){if(flingVelocity==0)saveModOffset();else invalidate();}return true;}
        return true;
      }
      if(act==MotionEvent.ACTION_DOWN){if(px>x+w-55*unit&&py>y&&py<y+55*unit){close();return true;} if(px<x||px>x+w||py<y||py>y+h){close();return true;}if(px>x+12*unit&&px<body-10*unit&&py>y+83*unit&&py<y+303*unit){int newTab=Math.min(4,(int)((py-y-83*unit)/(44*unit)));if(newTab!=tab){saveModOffset();stopGesture();tab=newTab;prefs.edit().putInt("lastTab",tab).apply();removeCallbacks(syncRunnable);if(tab==3){ensureMods();startSync();startListAnimation();}}invalidate();return true;}
        if(tab==3&&px>=left&&px<=right){
          if(py>=y+70*unit&&py<=y+96*unit){boolean selection=px>=left+(right-left)/2; if(selection!=modPackages){saveModPage();saveModOffset();stopGesture();modPackages=selection;prefs.edit().putBoolean("modPackages",selection).apply();modPage=filtering()?0:modPages.get(selection);if(filtering())modOffset=0;else loadModOffset();modRows=new ArrayList<>();allModRows=new ArrayList<>();modAvailable=false;refreshMods();startListAnimation();}return true;}
          if(py>=y+102*unit&&py<=y+129*unit){if(px>=right-94*unit){if(!modPackages&&!favoritesKnown){modStatus="Launcher favorites unavailable";invalidate();}else changeFilter(modQuery,!favoritesOnly);}else showSearch();return true;}
          if(py>=y+133*unit&&py<=y+159*unit){int selected=Math.max(0,Math.min(2,(int)((px-left)/((right-left)/3))));if(selected!=modLayout){saveModOffset();stopGesture();modLayout=selected;prefs.edit().putInt("modLayout",selected).apply();if(filtering())modOffset=0;else loadModOffset();if(modAvailable)clampModOffset();startListAnimation();}return true;}
          if(py>=y+166*unit&&py<=y+342*unit&&!modBusy){stopGesture();modGesture=true;modDragging=false;gestureX=px;gestureY=py;gestureOffset=modOffset;modVelocity=VelocityTracker.obtain();modVelocity.addMovement(e);return true;}
          if(modLayout==0&&py>=y+367*unit&&py<=y+399*unit){if(px>=right-135*unit&&px<right-73*unit)modPage=Math.max(0,modPage-1);else if(px>=right-68*unit)modPage=Math.min(Math.max(0,(modRows.size()-1)/4),modPage+1);saveModPage();startListAnimation();return true;}
        }
        if(tab==1&&px>=left&&px<=right&&py>=y+232*unit&&py<=y+283*unit){int row=(int)((py-y-232*unit)/(27*unit));float within=(py-y-232*unit)-row*27*unit;if(row<2&&within<=24*unit){int selected=Math.min(2,(int)((px-left)/((right-left)/3)))+row*3;if(selected!=motionMode){motionMode=selected;playField.reset();}save();invalidate();}return true;}
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
