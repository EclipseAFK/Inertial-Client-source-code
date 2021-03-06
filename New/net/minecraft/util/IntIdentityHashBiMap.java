package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.util.math.MathHelper;

public class IntIdentityHashBiMap implements IObjectIntIterable {
     private static final Object EMPTY = null;
     private Object[] values;
     private int[] intKeys;
     private Object[] byId;
     private int nextFreeIndex;
     private int mapSize;

     public IntIdentityHashBiMap(int initialCapacity) {
          initialCapacity = (int)((float)initialCapacity / 0.8F);
          this.values = (Object[])(new Object[initialCapacity]);
          this.intKeys = new int[initialCapacity];
          this.byId = (Object[])(new Object[initialCapacity]);
     }

     public int getId(@Nullable Object p_186815_1_) {
          return this.getValue(this.getIndex(p_186815_1_, this.hashObject(p_186815_1_)));
     }

     @Nullable
     public Object get(int idIn) {
          return idIn >= 0 && idIn < this.byId.length ? this.byId[idIn] : null;
     }

     private int getValue(int p_186805_1_) {
          return p_186805_1_ == -1 ? -1 : this.intKeys[p_186805_1_];
     }

     public int add(Object objectIn) {
          int i = this.nextId();
          this.put(objectIn, i);
          return i;
     }

     private int nextId() {
          while(this.nextFreeIndex < this.byId.length && this.byId[this.nextFreeIndex] != null) {
               ++this.nextFreeIndex;
          }

          return this.nextFreeIndex;
     }

     private void grow(int capacity) {
          Object[] ak = this.values;
          int[] aint = this.intKeys;
          this.values = (Object[])(new Object[capacity]);
          this.intKeys = new int[capacity];
          this.byId = (Object[])(new Object[capacity]);
          this.nextFreeIndex = 0;
          this.mapSize = 0;

          for(int i = 0; i < ak.length; ++i) {
               if (ak[i] != null) {
                    this.put(ak[i], aint[i]);
               }
          }

     }

     public void put(Object objectIn, int intKey) {
          int i = Math.max(intKey, this.mapSize + 1);
          int j;
          if ((float)i >= (float)this.values.length * 0.8F) {
               for(j = this.values.length << 1; j < intKey; j <<= 1) {
               }

               this.grow(j);
          }

          j = this.findEmpty(this.hashObject(objectIn));
          this.values[j] = objectIn;
          this.intKeys[j] = intKey;
          this.byId[intKey] = objectIn;
          ++this.mapSize;
          if (intKey == this.nextFreeIndex) {
               ++this.nextFreeIndex;
          }

     }

     private int hashObject(@Nullable Object obectIn) {
          return (MathHelper.hash(System.identityHashCode(obectIn)) & Integer.MAX_VALUE) % this.values.length;
     }

     private int getIndex(@Nullable Object objectIn, int p_186816_2_) {
          int j;
          for(j = p_186816_2_; j < this.values.length; ++j) {
               if (this.values[j] == objectIn) {
                    return j;
               }

               if (this.values[j] == EMPTY) {
                    return -1;
               }
          }

          for(j = 0; j < p_186816_2_; ++j) {
               if (this.values[j] == objectIn) {
                    return j;
               }

               if (this.values[j] == EMPTY) {
                    return -1;
               }
          }

          return -1;
     }

     private int findEmpty(int p_186806_1_) {
          int j;
          for(j = p_186806_1_; j < this.values.length; ++j) {
               if (this.values[j] == EMPTY) {
                    return j;
               }
          }

          for(j = 0; j < p_186806_1_; ++j) {
               if (this.values[j] == EMPTY) {
                    return j;
               }
          }

          throw new RuntimeException("Overflowed :(");
     }

     public Iterator iterator() {
          return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
     }

     public void clear() {
          Arrays.fill(this.values, (Object)null);
          Arrays.fill(this.byId, (Object)null);
          this.nextFreeIndex = 0;
          this.mapSize = 0;
     }

     public int size() {
          return this.mapSize;
     }
}
