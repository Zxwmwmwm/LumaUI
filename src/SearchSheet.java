package luma.ui;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
final class SearchSheet {
  interface Listener{void changed(String query);void closed();}
  private static int dp(Activity a,float n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
  private static GradientDrawable shape(Activity a,int color,int stroke,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(a,radius));if(stroke!=0)d.setStroke(dp(a,1),stroke);return d;}
  static Dialog show(Activity a,String initial,int accent,Listener listener){
    Dialog dialog=new Dialog(a);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    LinearLayout root=new LinearLayout(a);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(a,18),dp(a,14),dp(a,18),dp(a,16));root.setBackground(shape(a,0xff14151f,(accent&0xffffff)|0x99000000,20));
    TextView title=new TextView(a);title.setText("Search mods");title.setTextColor(Color.WHITE);title.setTextSize(18);title.setTypeface(Typeface.create("sans-serif-medium",0));root.addView(title);
    TextView hint=new TextView(a);hint.setText("Search by name, group, or ID");hint.setTextColor(0xffabaec0);hint.setTextSize(11);root.addView(hint);
    EditText input=new EditText(a);input.setSingleLine(true);input.setTextColor(Color.WHITE);input.setHintTextColor(0xff999baa);input.setTextSize(15);input.setHint("Find a mod…");input.setPadding(dp(a,12),dp(a,4),dp(a,12),dp(a,4));input.setBackground(shape(a,0xff292b39,0xff4a4c60,10));input.setImeOptions(EditorInfo.IME_ACTION_DONE|EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    LinearLayout.LayoutParams field=new LinearLayout.LayoutParams(-1,dp(a,46));field.setMargins(0,dp(a,12),0,dp(a,12));root.addView(input,field);
    LinearLayout actions=new LinearLayout(a);actions.setGravity(Gravity.RIGHT);TextView clear=new TextView(a);clear.setText("Clear");clear.setTextColor(0xffeeeefa);clear.setGravity(Gravity.CENTER);clear.setBackground(shape(a,0xff393a4b,0,9));actions.addView(clear,new LinearLayout.LayoutParams(dp(a,85),dp(a,38)));
    TextView done=new TextView(a);done.setText("Done");done.setTextColor(Color.WHITE);done.setGravity(Gravity.CENTER);done.setBackground(shape(a,0xff000000|(accent&0xffffff),0,9));LinearLayout.LayoutParams doneP=new LinearLayout.LayoutParams(dp(a,85),dp(a,38));doneP.setMargins(dp(a,8),0,0,0);actions.addView(done,doneP);root.addView(actions);
    input.setText(initial);input.setSelection(input.length());input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){listener.changed(s.toString());}public void afterTextChanged(Editable e){}});
    clear.setOnClickListener(v->input.setText(""));done.setOnClickListener(v->dialog.dismiss());input.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_DONE){dialog.dismiss();return true;}return false;});
    dialog.setOnDismissListener(d->{InputMethodManager imm=(InputMethodManager)a.getSystemService(Context.INPUT_METHOD_SERVICE);if(imm!=null)imm.hideSoftInputFromWindow(input.getWindowToken(),0);listener.closed();});dialog.setContentView(root);Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);}dialog.show();
    if(window!=null)window.setLayout(Math.max(1,Math.min(a.getResources().getDisplayMetrics().widthPixels-dp(a,32),dp(a,500))),-2);input.requestFocus();input.post(()->{if(!dialog.isShowing()||!input.isAttachedToWindow())return;InputMethodManager imm=(InputMethodManager)a.getSystemService(Context.INPUT_METHOD_SERVICE);if(imm!=null)imm.showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);});return dialog;
  }
}
