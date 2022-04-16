package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionType;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class BrewedPotionTrigger implements ICriterionTrigger {
     private static final ResourceLocation field_192176_a = new ResourceLocation("brewed_potion");
     private final Map field_192177_b = Maps.newHashMap();

     public ResourceLocation func_192163_a() {
          return field_192176_a;
     }

     public void func_192165_a(PlayerAdvancements p_192165_1_, ICriterionTrigger.Listener p_192165_2_) {
          BrewedPotionTrigger.Listeners brewedpotiontrigger$listeners = (BrewedPotionTrigger.Listeners)this.field_192177_b.get(p_192165_1_);
          if (brewedpotiontrigger$listeners == null) {
               brewedpotiontrigger$listeners = new BrewedPotionTrigger.Listeners(p_192165_1_);
               this.field_192177_b.put(p_192165_1_, brewedpotiontrigger$listeners);
          }

          brewedpotiontrigger$listeners.func_192349_a(p_192165_2_);
     }

     public void func_192164_b(PlayerAdvancements p_192164_1_, ICriterionTrigger.Listener p_192164_2_) {
          BrewedPotionTrigger.Listeners brewedpotiontrigger$listeners = (BrewedPotionTrigger.Listeners)this.field_192177_b.get(p_192164_1_);
          if (brewedpotiontrigger$listeners != null) {
               brewedpotiontrigger$listeners.func_192346_b(p_192164_2_);
               if (brewedpotiontrigger$listeners.func_192347_a()) {
                    this.field_192177_b.remove(p_192164_1_);
               }
          }

     }

     public void func_192167_a(PlayerAdvancements p_192167_1_) {
          this.field_192177_b.remove(p_192167_1_);
     }

     public BrewedPotionTrigger.Instance func_192166_a(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
          PotionType potiontype = null;
          if (p_192166_1_.has("potion")) {
               ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(p_192166_1_, "potion"));
               if (!PotionType.REGISTRY.containsKey(resourcelocation)) {
                    throw new JsonSyntaxException("Unknown potion '" + resourcelocation + "'");
               }

               potiontype = (PotionType)PotionType.REGISTRY.getObject(resourcelocation);
          }

          return new BrewedPotionTrigger.Instance(potiontype);
     }

     public void func_192173_a(EntityPlayerMP p_192173_1_, PotionType p_192173_2_) {
          BrewedPotionTrigger.Listeners brewedpotiontrigger$listeners = (BrewedPotionTrigger.Listeners)this.field_192177_b.get(p_192173_1_.func_192039_O());
          if (brewedpotiontrigger$listeners != null) {
               brewedpotiontrigger$listeners.func_192348_a(p_192173_2_);
          }

     }

     static class Listeners {
          private final PlayerAdvancements field_192350_a;
          private final Set field_192351_b = Sets.newHashSet();

          public Listeners(PlayerAdvancements p_i47399_1_) {
               this.field_192350_a = p_i47399_1_;
          }

          public boolean func_192347_a() {
               return this.field_192351_b.isEmpty();
          }

          public void func_192349_a(ICriterionTrigger.Listener p_192349_1_) {
               this.field_192351_b.add(p_192349_1_);
          }

          public void func_192346_b(ICriterionTrigger.Listener p_192346_1_) {
               this.field_192351_b.remove(p_192346_1_);
          }

          public void func_192348_a(PotionType p_192348_1_) {
               List list = null;
               Iterator var3 = this.field_192351_b.iterator();

               ICriterionTrigger.Listener listener1;
               while(var3.hasNext()) {
                    listener1 = (ICriterionTrigger.Listener)var3.next();
                    if (((BrewedPotionTrigger.Instance)listener1.func_192158_a()).func_192250_a(p_192348_1_)) {
                         if (list == null) {
                              list = Lists.newArrayList();
                         }

                         list.add(listener1);
                    }
               }

               if (list != null) {
                    var3 = list.iterator();

                    while(var3.hasNext()) {
                         listener1 = (ICriterionTrigger.Listener)var3.next();
                         listener1.func_192159_a(this.field_192350_a);
                    }
               }

          }
     }

     public static class Instance extends AbstractCriterionInstance {
          private final PotionType field_192251_a;

          public Instance(@Nullable PotionType p_i47398_1_) {
               super(BrewedPotionTrigger.field_192176_a);
               this.field_192251_a = p_i47398_1_;
          }

          public boolean func_192250_a(PotionType p_192250_1_) {
               return this.field_192251_a == null || this.field_192251_a == p_192250_1_;
          }
     }
}
