package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class LowerStringMap implements Map {
      private final Map internalMap = Maps.newLinkedHashMap();

      public int size() {
            return this.internalMap.size();
      }

      public boolean isEmpty() {
            return this.internalMap.isEmpty();
      }

      public boolean containsKey(Object p_containsKey_1_) {
            return this.internalMap.containsKey(p_containsKey_1_.toString().toLowerCase(Locale.ROOT));
      }

      public boolean containsValue(Object p_containsValue_1_) {
            return this.internalMap.containsKey(p_containsValue_1_);
      }

      public Object get(Object p_get_1_) {
            return this.internalMap.get(p_get_1_.toString().toLowerCase(Locale.ROOT));
      }

      public Object put(String p_put_1_, Object p_put_2_) {
            return this.internalMap.put(p_put_1_.toLowerCase(Locale.ROOT), p_put_2_);
      }

      public Object remove(Object p_remove_1_) {
            return this.internalMap.remove(p_remove_1_.toString().toLowerCase(Locale.ROOT));
      }

      public void putAll(Map p_putAll_1_) {
            Iterator var2 = p_putAll_1_.entrySet().iterator();

            while(var2.hasNext()) {
                  Entry entry = (Entry)var2.next();
                  this.put((String)entry.getKey(), entry.getValue());
            }

      }

      public void clear() {
            this.internalMap.clear();
      }

      public Set keySet() {
            return this.internalMap.keySet();
      }

      public Collection values() {
            return this.internalMap.values();
      }

      public Set entrySet() {
            return this.internalMap.entrySet();
      }
}
