package net.minecraft.world.storage.loot.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnchantRandomly extends LootFunction {
      private static final Logger LOGGER = LogManager.getLogger();
      private final List enchantments;

      public EnchantRandomly(LootCondition[] conditionsIn, @Nullable List enchantmentsIn) {
            super(conditionsIn);
            this.enchantments = enchantmentsIn == null ? Collections.emptyList() : enchantmentsIn;
      }

      public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
            Enchantment enchantment;
            if (this.enchantments.isEmpty()) {
                  List list = Lists.newArrayList();
                  Iterator var6 = Enchantment.REGISTRY.iterator();

                  label32:
                  while(true) {
                        Enchantment enchantment1;
                        do {
                              if (!var6.hasNext()) {
                                    if (list.isEmpty()) {
                                          LOGGER.warn("Couldn't find a compatible enchantment for {}", stack);
                                          return stack;
                                    }

                                    enchantment = (Enchantment)list.get(rand.nextInt(list.size()));
                                    break label32;
                              }

                              enchantment1 = (Enchantment)var6.next();
                        } while(stack.getItem() != Items.BOOK && !enchantment1.canApply(stack));

                        list.add(enchantment1);
                  }
            } else {
                  enchantment = (Enchantment)this.enchantments.get(rand.nextInt(this.enchantments.size()));
            }

            int i = MathHelper.getInt(rand, enchantment.getMinLevel(), enchantment.getMaxLevel());
            if (stack.getItem() == Items.BOOK) {
                  stack = new ItemStack(Items.ENCHANTED_BOOK);
                  ItemEnchantedBook.addEnchantment(stack, new EnchantmentData(enchantment, i));
            } else {
                  stack.addEnchantment(enchantment, i);
            }

            return stack;
      }

      public static class Serializer extends LootFunction.Serializer {
            public Serializer() {
                  super(new ResourceLocation("enchant_randomly"), EnchantRandomly.class);
            }

            public void serialize(JsonObject object, EnchantRandomly functionClazz, JsonSerializationContext serializationContext) {
                  if (!functionClazz.enchantments.isEmpty()) {
                        JsonArray jsonarray = new JsonArray();
                        Iterator var5 = functionClazz.enchantments.iterator();

                        while(var5.hasNext()) {
                              Enchantment enchantment = (Enchantment)var5.next();
                              ResourceLocation resourcelocation = (ResourceLocation)Enchantment.REGISTRY.getNameForObject(enchantment);
                              if (resourcelocation == null) {
                                    throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
                              }

                              jsonarray.add(new JsonPrimitive(resourcelocation.toString()));
                        }

                        object.add("enchantments", jsonarray);
                  }

            }

            public EnchantRandomly deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
                  List list = Lists.newArrayList();
                  if (object.has("enchantments")) {
                        Iterator var5 = JsonUtils.getJsonArray(object, "enchantments").iterator();

                        while(var5.hasNext()) {
                              JsonElement jsonelement = (JsonElement)var5.next();
                              String s = JsonUtils.getString(jsonelement, "enchantment");
                              Enchantment enchantment = (Enchantment)Enchantment.REGISTRY.getObject(new ResourceLocation(s));
                              if (enchantment == null) {
                                    throw new JsonSyntaxException("Unknown enchantment '" + s + "'");
                              }

                              list.add(enchantment);
                        }
                  }

                  return new EnchantRandomly(conditionsIn, list);
            }
      }
}
