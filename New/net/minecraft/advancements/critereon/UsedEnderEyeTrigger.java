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
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class UsedEnderEyeTrigger implements ICriterionTrigger {
     private static final ResourceLocation field_192242_a = new ResourceLocation("used_ender_eye");
     private final Map field_192243_b = Maps.newHashMap();

     public ResourceLocation func_192163_a() {
          return field_192242_a;
     }

     public void func_192165_a(PlayerAdvancements p_192165_1_, ICriterionTrigger.Listener p_192165_2_) {
          UsedEnderEyeTrigger.Listeners usedendereyetrigger$listeners = (UsedEnderEyeTrigger.Listeners)this.field_192243_b.get(p_192165_1_);
          if (usedendereyetrigger$listeners == null) {
               usedendereyetrigger$listeners = new UsedEnderEyeTrigger.Listeners(p_192165_1_);
               this.field_192243_b.put(p_192165_1_, usedendereyetrigger$listeners);
          }

          usedendereyetrigger$listeners.func_192546_a(p_192165_2_);
     }

     public void func_192164_b(PlayerAdvancements p_192164_1_, ICriterionTrigger.Listener p_192164_2_) {
          UsedEnderEyeTrigger.Listeners usedendereyetrigger$listeners = (UsedEnderEyeTrigger.Listeners)this.field_192243_b.get(p_192164_1_);
          if (usedendereyetrigger$listeners != null) {
               usedendereyetrigger$listeners.func_192544_b(p_192164_2_);
               if (usedendereyetrigger$listeners.func_192545_a()) {
                    this.field_192243_b.remove(p_192164_1_);
               }
          }

     }

     public void func_192167_a(PlayerAdvancements p_192167_1_) {
          this.field_192243_b.remove(p_192167_1_);
     }

     public UsedEnderEyeTrigger.Instance func_192166_a(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
          MinMaxBounds minmaxbounds = MinMaxBounds.func_192515_a(p_192166_1_.get("distance"));
          return new UsedEnderEyeTrigger.Instance(minmaxbounds);
     }

     public void func_192239_a(EntityPlayerMP p_192239_1_, BlockPos p_192239_2_) {
          UsedEnderEyeTrigger.Listeners usedendereyetrigger$listeners = (UsedEnderEyeTrigger.Listeners)this.field_192243_b.get(p_192239_1_.func_192039_O());
          if (usedendereyetrigger$listeners != null) {
               double d0 = p_192239_1_.posX - (double)p_192239_2_.getX();
               double d1 = p_192239_1_.posZ - (double)p_192239_2_.getZ();
               usedendereyetrigger$listeners.func_192543_a(d0 * d0 + d1 * d1);
          }

     }

     static class Listeners {
          private final PlayerAdvancements field_192547_a;
          private final Set field_192548_b = Sets.newHashSet();

          public Listeners(PlayerAdvancements p_i47450_1_) {
               this.field_192547_a = p_i47450_1_;
          }

          public boolean func_192545_a() {
               return this.field_192548_b.isEmpty();
          }

          public void func_192546_a(ICriterionTrigger.Listener p_192546_1_) {
               this.field_192548_b.add(p_192546_1_);
          }

          public void func_192544_b(ICriterionTrigger.Listener p_192544_1_) {
               this.field_192548_b.remove(p_192544_1_);
          }

          public void func_192543_a(double p_192543_1_) {
               List list = null;
               Iterator var4 = this.field_192548_b.iterator();

               ICriterionTrigger.Listener listener1;
               while(var4.hasNext()) {
                    listener1 = (ICriterionTrigger.Listener)var4.next();
                    if (((UsedEnderEyeTrigger.Instance)listener1.func_192158_a()).func_192288_a(p_192543_1_)) {
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
                         listener1.func_192159_a(this.field_192547_a);
                    }
               }

          }
     }

     public static class Instance extends AbstractCriterionInstance {
          private final MinMaxBounds field_192289_a;

          public Instance(MinMaxBounds p_i47449_1_) {
               super(UsedEnderEyeTrigger.field_192242_a);
               this.field_192289_a = p_i47449_1_;
          }

          public boolean func_192288_a(double p_192288_1_) {
               return this.field_192289_a.func_192513_a(p_192288_1_);
          }
     }
}
