package net.minecraft.advancements.critereon;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class PlacedBlockTrigger implements ICriterionTrigger {
      private static final ResourceLocation field_193174_a = new ResourceLocation("placed_block");
      private final Map field_193175_b = Maps.newHashMap();

      public ResourceLocation func_192163_a() {
            return field_193174_a;
      }

      public void func_192165_a(PlayerAdvancements p_192165_1_, ICriterionTrigger.Listener p_192165_2_) {
            PlacedBlockTrigger.Listeners placedblocktrigger$listeners = (PlacedBlockTrigger.Listeners)this.field_193175_b.get(p_192165_1_);
            if (placedblocktrigger$listeners == null) {
                  placedblocktrigger$listeners = new PlacedBlockTrigger.Listeners(p_192165_1_);
                  this.field_193175_b.put(p_192165_1_, placedblocktrigger$listeners);
            }

            placedblocktrigger$listeners.func_193490_a(p_192165_2_);
      }

      public void func_192164_b(PlayerAdvancements p_192164_1_, ICriterionTrigger.Listener p_192164_2_) {
            PlacedBlockTrigger.Listeners placedblocktrigger$listeners = (PlacedBlockTrigger.Listeners)this.field_193175_b.get(p_192164_1_);
            if (placedblocktrigger$listeners != null) {
                  placedblocktrigger$listeners.func_193487_b(p_192164_2_);
                  if (placedblocktrigger$listeners.func_193488_a()) {
                        this.field_193175_b.remove(p_192164_1_);
                  }
            }

      }

      public void func_192167_a(PlayerAdvancements p_192167_1_) {
            this.field_193175_b.remove(p_192167_1_);
      }

      public PlacedBlockTrigger.Instance func_192166_a(JsonObject p_192166_1_, JsonDeserializationContext p_192166_2_) {
            Block block = null;
            if (p_192166_1_.has("block")) {
                  ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(p_192166_1_, "block"));
                  if (!Block.REGISTRY.containsKey(resourcelocation)) {
                        throw new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
                  }

                  block = (Block)Block.REGISTRY.getObject(resourcelocation);
            }

            Map map = null;
            if (p_192166_1_.has("state")) {
                  if (block == null) {
                        throw new JsonSyntaxException("Can't define block state without a specific block type");
                  }

                  BlockStateContainer blockstatecontainer = block.getBlockState();

                  IProperty iproperty;
                  Optional optional;
                  for(Iterator var6 = JsonUtils.getJsonObject(p_192166_1_, "state").entrySet().iterator(); var6.hasNext(); map.put(iproperty, optional.get())) {
                        Entry entry = (Entry)var6.next();
                        iproperty = blockstatecontainer.getProperty((String)entry.getKey());
                        if (iproperty == null) {
                              throw new JsonSyntaxException("Unknown block state property '" + (String)entry.getKey() + "' for block '" + Block.REGISTRY.getNameForObject(block) + "'");
                        }

                        String s = JsonUtils.getString((JsonElement)entry.getValue(), (String)entry.getKey());
                        optional = iproperty.parseValue(s);
                        if (!optional.isPresent()) {
                              throw new JsonSyntaxException("Invalid block state value '" + s + "' for property '" + (String)entry.getKey() + "' on block '" + Block.REGISTRY.getNameForObject(block) + "'");
                        }

                        if (map == null) {
                              map = Maps.newHashMap();
                        }
                  }
            }

            LocationPredicate locationpredicate = LocationPredicate.func_193454_a(p_192166_1_.get("location"));
            ItemPredicate itempredicate = ItemPredicate.func_192492_a(p_192166_1_.get("item"));
            return new PlacedBlockTrigger.Instance(block, map, locationpredicate, itempredicate);
      }

      public void func_193173_a(EntityPlayerMP p_193173_1_, BlockPos p_193173_2_, ItemStack p_193173_3_) {
            IBlockState iblockstate = p_193173_1_.world.getBlockState(p_193173_2_);
            PlacedBlockTrigger.Listeners placedblocktrigger$listeners = (PlacedBlockTrigger.Listeners)this.field_193175_b.get(p_193173_1_.func_192039_O());
            if (placedblocktrigger$listeners != null) {
                  placedblocktrigger$listeners.func_193489_a(iblockstate, p_193173_2_, p_193173_1_.getServerWorld(), p_193173_3_);
            }

      }

      static class Listeners {
            private final PlayerAdvancements field_193491_a;
            private final Set field_193492_b = Sets.newHashSet();

            public Listeners(PlayerAdvancements p_i47567_1_) {
                  this.field_193491_a = p_i47567_1_;
            }

            public boolean func_193488_a() {
                  return this.field_193492_b.isEmpty();
            }

            public void func_193490_a(ICriterionTrigger.Listener p_193490_1_) {
                  this.field_193492_b.add(p_193490_1_);
            }

            public void func_193487_b(ICriterionTrigger.Listener p_193487_1_) {
                  this.field_193492_b.remove(p_193487_1_);
            }

            public void func_193489_a(IBlockState p_193489_1_, BlockPos p_193489_2_, WorldServer p_193489_3_, ItemStack p_193489_4_) {
                  List list = null;
                  Iterator var6 = this.field_193492_b.iterator();

                  ICriterionTrigger.Listener listener1;
                  while(var6.hasNext()) {
                        listener1 = (ICriterionTrigger.Listener)var6.next();
                        if (((PlacedBlockTrigger.Instance)listener1.func_192158_a()).func_193210_a(p_193489_1_, p_193489_2_, p_193489_3_, p_193489_4_)) {
                              if (list == null) {
                                    list = Lists.newArrayList();
                              }

                              list.add(listener1);
                        }
                  }

                  if (list != null) {
                        var6 = list.iterator();

                        while(var6.hasNext()) {
                              listener1 = (ICriterionTrigger.Listener)var6.next();
                              listener1.func_192159_a(this.field_193491_a);
                        }
                  }

            }
      }

      public static class Instance extends AbstractCriterionInstance {
            private final Block field_193211_a;
            private final Map field_193212_b;
            private final LocationPredicate field_193213_c;
            private final ItemPredicate field_193214_d;

            public Instance(@Nullable Block p_i47566_1_, @Nullable Map p_i47566_2_, LocationPredicate p_i47566_3_, ItemPredicate p_i47566_4_) {
                  super(PlacedBlockTrigger.field_193174_a);
                  this.field_193211_a = p_i47566_1_;
                  this.field_193212_b = p_i47566_2_;
                  this.field_193213_c = p_i47566_3_;
                  this.field_193214_d = p_i47566_4_;
            }

            public boolean func_193210_a(IBlockState p_193210_1_, BlockPos p_193210_2_, WorldServer p_193210_3_, ItemStack p_193210_4_) {
                  if (this.field_193211_a != null && p_193210_1_.getBlock() != this.field_193211_a) {
                        return false;
                  } else {
                        if (this.field_193212_b != null) {
                              Iterator var5 = this.field_193212_b.entrySet().iterator();

                              while(var5.hasNext()) {
                                    Entry entry = (Entry)var5.next();
                                    if (p_193210_1_.getValue((IProperty)entry.getKey()) != entry.getValue()) {
                                          return false;
                                    }
                              }
                        }

                        return !this.field_193213_c.func_193453_a(p_193210_3_, (float)p_193210_2_.getX(), (float)p_193210_2_.getY(), (float)p_193210_2_.getZ()) ? false : this.field_193214_d.func_192493_a(p_193210_4_);
                  }
            }
      }
}
