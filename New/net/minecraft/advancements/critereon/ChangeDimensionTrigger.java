package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;

public class ChangeDimensionTrigger implements ICriterionTrigger {
     private static final ResourceLocation field_193144_a = new ResourceLocation("changed_dimension");
     private final Map field_193145_b = Maps.newHashMap();

     public ResourceLocation func_192163_a() {
          return field_193144_a;
     }

     public void func_192165_a(PlayerAdvancements p_192165_1_, ICriterionTrigger.Listener p_192165_2_) {
          ChangeDimensionTrigger.Listeners changedimensiontrigger$listeners = (ChangeDimensionTrigger.Listeners)this.field_193145_b.get(p_192165_1_);
          if (changedimensiontrigger$listeners == null) {
               changedimensiontrigger$listeners = new ChangeDimensionTrigger.Listeners(p_192165_1_);
               this.field_193145_b.put(p_192165_1_, changedimensiontrigger$listeners);
          }

          changedimensiontrigger$listeners.func_193233_a(p_192165_2_);
     }

     public void func_192164_b(PlayerAdvancements p_192164_1_, ICriterionTrigger.Listener p_192164_2_) {
          ChangeDimensionTrigger.Listeners changedimensiontrigger$listeners = (ChangeDimensionTrigger.Listeners)this.field_193145_b.get(p_192164_1_);
          if (changedimensiontrigger$listeners != null) {
               changedimensiontrigger$listeners.func_193231_b(p_192164_2_);
               if (changedimensiontrigger$listeners.func_193232_a()) {
                    this.field_193145_b.remove(p_192164_1_);
               }
          }

     }

     public void func_192167_a(PlayerAdvancements p_192167_1_) {
          this.field_193145_b.remove(p_192167_1_);
     }

     public ChangeDimensionTrigger.Instance func_192166_a(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
          DimensionType dimensiontype = p_192166_1_.has("from") ? DimensionType.func_193417_a(JsonUtils.getString(p_192166_1_, "from")) : null;
          DimensionType dimensiontype1 = p_192166_1_.has("to") ? DimensionType.func_193417_a(JsonUtils.getString(p_192166_1_, "to")) : null;
          return new ChangeDimensionTrigger.Instance(dimensiontype, dimensiontype1);
     }

     public void func_193143_a(EntityPlayerMP p_193143_1_, DimensionType p_193143_2_, DimensionType p_193143_3_) {
          ChangeDimensionTrigger.Listeners changedimensiontrigger$listeners = (ChangeDimensionTrigger.Listeners)this.field_193145_b.get(p_193143_1_.func_192039_O());
          if (changedimensiontrigger$listeners != null) {
               changedimensiontrigger$listeners.func_193234_a(p_193143_2_, p_193143_3_);
          }

     }

     static class Listeners {
          private final PlayerAdvancements field_193235_a;
          private final Set field_193236_b = Sets.newHashSet();

          public Listeners(PlayerAdvancements p_i47476_1_) {
               this.field_193235_a = p_i47476_1_;
          }

          public boolean func_193232_a() {
               return this.field_193236_b.isEmpty();
          }

          public void func_193233_a(ICriterionTrigger.Listener p_193233_1_) {
               this.field_193236_b.add(p_193233_1_);
          }

          public void func_193231_b(ICriterionTrigger.Listener p_193231_1_) {
               this.field_193236_b.remove(p_193231_1_);
          }

          public void func_193234_a(DimensionType p_193234_1_, DimensionType p_193234_2_) {
               List list = null;
               Iterator var4 = this.field_193236_b.iterator();

               ICriterionTrigger.Listener listener1;
               while(var4.hasNext()) {
                    listener1 = (ICriterionTrigger.Listener)var4.next();
                    if (((ChangeDimensionTrigger.Instance)listener1.func_192158_a()).func_193190_a(p_193234_1_, p_193234_2_)) {
                         if (list == null) {
                              list = Lists.newArrayList();
                         }

                         list.add(listener1);
                    }
               }

               if (list != null) {
                    var4 = list.iterator();

                    while(var4.hasNext()) {
                         listener1 = (ICriterionTrigger.Listener)var4.next();
                         listener1.func_192159_a(this.field_193235_a);
                    }
               }

          }
     }

     public static class Instance extends AbstractCriterionInstance {
          @Nullable
          private final DimensionType field_193191_a;
          @Nullable
          private final DimensionType field_193192_b;

          public Instance(@Nullable DimensionType p_i47475_1_, @Nullable DimensionType p_i47475_2_) {
               super(ChangeDimensionTrigger.field_193144_a);
               this.field_193191_a = p_i47475_1_;
               this.field_193192_b = p_i47475_2_;
          }

          public boolean func_193190_a(DimensionType p_193190_1_, DimensionType p_193190_2_) {
               if (this.field_193191_a != null && this.field_193191_a != p_193190_1_) {
                    return false;
               } else {
                    return this.field_193192_b == null || this.field_193192_b == p_193190_2_;
               }
          }
     }
}
