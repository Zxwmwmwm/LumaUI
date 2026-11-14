package luma.ui;
public class AdaptiveColorTest {
 static void check(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){int gray=AdaptiveColor.average(new int[]{0xff888888,0xff666666,0x00123456});check(gray==0xff777777);check(AdaptiveColor.average(new int[]{0})==0);check(AdaptiveColor.average(new int[]{0xffffffff})==0xffffffff);int cyan=AdaptiveColor.panel(0xff00e0d0);check(((cyan>>8)&255)>((cyan>>16)&255));int a=AdaptiveColor.accent(0xff00e0d0);check(((a>>8)&255)>((a>>16)&255));for(int c:new int[]{AdaptiveColor.panel(-1),a})check((c>>>24)==255);System.out.println("Adaptive panel color tests passed");}
}
