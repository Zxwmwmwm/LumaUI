package luma.ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
/** Filtering only: source rows remain intact so launcher sync never loses hidden entries. */
final class ModSearch {
  static List<ModBridge.Row> filter(List<ModBridge.Row> rows,String query,boolean favoritesOnly,Set<String> favorites){
    String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);List<ModBridge.Row> result=new ArrayList<>();
    for(ModBridge.Row row:rows){if(favoritesOnly&&!favorites.contains(row.stableKey))continue;
      if(q.isEmpty()||(row.name+" "+row.group+" "+row.id).toLowerCase(Locale.ROOT).contains(q))result.add(row);}
    return result;
  }
}
