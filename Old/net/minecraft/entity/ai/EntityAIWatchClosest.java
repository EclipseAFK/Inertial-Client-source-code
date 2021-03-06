package net.minecraft.entity.ai;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

public class EntityAIWatchClosest extends EntityAIBase {
      protected EntityLiving theWatcher;
      protected Entity closestEntity;
      protected float maxDistanceForPlayer;
      private int lookTime;
      private final float chance;
      protected Class watchedClass;

      public EntityAIWatchClosest(EntityLiving entitylivingIn, Class watchTargetClass, float maxDistance) {
            this.theWatcher = entitylivingIn;
            this.watchedClass = watchTargetClass;
            this.maxDistanceForPlayer = maxDistance;
            this.chance = 0.02F;
            this.setMutexBits(2);
      }

      public EntityAIWatchClosest(EntityLiving entitylivingIn, Class watchTargetClass, float maxDistance, float chanceIn) {
            this.theWatcher = entitylivingIn;
            this.watchedClass = watchTargetClass;
            this.maxDistanceForPlayer = maxDistance;
            this.chance = chanceIn;
            this.setMutexBits(2);
      }

      public boolean shouldExecute() {
            if (this.theWatcher.getRNG().nextFloat() >= this.chance) {
                  return false;
            } else {
                  if (this.theWatcher.getAttackTarget() != null) {
                        this.closestEntity = this.theWatcher.getAttackTarget();
                  }

                  if (this.watchedClass == EntityPlayer.class) {
                        this.closestEntity = this.theWatcher.world.func_190525_a(this.theWatcher.posX, this.theWatcher.posY, this.theWatcher.posZ, (double)this.maxDistanceForPlayer, Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.func_191324_b(this.theWatcher)));
                  } else {
                        this.closestEntity = this.theWatcher.world.findNearestEntityWithinAABB(this.watchedClass, this.theWatcher.getEntityBoundingBox().expand((double)this.maxDistanceForPlayer, 3.0D, (double)this.maxDistanceForPlayer), this.theWatcher);
                  }

                  return this.closestEntity != null;
            }
      }

      public boolean continueExecuting() {
            if (!this.closestEntity.isEntityAlive()) {
                  return false;
            } else if (this.theWatcher.getDistanceSqToEntity(this.closestEntity) > (double)(this.maxDistanceForPlayer * this.maxDistanceForPlayer)) {
                  return false;
            } else {
                  return this.lookTime > 0;
            }
      }

      public void startExecuting() {
            this.lookTime = 40 + this.theWatcher.getRNG().nextInt(40);
      }

      public void resetTask() {
            this.closestEntity = null;
      }

      public void updateTask() {
            this.theWatcher.getLookHelper().setLookPosition(this.closestEntity.posX, this.closestEntity.posY + (double)this.closestEntity.getEyeHeight(), this.closestEntity.posZ, (float)this.theWatcher.getHorizontalFaceSpeed(), (float)this.theWatcher.getVerticalFaceSpeed());
            --this.lookTime;
      }
}
