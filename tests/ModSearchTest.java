package luma.ui;
import java.util.*;
public class ModSearchTest {
  static void check(boolean c){if(!c)throw new AssertionError();}
  public static void main(String[] args){
    List<ModBridge.Row> rows=Arrays.asList(new ModBridge.Row("zoom","Zoom","Built in",true,false,true,"INBUILT","inbuilt:zoom",null),new ModBridge.Row("xray","Crystal","Tools",false,false,false,"EXTERNAL","external:xray",null));
    Set<String> favorites=new HashSet<>();favorites.add("external:xray");
    check(ModSearch.filter(rows,"",false,favorites).size()==2);check(ModSearch.filter(rows,"  ZOoM ",false,favorites).get(0).id.equals("zoom"));
    check(ModSearch.filter(rows,"tools",true,favorites).get(0).id.equals("xray"));check(ModSearch.filter(rows,"zoom",true,favorites).isEmpty());
    check(ModSearch.filter(rows,"xray",false,favorites).size()==1);check(ModSearch.filter(rows,"  ",true,favorites).size()==1);check(ModSearch.filter(rows,"none",false,favorites).isEmpty());
    ModBridge.Row changed=rows.get(0).withEnabled(false);check(changed.stableKey.equals("inbuilt:zoom")&&!changed.enabled);check(rows.size()==2);System.out.println("Search and favorite filtering tests passed");
  }
}
