package luma.ui;
public final class SettingsMotionTest {
  public static void main(String[] args){
    if(SettingsMotion.duration(0)!=290||SettingsMotion.duration(1)!=180||SettingsMotion.duration(-1)!=290||SettingsMotion.duration(5)!=180)throw new AssertionError("speed");
    if(SettingsMotion.progress(90,100,200)!=0||SettingsMotion.progress(200,100,200)!=.5f||SettingsMotion.progress(400,100,200)!=1||SettingsMotion.progress(0,0,0)!=1)throw new AssertionError("progress");
    System.out.println("Settings motion tests passed");
  }
}
