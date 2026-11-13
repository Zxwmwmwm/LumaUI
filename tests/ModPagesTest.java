package luma.ui;
public final class ModPagesTest {
  public static void main(String[] args){
    ModPages pages=new ModPages();pages.set(false,3);pages.set(true,1);
    if(pages.visible(false,20)!=3||pages.visible(true,8)!=1)throw new AssertionError("separate pages");
    if(pages.visible(false,4)!=0||pages.get(false)!=3)throw new AssertionError("partial list must not erase saved page");
    ModPages reopened=new ModPages();reopened.set(false,pages.get(false));reopened.set(true,pages.get(true));
    if(reopened.visible(false,20)!=3||reopened.visible(true,8)!=1)throw new AssertionError("restore pages");
    reopened.set(false,-1);if(reopened.get(false)!=0)throw new AssertionError("negative page");
    System.out.println("Mod page memory tests passed");
  }
}
