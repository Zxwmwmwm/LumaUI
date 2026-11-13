package luma.ui;

import android.app.Activity;
import android.app.Dialog;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/** Luma-styled shell around the launcher's own complete configuration renderer. */
final class SettingsSheet {
  private static int dp(Activity a,float value){return Math.round(value*a.getResources().getDisplayMetrics().density);}
  private static GradientDrawable shape(int color,int border,float radius,Activity a){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(a,radius));if(border!=0)d.setStroke(dp(a,1),border);return d;}
  private static TextView label(Activity a,String value,int color,float size,boolean bold){TextView t=new TextView(a);t.setText(value);t.setTextColor(color);t.setTextSize(size);t.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",0));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
  static Dialog show(final Activity a,final ModBridge.Row row,int accent,int panel,float speed,boolean ripple,final Runnable closed)throws Exception{
    final boolean animate=ValueAnimator.areAnimatorsEnabled();final long duration=SettingsMotion.duration(speed);
    final AnimatedDialog dialog=new AnimatedDialog(a,animate,duration);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    LinearLayout shell=new LinearLayout(a);shell.setOrientation(LinearLayout.VERTICAL);shell.setPadding(dp(a,16),dp(a,12),dp(a,16),dp(a,14));
    int base=Color.rgb((Color.red(panel)+14)/2,(Color.green(panel)+16)/2,(Color.blue(panel)+26)/2);
    shell.setBackground(shape(0xf9000000|(base&0xffffff),(accent&0xffffff)|0x77000000,21,a));
    LinearLayout header=new LinearLayout(a);header.setGravity(Gravity.CENTER_VERTICAL);
    TextView mark=label(a,"⚙",Color.WHITE,21,true);mark.setGravity(Gravity.CENTER);mark.setBackground(shape((accent&0xffffff)|0x99000000,0,12,a));header.addView(mark,new LinearLayout.LayoutParams(dp(a,40),dp(a,40)));
    LinearLayout titles=new LinearLayout(a);titles.setOrientation(LinearLayout.VERTICAL);titles.setPadding(dp(a,12),0,0,0);
    TextView caption=label(a,"MODULE SETTINGS",accent,9,true),title=label(a,row.name,0xfff6f5ff,18,true);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);
    titles.addView(caption);titles.addView(title);header.addView(titles,new LinearLayout.LayoutParams(0,dp(a,48),1));
    TextView close=label(a,"×",0xffeeeefa,24,false);close.setGravity(Gravity.CENTER);close.setContentDescription("Close settings");close.setBackground(shape(0xff31323f,0,10,a));header.addView(close,new LinearLayout.LayoutParams(dp(a,40),dp(a,40)));close.setOnClickListener(v->dialog.closeAnimated(false));shell.addView(header);
    View line=new View(a);line.setBackgroundColor((accent&0xffffff)|0x66000000);LinearLayout.LayoutParams lineParams=new LinearLayout.LayoutParams(-1,dp(a,1));lineParams.setMargins(0,dp(a,10),0,dp(a,10));shell.addView(line,lineParams);
    final FrameLayout container=new FrameLayout(a);shell.addView(container,new LinearLayout.LayoutParams(-1,0,1));
    TextView footer=label(a,"Changes apply through the launcher",0xffa5a7bb,10,false);footer.setPadding(dp(a,3),dp(a,8),0,0);shell.addView(footer,new LinearLayout.LayoutParams(-1,dp(a,25)));
    ModBridge.renderSettings(a,container,row,()->ModBridge.applySettings(row.id));
    final java.util.WeakHashMap<View,Boolean> styled=new java.util.WeakHashMap<>();
    final java.util.WeakHashMap<View,android.graphics.drawable.Drawable> categories=new java.util.WeakHashMap<>();
    final android.view.ViewTreeObserver.OnPreDrawListener styler=()->{tintChildren(container,accent,panel,a,styled,categories,animate&&!dialog.gestureActive,duration);return true;};
    container.getViewTreeObserver().addOnPreDrawListener(styler);
    dialog.setOnDismissListener(d->{shell.animate().cancel();container.animate().cancel();mark.animate().cancel();if(container.getViewTreeObserver().isAlive())container.getViewTreeObserver().removeOnPreDrawListener(styler);ModBridge.stopSettings(container);if(closed!=null)closed.run();});
    FrameLayout stage=new FrameLayout(a);stage.addView(shell,new FrameLayout.LayoutParams(-1,-1));
    PulseOverlay pulse=new PulseOverlay(a,accent,ripple&&animate,duration);stage.addView(pulse,new FrameLayout.LayoutParams(-1,-1));dialog.bind(shell,pulse,container);
    dialog.setContentView(stage);Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=.65f;window.setAttributes(attrs);}
    try{dialog.show();}catch(RuntimeException e){ModBridge.stopSettings(container);throw e;}
    if(window!=null){android.util.DisplayMetrics dm=a.getResources().getDisplayMetrics();window.setLayout(Math.max(1,Math.min(dm.widthPixels-dp(a,24),dp(a,700))),Math.max(1,Math.min(dm.heightPixels-dp(a,24),dp(a,550))));}
    if(animate){DecelerateInterpolator ease=new DecelerateInterpolator();shell.setAlpha(0);shell.setScaleX(.96f);shell.setScaleY(.96f);
      shell.animate().alpha(1f).scaleX(1f).scaleY(1f).setInterpolator(ease).setDuration(duration).start();
      container.setAlpha(0);container.setTranslationY(dp(a,9));container.animate().alpha(1f).translationY(0).setStartDelay(duration/5).setDuration(duration).setInterpolator(ease).start();
      mark.setRotation(-35);mark.animate().rotation(0).setDuration(duration+50).setInterpolator(ease).start();}
    return dialog;
  }
  private static int blend(int a,int b,float f){return Color.rgb(Math.round(Color.red(a)*(1-f)+Color.red(b)*f),Math.round(Color.green(a)*(1-f)+Color.green(b)*f),Math.round(Color.blue(a)*(1-f)+Color.blue(b)*f));}
  private static boolean recycler(View view){return view!=null&&view.getClass().getName().contains("RecyclerView");}
  private static boolean category(View view){return view instanceof TextView&&view.getParent() instanceof View&&recycler((View)view.getParent());}
  private static boolean selected(View view){return view.isSelected()||(view instanceof TextView&&((TextView)view).getTypeface()!=null&&((TextView)view).getTypeface().isBold());}
  private static void materialTint(View view,String method,ColorStateList colors){try{view.getClass().getMethod(method,ColorStateList.class).invoke(view,colors);}catch(Exception ignored){}}
  private static void tintChildren(View view,int accent,int panel,Activity a,java.util.WeakHashMap<View,Boolean> styled,java.util.WeakHashMap<View,android.graphics.drawable.Drawable> categories,boolean animate,long duration){
    // RecyclerView can rebind an existing category without recreating it. Reapply
    // the Luma palette after each native bind instead of styling a view only once.
    boolean first=!styled.containsKey(view);if(first)styled.put(view,Boolean.TRUE);
    if(category(view)){
      TextView t=(TextView)view;boolean active=selected(view);int color=active?accent:0xffaeb0c5;
      if(t.getCurrentTextColor()!=color)t.setTextColor(color);
      if(categories.get(view)!=view.getBackground()){
        view.setBackground(shape(active?blend(panel,accent,.20f):0x00000000,0,10,a));categories.put(view,view.getBackground());
      }
    }else if(view instanceof TextView){TextView t=(TextView)view;if(t.getCurrentTextColor()!=0xffefeff9)t.setTextColor(0xffefeff9);if(first&&t instanceof EditText)t.setHintTextColor(0xff9a9cae);}
    if(first&&view instanceof Button)view.setBackgroundTintList(ColorStateList.valueOf((accent&0xffffff)|0xff000000));
    if(first&&view instanceof CompoundButton){ColorStateList activeColors=new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{accent,0xff8c8d9d});CompoundButton button=(CompoundButton)view;button.setButtonTintList(activeColors);
      if(button instanceof android.widget.Switch){android.widget.Switch sw=(android.widget.Switch)button;sw.setThumbTintList(activeColors);sw.setTrackTintList(activeColors);}}
    if(first&&view instanceof SeekBar){SeekBar seek=(SeekBar)view;seek.setProgressTintList(ColorStateList.valueOf(accent));seek.setThumbTintList(ColorStateList.valueOf(accent));}
    if(first&&view.getClass().getName().startsWith("com.google.android.material.")){
      ColorStateList ac=ColorStateList.valueOf(accent);ColorStateList activeColors=new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{accent,0xff8c8d9d});materialTint(view,"setTrackActiveTintList",ac);materialTint(view,"setThumbTintList",ac);materialTint(view,"setHaloTintList",ac);materialTint(view,"setTickActiveTintList",ac);materialTint(view,"setChipStrokeColor",activeColors);
      materialTint(view,"setChipBackgroundColor",new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{blend(panel,accent,.28f),blend(panel,Color.WHITE,.07f)}));
    }
    if(first&&view instanceof LinearLayout&&!(view instanceof RadioGroup)&&view.getBackground()!=null){
      view.setBackground(shape(blend(panel,Color.WHITE,.07f),blend(panel,accent,.24f),10,a));
      if(animate&&view.getParent() instanceof FrameLayout){View parent=(View)view.getParent();
        if(parent.getParent() instanceof View&&recycler((View)parent.getParent())){
          float original=view.getAlpha();view.setAlpha(0f);view.setTranslationY(dp(a,7));view.animate().alpha(original).translationY(0).setDuration(duration).setInterpolator(new DecelerateInterpolator()).start();
        }}
    }
    if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++)tintChildren(g.getChildAt(i),accent,panel,a,styled,categories,animate,duration);}
  }
  private static View findCategory(View view,float rawX,float rawY){
    if(view.getVisibility()!=View.VISIBLE)return null;int[] location=new int[2];view.getLocationOnScreen(location);if(rawX<location[0]||rawX>=location[0]+view.getWidth()||rawY<location[1]||rawY>=location[1]+view.getHeight())return null;
    if(category(view))return view;
    if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=group.getChildCount()-1;i>=0;i--){View hit=findCategory(group.getChildAt(i),rawX,rawY);if(hit!=null)return hit;}}
    return null;
  }
  private static void animateCategoryContent(ViewGroup list,long duration){
    if(!(list.getParent() instanceof ViewGroup))return;ViewGroup root=(ViewGroup)list.getParent();
    for(int i=0;i<root.getChildCount();i++){View content=root.getChildAt(i);if(content!=list&&recycler(content)){
      content.animate().cancel();content.setAlpha(.25f);content.setTranslationY(content.getResources().getDisplayMetrics().density*12);
      content.animate().alpha(1f).translationY(0).setDuration(Math.max(300,duration+90)).setInterpolator(new DecelerateInterpolator()).start();return;}}
  }
  /** Observes touches without installing handlers on launcher-owned controls. */
  private static final class AnimatedDialog extends Dialog {
    private final boolean animate;private final long duration;private View shell,container,categoryDown;private ViewGroup categoryList;private PulseOverlay pulse;private boolean closing,categoryWasSelected;private float downX,downY;boolean gestureActive;
    AnimatedDialog(Activity a,boolean animate,long duration){super(a);this.animate=animate;this.duration=duration;}
    void bind(View shell,PulseOverlay pulse,View container){this.shell=shell;this.pulse=pulse;this.container=container;}
    @Override public boolean dispatchTouchEvent(MotionEvent event){int action=event.getActionMasked();
      if(action==MotionEvent.ACTION_DOWN){gestureActive=true;downX=event.getRawX();downY=event.getRawY();categoryDown=container==null?null:findCategory(container,downX,downY);categoryWasSelected=categoryDown!=null&&selected(categoryDown);categoryList=categoryDown!=null&&categoryDown.getParent() instanceof ViewGroup?(ViewGroup)categoryDown.getParent():null;if(pulse!=null)pulse.show(downX,downY);}
      boolean handled=super.dispatchTouchEvent(event);
      if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){gestureActive=false;float dx=event.getRawX()-downX,dy=event.getRawY()-downY;final View tapped=categoryDown;final ViewGroup list=categoryList;categoryDown=null;categoryList=null;
        float threshold=container==null?0:container.getResources().getDisplayMetrics().density*12;
        if(action==MotionEvent.ACTION_UP&&animate&&tapped!=null&&list!=null&&!categoryWasSelected&&dx*dx+dy*dy<threshold*threshold&&container!=null)
          container.post(()->{if(isShowing()&&!closing&&list.isAttachedToWindow())animateCategoryContent(list,duration);});}
      return handled;}
    @Override public void onBackPressed(){closeAnimated(true);}
    @Override public void cancel(){closeAnimated(true);}
    void closeAnimated(final boolean cancel){
      if(closing)return;closing=true;
      if(!animate||shell==null||!isShowing()||!shell.isAttachedToWindow()){finish(cancel);return;}
      shell.animate().cancel();shell.animate().alpha(0).scaleX(.97f).scaleY(.97f).setDuration(Math.max(100,duration*2/3)).withEndAction(()->finish(cancel)).start();
    }
    private void finish(boolean cancel){if(cancel)super.cancel();else super.dismiss();}
  }
  /** A short-lived overlay redraw; it is not clickable and cannot steal gestures. */
  private static final class PulseOverlay extends View {
    private final Paint paint=new Paint(3);private final int accent;private final boolean enabled;private final long duration;private final int[] location=new int[2];
    private long start=-1;private float x,y;
    PulseOverlay(Activity a,int accent,boolean enabled,long duration){super(a);this.accent=accent;this.enabled=enabled;this.duration=duration;setClickable(false);setFocusable(false);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void show(float rawX,float rawY){if(!enabled)return;getLocationOnScreen(location);x=rawX-location[0];y=rawY-location[1];start=SystemClock.uptimeMillis();invalidate();}
    @Override protected void onDraw(Canvas canvas){if(start<0)return;float t=SettingsMotion.progress(SystemClock.uptimeMillis(),start,duration);if(t>=1){start=-1;return;}
      float density=getResources().getDisplayMetrics().density;float life=1-t;paint.setStyle(Paint.Style.FILL);paint.setColor((accent&0xffffff)|((int)(24*life)<<24));canvas.drawCircle(x,y,(9+23*t)*density,paint);
      paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.3f*density);paint.setColor((accent&0xffffff)|((int)(135*life)<<24));canvas.drawCircle(x,y,(8+26*t)*density,paint);postInvalidateOnAnimation();}
  }
}
