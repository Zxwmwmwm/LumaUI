package luma.ui;

import android.app.Activity;
import android.app.Dialog;
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
  static Dialog show(final Activity a,final ModBridge.Row row,int accent,int panel,final Runnable closed)throws Exception{
    final Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    LinearLayout shell=new LinearLayout(a);shell.setOrientation(LinearLayout.VERTICAL);shell.setPadding(dp(a,16),dp(a,12),dp(a,16),dp(a,14));
    int base=Color.rgb((Color.red(panel)+14)/2,(Color.green(panel)+16)/2,(Color.blue(panel)+26)/2);
    shell.setBackground(shape(0xf9000000|(base&0xffffff),(accent&0xffffff)|0x77000000,21,a));
    LinearLayout header=new LinearLayout(a);header.setGravity(Gravity.CENTER_VERTICAL);
    TextView mark=label(a,"⚙",Color.WHITE,21,true);mark.setGravity(Gravity.CENTER);mark.setBackground(shape((accent&0xffffff)|0x99000000,0,12,a));header.addView(mark,new LinearLayout.LayoutParams(dp(a,40),dp(a,40)));
    LinearLayout titles=new LinearLayout(a);titles.setOrientation(LinearLayout.VERTICAL);titles.setPadding(dp(a,12),0,0,0);
    TextView caption=label(a,"MODULE SETTINGS",accent,9,true),title=label(a,row.name,0xfff6f5ff,18,true);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);
    titles.addView(caption);titles.addView(title);header.addView(titles,new LinearLayout.LayoutParams(0,dp(a,48),1));
    TextView close=label(a,"×",0xffeeeefa,24,false);close.setGravity(Gravity.CENTER);close.setContentDescription("Close settings");close.setBackground(shape(0xff31323f,0,10,a));header.addView(close,new LinearLayout.LayoutParams(dp(a,40),dp(a,40)));close.setOnClickListener(v->dialog.dismiss());shell.addView(header);
    View line=new View(a);line.setBackgroundColor((accent&0xffffff)|0x66000000);LinearLayout.LayoutParams lineParams=new LinearLayout.LayoutParams(-1,dp(a,1));lineParams.setMargins(0,dp(a,10),0,dp(a,10));shell.addView(line,lineParams);
    final FrameLayout container=new FrameLayout(a);shell.addView(container,new LinearLayout.LayoutParams(-1,0,1));
    TextView footer=label(a,"Changes apply through the launcher",0xffa5a7bb,10,false);footer.setPadding(dp(a,3),dp(a,8),0,0);shell.addView(footer,new LinearLayout.LayoutParams(-1,dp(a,25)));
    ModBridge.renderSettings(a,container,row,()->ModBridge.applySettings(row.id));
    final java.util.WeakHashMap<View,Boolean> styled=new java.util.WeakHashMap<>();
    final android.view.ViewTreeObserver.OnGlobalLayoutListener styler=()->tintChildren(container,accent,a,styled);
    container.getViewTreeObserver().addOnGlobalLayoutListener(styler);
    dialog.setOnDismissListener(d->{if(container.getViewTreeObserver().isAlive())container.getViewTreeObserver().removeOnGlobalLayoutListener(styler);ModBridge.stopSettings(container);if(closed!=null)closed.run();});
    dialog.setContentView(shell);Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=.65f;window.setAttributes(attrs);}
    try{dialog.show();}catch(RuntimeException e){ModBridge.stopSettings(container);throw e;}
    if(window!=null){android.util.DisplayMetrics dm=a.getResources().getDisplayMetrics();window.setLayout(Math.max(1,Math.min(dm.widthPixels-dp(a,24),dp(a,700))),Math.max(1,Math.min(dm.heightPixels-dp(a,24),dp(a,550))));}
    shell.setAlpha(0);shell.setScaleX(.96f);shell.setScaleY(.96f);shell.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180).start();return dialog;
  }
  private static void tintChildren(View view,int accent,Activity a,java.util.WeakHashMap<View,Boolean> styled){
    // Only alter each control once; preserve launcher's listeners and dynamically added rows.
    boolean first=!styled.containsKey(view);if(first)styled.put(view,Boolean.TRUE);
    if(first&&view instanceof TextView){TextView t=(TextView)view;t.setTextColor(0xffefeff9);if(t instanceof EditText)t.setHintTextColor(0xff9a9cae);}
    if(first&&view instanceof Button){view.setBackgroundTintList(ColorStateList.valueOf((accent&0xffffff)|0xff000000));}
    if(first&&view instanceof CompoundButton){CompoundButton c=(CompoundButton)view;c.setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{accent,0xff8c8d9d}));}
    if(first&&view instanceof SeekBar){SeekBar s=(SeekBar)view;s.setProgressTintList(ColorStateList.valueOf(accent));s.setThumbTintList(ColorStateList.valueOf(accent));}
    if(first&&view instanceof LinearLayout&&!(view instanceof RadioGroup)&&view.getBackground()!=null){view.setBackground(shape(0xff252533,0xff414154,10,a));}
    if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++)tintChildren(g.getChildAt(i),accent,a,styled);}
  }
}
