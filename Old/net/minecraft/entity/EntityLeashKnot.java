package net.minecraft.entity;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityLeashKnot extends EntityHanging {
      public EntityLeashKnot(World worldIn) {
            super(worldIn);
      }

      public EntityLeashKnot(World worldIn, BlockPos hangingPositionIn) {
            super(worldIn, hangingPositionIn);
            this.setPosition((double)hangingPositionIn.getX() + 0.5D, (double)hangingPositionIn.getY() + 0.5D, (double)hangingPositionIn.getZ() + 0.5D);
            float f = 0.125F;
            float f1 = 0.1875F;
            float f2 = 0.25F;
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.1875D, this.posY - 0.25D + 0.125D, this.posZ - 0.1875D, this.posX + 0.1875D, this.posY + 0.25D + 0.125D, this.posZ + 0.1875D));
            this.forceSpawn = true;
      }

      public void setPosition(double x, double y, double z) {
            super.setPosition((double)MathHelper.floor(x) + 0.5D, (double)MathHelper.floor(y) + 0.5D, (double)MathHelper.floor(z) + 0.5D);
      }

      protected void updateBoundingBox() {
            this.posX = (double)this.hangingPosition.getX() + 0.5D;
            this.posY = (double)this.hangingPosition.getY() + 0.5D;
            this.posZ = (double)this.hangingPosition.getZ() + 0.5D;
      }

      public void updateFacingWithBoundingBox(EnumFacing facingDirectionIn) {
      }

      public int getWidthPixels() {
            return 9;
      }

      public int getHeightPixels() {
            return 9;
      }

      public float getEyeHeight() {
            return -0.0625F;
      }

      public boolean isInRangeToRenderDist(double distance) {
            return distance < 1024.0D;
      }

      public void onBroken(@Nullable Entity brokenEntity) {
            this.playSound(SoundEvents.ENTITY_LEASHKNOT_BREAK, 1.0F, 1.0F);
      }

      public boolean writeToNBTOptional(NBTTagCompound compound) {
            return false;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
      }

      public boolean processInitialInteract(EntityPlayer player, EnumHand stack) {
            if (this.world.isRemote) {
                  return true;
            } else {
                  boolean flag = false;
                  double d0 = 7.0D;
                  List list = this.world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - 7.0D, this.posY - 7.0D, this.posZ - 7.0D, this.posX + 7.0D, this.posY + 7.0D, this.posZ + 7.0D));
                  Iterator var7 = list.iterator();

                  EntityLiving entityliving1;
                  while(var7.hasNext()) {
                        entityliving1 = (EntityLiving)var7.next();
                        if (entityliving1.getLeashed() && entityliving1.getLeashedToEntity() == player) {
                              entityliving1.setLeashedToEntity(this, true);
                              flag = true;
                        }
                  }

                  if (!flag) {
                        this.setDead();
                        if (player.capabilities.isCreativeMode) {
                              var7 = list.iterator();

                              while(var7.hasNext()) {
                                    entityliving1 = (EntityLiving)var7.next();
                                    if (entityliving1.getLeashed() && entityliving1.getLeashedToEntity() == this) {
                                          entityliving1.clearLeashed(true, false);
                                    }
                              }
                        }
                  }

                  return true;
            }
      }

      public boolean onValidSurface() {
            return this.world.getBlockState(this.hangingPosition).getBlock() instanceof BlockFence;
      }

      public static EntityLeashKnot createKnot(World worldIn, BlockPos fence) {
            EntityLeashKnot entityleashknot = new EntityLeashKnot(worldIn, fence);
            worldIn.spawnEntityInWorld(entityleashknot);
            entityleashknot.playPlaceSound();
            return entityleashknot;
      }

      @Nullable
      public static EntityLeashKnot getKnotForPosition(World worldIn, BlockPos pos) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            Iterator var5 = worldIn.getEntitiesWithinAABB(EntityLeashKnot.class, new AxisAlignedBB((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D)).iterator();

            EntityLeashKnot entityleashknot;
            do {
                  if (!var5.hasNext()) {
                        return null;
                  }

                  entityleashknot = (EntityLeashKnot)var5.next();
            } while(!entityleashknot.getHangingPosition().equals(pos));

            return entityleashknot;
      }

      public void playPlaceSound() {
            this.playSound(SoundEvents.ENTITY_LEASHKNOT_PLACE, 1.0F, 1.0F);
      }
}
