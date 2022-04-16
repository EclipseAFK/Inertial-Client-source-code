package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class EntityAILeapAtTarget extends EntityAIBase {
     EntityLiving leaper;
     EntityLivingBase leapTarget;
     float leapMotionY;

     public EntityAILeapAtTarget(EntityLiving leapingEntity, float leapMotionYIn) {
          this.leaper = leapingEntity;
          this.leapMotionY = leapMotionYIn;
          this.setMutexBits(5);
     }

     public boolean shouldExecute() {
          this.leapTarget = this.leaper.getAttackTarget();
          if (this.leapTarget == null) {
               return false;
          } else {
               double d0 = this.leaper.getDistanceSqToEntity(this.leapTarget);
               if (d0 >= 4.0D && d0 <= 16.0D) {
                    if (!this.leaper.onGround) {
                         return false;
                    } else {
                         return this.leaper.getRNG().nextInt(5) == 0;
                    }
               } else {
                    return false;
               }
          }
     }

     public boolean continueExecuting() {
          return !this.leaper.onGround;
     }

     public void startExecuting() {
          double d0 = this.leapTarget.posX - this.leaper.posX;
          double d1 = this.leapTarget.posZ - this.leaper.posZ;
          float f = MathHelper.sqrt(d0 * d0 + d1 * d1);
          if ((double)f >= 1.0E-4D) {
               EntityLiving var10000 = this.leaper;
               var10000.motionX += d0 / (double)f * 0.5D * 0.800000011920929D + this.leaper.motionX * 0.20000000298023224D;
               var10000 = this.leaper;
               var10000.motionZ += d1 / (double)f * 0.5D * 0.800000011920929D + this.leaper.motionZ * 0.20000000298023224D;
          }

          this.leaper.motionY = (double)this.leapMotionY;
     }
}
