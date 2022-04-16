package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemWrittenBook extends Item {
     public ItemWrittenBook() {
          this.setMaxStackSize(1);
     }

     public static boolean validBookTagContents(NBTTagCompound nbt) {
          if (!ItemWritableBook.isNBTValid(nbt)) {
               return false;
          } else if (!nbt.hasKey("title", 8)) {
               return false;
          } else {
               String s = nbt.getString("title");
               return s != null && s.length() <= 32 ? nbt.hasKey("author", 8) : false;
          }
     }

     public static int getGeneration(ItemStack book) {
          return book.getTagCompound().getInteger("generation");
     }

     public String getItemStackDisplayName(ItemStack stack) {
          if (stack.hasTagCompound()) {
               NBTTagCompound nbttagcompound = stack.getTagCompound();
               String s = nbttagcompound.getString("title");
               if (!StringUtils.isNullOrEmpty(s)) {
                    return s;
               }
          }

          return super.getItemStackDisplayName(stack);
     }

     public void addInformation(ItemStack stack, @Nullable World playerIn, List tooltip, ITooltipFlag advanced) {
          if (stack.hasTagCompound()) {
               NBTTagCompound nbttagcompound = stack.getTagCompound();
               String s = nbttagcompound.getString("author");
               if (!StringUtils.isNullOrEmpty(s)) {
                    tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("book.byAuthor", s));
               }

               tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("book.generation." + nbttagcompound.getInteger("generation")));
          }

     }

     public ActionResult onItemRightClick(World itemStackIn, EntityPlayer worldIn, EnumHand playerIn) {
          ItemStack itemstack = worldIn.getHeldItem(playerIn);
          if (!itemStackIn.isRemote) {
               this.resolveContents(itemstack, worldIn);
          }

          worldIn.openBook(itemstack, playerIn);
          worldIn.addStat(StatList.getObjectUseStats(this));
          return new ActionResult(EnumActionResult.SUCCESS, itemstack);
     }

     private void resolveContents(ItemStack stack, EntityPlayer player) {
          if (stack.getTagCompound() != null) {
               NBTTagCompound nbttagcompound = stack.getTagCompound();
               if (!nbttagcompound.getBoolean("resolved")) {
                    nbttagcompound.setBoolean("resolved", true);
                    if (validBookTagContents(nbttagcompound)) {
                         NBTTagList nbttaglist = nbttagcompound.getTagList("pages", 8);

                         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                              String s = nbttaglist.getStringTagAt(i);

                              Object itextcomponent;
                              try {
                                   ITextComponent itextcomponent = ITextComponent.Serializer.fromJsonLenient(s);
                                   itextcomponent = TextComponentUtils.processComponent(player, itextcomponent, player);
                              } catch (Exception var9) {
                                   itextcomponent = new TextComponentString(s);
                              }

                              nbttaglist.set(i, new NBTTagString(ITextComponent.Serializer.componentToJson((ITextComponent)itextcomponent)));
                         }

                         nbttagcompound.setTag("pages", nbttaglist);
                         if (player instanceof EntityPlayerMP && player.getHeldItemMainhand() == stack) {
                              Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
                              ((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(0, slot.slotNumber, stack));
                         }
                    }
               }
          }

     }

     public boolean hasEffect(ItemStack stack) {
          return true;
     }
}