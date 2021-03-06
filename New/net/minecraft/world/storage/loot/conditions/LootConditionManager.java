package net.minecraft.world.storage.loot.conditions;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;

public class LootConditionManager {
     private static final Map NAME_TO_SERIALIZER_MAP = Maps.newHashMap();
     private static final Map CLASS_TO_SERIALIZER_MAP = Maps.newHashMap();

     public static void registerCondition(LootCondition.Serializer condition) {
          ResourceLocation resourcelocation = condition.getLootTableLocation();
          Class oclass = condition.getConditionClass();
          if (NAME_TO_SERIALIZER_MAP.containsKey(resourcelocation)) {
               throw new IllegalArgumentException("Can't re-register item condition name " + resourcelocation);
          } else if (CLASS_TO_SERIALIZER_MAP.containsKey(oclass)) {
               throw new IllegalArgumentException("Can't re-register item condition class " + oclass.getName());
          } else {
               NAME_TO_SERIALIZER_MAP.put(resourcelocation, condition);
               CLASS_TO_SERIALIZER_MAP.put(oclass, condition);
          }
     }

     public static boolean testAllConditions(@Nullable LootCondition[] conditions, Random rand, LootContext context) {
          if (conditions == null) {
               return true;
          } else {
               LootCondition[] var3 = conditions;
               int var4 = conditions.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                    LootCondition lootcondition = var3[var5];
                    if (!lootcondition.testCondition(rand, context)) {
                         return false;
                    }
               }

               return true;
          }
     }

     public static LootCondition.Serializer getSerializerForName(ResourceLocation location) {
          LootCondition.Serializer serializer = (LootCondition.Serializer)NAME_TO_SERIALIZER_MAP.get(location);
          if (serializer == null) {
               throw new IllegalArgumentException("Unknown loot item condition '" + location + "'");
          } else {
               return serializer;
          }
     }

     public static LootCondition.Serializer getSerializerFor(LootCondition conditionClass) {
          LootCondition.Serializer serializer = (LootCondition.Serializer)CLASS_TO_SERIALIZER_MAP.get(conditionClass.getClass());
          if (serializer == null) {
               throw new IllegalArgumentException("Unknown loot item condition " + conditionClass);
          } else {
               return serializer;
          }
     }

     static {
          registerCondition(new RandomChance.Serializer());
          registerCondition(new RandomChanceWithLooting.Serializer());
          registerCondition(new EntityHasProperty.Serializer());
          registerCondition(new KilledByPlayer.Serializer());
          registerCondition(new EntityHasScore.Serializer());
     }

     public static class Serializer implements JsonDeserializer, JsonSerializer {
          public LootCondition deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
               JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "condition");
               ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonobject, "condition"));

               LootCondition.Serializer serializer;
               try {
                    serializer = LootConditionManager.getSerializerForName(resourcelocation);
               } catch (IllegalArgumentException var8) {
                    throw new JsonSyntaxException("Unknown condition '" + resourcelocation + "'");
               }

               return serializer.deserialize(jsonobject, p_deserialize_3_);
          }

          public JsonElement serialize(LootCondition p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
               LootCondition.Serializer serializer = LootConditionManager.getSerializerFor(p_serialize_1_);
               JsonObject jsonobject = new JsonObject();
               serializer.serialize(jsonobject, p_serialize_1_, p_serialize_3_);
               jsonobject.addProperty("condition", serializer.getLootTableLocation().toString());
               return jsonobject;
          }
     }
}
