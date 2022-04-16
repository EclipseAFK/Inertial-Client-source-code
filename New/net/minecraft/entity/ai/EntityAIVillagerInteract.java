package net.minecraft.entity.ai;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class EntityAIVillagerInteract extends EntityAIWatchClosest2 {
     private int interactionDelay;
     private final EntityVillager villager;

     public EntityAIVillagerInteract(EntityVillager villagerIn) {
          super(villagerIn, EntityVillager.class, 3.0F, 0.02F);
          this.villager = villagerIn;
     }

     public void startExecuting() {
          super.startExecuting();
          if (this.villager.canAbondonItems() && this.closestEntity instanceof EntityVillager && ((EntityVillager)this.closestEntity).wantsMoreFood()) {
               this.interactionDelay = 10;
          } else {
               this.interactionDelay = 0;
          }

     }

     public void updateTask() {
          super.updateTask();
          if (this.interactionDelay > 0) {
               --this.interactionDelay;
               if (this.interactionDelay == 0) {
                    InventoryBasic inventorybasic = this.villager.getVillagerInventory();

                    for(int i = 0; i < inventorybasic.getSizeInventory(); ++i) {
                         ItemStack itemstack = inventorybasic.getStackInSlot(i);
                         ItemStack itemstack1 = ItemStack.field_190927_a;
                         if (!itemstack.func_190926_b()) {
                              Item item = itemstack.getItem();
                              int j;
                              if ((item == Items.BREAD || item == Items.POTATO || item == Items.CARROT || item == Items.BEETROOT) && itemstack.func_190916_E() > 3) {
                                   j = itemstack.func_190916_E() / 2;
                                   itemstack.func_190918_g(j);
                                   itemstack1 = new ItemStack(item, j, itemstack.getMetadata());
                              } else if (item == Items.WHEAT && itemstack.func_190916_E() > 5) {
                                   j = itemstack.func_190916_E() / 2 / 3 * 3;
                                   int k = j / 3;
                                   itemstack.func_190918_g(j);
                                   itemstack1 = new ItemStack(Items.BREAD, k, 0);
                              }

                              if (itemstack.func_190926_b()) {
                                   inventorybasic.setInventorySlotContents(i, ItemStack.field_190927_a);
                              }
                         }

                         if (!itemstack1.func_190926_b()) {
                              double d0 = this.villager.posY - 0.30000001192092896D + (double)this.villager.getEyeHeight();
                              EntityItem entityitem = new EntityItem(this.villager.world, this.villager.posX, d0, this.villager.posZ, itemstack1);
                              float f = 0.3F;
                              float f1 = this.villager.rotationYawHead;
                              float f2 = this.villager.rotationPitch;
                              entityitem.motionX = (double)(-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * 0.3F);
                              entityitem.motionZ = (double)(MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * 0.3F);
                              entityitem.motionY = (double)(-MathHelper.sin(f2 * 0.017453292F) * 0.3F + 0.1F);
                              entityitem.setDefaultPickupDelay();
                              this.villager.world.spawnEntityInWorld(entityitem);
                              break;
                         }
                    }
               }
          }

     }
}
