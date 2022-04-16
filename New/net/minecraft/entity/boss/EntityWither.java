package net.minecraft.entity.boss;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityWither extends EntityMob implements IRangedAttackMob {
     private static final DataParameter FIRST_HEAD_TARGET;
     private static final DataParameter SECOND_HEAD_TARGET;
     private static final DataParameter THIRD_HEAD_TARGET;
     private static final DataParameter[] HEAD_TARGETS;
     private static final DataParameter INVULNERABILITY_TIME;
     private final float[] xRotationHeads = new float[2];
     private final float[] yRotationHeads = new float[2];
     private final float[] xRotOHeads = new float[2];
     private final float[] yRotOHeads = new float[2];
     private final int[] nextHeadUpdate = new int[2];
     private final int[] idleHeadUpdates = new int[2];
     private int blockBreakCounter;
     private final BossInfoServer bossInfo;
     private static final Predicate NOT_UNDEAD;

     public EntityWither(World worldIn) {
          super(worldIn);
          this.bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
          this.setHealth(this.getMaxHealth());
          this.setSize(0.9F, 3.5F);
          this.isImmuneToFire = true;
          ((PathNavigateGround)this.getNavigator()).setCanSwim(true);
          this.experienceValue = 50;
     }

     protected void initEntityAI() {
          this.tasks.addTask(0, new EntityWither.AIDoNothing());
          this.tasks.addTask(1, new EntityAISwimming(this));
          this.tasks.addTask(2, new EntityAIAttackRanged(this, 1.0D, 40, 20.0F));
          this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
          this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
          this.tasks.addTask(7, new EntityAILookIdle(this));
          this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
          this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, false, false, NOT_UNDEAD));
     }

     protected void entityInit() {
          super.entityInit();
          this.dataManager.register(FIRST_HEAD_TARGET, 0);
          this.dataManager.register(SECOND_HEAD_TARGET, 0);
          this.dataManager.register(THIRD_HEAD_TARGET, 0);
          this.dataManager.register(INVULNERABILITY_TIME, 0);
     }

     public static void registerFixesWither(DataFixer fixer) {
          EntityLiving.registerFixesMob(fixer, EntityWither.class);
     }

     public void writeEntityToNBT(NBTTagCompound compound) {
          super.writeEntityToNBT(compound);
          compound.setInteger("Invul", this.getInvulTime());
     }

     public void readEntityFromNBT(NBTTagCompound compound) {
          super.readEntityFromNBT(compound);
          this.setInvulTime(compound.getInteger("Invul"));
          if (this.hasCustomName()) {
               this.bossInfo.setName(this.getDisplayName());
          }

     }

     public void setCustomNameTag(String name) {
          super.setCustomNameTag(name);
          this.bossInfo.setName(this.getDisplayName());
     }

     protected SoundEvent getAmbientSound() {
          return SoundEvents.ENTITY_WITHER_AMBIENT;
     }

     protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
          return SoundEvents.ENTITY_WITHER_HURT;
     }

     protected SoundEvent getDeathSound() {
          return SoundEvents.ENTITY_WITHER_DEATH;
     }

     public void onLivingUpdate() {
          this.motionY *= 0.6000000238418579D;
          double d11;
          double d12;
          double d13;
          if (!this.world.isRemote && this.getWatchedTargetId(0) > 0) {
               Entity entity = this.world.getEntityByID(this.getWatchedTargetId(0));
               if (entity != null) {
                    if (this.posY < entity.posY || !this.isArmored() && this.posY < entity.posY + 5.0D) {
                         if (this.motionY < 0.0D) {
                              this.motionY = 0.0D;
                         }

                         this.motionY += (0.5D - this.motionY) * 0.6000000238418579D;
                    }

                    double d0 = entity.posX - this.posX;
                    d11 = entity.posZ - this.posZ;
                    d12 = d0 * d0 + d11 * d11;
                    if (d12 > 9.0D) {
                         d13 = (double)MathHelper.sqrt(d12);
                         this.motionX += (d0 / d13 * 0.5D - this.motionX) * 0.6000000238418579D;
                         this.motionZ += (d11 / d13 * 0.5D - this.motionZ) * 0.6000000238418579D;
                    }
               }
          }

          if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.05000000074505806D) {
               this.rotationYaw = (float)MathHelper.atan2(this.motionZ, this.motionX) * 57.295776F - 90.0F;
          }

          super.onLivingUpdate();

          int j;
          for(j = 0; j < 2; ++j) {
               this.yRotOHeads[j] = this.yRotationHeads[j];
               this.xRotOHeads[j] = this.xRotationHeads[j];
          }

          int i1;
          for(j = 0; j < 2; ++j) {
               i1 = this.getWatchedTargetId(j + 1);
               Entity entity1 = null;
               if (i1 > 0) {
                    entity1 = this.world.getEntityByID(i1);
               }

               if (entity1 != null) {
                    d11 = this.getHeadX(j + 1);
                    d12 = this.getHeadY(j + 1);
                    d13 = this.getHeadZ(j + 1);
                    double d6 = entity1.posX - d11;
                    double d7 = entity1.posY + (double)entity1.getEyeHeight() - d12;
                    double d8 = entity1.posZ - d13;
                    double d9 = (double)MathHelper.sqrt(d6 * d6 + d8 * d8);
                    float f = (float)(MathHelper.atan2(d8, d6) * 57.29577951308232D) - 90.0F;
                    float f1 = (float)(-(MathHelper.atan2(d7, d9) * 57.29577951308232D));
                    this.xRotationHeads[j] = this.rotlerp(this.xRotationHeads[j], f1, 40.0F);
                    this.yRotationHeads[j] = this.rotlerp(this.yRotationHeads[j], f, 10.0F);
               } else {
                    this.yRotationHeads[j] = this.rotlerp(this.yRotationHeads[j], this.renderYawOffset, 10.0F);
               }
          }

          boolean flag = this.isArmored();

          for(i1 = 0; i1 < 3; ++i1) {
               double d10 = this.getHeadX(i1);
               double d2 = this.getHeadY(i1);
               double d4 = this.getHeadZ(i1);
               this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d10 + this.rand.nextGaussian() * 0.30000001192092896D, d2 + this.rand.nextGaussian() * 0.30000001192092896D, d4 + this.rand.nextGaussian() * 0.30000001192092896D, 0.0D, 0.0D, 0.0D);
               if (flag && this.world.rand.nextInt(4) == 0) {
                    this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, d10 + this.rand.nextGaussian() * 0.30000001192092896D, d2 + this.rand.nextGaussian() * 0.30000001192092896D, d4 + this.rand.nextGaussian() * 0.30000001192092896D, 0.699999988079071D, 0.699999988079071D, 0.5D);
               }
          }

          if (this.getInvulTime() > 0) {
               for(i1 = 0; i1 < 3; ++i1) {
                    this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + this.rand.nextGaussian(), this.posY + (double)(this.rand.nextFloat() * 3.3F), this.posZ + this.rand.nextGaussian(), 0.699999988079071D, 0.699999988079071D, 0.8999999761581421D);
               }
          }

     }

     protected void updateAITasks() {
          int i;
          if (this.getInvulTime() > 0) {
               i = this.getInvulTime() - 1;
               if (i <= 0) {
                    this.world.newExplosion(this, this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, 7.0F, false, this.world.getGameRules().getBoolean("mobGriefing"));
                    this.world.playBroadcastSound(1023, new BlockPos(this), 0);
               }

               this.setInvulTime(i);
               if (this.ticksExisted % 10 == 0) {
                    this.heal(10.0F);
               }
          } else {
               super.updateAITasks();

               int l1;
               int i2;
               for(i = 1; i < 3; ++i) {
                    if (this.ticksExisted >= this.nextHeadUpdate[i - 1]) {
                         this.nextHeadUpdate[i - 1] = this.ticksExisted + 10 + this.rand.nextInt(10);
                         if (this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) {
                              l1 = i - 1;
                              i2 = this.idleHeadUpdates[i - 1];
                              this.idleHeadUpdates[l1] = this.idleHeadUpdates[i - 1] + 1;
                              if (i2 > 15) {
                                   float f = 10.0F;
                                   float f1 = 5.0F;
                                   double d0 = MathHelper.nextDouble(this.rand, this.posX - 10.0D, this.posX + 10.0D);
                                   double d1 = MathHelper.nextDouble(this.rand, this.posY - 5.0D, this.posY + 5.0D);
                                   double d2 = MathHelper.nextDouble(this.rand, this.posZ - 10.0D, this.posZ + 10.0D);
                                   this.launchWitherSkullToCoords(i + 1, d0, d1, d2, true);
                                   this.idleHeadUpdates[i - 1] = 0;
                              }
                         }

                         l1 = this.getWatchedTargetId(i);
                         if (l1 > 0) {
                              Entity entity = this.world.getEntityByID(l1);
                              if (entity != null && entity.isEntityAlive() && this.getDistanceSqToEntity(entity) <= 900.0D && this.canEntityBeSeen(entity)) {
                                   if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.disableDamage) {
                                        this.updateWatchedTargetId(i, 0);
                                   } else {
                                        this.launchWitherSkullToEntity(i + 1, (EntityLivingBase)entity);
                                        this.nextHeadUpdate[i - 1] = this.ticksExisted + 40 + this.rand.nextInt(20);
                                        this.idleHeadUpdates[i - 1] = 0;
                                   }
                              } else {
                                   this.updateWatchedTargetId(i, 0);
                              }
                         } else {
                              List list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(20.0D, 8.0D, 20.0D), Predicates.and(NOT_UNDEAD, EntitySelectors.NOT_SPECTATING));

                              for(int j2 = 0; j2 < 10 && !list.isEmpty(); ++j2) {
                                   EntityLivingBase entitylivingbase = (EntityLivingBase)list.get(this.rand.nextInt(list.size()));
                                   if (entitylivingbase != this && entitylivingbase.isEntityAlive() && this.canEntityBeSeen(entitylivingbase)) {
                                        if (entitylivingbase instanceof EntityPlayer) {
                                             if (!((EntityPlayer)entitylivingbase).capabilities.disableDamage) {
                                                  this.updateWatchedTargetId(i, entitylivingbase.getEntityId());
                                             }
                                        } else {
                                             this.updateWatchedTargetId(i, entitylivingbase.getEntityId());
                                        }
                                        break;
                                   }

                                   list.remove(entitylivingbase);
                              }
                         }
                    }
               }

               if (this.getAttackTarget() != null) {
                    this.updateWatchedTargetId(0, this.getAttackTarget().getEntityId());
               } else {
                    this.updateWatchedTargetId(0, 0);
               }

               if (this.blockBreakCounter > 0) {
                    --this.blockBreakCounter;
                    if (this.blockBreakCounter == 0 && this.world.getGameRules().getBoolean("mobGriefing")) {
                         i = MathHelper.floor(this.posY);
                         l1 = MathHelper.floor(this.posX);
                         i2 = MathHelper.floor(this.posZ);
                         boolean flag = false;
                         int k2 = -1;

                         while(true) {
                              if (k2 > 1) {
                                   if (flag) {
                                        this.world.playEvent((EntityPlayer)null, 1022, new BlockPos(this), 0);
                                   }
                                   break;
                              }

                              for(int l2 = -1; l2 <= 1; ++l2) {
                                   for(int j = 0; j <= 3; ++j) {
                                        int i3 = l1 + k2;
                                        int k = i + j;
                                        int l = i2 + l2;
                                        BlockPos blockpos = new BlockPos(i3, k, l);
                                        IBlockState iblockstate = this.world.getBlockState(blockpos);
                                        Block block = iblockstate.getBlock();
                                        if (iblockstate.getMaterial() != Material.AIR && canDestroyBlock(block)) {
                                             flag = this.world.destroyBlock(blockpos, true) || flag;
                                        }
                                   }
                              }

                              ++k2;
                         }
                    }
               }

               if (this.ticksExisted % 20 == 0) {
                    this.heal(1.0F);
               }

               this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
          }

     }

     public static boolean canDestroyBlock(Block blockIn) {
          return blockIn != Blocks.BEDROCK && blockIn != Blocks.END_PORTAL && blockIn != Blocks.END_PORTAL_FRAME && blockIn != Blocks.COMMAND_BLOCK && blockIn != Blocks.REPEATING_COMMAND_BLOCK && blockIn != Blocks.CHAIN_COMMAND_BLOCK && blockIn != Blocks.BARRIER && blockIn != Blocks.STRUCTURE_BLOCK && blockIn != Blocks.STRUCTURE_VOID && blockIn != Blocks.PISTON_EXTENSION && blockIn != Blocks.END_GATEWAY;
     }

     public void ignite() {
          this.setInvulTime(220);
          this.setHealth(this.getMaxHealth() / 3.0F);
     }

     public void setInWeb() {
     }

     public void addTrackingPlayer(EntityPlayerMP player) {
          super.addTrackingPlayer(player);
          this.bossInfo.addPlayer(player);
     }

     public void removeTrackingPlayer(EntityPlayerMP player) {
          super.removeTrackingPlayer(player);
          this.bossInfo.removePlayer(player);
     }

     private double getHeadX(int p_82214_1_) {
          if (p_82214_1_ <= 0) {
               return this.posX;
          } else {
               float f = (this.renderYawOffset + (float)(180 * (p_82214_1_ - 1))) * 0.017453292F;
               float f1 = MathHelper.cos(f);
               return this.posX + (double)f1 * 1.3D;
          }
     }

     private double getHeadY(int p_82208_1_) {
          return p_82208_1_ <= 0 ? this.posY + 3.0D : this.posY + 2.2D;
     }

     private double getHeadZ(int p_82213_1_) {
          if (p_82213_1_ <= 0) {
               return this.posZ;
          } else {
               float f = (this.renderYawOffset + (float)(180 * (p_82213_1_ - 1))) * 0.017453292F;
               float f1 = MathHelper.sin(f);
               return this.posZ + (double)f1 * 1.3D;
          }
     }

     private float rotlerp(float p_82204_1_, float p_82204_2_, float p_82204_3_) {
          float f = MathHelper.wrapDegrees(p_82204_2_ - p_82204_1_);
          if (f > p_82204_3_) {
               f = p_82204_3_;
          }

          if (f < -p_82204_3_) {
               f = -p_82204_3_;
          }

          return p_82204_1_ + f;
     }

     private void launchWitherSkullToEntity(int p_82216_1_, EntityLivingBase p_82216_2_) {
          this.launchWitherSkullToCoords(p_82216_1_, p_82216_2_.posX, p_82216_2_.posY + (double)p_82216_2_.getEyeHeight() * 0.5D, p_82216_2_.posZ, p_82216_1_ == 0 && this.rand.nextFloat() < 0.001F);
     }

     private void launchWitherSkullToCoords(int p_82209_1_, double x, double y, double z, boolean invulnerable) {
          this.world.playEvent((EntityPlayer)null, 1024, new BlockPos(this), 0);
          double d0 = this.getHeadX(p_82209_1_);
          double d1 = this.getHeadY(p_82209_1_);
          double d2 = this.getHeadZ(p_82209_1_);
          double d3 = x - d0;
          double d4 = y - d1;
          double d5 = z - d2;
          EntityWitherSkull entitywitherskull = new EntityWitherSkull(this.world, this, d3, d4, d5);
          if (invulnerable) {
               entitywitherskull.setInvulnerable(true);
          }

          entitywitherskull.posY = d1;
          entitywitherskull.posX = d0;
          entitywitherskull.posZ = d2;
          this.world.spawnEntityInWorld(entitywitherskull);
     }

     public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
          this.launchWitherSkullToEntity(0, target);
     }

     public boolean attackEntityFrom(DamageSource source, float amount) {
          if (this.isEntityInvulnerable(source)) {
               return false;
          } else if (source != DamageSource.drown && !(source.getEntity() instanceof EntityWither)) {
               if (this.getInvulTime() > 0 && source != DamageSource.outOfWorld) {
                    return false;
               } else {
                    Entity entity1;
                    if (this.isArmored()) {
                         entity1 = source.getSourceOfDamage();
                         if (entity1 instanceof EntityArrow) {
                              return false;
                         }
                    }

                    entity1 = source.getEntity();
                    if (entity1 != null && !(entity1 instanceof EntityPlayer) && entity1 instanceof EntityLivingBase && ((EntityLivingBase)entity1).getCreatureAttribute() == this.getCreatureAttribute()) {
                         return false;
                    } else {
                         if (this.blockBreakCounter <= 0) {
                              this.blockBreakCounter = 20;
                         }

                         for(int i = 0; i < this.idleHeadUpdates.length; ++i) {
                              int[] var10000 = this.idleHeadUpdates;
                              var10000[i] += 3;
                         }

                         return super.attackEntityFrom(source, amount);
                    }
               }
          } else {
               return false;
          }
     }

     protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
          EntityItem entityitem = this.dropItem(Items.NETHER_STAR, 1);
          if (entityitem != null) {
               entityitem.setNoDespawn();
          }

     }

     protected void despawnEntity() {
          this.entityAge = 0;
     }

     public int getBrightnessForRender() {
          return 15728880;
     }

     public void fall(float distance, float damageMultiplier) {
     }

     public void addPotionEffect(PotionEffect potioneffectIn) {
     }

     protected void applyEntityAttributes() {
          super.applyEntityAttributes();
          this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
          this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6000000238418579D);
          this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
          this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
     }

     public float getHeadYRotation(int p_82207_1_) {
          return this.yRotationHeads[p_82207_1_];
     }

     public float getHeadXRotation(int p_82210_1_) {
          return this.xRotationHeads[p_82210_1_];
     }

     public int getInvulTime() {
          return (Integer)this.dataManager.get(INVULNERABILITY_TIME);
     }

     public void setInvulTime(int time) {
          this.dataManager.set(INVULNERABILITY_TIME, time);
     }

     public int getWatchedTargetId(int head) {
          return (Integer)this.dataManager.get(HEAD_TARGETS[head]);
     }

     public void updateWatchedTargetId(int targetOffset, int newId) {
          this.dataManager.set(HEAD_TARGETS[targetOffset], newId);
     }

     public boolean isArmored() {
          return this.getHealth() <= this.getMaxHealth() / 2.0F;
     }

     public EnumCreatureAttribute getCreatureAttribute() {
          return EnumCreatureAttribute.UNDEAD;
     }

     protected boolean canBeRidden(Entity entityIn) {
          return false;
     }

     public boolean isNonBoss() {
          return false;
     }

     public void setSwingingArms(boolean swingingArms) {
     }

     static {
          FIRST_HEAD_TARGET = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
          SECOND_HEAD_TARGET = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
          THIRD_HEAD_TARGET = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
          HEAD_TARGETS = new DataParameter[]{FIRST_HEAD_TARGET, SECOND_HEAD_TARGET, THIRD_HEAD_TARGET};
          INVULNERABILITY_TIME = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
          NOT_UNDEAD = new Predicate() {
               public boolean apply(@Nullable Entity p_apply_1_) {
                    return p_apply_1_ instanceof EntityLivingBase && ((EntityLivingBase)p_apply_1_).getCreatureAttribute() != EnumCreatureAttribute.UNDEAD && ((EntityLivingBase)p_apply_1_).func_190631_cK();
               }
          };
     }

     class AIDoNothing extends EntityAIBase {
          public AIDoNothing() {
               this.setMutexBits(7);
          }

          public boolean shouldExecute() {
               return EntityWither.this.getInvulTime() > 0;
          }
     }
}
