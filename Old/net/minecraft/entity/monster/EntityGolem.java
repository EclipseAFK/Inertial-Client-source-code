package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public abstract class EntityGolem extends EntityCreature implements IAnimals {
      public EntityGolem(World worldIn) {
            super(worldIn);
      }

      public void fall(float distance, float damageMultiplier) {
      }

      @Nullable
      protected SoundEvent getAmbientSound() {
            return null;
      }

      @Nullable
      protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
            return null;
      }

      @Nullable
      protected SoundEvent getDeathSound() {
            return null;
      }

      public int getTalkInterval() {
            return 120;
      }

      protected boolean canDespawn() {
            return false;
      }
}
