package luma.ui;
public final class ModLayoutTest {
  public static void main(String[] args){
    if(ModLayout.max(ModLayout.VERTICAL,10,400)!=264f)throw new AssertionError("vertical extent");
    if(ModLayout.max(ModLayout.HORIZONTAL,4,400)!=440f)throw new AssertionError("horizontal extent");
    if(ModLayout.clamp(900,ModLayout.VERTICAL,10,400)!=264f||ModLayout.clamp(-1,1,10,400)!=0)throw new AssertionError("clamp");
    if(ModLayout.index(ModLayout.PAGES,3,0,2,20)!=8||ModLayout.index(ModLayout.VERTICAL,45,88,0,20)!=3||ModLayout.index(ModLayout.HORIZONTAL,20,210,0,20)!=1)throw new AssertionError("hit test");
    if(ModLayout.index(ModLayout.HORIZONTAL,20,420,0,2)!=-1||ModLayout.max(2,0,400)!=0)throw new AssertionError("empty");
    System.out.println("Mod list geometry tests passed");
  }
}
