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
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class RecipeUnlockedTrigger implements ICriterionTrigger {
     private static final ResourceLocation field_192227_a = new ResourceLocation("recipe_unlocked");
     private final Map field_192228_b = Maps.newHashMap();

     public ResourceLocation func_192163_a() {
          return field_192227_a;
     }

     public void func_192165_a(PlayerAdvancements p_192165_1_, ICriterionTrigger.Listener p_192165_2_) {
          RecipeUnlockedTrigger.Listeners recipeunlockedtrigger$listeners = (RecipeUnlockedTrigger.Listeners)this.field_192228_b.get(p_192165_1_);
          if (recipeunlockedtrigger$listeners == null) {
               recipeunlockedtrigger$listeners = new RecipeUnlockedTrigger.Listeners(p_192165_1_);
               this.field_192228_b.put(p_192165_1_, recipeunlockedtrigger$listeners);
          }

          recipeunlockedtrigger$listeners.func_192528_a(p_192165_2_);
     }

     public void func_192164_b(PlayerAdvancements p_192164_1_, ICriterionTrigger.Listener p_192164_2_) {
          RecipeUnlockedTrigger.Listeners recipeunlockedtrigger$listeners = (RecipeUnlockedTrigger.Listeners)this.field_192228_b.get(p_192164_1_);
          if (recipeunlockedtrigger$listeners != null) {
               recipeunlockedtrigger$listeners.func_192525_b(p_192164_2_);
               if (recipeunlockedtrigger$listeners.func_192527_a()) {
                    this.field_192228_b.remove(p_192164_1_);
               }
          }

     }

     public void func_192167_a(PlayerAdvancements p_192167_1_) {
          this.field_192228_b.remove(p_192167_1_);
     }

     public RecipeUnlockedTrigger.Instance func_192166_a(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
          ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(p_192166_1_, "recipe"));
          IRecipe irecipe = CraftingManager.func_193373_a(resourcelocation);
          if (irecipe == null) {
               throw new JsonSyntaxException("Unknown recipe '" + resourcelocation + "'");
          } else {
               return new RecipeUnlockedTrigger.Instance(irecipe);
          }
     }

     public void func_192225_a(EntityPlayerMP p_192225_1_, IRecipe p_192225_2_) {
          RecipeUnlockedTrigger.Listeners recipeunlockedtrigger$listeners = (RecipeUnlockedTrigger.Listeners)this.field_192228_b.get(p_192225_1_.func_192039_O());
          if (recipeunlockedtrigger$listeners != null) {
               recipeunlockedtrigger$listeners.func_193493_a(p_192225_2_);
          }

     }

     static class Listeners {
          private final PlayerAdvancements field_192529_a;
          private final Set field_192530_b = Sets.newHashSet();

          public Listeners(PlayerAdvancements p_i47397_1_) {
               this.field_192529_a = p_i47397_1_;
          }

          public boolean func_192527_a() {
               return this.field_192530_b.isEmpty();
          }

          public void func_192528_a(ICriterionTrigger.Listener p_192528_1_) {
               this.field_192530_b.add(p_192528_1_);
          }

          public void func_192525_b(ICriterionTrigger.Listener p_192525_1_) {
               this.field_192530_b.remove(p_192525_1_);
          }

          public void func_193493_a(IRecipe p_193493_1_) {
               List list = null;
               Iterator var3 = this.field_192530_b.iterator();

               ICriterionTrigger.Listener listener1;
               while(var3.hasNext()) {
                    listener1 = (ICriterionTrigger.Listener)var3.next();
                    if (((RecipeUnlockedTrigger.Instance)listener1.func_192158_a()).func_193215_a(p_193493_1_)) {
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
                         listener1.func_192159_a(this.field_192529_a);
                    }
               }

          }
     }

     public static class Instance extends AbstractCriterionInstance {
          private final IRecipe field_192282_a;

          public Instance(IRecipe p_i47526_1_) {
               super(RecipeUnlockedTrigger.field_192227_a);
               this.field_192282_a = p_i47526_1_;
          }

          public boolean func_193215_a(IRecipe p_193215_1_) {
               return this.field_192282_a == p_193215_1_;
          }
     }
}
