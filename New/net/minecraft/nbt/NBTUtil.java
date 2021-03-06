package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NBTUtil {
     private static final Logger field_193591_a = LogManager.getLogger();

     @Nullable
     public static GameProfile readGameProfileFromNBT(NBTTagCompound compound) {
          String s = null;
          String s1 = null;
          if (compound.hasKey("Name", 8)) {
               s = compound.getString("Name");
          }

          if (compound.hasKey("Id", 8)) {
               s1 = compound.getString("Id");
          }

          try {
               UUID uuid;
               try {
                    uuid = UUID.fromString(s1);
               } catch (Throwable var12) {
                    uuid = null;
               }

               GameProfile gameprofile = new GameProfile(uuid, s);
               if (compound.hasKey("Properties", 10)) {
                    NBTTagCompound nbttagcompound = compound.getCompoundTag("Properties");
                    Iterator var6 = nbttagcompound.getKeySet().iterator();

                    while(var6.hasNext()) {
                         String s2 = (String)var6.next();
                         NBTTagList nbttaglist = nbttagcompound.getTagList(s2, 10);

                         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                              NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                              String s3 = nbttagcompound1.getString("Value");
                              if (nbttagcompound1.hasKey("Signature", 8)) {
                                   gameprofile.getProperties().put(s2, new Property(s2, s3, nbttagcompound1.getString("Signature")));
                              } else {
                                   gameprofile.getProperties().put(s2, new Property(s2, s3));
                              }
                         }
                    }
               }

               return gameprofile;
          } catch (Throwable var13) {
               return null;
          }
     }

     public static NBTTagCompound writeGameProfile(NBTTagCompound tagCompound, GameProfile profile) {
          if (!StringUtils.isNullOrEmpty(profile.getName())) {
               tagCompound.setString("Name", profile.getName());
          }

          if (profile.getId() != null) {
               tagCompound.setString("Id", profile.getId().toString());
          }

          if (!profile.getProperties().isEmpty()) {
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               Iterator var3 = profile.getProperties().keySet().iterator();

               while(var3.hasNext()) {
                    String s = (String)var3.next();
                    NBTTagList nbttaglist = new NBTTagList();

                    NBTTagCompound nbttagcompound1;
                    for(Iterator var6 = profile.getProperties().get(s).iterator(); var6.hasNext(); nbttaglist.appendTag(nbttagcompound1)) {
                         Property property = (Property)var6.next();
                         nbttagcompound1 = new NBTTagCompound();
                         nbttagcompound1.setString("Value", property.getValue());
                         if (property.hasSignature()) {
                              nbttagcompound1.setString("Signature", property.getSignature());
                         }
                    }

                    nbttagcompound.setTag(s, nbttaglist);
               }

               tagCompound.setTag("Properties", nbttagcompound);
          }

          return tagCompound;
     }

     @VisibleForTesting
     public static boolean areNBTEquals(NBTBase nbt1, NBTBase nbt2, boolean compareTagList) {
          if (nbt1 == nbt2) {
               return true;
          } else if (nbt1 == null) {
               return true;
          } else if (nbt2 == null) {
               return false;
          } else if (!nbt1.getClass().equals(nbt2.getClass())) {
               return false;
          } else if (nbt1 instanceof NBTTagCompound) {
               NBTTagCompound nbttagcompound = (NBTTagCompound)nbt1;
               NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbt2;
               Iterator var11 = nbttagcompound.getKeySet().iterator();

               String s;
               NBTBase nbtbase1;
               do {
                    if (!var11.hasNext()) {
                         return true;
                    }

                    s = (String)var11.next();
                    nbtbase1 = nbttagcompound.getTag(s);
               } while(areNBTEquals(nbtbase1, nbttagcompound1.getTag(s), compareTagList));

               return false;
          } else if (nbt1 instanceof NBTTagList && compareTagList) {
               NBTTagList nbttaglist = (NBTTagList)nbt1;
               NBTTagList nbttaglist1 = (NBTTagList)nbt2;
               if (nbttaglist.hasNoTags()) {
                    return nbttaglist1.hasNoTags();
               } else {
                    for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                         NBTBase nbtbase = nbttaglist.get(i);
                         boolean flag = false;

                         for(int j = 0; j < nbttaglist1.tagCount(); ++j) {
                              if (areNBTEquals(nbtbase, nbttaglist1.get(j), compareTagList)) {
                                   flag = true;
                                   break;
                              }
                         }

                         if (!flag) {
                              return false;
                         }
                    }

                    return true;
               }
          } else {
               return nbt1.equals(nbt2);
          }
     }

     public static NBTTagCompound createUUIDTag(UUID uuid) {
          NBTTagCompound nbttagcompound = new NBTTagCompound();
          nbttagcompound.setLong("M", uuid.getMostSignificantBits());
          nbttagcompound.setLong("L", uuid.getLeastSignificantBits());
          return nbttagcompound;
     }

     public static UUID getUUIDFromTag(NBTTagCompound tag) {
          return new UUID(tag.getLong("M"), tag.getLong("L"));
     }

     public static BlockPos getPosFromTag(NBTTagCompound tag) {
          return new BlockPos(tag.getInteger("X"), tag.getInteger("Y"), tag.getInteger("Z"));
     }

     public static NBTTagCompound createPosTag(BlockPos pos) {
          NBTTagCompound nbttagcompound = new NBTTagCompound();
          nbttagcompound.setInteger("X", pos.getX());
          nbttagcompound.setInteger("Y", pos.getY());
          nbttagcompound.setInteger("Z", pos.getZ());
          return nbttagcompound;
     }

     public static IBlockState readBlockState(NBTTagCompound tag) {
          if (!tag.hasKey("Name", 8)) {
               return Blocks.AIR.getDefaultState();
          } else {
               Block block = (Block)Block.REGISTRY.getObject(new ResourceLocation(tag.getString("Name")));
               IBlockState iblockstate = block.getDefaultState();
               if (tag.hasKey("Properties", 10)) {
                    NBTTagCompound nbttagcompound = tag.getCompoundTag("Properties");
                    BlockStateContainer blockstatecontainer = block.getBlockState();
                    Iterator var5 = nbttagcompound.getKeySet().iterator();

                    while(var5.hasNext()) {
                         String s = (String)var5.next();
                         IProperty iproperty = blockstatecontainer.getProperty(s);
                         if (iproperty != null) {
                              iblockstate = func_193590_a(iblockstate, iproperty, s, nbttagcompound, tag);
                         }
                    }
               }

               return iblockstate;
          }
     }

     private static IBlockState func_193590_a(IBlockState p_193590_0_, IProperty p_193590_1_, String p_193590_2_, NBTTagCompound p_193590_3_, NBTTagCompound p_193590_4_) {
          Optional optional = p_193590_1_.parseValue(p_193590_3_.getString(p_193590_2_));
          if (optional.isPresent()) {
               return p_193590_0_.withProperty(p_193590_1_, (Comparable)optional.get());
          } else {
               field_193591_a.warn("Unable to read property: {} with value: {} for blockstate: {}", p_193590_2_, p_193590_3_.getString(p_193590_2_), p_193590_4_.toString());
               return p_193590_0_;
          }
     }

     public static NBTTagCompound writeBlockState(NBTTagCompound tag, IBlockState state) {
          tag.setString("Name", ((ResourceLocation)Block.REGISTRY.getNameForObject(state.getBlock())).toString());
          if (!state.getProperties().isEmpty()) {
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               UnmodifiableIterator unmodifiableiterator = state.getProperties().entrySet().iterator();

               while(unmodifiableiterator.hasNext()) {
                    Entry entry = (Entry)unmodifiableiterator.next();
                    IProperty iproperty = (IProperty)entry.getKey();
                    nbttagcompound.setString(iproperty.getName(), getName(iproperty, (Comparable)entry.getValue()));
               }

               tag.setTag("Properties", nbttagcompound);
          }

          return tag;
     }

     private static String getName(IProperty p_190010_0_, Comparable p_190010_1_) {
          return p_190010_0_.getName(p_190010_1_);
     }
}
