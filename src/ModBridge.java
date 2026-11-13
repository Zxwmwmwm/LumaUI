package luma.ui;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/** Talks to LeviLauncher's own mod-menu APIs; no independent mod state. */
final class ModBridge {
  private static final String ROOT="org.levimc.launcher.core.mods.";
  static final class Row {
    final String id,name,group,source; final boolean enabled,packageMod,hasSettings; final Object handle;
    Row(String id,String name,String group,boolean enabled,boolean packageMod,boolean hasSettings,String source,Object handle){this.id=id;this.name=name;this.group=group;this.enabled=enabled;this.packageMod=packageMod;this.hasSettings=hasSettings;this.source=source;this.handle=handle;}
    Row withEnabled(boolean value){return new Row(id,name,group,value,packageMod,hasSettings,source,handle);}
  }
  static final class Result {
    final List<Row> rows=new ArrayList<>(); boolean available; boolean partial; String message="";
  }
  private static String text(Object value,String fallback){String s=value==null?"":value.toString().trim();return s.isEmpty()?fallback:s;}
  private static Object call(Object target,String method)throws Exception{return target.getClass().getMethod(method).invoke(target);}
  static Result modules(Object activity){
    Result out=new Result();String[] providers={"inbuilt.InbuiltModuleProvider","inbuilt.ExternalModuleProvider"};
    for(String name:providers)try{
      Class<?> provider=Class.forName(ROOT+name);Class<?> activityClass=Class.forName("android.app.Activity");
      Object result=provider.getMethod("load",activityClass).invoke(null,activity);if(!(result instanceof List))throw new IllegalStateException("Not a module list");
      for(Object mod:(List<?>)result){if(mod==null)continue;String id=text(call(mod,"getId"),"");if(id.isEmpty())continue;
        String title=text(call(mod,"getName"),id),group=text(call(mod,"getGroupName"),"Module");
        out.rows.add(new Row(id,title,group,Boolean.TRUE.equals(call(mod,"isEnabled")),false,Boolean.TRUE.equals(call(mod,"hasConfig")),text(call(mod,"getSource"),""),mod));}
      out.available=true;
    }catch(Throwable e){out.partial=true;}
    if(!out.available)out.message="Launcher module menu unavailable";else if(out.partial)out.message="Some modules unavailable";
    return out;
  }
  // Read the launcher's authoritative live state without rebuilding config schemas.
  static List<Row> syncModules(Object activity,List<Row> rows){
    try{
      Class<?> overlay=Class.forName(ROOT+"inbuilt.overlay.InbuiltOverlayManager");Object instance=overlay.getMethod("getInstance").invoke(null);
      Class<?> managers=Class.forName(ROOT+"inbuilt.manager.InbuiltModManager");Class<?> context=Class.forName("android.content.Context");
      Object manager=managers.getMethod("getInstance",context).invoke(null,activity);
      Method inbuilt=overlay.getMethod("isModActive",String.class),external=managers.getMethod("resolveExternalModuleEnabled",String.class,boolean.class);
      List<Row> updated=new ArrayList<>();for(Row row:rows){boolean enabled=row.enabled;
        if("INBUILT".equals(row.source)&&instance!=null)enabled=Boolean.TRUE.equals(inbuilt.invoke(instance,row.id));
        else if("EXTERNAL".equals(row.source))enabled=Boolean.TRUE.equals(external.invoke(manager,row.id,row.enabled));
        updated.add(row.withEnabled(enabled));
      }return updated;
    }catch(Throwable ignored){return null;}
  }
  static boolean openCustomSettings(Row row)throws Exception{return Boolean.TRUE.equals(call(row.handle,"openCustomConfig"));}
  static void renderSettings(Object context,Object container,Row row,Runnable changed)throws Exception{
    Class<?> type=Class.forName(ROOT+"inbuilt.overlay.ModConfigView"),contextClass=Class.forName("android.content.Context"),viewGroup=Class.forName("android.view.ViewGroup"),modClass=Class.forName(ROOT+"inbuilt.UnifiedMod");
    type.getMethod("render",contextClass,viewGroup,modClass,boolean.class,Runnable.class).invoke(null,context,container,row.handle,false,changed);
  }
  static void stopSettings(Object container){try{Class<?> type=Class.forName(ROOT+"inbuilt.overlay.RuntimeConfigView");Method m=type.getDeclaredMethod("stop",Class.forName("android.view.ViewGroup"));m.setAccessible(true);m.invoke(null,container);}catch(Throwable ignored){}}
  static void applySettings(String id){try{Class<?> type=Class.forName(ROOT+"inbuilt.overlay.InbuiltOverlayManager");Object manager=type.getMethod("getInstance").invoke(null);if(manager!=null)type.getMethod("applyConfigurationChanges",String.class).invoke(manager,id);}catch(Throwable ignored){}}
  private static Object manager()throws Exception{return Class.forName(ROOT+"ModManager").getMethod("getInstance").invoke(null);}
  static Result packages(){
    Result out=new Result();try{
      Object manager=manager();if(call(manager,"getCurrentVersion")==null){out.message="Launcher mod list unavailable";return out;}
      Object list=call(manager,"getMods");if(!(list instanceof List))throw new IllegalStateException("Not a mod list");
      for(Object mod:(List<?>)list){if(mod==null)continue;String id=text(call(mod,"getId"),"");if(id.isEmpty())continue;
        out.rows.add(new Row(id,text(call(mod,"getDisplayName"),id),"NEXT LAUNCH",Boolean.TRUE.equals(call(mod,"isEnabled")),true,false,"",manager));}
      out.available=true;
    }catch(Throwable e){out.message="Launcher mod list unavailable";}
    return out;
  }
  private static boolean saved(Object manager,String id,boolean expected)throws Exception {
    Field fileField=manager.getClass().getDeclaredField("configFile"),gsonField=manager.getClass().getDeclaredField("gson");
    fileField.setAccessible(true);gsonField.setAccessible(true);File file=(File)fileField.get(manager);Object gson=gsonField.get(manager);
    if(file==null||gson==null)return false;
    try(Reader reader=new FileReader(file)){
      Object entries=gson.getClass().getMethod("fromJson",Reader.class,Class.class).invoke(gson,reader,List.class);
      if(!(entries instanceof List))return false;
      for(Object entry:(List<?>)entries)if(entry instanceof Map){Map<?,?> map=(Map<?,?>)entry;
        if(id.equals(map.get("name")))return Boolean.valueOf(expected).equals(map.get("enabled"));}
    }
    return false;
  }
  static boolean toggle(Row row){
    if(row==null)return false;try{
      boolean next=!row.enabled;
      if(!row.packageMod){row.handle.getClass().getMethod("applyEnabled",boolean.class).invoke(row.handle,next);return Boolean.TRUE.equals(call(row.handle,"isEnabled"))==next;}
      // Installed native mods are launcher configuration, not live DLL unloading.
      synchronized(row.handle){
      Object current=call(row.handle,"getMods");if(!(current instanceof List))return false;boolean present=false;
      for(Object mod:(List<?>)current)if(row.id.equals(text(call(mod,"getId"),""))&&row.enabled==Boolean.TRUE.equals(call(mod,"isEnabled"))){present=true;break;}
      if(!present)return false;
      Method set=row.handle.getClass().getMethod("setModEnabled",String.class,boolean.class);set.invoke(row.handle,row.id,next);
      current=call(row.handle,"getMods");if(!(current instanceof List))return false;
      for(Object mod:(List<?>)current)if(row.id.equals(text(call(mod,"getId"),"")))return Boolean.TRUE.equals(call(mod,"isEnabled"))==next&&saved(row.handle,row.id,next);
      }
    }catch(Throwable ignored){}
    return false;
  }
}
