package net.minecraft.entity;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentFrostWalker;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.CombatRules;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.rich.Main;
import wtf.rich.client.features.impl.combat.KillAura;
import wtf.rich.client.features.impl.player.NoPush;
import wtf.rich.client.features.impl.visuals.NoRender;
import wtf.rich.client.features.impl.visuals.SwingAnimations;

public abstract class EntityLivingBase extends Entity {
     private static final Logger field_190632_a = LogManager.getLogger();
     private static final UUID SPRINTING_SPEED_BOOST_ID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
     private static final AttributeModifier SPRINTING_SPEED_BOOST;
     protected static final DataParameter HAND_STATES;
     private static final DataParameter HEALTH;
     private static final DataParameter POTION_EFFECTS;
     private static final DataParameter HIDE_PARTICLES;
     private static final DataParameter ARROW_COUNT_IN_ENTITY;
     private AbstractAttributeMap attributeMap;
     private final CombatTracker _combatTracker = new CombatTracker(this);
     private final Map activePotionsMap = Maps.newHashMap();
     private final NonNullList handInventory;
     private final NonNullList armorArray;
     public boolean isSwingInProgress;
     public EnumHand swingingHand;
     public int swingProgressInt;
     public int arrowHitTimer;
     public int hurtTime;
     public int maxHurtTime;
     public float attackedAtYaw;
     public int deathTime;
     public float prevSwingProgress;
     public float swingProgress;
     protected int ticksSinceLastSwing;
     public float prevLimbSwingAmount;
     public float limbSwingAmount;
     public float limbSwing;
     public int maxHurtResistantTime;
     public float prevCameraPitch;
     public float cameraPitch;
     public float randomUnused2;
     public float randomUnused1;
     public float renderYawOffset;
     public float prevRenderYawOffset;
     public float rotationYawHead;
     public float rotationPitchHead;
     public float prevRotationYawHead;
     public float prevRotationPitchHead;
     public float jumpMovementFactor;
     protected EntityPlayer attackingPlayer;
     protected int recentlyHit;
     protected boolean dead;
     protected int entityAge;
     protected float prevOnGroundSpeedFactor;
     protected float onGroundSpeedFactor;
     protected float movedDistance;
     protected float prevMovedDistance;
     protected float unused180;
     protected int scoreValue;
     protected float lastDamage;
     protected boolean isJumping;
     public float moveStrafing;
     public float moveForward;
     public float field_191988_bg;
     public float randomYawVelocity;
     protected int newPosRotationIncrements;
     protected double interpTargetX;
     protected double interpTargetY;
     protected double interpTargetZ;
     protected double interpTargetYaw;
     protected double interpTargetPitch;
     private boolean potionsNeedUpdate;
     private EntityLivingBase entityLivingToAttack;
     private int revengeTimer;
     private EntityLivingBase lastAttacker;
     private int lastAttackerTime;
     private float landMovementFactor;
     public int jumpTicks;
     private float absorptionAmount;
     protected ItemStack activeItemStack;
     protected int activeItemStackUseCount;
     protected int ticksElytraFlying;
     private BlockPos prevBlockpos;
     private DamageSource lastDamageSource;
     private long lastDamageStamp;

     public void onKillCommand() {
          this.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
     }

     public EntityLivingBase(World worldIn) {
          super(worldIn);
          this.handInventory = NonNullList.func_191197_a(2, ItemStack.field_190927_a);
          this.armorArray = NonNullList.func_191197_a(4, ItemStack.field_190927_a);
          this.maxHurtResistantTime = 20;
          this.jumpMovementFactor = 0.02F;
          this.potionsNeedUpdate = true;
          this.activeItemStack = ItemStack.field_190927_a;
          this.applyEntityAttributes();
          this.setHealth(this.getMaxHealth());
          this.preventEntitySpawning = true;
          this.randomUnused1 = (float)((Math.random() + 1.0D) * 0.009999999776482582D);
          this.setPosition(this.posX, this.posY, this.posZ);
          this.randomUnused2 = (float)Math.random() * 12398.0F;
          this.rotationYaw = (float)(Math.random() * 6.283185307179586D);
          this.rotationYawHead = this.rotationYaw;
          this.rotationPitchHead = this.rotationPitch;
          this.stepHeight = 0.6F;
     }

     protected void entityInit() {
          this.dataManager.register(HAND_STATES, (byte)0);
          this.dataManager.register(POTION_EFFECTS, 0);
          this.dataManager.register(HIDE_PARTICLES, false);
          this.dataManager.register(ARROW_COUNT_IN_ENTITY, 0);
          this.dataManager.register(HEALTH, 1.0F);
     }

     protected void applyEntityAttributes() {
          this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
          this.getAttributeMap().registerAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
          this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
          this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR);
          this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
     }

     protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
          if (!this.isInWater()) {
               this.handleWaterMovement();
          }

          if (!this.world.isRemote && this.fallDistance > 3.0F && onGroundIn) {
               float f = (float)MathHelper.ceil(this.fallDistance - 3.0F);
               if (state.getMaterial() != Material.AIR) {
                    double d0 = Math.min((double)(0.2F + f / 15.0F), 2.5D);
                    int i = (int)(150.0D * d0);
                    ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY, this.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(state));
               }
          }

          super.updateFallState(y, onGroundIn, state, pos);
     }

     public boolean canBreatheUnderwater() {
          return false;
     }

     public void onEntityUpdate() {
          this.prevSwingProgress = this.swingProgress;
          super.onEntityUpdate();
          this.world.theProfiler.startSection("livingEntityBaseTick");
          boolean flag = this instanceof EntityPlayer;
          if (this.isEntityAlive()) {
               if (this.isEntityInsideOpaqueBlock()) {
                    this.attackEntityFrom(DamageSource.inWall, 1.0F);
               } else if (flag && !this.world.getWorldBorder().contains(this.getEntityBoundingBox())) {
                    double d0 = this.world.getWorldBorder().getClosestDistance(this) + this.world.getWorldBorder().getDamageBuffer();
                    if (d0 < 0.0D) {
                         double d1 = this.world.getWorldBorder().getDamageAmount();
                         if (d1 > 0.0D) {
                              this.attackEntityFrom(DamageSource.inWall, (float)Math.max(1, MathHelper.floor(-d0 * d1)));
                         }
                    }
               }
          }

          if (this.isImmuneToFire() || this.world.isRemote) {
               this.extinguish();
          }

          boolean flag1 = flag && ((EntityPlayer)this).capabilities.disableDamage;
          if (this.isEntityAlive()) {
               if (!this.isInsideOfMaterial(Material.WATER)) {
                    this.setAir(300);
               } else {
                    if (!this.canBreatheUnderwater() && !this.isPotionActive(MobEffects.WATER_BREATHING) && !flag1) {
                         this.setAir(this.decreaseAirSupply(this.getAir()));
                         if (this.getAir() == -20) {
                              this.setAir(0);

                              for(int i = 0; i < 8; ++i) {
                                   float f2 = this.rand.nextFloat() - this.rand.nextFloat();
                                   float f = this.rand.nextFloat() - this.rand.nextFloat();
                                   float f1 = this.rand.nextFloat() - this.rand.nextFloat();
                                   this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)f2, this.posY + (double)f, this.posZ + (double)f1, this.motionX, this.motionY, this.motionZ);
                              }

                              this.attackEntityFrom(DamageSource.drown, 2.0F);
                         }
                    }

                    if (!this.world.isRemote && this.isRiding() && this.getRidingEntity() instanceof EntityLivingBase) {
                         this.dismountRidingEntity();
                    }
               }

               if (!this.world.isRemote) {
                    BlockPos blockpos = new BlockPos(this);
                    if (!Objects.equal(this.prevBlockpos, blockpos)) {
                         this.prevBlockpos = blockpos;
                         this.frostWalk(blockpos);
                    }
               }
          }

          if (this.isEntityAlive() && this.isWet()) {
               this.extinguish();
          }

          this.prevCameraPitch = this.cameraPitch;
          if (this.hurtTime > 0) {
               --this.hurtTime;
          }

          if (this.hurtResistantTime > 0 && !(this instanceof EntityPlayerMP)) {
               --this.hurtResistantTime;
          }

          if (this.getHealth() <= 0.0F) {
               this.onDeathUpdate();
          }

          if (this.recentlyHit > 0) {
               --this.recentlyHit;
          } else {
               this.attackingPlayer = null;
          }

          if (this.lastAttacker != null && !this.lastAttacker.isEntityAlive()) {
               this.lastAttacker = null;
          }

          if (this.entityLivingToAttack != null) {
               if (!this.entityLivingToAttack.isEntityAlive()) {
                    this.setRevengeTarget((EntityLivingBase)null);
               } else if (this.ticksExisted - this.revengeTimer > 100) {
                    this.setRevengeTarget((EntityLivingBase)null);
               }
          }

          this.updatePotionEffects();
          this.prevMovedDistance = this.movedDistance;
          this.prevRenderYawOffset = this.renderYawOffset;
          this.prevRotationYawHead = this.rotationYawHead;
          this.prevRotationPitchHead = this.rotationPitchHead;
          this.prevRotationYaw = this.rotationYaw;
          this.prevRotationPitch = this.rotationPitch;
          this.world.theProfiler.endSection();
     }

     protected void frostWalk(BlockPos pos) {
          int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FROST_WALKER, this);
          if (i > 0) {
               EnchantmentFrostWalker.freezeNearby(this, this.world, pos, i);
          }

     }

     public boolean isChild() {
          return false;
     }

     protected void onDeathUpdate() {
          ++this.deathTime;
          if (this.deathTime == 20) {
               int k;
               if (!this.world.isRemote && (this.isPlayer() || this.recentlyHit > 0 && this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot"))) {
                    k = this.getExperiencePoints(this.attackingPlayer);

                    while(k > 0) {
                         int j = EntityXPOrb.getXPSplit(k);
                         k -= j;
                         this.world.spawnEntityInWorld(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
                    }
               }

               this.setDead();

               for(k = 0; k < 20; ++k) {
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d2, d0, d1);
               }
          }

     }

     protected boolean canDropLoot() {
          return !this.isChild();
     }

     protected int decreaseAirSupply(int air) {
          int i = EnchantmentHelper.getRespirationModifier(this);
          return i > 0 && this.rand.nextInt(i + 1) > 0 ? air : air - 1;
     }

     protected int getExperiencePoints(EntityPlayer player) {
          return 0;
     }

     protected boolean isPlayer() {
          return false;
     }

     public Random getRNG() {
          return this.rand;
     }

     @Nullable
     public EntityLivingBase getAITarget() {
          return this.entityLivingToAttack;
     }

     public int getRevengeTimer() {
          return this.revengeTimer;
     }

     public void setRevengeTarget(@Nullable EntityLivingBase livingBase) {
          this.entityLivingToAttack = livingBase;
          this.revengeTimer = this.ticksExisted;
     }

     public EntityLivingBase getLastAttacker() {
          return this.lastAttacker;
     }

     public int getLastAttackerTime() {
          return this.lastAttackerTime;
     }

     public void setLastAttacker(Entity entityIn) {
          if (entityIn instanceof EntityLivingBase) {
               this.lastAttacker = (EntityLivingBase)entityIn;
          } else {
               this.lastAttacker = null;
          }

          this.lastAttackerTime = this.ticksExisted;
     }

     public int getAge() {
          return this.entityAge;
     }

     protected void playEquipSound(ItemStack stack) {
          if (!stack.func_190926_b()) {
               SoundEvent soundevent = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
               Item item = stack.getItem();
               if (item instanceof ItemArmor) {
                    soundevent = ((ItemArmor)item).getArmorMaterial().getSoundEvent();
               } else if (item == Items.ELYTRA) {
                    soundevent = SoundEvents.field_191258_p;
               }

               this.playSound(soundevent, 1.0F, 1.0F);
          }

     }

     public void writeEntityToNBT(NBTTagCompound compound) {
          compound.setFloat("Health", this.getHealth());
          compound.setShort("HurtTime", (short)this.hurtTime);
          compound.setInteger("HurtByTimestamp", this.revengeTimer);
          compound.setShort("DeathTime", (short)this.deathTime);
          compound.setFloat("AbsorptionAmount", this.getAbsorptionAmount());
          EntityEquipmentSlot[] var2 = EntityEquipmentSlot.values();
          int var3 = var2.length;

          int var4;
          EntityEquipmentSlot entityequipmentslot1;
          ItemStack itemstack1;
          for(var4 = 0; var4 < var3; ++var4) {
               entityequipmentslot1 = var2[var4];
               itemstack1 = this.getItemStackFromSlot(entityequipmentslot1);
               if (!itemstack1.func_190926_b()) {
                    this.getAttributeMap().removeAttributeModifiers(itemstack1.getAttributeModifiers(entityequipmentslot1));
               }
          }

          compound.setTag("Attributes", SharedMonsterAttributes.writeBaseAttributeMapToNBT(this.getAttributeMap()));
          var2 = EntityEquipmentSlot.values();
          var3 = var2.length;

          for(var4 = 0; var4 < var3; ++var4) {
               entityequipmentslot1 = var2[var4];
               itemstack1 = this.getItemStackFromSlot(entityequipmentslot1);
               if (!itemstack1.func_190926_b()) {
                    this.getAttributeMap().applyAttributeModifiers(itemstack1.getAttributeModifiers(entityequipmentslot1));
               }
          }

          if (!this.activePotionsMap.isEmpty()) {
               NBTTagList nbttaglist = new NBTTagList();
               Iterator var8 = this.activePotionsMap.values().iterator();

               while(var8.hasNext()) {
                    PotionEffect potioneffect = (PotionEffect)var8.next();
                    nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
               }

               compound.setTag("ActiveEffects", nbttaglist);
          }

          compound.setBoolean("FallFlying", this.isElytraFlying());
     }

     public void readEntityFromNBT(NBTTagCompound compound) {
          this.setAbsorptionAmount(compound.getFloat("AbsorptionAmount"));
          if (compound.hasKey("Attributes", 9) && this.world != null && !this.world.isRemote) {
               SharedMonsterAttributes.setAttributeModifiers(this.getAttributeMap(), compound.getTagList("Attributes", 10));
          }

          if (compound.hasKey("ActiveEffects", 9)) {
               NBTTagList nbttaglist = compound.getTagList("ActiveEffects", 10);

               for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                    NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                    PotionEffect potioneffect = PotionEffect.readCustomPotionEffectFromNBT(nbttagcompound);
                    if (potioneffect != null) {
                         this.activePotionsMap.put(potioneffect.getPotion(), potioneffect);
                    }
               }
          }

          if (compound.hasKey("Health", 99)) {
               this.setHealth(compound.getFloat("Health"));
          }

          this.hurtTime = compound.getShort("HurtTime");
          this.deathTime = compound.getShort("DeathTime");
          this.revengeTimer = compound.getInteger("HurtByTimestamp");
          if (compound.hasKey("Team", 8)) {
               String s = compound.getString("Team");
               boolean flag = this.world.getScoreboard().addPlayerToTeam(this.getCachedUniqueIdString(), s);
               if (!flag) {
                    field_190632_a.warn("Unable to add mob to team \"" + s + "\" (that team probably doesn't exist)");
               }
          }

          if (compound.getBoolean("FallFlying")) {
               this.setFlag(7, true);
          }

     }

     protected void updatePotionEffects() {
          Iterator iterator = this.activePotionsMap.keySet().iterator();

          try {
               while(iterator.hasNext()) {
                    Potion potion = (Potion)iterator.next();
                    PotionEffect potioneffect = (PotionEffect)this.activePotionsMap.get(potion);
                    if (!potioneffect.onUpdate(this)) {
                         if (!this.world.isRemote) {
                              iterator.remove();
                              this.onFinishedPotionEffect(potioneffect);
                         }
                    } else if (potioneffect.getDuration() % 600 == 0) {
                         this.onChangedPotionEffect(potioneffect, false);
                    }
               }
          } catch (ConcurrentModificationException var11) {
          }

          if (this.potionsNeedUpdate) {
               if (!this.world.isRemote) {
                    this.updatePotionMetadata();
               }

               this.potionsNeedUpdate = false;
          }

          int i = (Integer)this.dataManager.get(POTION_EFFECTS);
          boolean flag1 = (Boolean)this.dataManager.get(HIDE_PARTICLES);
          if (i > 0) {
               boolean flag;
               if (this.isInvisible()) {
                    flag = this.rand.nextInt(15) == 0;
               } else {
                    flag = this.rand.nextBoolean();
               }

               if (flag1) {
                    flag &= this.rand.nextInt(5) == 0;
               }

               if (flag && i > 0) {
                    double d0 = (double)(i >> 16 & 255) / 255.0D;
                    double d1 = (double)(i >> 8 & 255) / 255.0D;
                    double d2 = (double)(i >> 0 & 255) / 255.0D;
                    this.world.spawnParticle(flag1 ? EnumParticleTypes.SPELL_MOB_AMBIENT : EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, d0, d1, d2);
               }
          }

     }

     protected void updatePotionMetadata() {
          if (this.activePotionsMap.isEmpty()) {
               this.resetPotionEffectMetadata();
               this.setInvisible(false);
          } else {
               Collection collection = this.activePotionsMap.values();
               this.dataManager.set(HIDE_PARTICLES, areAllPotionsAmbient(collection));
               this.dataManager.set(POTION_EFFECTS, PotionUtils.getPotionColorFromEffectList(collection));
               this.setInvisible(this.isPotionActive(MobEffects.INVISIBILITY));
          }

     }

     public static boolean areAllPotionsAmbient(Collection potionEffects) {
          Iterator var1 = potionEffects.iterator();

          PotionEffect potioneffect;
          do {
               if (!var1.hasNext()) {
                    return true;
               }

               potioneffect = (PotionEffect)var1.next();
          } while(potioneffect.getIsAmbient());

          return false;
     }

     protected void resetPotionEffectMetadata() {
          this.dataManager.set(HIDE_PARTICLES, false);
          this.dataManager.set(POTION_EFFECTS, 0);
     }

     public void clearActivePotions() {
          if (!this.world.isRemote) {
               Iterator iterator = this.activePotionsMap.values().iterator();

               while(iterator.hasNext()) {
                    this.onFinishedPotionEffect((PotionEffect)iterator.next());
                    iterator.remove();
               }
          }

     }

     public Collection getActivePotionEffects() {
          return this.activePotionsMap.values();
     }

     public Map func_193076_bZ() {
          return this.activePotionsMap;
     }

     public boolean isPotionActive(Potion potionIn) {
          return this.activePotionsMap.containsKey(potionIn);
     }

     @Nullable
     public PotionEffect getActivePotionEffect(Potion potionIn) {
          return (PotionEffect)this.activePotionsMap.get(potionIn);
     }

     public void addPotionEffect(PotionEffect potioneffectIn) {
          if (this.isPotionApplicable(potioneffectIn)) {
               PotionEffect potioneffect = (PotionEffect)this.activePotionsMap.get(potioneffectIn.getPotion());
               if (potioneffect == null) {
                    this.activePotionsMap.put(potioneffectIn.getPotion(), potioneffectIn);
                    this.onNewPotionEffect(potioneffectIn);
               } else {
                    potioneffect.combine(potioneffectIn);
                    this.onChangedPotionEffect(potioneffect, true);
               }
          }

     }

     public boolean isPotionApplicable(PotionEffect potioneffectIn) {
          if (this.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
               Potion potion = potioneffectIn.getPotion();
               if (potion == MobEffects.REGENERATION || potion == MobEffects.POISON) {
                    return false;
               }
          }

          return true;
     }

     public boolean isEntityUndead() {
          return this.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD;
     }

     @Nullable
     public PotionEffect removeActivePotionEffect(@Nullable Potion potioneffectin) {
          return (PotionEffect)this.activePotionsMap.remove(potioneffectin);
     }

     public void removePotionEffect(Potion potionIn) {
          PotionEffect potioneffect = this.removeActivePotionEffect(potionIn);
          if (potioneffect != null) {
               this.onFinishedPotionEffect(potioneffect);
          }

     }

     protected void onNewPotionEffect(PotionEffect id) {
          this.potionsNeedUpdate = true;
          if (!this.world.isRemote) {
               id.getPotion().applyAttributesModifiersToEntity(this, this.getAttributeMap(), id.getAmplifier());
          }

     }

     protected void onChangedPotionEffect(PotionEffect id, boolean p_70695_2_) {
          this.potionsNeedUpdate = true;
          if (p_70695_2_ && !this.world.isRemote) {
               Potion potion = id.getPotion();
               potion.removeAttributesModifiersFromEntity(this, this.getAttributeMap(), id.getAmplifier());
               potion.applyAttributesModifiersToEntity(this, this.getAttributeMap(), id.getAmplifier());
          }

     }

     protected void onFinishedPotionEffect(PotionEffect effect) {
          this.potionsNeedUpdate = true;
          if (!this.world.isRemote) {
               effect.getPotion().removeAttributesModifiersFromEntity(this, this.getAttributeMap(), effect.getAmplifier());
          }

     }

     public void heal(float healAmount) {
          float f = this.getHealth();
          if (f > 0.0F) {
               this.setHealth(f + healAmount);
          }

     }

     public final float getHealth() {
          return (Float)this.dataManager.get(HEALTH);
     }

     public void setHealth(float health) {
          this.dataManager.set(HEALTH, MathHelper.clamp(health, 0.0F, this.getMaxHealth()));
     }

     public boolean attackEntityFrom(DamageSource source, float amount) {
          if (this.isEntityInvulnerable(source)) {
               return false;
          } else if (this.world.isRemote) {
               return false;
          } else {
               this.entityAge = 0;
               if (this.getHealth() <= 0.0F) {
                    return false;
               } else if (source.isFireDamage() && this.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
                    return false;
               } else {
                    float f = amount;
                    if ((source == DamageSource.anvil || source == DamageSource.fallingBlock) && !this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).func_190926_b()) {
                         this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).damageItem((int)(amount * 4.0F + this.rand.nextFloat() * amount * 2.0F), this);
                         amount *= 0.75F;
                    }

                    boolean flag = false;
                    if (amount > 0.0F && this.canBlockDamageSource(source)) {
                         this.damageShield(amount);
                         amount = 0.0F;
                         if (!source.isProjectile()) {
                              Entity entity = source.getSourceOfDamage();
                              if (entity instanceof EntityLivingBase) {
                                   this.func_190629_c((EntityLivingBase)entity);
                              }
                         }

                         flag = true;
                    }

                    this.limbSwingAmount = 1.5F;
                    boolean flag1 = true;
                    if ((float)this.hurtResistantTime > (float)this.maxHurtResistantTime / 2.0F) {
                         if (amount <= this.lastDamage) {
                              return false;
                         }

                         this.damageEntity(source, amount - this.lastDamage);
                         this.lastDamage = amount;
                         flag1 = false;
                    } else {
                         this.lastDamage = amount;
                         this.hurtResistantTime = this.maxHurtResistantTime;
                         this.damageEntity(source, amount);
                         this.maxHurtTime = 10;
                         this.hurtTime = this.maxHurtTime;
                    }

                    this.attackedAtYaw = 0.0F;
                    Entity entity1 = source.getEntity();
                    if (entity1 != null) {
                         if (entity1 instanceof EntityLivingBase) {
                              this.setRevengeTarget((EntityLivingBase)entity1);
                         }

                         if (entity1 instanceof EntityPlayer) {
                              this.recentlyHit = 100;
                              this.attackingPlayer = (EntityPlayer)entity1;
                         } else if (entity1 instanceof EntityWolf) {
                              EntityWolf entitywolf = (EntityWolf)entity1;
                              if (entitywolf.isTamed()) {
                                   this.recentlyHit = 100;
                                   this.attackingPlayer = null;
                              }
                         }
                    }

                    if (flag1) {
                         if (flag) {
                              this.world.setEntityState(this, (byte)29);
                         } else if (source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage()) {
                              this.world.setEntityState(this, (byte)33);
                         } else {
                              byte b0;
                              if (source == DamageSource.drown) {
                                   b0 = 36;
                              } else if (source.isFireDamage()) {
                                   b0 = 37;
                              } else {
                                   b0 = 2;
                              }

                              this.world.setEntityState(this, b0);
                         }

                         if (source != DamageSource.drown && (!flag || amount > 0.0F)) {
                              this.setBeenAttacked();
                         }

                         if (entity1 != null) {
                              double d1 = entity1.posX - this.posX;

                              double d0;
                              for(d0 = entity1.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                                   d1 = (Math.random() - Math.random()) * 0.01D;
                              }

                              this.attackedAtYaw = (float)(MathHelper.atan2(d0, d1) * 57.29577951308232D - (double)this.rotationYaw);
                              this.knockBack(entity1, 0.4F, d1, d0);
                         } else {
                              this.attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
                         }
                    }

                    if (this.getHealth() <= 0.0F) {
                         if (!this.func_190628_d(source)) {
                              SoundEvent soundevent = this.getDeathSound();
                              if (flag1 && soundevent != null) {
                                   this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
                              }

                              this.onDeath(source);
                         }
                    } else if (flag1) {
                         this.playHurtSound(source);
                    }

                    boolean flag2 = !flag || amount > 0.0F;
                    if (flag2) {
                         this.lastDamageSource = source;
                         this.lastDamageStamp = this.world.getTotalWorldTime();
                    }

                    if (this instanceof EntityPlayerMP) {
                         CriteriaTriggers.field_192128_h.func_192200_a((EntityPlayerMP)this, source, f, amount, flag);
                    }

                    if (entity1 instanceof EntityPlayerMP) {
                         CriteriaTriggers.field_192127_g.func_192220_a((EntityPlayerMP)entity1, this, source, f, amount, flag);
                    }

                    return flag2;
               }
          }
     }

     protected void func_190629_c(EntityLivingBase p_190629_1_) {
          p_190629_1_.knockBack(this, 0.5F, this.posX - p_190629_1_.posX, this.posZ - p_190629_1_.posZ);
     }

     private boolean func_190628_d(DamageSource p_190628_1_) {
          if (p_190628_1_.canHarmInCreative()) {
               return false;
          } else {
               ItemStack itemstack = null;
               EnumHand[] var3 = EnumHand.values();
               int var4 = var3.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                    EnumHand enumhand = var3[var5];
                    ItemStack itemstack1 = this.getHeldItem(enumhand);
                    if (itemstack1.getItem() == Items.TOTEM_OF_UNDYING) {
                         itemstack = itemstack1.copy();
                         itemstack1.func_190918_g(1);
                         break;
                    }
               }

               if (itemstack != null) {
                    if (this instanceof EntityPlayerMP) {
                         EntityPlayerMP entityplayermp = (EntityPlayerMP)this;
                         entityplayermp.addStat(StatList.getObjectUseStats(Items.TOTEM_OF_UNDYING));
                         CriteriaTriggers.field_193130_A.func_193187_a(entityplayermp, itemstack);
                    }

                    this.setHealth(1.0F);
                    this.clearActivePotions();
                    this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 900, 1));
                    this.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 100, 1));
                    this.world.setEntityState(this, (byte)35);
               }

               return itemstack != null;
          }
     }

     @Nullable
     public DamageSource getLastDamageSource() {
          if (this.world.getTotalWorldTime() - this.lastDamageStamp > 40L) {
               this.lastDamageSource = null;
          }

          return this.lastDamageSource;
     }

     protected void playHurtSound(DamageSource source) {
          SoundEvent soundevent = this.getHurtSound(source);
          if (soundevent != null) {
               this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
          }

     }

     private boolean canBlockDamageSource(DamageSource damageSourceIn) {
          if (!damageSourceIn.isUnblockable() && this.isActiveItemStackBlocking(2)) {
               Vec3d vec3d = damageSourceIn.getDamageLocation();
               if (vec3d != null) {
                    Vec3d vec3d1 = this.getLook(1.0F);
                    Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
                    vec3d2 = new Vec3d(vec3d2.xCoord, 0.0D, vec3d2.zCoord);
                    if (vec3d2.dotProduct(vec3d1) < 0.0D) {
                         return true;
                    }
               }
          }

          return false;
     }

     public void renderBrokenItemStack(ItemStack stack) {
          this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);

          for(int i = 0; i < 5; ++i) {
               Vec3d vec3d = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
               vec3d = vec3d.rotatePitch(-this.rotationPitch * 0.017453292F);
               vec3d = vec3d.rotateYaw(-this.rotationYaw * 0.017453292F);
               double d0 = (double)(-this.rand.nextFloat()) * 0.6D - 0.3D;
               Vec3d vec3d1 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
               vec3d1 = vec3d1.rotatePitch(-this.rotationPitch * 0.017453292F);
               vec3d1 = vec3d1.rotateYaw(-this.rotationYaw * 0.017453292F);
               vec3d1 = vec3d1.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
               this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, vec3d.xCoord, vec3d.yCoord + 0.05D, vec3d.zCoord, Item.getIdFromItem(stack.getItem()));
          }

     }

     public void onDeath(DamageSource cause) {
          if (!this.dead) {
               Entity entity = cause.getEntity();
               EntityLivingBase entitylivingbase = this.getAttackingEntity();
               if (this.scoreValue >= 0 && entitylivingbase != null) {
                    entitylivingbase.func_191956_a(this, this.scoreValue, cause);
               }

               if (entity != null) {
                    entity.onKillEntity(this);
               }

               this.dead = true;
               this.getCombatTracker().reset();
               if (!this.world.isRemote) {
                    int i = 0;
                    if (entity instanceof EntityPlayer) {
                         i = EnchantmentHelper.getLootingModifier((EntityLivingBase)entity);
                    }

                    if (this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot")) {
                         boolean flag = this.recentlyHit > 0;
                         this.dropLoot(flag, i, cause);
                    }
               }

               this.world.setEntityState(this, (byte)3);
          }

     }

     protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
          this.dropFewItems(wasRecentlyHit, lootingModifier);
          this.dropEquipment(wasRecentlyHit, lootingModifier);
     }

     protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
     }

     public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
          if (this.rand.nextDouble() >= this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
               this.isAirBorne = true;
               float f = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio);
               this.motionX /= 2.0D;
               this.motionZ /= 2.0D;
               this.motionX -= xRatio / (double)f * (double)strength;
               this.motionZ -= zRatio / (double)f * (double)strength;
               if (this.onGround) {
                    this.motionY /= 2.0D;
                    this.motionY += (double)strength;
                    if (this.motionY > 0.4000000059604645D) {
                         this.motionY = 0.4000000059604645D;
                    }
               }
          }

     }

     @Nullable
     protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
          return SoundEvents.ENTITY_GENERIC_HURT;
     }

     @Nullable
     protected SoundEvent getDeathSound() {
          return SoundEvents.ENTITY_GENERIC_DEATH;
     }

     protected SoundEvent getFallSound(int heightIn) {
          return heightIn > 4 ? SoundEvents.ENTITY_GENERIC_BIG_FALL : SoundEvents.ENTITY_GENERIC_SMALL_FALL;
     }

     protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
     }

     public boolean isOnLadder() {
          int i = MathHelper.floor(this.posX);
          int j = MathHelper.floor(this.getEntityBoundingBox().minY);
          int k = MathHelper.floor(this.posZ);
          if (this instanceof EntityPlayer && ((EntityPlayer)this).isSpectator()) {
               return false;
          } else {
               BlockPos blockpos = new BlockPos(i, j, k);
               IBlockState iblockstate = this.world.getBlockState(blockpos);
               Block block = iblockstate.getBlock();
               if (block != Blocks.LADDER && block != Blocks.VINE) {
                    return block instanceof BlockTrapDoor && this.canGoThroughtTrapDoorOnLadder(blockpos, iblockstate);
               } else {
                    return true;
               }
          }
     }

     private boolean canGoThroughtTrapDoorOnLadder(BlockPos pos, IBlockState state) {
          if ((Boolean)state.getValue(BlockTrapDoor.OPEN)) {
               IBlockState iblockstate = this.world.getBlockState(pos.down());
               if (iblockstate.getBlock() == Blocks.LADDER && iblockstate.getValue(BlockLadder.FACING) == state.getValue(BlockTrapDoor.FACING)) {
                    return true;
               }
          }

          return false;
     }

     public boolean isEntityAlive() {
          return !this.isDead && this.getHealth() > 0.0F;
     }

     public void fall(float distance, float damageMultiplier) {
          super.fall(distance, damageMultiplier);
          PotionEffect potioneffect = this.getActivePotionEffect(MobEffects.JUMP_BOOST);
          float f = potioneffect == null ? 0.0F : (float)(potioneffect.getAmplifier() + 1);
          int i = MathHelper.ceil((distance - 3.0F - f) * damageMultiplier);
          if (i > 0) {
               this.playSound(this.getFallSound(i), 1.0F, 1.0F);
               this.attackEntityFrom(DamageSource.fall, (float)i);
               int j = MathHelper.floor(this.posX);
               int k = MathHelper.floor(this.posY - 0.20000000298023224D);
               int l = MathHelper.floor(this.posZ);
               IBlockState iblockstate = this.world.getBlockState(new BlockPos(j, k, l));
               if (iblockstate.getMaterial() != Material.AIR) {
                    SoundType soundtype = iblockstate.getBlock().getSoundType();
                    this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
               }
          }

     }

     public void performHurtAnimation() {
          this.maxHurtTime = 10;
          this.hurtTime = this.maxHurtTime;
          this.attackedAtYaw = 0.0F;
     }

     public int getTotalArmorValue() {
          IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.ARMOR);
          return MathHelper.floor(iattributeinstance.getAttributeValue());
     }

     protected void damageArmor(float damage) {
     }

     protected void damageShield(float damage) {
     }

     protected float applyArmorCalculations(DamageSource source, float damage) {
          if (!source.isUnblockable()) {
               this.damageArmor(damage);
               damage = CombatRules.getDamageAfterAbsorb(damage, (float)this.getTotalArmorValue(), (float)this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
          }

          return damage;
     }

     protected float applyPotionDamageCalculations(DamageSource source, float damage) {
          if (source.isDamageAbsolute()) {
               return damage;
          } else {
               int k;
               if (this.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.outOfWorld) {
                    k = (this.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    int j = 25 - k;
                    float f = damage * (float)j;
                    damage = f / 25.0F;
               }

               if (damage <= 0.0F) {
                    return 0.0F;
               } else {
                    k = EnchantmentHelper.getEnchantmentModifierDamage(this.getArmorInventoryList(), source);
                    if (k > 0) {
                         damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);
                    }

                    return damage;
               }
          }
     }

     protected void damageEntity(DamageSource damageSrc, float damageAmount) {
          if (!this.isEntityInvulnerable(damageSrc)) {
               damageAmount = this.applyArmorCalculations(damageSrc, damageAmount);
               damageAmount = this.applyPotionDamageCalculations(damageSrc, damageAmount);
               float f = damageAmount;
               damageAmount = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0F);
               this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - damageAmount));
               if (damageAmount != 0.0F) {
                    float f1 = this.getHealth();
                    this.setHealth(f1 - damageAmount);
                    this.getCombatTracker().trackDamage(damageSrc, f1, damageAmount);
                    this.setAbsorptionAmount(this.getAbsorptionAmount() - damageAmount);
               }
          }

     }

     public CombatTracker getCombatTracker() {
          return this._combatTracker;
     }

     @Nullable
     public EntityLivingBase getAttackingEntity() {
          if (this._combatTracker.getBestAttacker() != null) {
               return this._combatTracker.getBestAttacker();
          } else if (this.attackingPlayer != null) {
               return this.attackingPlayer;
          } else {
               return this.entityLivingToAttack != null ? this.entityLivingToAttack : null;
          }
     }

     public final float getMaxHealth() {
          return (float)this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
     }

     public final int getArrowCountInEntity() {
          return (Integer)this.dataManager.get(ARROW_COUNT_IN_ENTITY);
     }

     public final void setArrowCountInEntity(int count) {
          if (!Main.instance.featureDirector.getFeatureByClass(NoRender.class).isToggled() || !NoRender.noArrow.getBoolValue()) {
               this.dataManager.set(ARROW_COUNT_IN_ENTITY, count);
          }

     }

     private int getArmSwingAnimationEnd() {
          if (Main.instance.featureDirector.getFeatureByClass(SwingAnimations.class).isToggled()) {
               int speed = (int)SwingAnimations.swingSpeed.getNumberValue();
               return this.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : speed;
          } else {
               return this.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
          }
     }

     public void swingArm(EnumHand hand) {
          if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
               this.swingProgressInt = -1;
               this.isSwingInProgress = true;
               this.swingingHand = hand;
               if (this.world instanceof WorldServer) {
                    ((WorldServer)this.world).getEntityTracker().sendToAllTrackingEntity(this, new SPacketAnimation(this, hand == EnumHand.MAIN_HAND ? 0 : 3));
               }
          }

     }

     public void handleStatusUpdate(byte id) {
          boolean flag = id == 33;
          boolean flag1 = id == 36;
          boolean flag2 = id == 37;
          if (id != 2 && !flag && !flag1 && !flag2) {
               if (id == 3) {
                    SoundEvent soundevent1 = this.getDeathSound();
                    if (soundevent1 != null) {
                         this.playSound(soundevent1, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                    }

                    this.setHealth(0.0F);
                    this.onDeath(DamageSource.generic);
               } else if (id == 30) {
                    this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
               } else if (id == 29) {
                    this.playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1.0F, 0.8F + this.world.rand.nextFloat() * 0.4F);
               } else {
                    super.handleStatusUpdate(id);
               }
          } else {
               this.limbSwingAmount = 1.5F;
               this.hurtResistantTime = this.maxHurtResistantTime;
               this.maxHurtTime = 10;
               this.hurtTime = this.maxHurtTime;
               this.attackedAtYaw = 0.0F;
               if (flag) {
                    this.playSound(SoundEvents.ENCHANT_THORNS_HIT, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
               }

               DamageSource damagesource;
               if (flag2) {
                    damagesource = DamageSource.onFire;
               } else if (flag1) {
                    damagesource = DamageSource.drown;
               } else {
                    damagesource = DamageSource.generic;
               }

               SoundEvent soundevent = this.getHurtSound(damagesource);
               if (soundevent != null) {
                    this.playSound(soundevent, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
               }

               this.attackEntityFrom(DamageSource.generic, 0.0F);
          }

     }

     protected void kill() {
          this.attackEntityFrom(DamageSource.outOfWorld, 4.0F);
     }

     protected void updateArmSwingProgress() {
          int i = this.getArmSwingAnimationEnd();
          if (this.isSwingInProgress) {
               ++this.swingProgressInt;
               if (this.swingProgressInt >= i) {
                    this.swingProgressInt = 0;
                    this.isSwingInProgress = false;
               }
          } else {
               this.swingProgressInt = 0;
          }

          this.swingProgress = (float)this.swingProgressInt / (float)i;
     }

     public IAttributeInstance getEntityAttribute(IAttribute attribute) {
          return this.getAttributeMap().getAttributeInstance(attribute);
     }

     public AbstractAttributeMap getAttributeMap() {
          if (this.attributeMap == null) {
               this.attributeMap = new AttributeMap();
          }

          return this.attributeMap;
     }

     public EnumCreatureAttribute getCreatureAttribute() {
          return EnumCreatureAttribute.UNDEFINED;
     }

     public ItemStack getHeldItemMainhand() {
          return this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
     }

     public ItemStack getHeldItemOffhand() {
          return this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
     }

     public ItemStack getHeldItem(EnumHand hand) {
          if (hand == EnumHand.MAIN_HAND) {
               return this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
          } else if (hand == EnumHand.OFF_HAND) {
               return this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
          } else {
               throw new IllegalArgumentException("Invalid hand " + hand);
          }
     }

     public void setHeldItem(EnumHand hand, ItemStack stack) {
          if (hand == EnumHand.MAIN_HAND) {
               this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
          } else {
               if (hand != EnumHand.OFF_HAND) {
                    throw new IllegalArgumentException("Invalid hand " + hand);
               }

               this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
          }

     }

     public boolean func_190630_a(EntityEquipmentSlot p_190630_1_) {
          return !this.getItemStackFromSlot(p_190630_1_).func_190926_b();
     }

     public abstract Iterable getArmorInventoryList();

     public abstract ItemStack getItemStackFromSlot(EntityEquipmentSlot var1);

     public abstract void setItemStackToSlot(EntityEquipmentSlot var1, ItemStack var2);

     public void setSprinting(boolean sprinting) {
          super.setSprinting(sprinting);
          IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
          if (iattributeinstance.getModifier(SPRINTING_SPEED_BOOST_ID) != null) {
               iattributeinstance.removeModifier(SPRINTING_SPEED_BOOST);
          }

          if (sprinting) {
               iattributeinstance.applyModifier(SPRINTING_SPEED_BOOST);
          }

     }

     protected float getSoundVolume() {
          return 1.0F;
     }

     protected float getSoundPitch() {
          return this.isChild() ? (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F : (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F;
     }

     protected boolean isMovementBlocked() {
          return this.getHealth() <= 0.0F;
     }

     public void dismountEntity(Entity entityIn) {
          double d1;
          double d4;
          if (!(entityIn instanceof EntityBoat) && !(entityIn instanceof AbstractHorse)) {
               d1 = entityIn.posX;
               double d13 = entityIn.getEntityBoundingBox().minY + (double)entityIn.height;
               double d14 = entityIn.posZ;
               EnumFacing enumfacing1 = entityIn.getAdjustedHorizontalFacing();
               if (enumfacing1 != null) {
                    EnumFacing enumfacing = enumfacing1.rotateY();
                    int[][] aint1 = new int[][]{{0, 1}, {0, -1}, {-1, 1}, {-1, -1}, {1, 1}, {1, -1}, {-1, 0}, {1, 0}, {0, 1}};
                    d4 = Math.floor(this.posX) + 0.5D;
                    double d6 = Math.floor(this.posZ) + 0.5D;
                    double d7 = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX;
                    double d8 = this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ;
                    AxisAlignedBB axisalignedbb = new AxisAlignedBB(d4 - d7 / 2.0D, entityIn.getEntityBoundingBox().minY, d6 - d8 / 2.0D, d4 + d7 / 2.0D, Math.floor(entityIn.getEntityBoundingBox().minY) + (double)this.height, d6 + d8 / 2.0D);
                    int[][] var20 = aint1;
                    int var21 = aint1.length;

                    for(int var22 = 0; var22 < var21; ++var22) {
                         int[] aint = var20[var22];
                         double d9 = (double)(enumfacing1.getFrontOffsetX() * aint[0] + enumfacing.getFrontOffsetX() * aint[1]);
                         double d10 = (double)(enumfacing1.getFrontOffsetZ() * aint[0] + enumfacing.getFrontOffsetZ() * aint[1]);
                         double d11 = d4 + d9;
                         double d12 = d6 + d10;
                         AxisAlignedBB axisalignedbb1 = axisalignedbb.offset(d9, 0.0D, d10);
                         if (!this.world.collidesWithAnyBlock(axisalignedbb1)) {
                              if (this.world.getBlockState(new BlockPos(d11, this.posY, d12)).isFullyOpaque()) {
                                   this.setPositionAndUpdate(d11, this.posY + 1.0D, d12);
                                   return;
                              }

                              BlockPos blockpos = new BlockPos(d11, this.posY - 1.0D, d12);
                              if (this.world.getBlockState(blockpos).isFullyOpaque() || this.world.getBlockState(blockpos).getMaterial() == Material.WATER) {
                                   d1 = d11;
                                   d13 = this.posY + 1.0D;
                                   d14 = d12;
                              }
                         } else if (!this.world.collidesWithAnyBlock(axisalignedbb1.offset(0.0D, 1.0D, 0.0D)) && this.world.getBlockState(new BlockPos(d11, this.posY + 1.0D, d12)).isFullyOpaque()) {
                              d1 = d11;
                              d13 = this.posY + 2.0D;
                              d14 = d12;
                         }
                    }
               }

               this.setPositionAndUpdate(d1, d13, d14);
          } else {
               d1 = (double)(this.width / 2.0F + entityIn.width / 2.0F) + 0.4D;
               float f;
               if (entityIn instanceof EntityBoat) {
                    f = 0.0F;
               } else {
                    f = 1.5707964F * (float)(this.getPrimaryHand() == EnumHandSide.RIGHT ? -1 : 1);
               }

               float f1 = -MathHelper.sin(-this.rotationYaw * 0.017453292F - 3.1415927F + f);
               float f2 = -MathHelper.cos(-this.rotationYaw * 0.017453292F - 3.1415927F + f);
               double d2 = Math.abs(f1) > Math.abs(f2) ? d1 / (double)Math.abs(f1) : d1 / (double)Math.abs(f2);
               double d3 = this.posX + (double)f1 * d2;
               d4 = this.posZ + (double)f2 * d2;
               this.setPosition(d3, entityIn.posY + (double)entityIn.height + 0.001D, d4);
               if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
                    this.setPosition(d3, entityIn.posY + (double)entityIn.height + 1.001D, d4);
                    if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
                         this.setPosition(entityIn.posX, entityIn.posY + (double)this.height + 0.001D, entityIn.posZ);
                    }
               }
          }

     }

     public boolean getAlwaysRenderNameTagForRender() {
          return this.getAlwaysRenderNameTag();
     }

     protected float getJumpUpwardsMotion() {
          return 0.42F;
     }

     protected void jump() {
          this.motionY = (double)this.getJumpUpwardsMotion();
          if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
               this.motionY += (double)((float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
          }

          if (this.isSprinting()) {
               float f = this.rotationYaw * 0.017453292F;
               this.motionX -= (double)(MathHelper.sin(f) * 0.2F);
               this.motionZ += (double)(MathHelper.cos(f) * 0.2F);
          }

          this.isAirBorne = true;
     }

     protected void handleJumpWater() {
          this.motionY += 0.03999999910593033D;
     }

     protected void handleJumpLava() {
          this.motionY += 0.03999999910593033D;
     }

     protected float getWaterSlowDown() {
          return 0.8F;
     }

     public void func_191986_a(float p_191986_1_, float p_191986_2_, float p_191986_3_) {
          double d0;
          double d6;
          double d8;
          if (this.isServerWorld() || this.canPassengerSteer()) {
               float f8;
               float f7;
               float f9;
               if (this.isInWater() && (!(this instanceof EntityPlayer) || !((EntityPlayer)this).capabilities.isFlying)) {
                    d0 = this.posY;
                    f7 = this.getWaterSlowDown();
                    f8 = 0.02F;
                    f9 = (float)EnchantmentHelper.getDepthStriderModifier(this);
                    if (f9 > 3.0F) {
                         f9 = 3.0F;
                    }

                    if (!this.onGround) {
                         f9 *= 0.5F;
                    }

                    if (f9 > 0.0F) {
                         f7 += (0.54600006F - f7) * f9 / 3.0F;
                         f8 += (this.getAIMoveSpeed() - f8) * f9 / 3.0F;
                    }

                    this.moveFlying(p_191986_1_, p_191986_2_, p_191986_3_, f8);
                    this.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                    this.motionX *= (double)f7;
                    this.motionY *= 0.800000011920929D;
                    this.motionZ *= (double)f7;
                    if (!this.hasNoGravity()) {
                         this.motionY -= 0.02D;
                    }

                    if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ)) {
                         this.motionY = 0.30000001192092896D;
                    }
               } else if (!this.isInLava() || this instanceof EntityPlayer && ((EntityPlayer)this).capabilities.isFlying) {
                    if (this.isElytraFlying()) {
                         if (this.motionY > -0.5D) {
                              this.fallDistance = 1.0F;
                         }

                         Vec3d vec3d = this.getLookVec();
                         float f = this.rotationPitch * 0.017453292F;
                         d6 = Math.sqrt(vec3d.xCoord * vec3d.xCoord + vec3d.zCoord * vec3d.zCoord);
                         d8 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                         double d1 = vec3d.lengthVector();
                         float f4 = MathHelper.cos(f);
                         f4 = (float)((double)f4 * (double)f4 * Math.min(1.0D, d1 / 0.4D));
                         this.motionY += -0.08D + (double)f4 * 0.06D;
                         double d11;
                         if (this.motionY < 0.0D && d6 > 0.0D) {
                              d11 = this.motionY * -0.1D * (double)f4;
                              this.motionY += d11;
                              this.motionX += vec3d.xCoord * d11 / d6;
                              this.motionZ += vec3d.zCoord * d11 / d6;
                         }

                         if (f < 0.0F) {
                              d11 = d8 * (double)(-MathHelper.sin(f)) * 0.04D;
                              this.motionY += d11 * 3.2D;
                              this.motionX -= vec3d.xCoord * d11 / d6;
                              this.motionZ -= vec3d.zCoord * d11 / d6;
                         }

                         if (d6 > 0.0D) {
                              this.motionX += (vec3d.xCoord / d6 * d8 - this.motionX) * 0.1D;
                              this.motionZ += (vec3d.zCoord / d6 * d8 - this.motionZ) * 0.1D;
                         }

                         this.motionX *= 0.9900000095367432D;
                         this.motionY *= 0.9800000190734863D;
                         this.motionZ *= 0.9900000095367432D;
                         this.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                         if (this.isCollidedHorizontally && !this.world.isRemote) {
                              d11 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                              double d3 = d8 - d11;
                              float f5 = (float)(d3 * 10.0D - 3.0D);
                              if (f5 > 0.0F) {
                                   this.playSound(this.getFallSound((int)f5), 1.0F, 1.0F);
                                   this.attackEntityFrom(DamageSource.flyIntoWall, f5);
                              }
                         }

                         if (this.onGround && !this.world.isRemote) {
                              this.setFlag(7, false);
                         }
                    } else {
                         float f6 = 0.91F;
                         BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);
                         if (this.onGround) {
                              f6 = this.world.getBlockState(blockpos$pooledmutableblockpos).getBlock().slipperiness * 0.91F;
                         }

                         f7 = 0.16277136F / (f6 * f6 * f6);
                         if (this.onGround) {
                              f8 = this.getAIMoveSpeed() * f7;
                         } else {
                              f8 = this.jumpMovementFactor;
                         }

                         this.moveFlying(p_191986_1_, p_191986_2_, p_191986_3_, f8);
                         f6 = 0.91F;
                         if (this.onGround) {
                              f6 = this.world.getBlockState(blockpos$pooledmutableblockpos.setPos(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ)).getBlock().slipperiness * 0.91F;
                         }

                         if (this.isOnLadder()) {
                              f9 = 0.15F;
                              this.motionX = MathHelper.clamp(this.motionX, -0.15000000596046448D, 0.15000000596046448D);
                              this.motionZ = MathHelper.clamp(this.motionZ, -0.15000000596046448D, 0.15000000596046448D);
                              this.fallDistance = 0.0F;
                              if (this.motionY < -0.15D) {
                                   this.motionY = -0.15D;
                              }

                              boolean flag = this.isSneaking() && this instanceof EntityPlayer;
                              if (flag && this.motionY < 0.0D) {
                                   this.motionY = 0.0D;
                              }
                         }

                         this.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                         if (this.isCollidedHorizontally && this.isOnLadder()) {
                              this.motionY = 0.2D;
                         }

                         if (this.isPotionActive(MobEffects.LEVITATION)) {
                              this.motionY += (0.05D * (double)(this.getActivePotionEffect(MobEffects.LEVITATION).getAmplifier() + 1) - this.motionY) * 0.2D;
                         } else {
                              blockpos$pooledmutableblockpos.setPos(this.posX, 0.0D, this.posZ);
                              if (!this.world.isRemote || this.world.isBlockLoaded(blockpos$pooledmutableblockpos) && this.world.getChunkFromBlockCoords(blockpos$pooledmutableblockpos).isLoaded()) {
                                   if (!this.hasNoGravity()) {
                                        this.motionY -= 0.08D;
                                   }
                              } else if (this.posY > 0.0D) {
                                   this.motionY = -0.1D;
                              } else {
                                   this.motionY = 0.0D;
                              }
                         }

                         this.motionY *= 0.9800000190734863D;
                         this.motionX *= (double)f6;
                         this.motionZ *= (double)f6;
                         blockpos$pooledmutableblockpos.release();
                    }
               } else {
                    d0 = this.posY;
                    this.moveFlying(p_191986_1_, p_191986_2_, p_191986_3_, 0.02F);
                    this.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                    this.motionX *= 0.5D;
                    this.motionY *= 0.5D;
                    this.motionZ *= 0.5D;
                    if (!this.hasNoGravity()) {
                         this.motionY -= 0.02D;
                    }

                    if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ)) {
                         this.motionY = 0.30000001192092896D;
                    }
               }
          }

          this.prevLimbSwingAmount = this.limbSwingAmount;
          d0 = this.posX - this.prevPosX;
          d6 = this.posZ - this.prevPosZ;
          d8 = this instanceof net.minecraft.entity.passive.EntityFlying ? this.posY - this.prevPosY : 0.0D;
          float f10 = MathHelper.sqrt(d0 * d0 + d8 * d8 + d6 * d6) * 4.0F;
          if (f10 > 1.0F) {
               f10 = 1.0F;
          }

          this.limbSwingAmount += (f10 - this.limbSwingAmount) * 0.4F;
          this.limbSwing += this.limbSwingAmount;
     }

     public float getAIMoveSpeed() {
          return this.landMovementFactor;
     }

     public void setAIMoveSpeed(float speedIn) {
          this.landMovementFactor = speedIn;
     }

     public boolean attackEntityAsMob(Entity entityIn) {
          this.setLastAttacker(entityIn);
          return false;
     }

     public boolean isPlayerSleeping() {
          return false;
     }

     public void onUpdate() {
          super.onUpdate();
          this.updateActiveHand();
          if (!this.world.isRemote) {
               int i = this.getArrowCountInEntity();
               if (i > 0) {
                    if (this.arrowHitTimer <= 0) {
                         this.arrowHitTimer = 20 * (30 - i);
                    }

                    --this.arrowHitTimer;
                    if (this.arrowHitTimer <= 0) {
                         this.setArrowCountInEntity(i - 1);
                    }
               }

               EntityEquipmentSlot[] var2 = EntityEquipmentSlot.values();
               int var3 = var2.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                    EntityEquipmentSlot entityequipmentslot = var2[var4];
                    ItemStack itemstack;
                    switch(entityequipmentslot.getSlotType()) {
                    case HAND:
                         itemstack = (ItemStack)this.handInventory.get(entityequipmentslot.getIndex());
                         break;
                    case ARMOR:
                         itemstack = (ItemStack)this.armorArray.get(entityequipmentslot.getIndex());
                         break;
                    default:
                         continue;
                    }

                    ItemStack itemstack1 = this.getItemStackFromSlot(entityequipmentslot);
                    if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                         ((WorldServer)this.world).getEntityTracker().sendToAllTrackingEntity(this, new SPacketEntityEquipment(this.getEntityId(), entityequipmentslot, itemstack1));
                         if (!itemstack.func_190926_b()) {
                              this.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(entityequipmentslot));
                         }

                         if (!itemstack1.func_190926_b()) {
                              this.getAttributeMap().applyAttributeModifiers(itemstack1.getAttributeModifiers(entityequipmentslot));
                         }

                         switch(entityequipmentslot.getSlotType()) {
                         case HAND:
                              this.handInventory.set(entityequipmentslot.getIndex(), itemstack1.func_190926_b() ? ItemStack.field_190927_a : itemstack1.copy());
                              break;
                         case ARMOR:
                              this.armorArray.set(entityequipmentslot.getIndex(), itemstack1.func_190926_b() ? ItemStack.field_190927_a : itemstack1.copy());
                         }
                    }
               }

               if (this.ticksExisted % 20 == 0) {
                    this.getCombatTracker().reset();
               }

               if (!this.glowing) {
                    boolean flag = this.isPotionActive(MobEffects.GLOWING);
                    if (this.getFlag(6) != flag) {
                         this.setFlag(6, flag);
                    }
               }
          }

          this.onLivingUpdate();
          double d0 = this.posX - this.prevPosX;
          double d1 = this.posZ - this.prevPosZ;
          float f3 = (float)(d0 * d0 + d1 * d1);
          float f4 = this.renderYawOffset;
          float f5 = 0.0F;
          this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
          float f = 0.0F;
          if (f3 > 0.0025000002F) {
               f = 1.0F;
               f5 = (float)Math.sqrt((double)f3) * 3.0F;
               float f1 = (float)MathHelper.atan2(d1, d0) * 57.295776F - 90.0F;
               float f2 = MathHelper.abs(MathHelper.wrapDegrees(this.rotationYaw) - f1);
               if (95.0F < f2 && f2 < 265.0F) {
                    f4 = f1 - 180.0F;
               } else {
                    f4 = f1;
               }
          }

          if (this.swingProgress > 0.0F) {
               f4 = this.rotationYaw;
          }

          if (!this.onGround) {
               f = 0.0F;
          }

          this.onGroundSpeedFactor += (f - this.onGroundSpeedFactor) * 0.3F;
          this.world.theProfiler.startSection("headTurn");
          f5 = this.updateDistance(f4, f5);
          this.world.theProfiler.endSection();
          this.world.theProfiler.startSection("rangeChecks");

          while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
               this.prevRotationYaw -= 360.0F;
          }

          while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
               this.prevRotationYaw += 360.0F;
          }

          while(this.renderYawOffset - this.prevRenderYawOffset < -180.0F) {
               this.prevRenderYawOffset -= 360.0F;
          }

          while(this.renderYawOffset - this.prevRenderYawOffset >= 180.0F) {
               this.prevRenderYawOffset += 360.0F;
          }

          while(this.rotationPitch - this.prevRotationPitch < -180.0F) {
               this.prevRotationPitch -= 360.0F;
          }

          while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
               this.prevRotationPitch += 360.0F;
          }

          while(this.rotationYawHead - this.prevRotationYawHead < -180.0F) {
               this.prevRotationYawHead -= 360.0F;
          }

          while(this.rotationYawHead - this.prevRotationYawHead >= 180.0F) {
               this.prevRotationYawHead += 360.0F;
          }

          while(this.rotationPitchHead - this.prevRotationPitchHead < -180.0F) {
               this.prevRotationPitchHead -= 360.0F;
          }

          while(this.rotationPitchHead - this.prevRotationPitchHead >= 180.0F) {
               this.prevRotationPitchHead += 360.0F;
          }

          this.world.theProfiler.endSection();
          this.movedDistance += f5;
          if (this.isElytraFlying()) {
               ++this.ticksElytraFlying;
          } else {
               this.ticksElytraFlying = 0;
          }

     }

     protected float updateDistance(float p_110146_1_, float p_110146_2_) {
          float f = MathHelper.wrapDegrees(p_110146_1_ - this.renderYawOffset);
          this.renderYawOffset += f * 0.3F;
          float f1 = MathHelper.wrapDegrees(this.rotationYaw - this.renderYawOffset);
          boolean flag = f1 < -90.0F || f1 >= 90.0F;
          if (f1 < -75.0F) {
               f1 = -75.0F;
          }

          if (f1 >= 75.0F) {
               f1 = 75.0F;
          }

          this.renderYawOffset = this.rotationYaw - f1;
          if (f1 * f1 > 2500.0F) {
               this.renderYawOffset += f1 * 0.2F;
          }

          if (flag) {
               p_110146_2_ *= -1.0F;
          }

          return p_110146_2_;
     }

     public void onLivingUpdate() {
          if (this.jumpTicks > 0) {
               --this.jumpTicks;
          }

          if (this.newPosRotationIncrements > 0 && !this.canPassengerSteer()) {
               double d0 = this.posX + (this.interpTargetX - this.posX) / (double)this.newPosRotationIncrements;
               double d1 = this.posY + (this.interpTargetY - this.posY) / (double)this.newPosRotationIncrements;
               double d2 = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.newPosRotationIncrements;
               double d3 = MathHelper.wrapDegrees(this.interpTargetYaw - (double)this.rotationYaw);
               this.rotationYaw = (float)((double)this.rotationYaw + d3 / (double)this.newPosRotationIncrements);
               this.rotationPitch = (float)((double)this.rotationPitch + (this.interpTargetPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
               --this.newPosRotationIncrements;
               this.setPosition(d0, d1, d2);
               this.setRotation(this.rotationYaw, this.rotationPitch);
          } else if (!this.isServerWorld()) {
               this.motionX *= 0.98D;
               this.motionY *= 0.98D;
               this.motionZ *= 0.98D;
          }

          if (Math.abs(this.motionX) < 0.003D) {
               this.motionX = 0.0D;
          }

          if (Math.abs(this.motionY) < 0.003D) {
               this.motionY = 0.0D;
          }

          if (Math.abs(this.motionZ) < 0.003D) {
               this.motionZ = 0.0D;
          }

          this.world.theProfiler.startSection("ai");
          if (this.isMovementBlocked()) {
               this.isJumping = false;
               this.moveStrafing = 0.0F;
               this.field_191988_bg = 0.0F;
               this.randomYawVelocity = 0.0F;
          } else if (this.isServerWorld()) {
               this.world.theProfiler.startSection("newAi");
               this.updateEntityActionState();
               this.world.theProfiler.endSection();
          }

          this.world.theProfiler.endSection();
          this.world.theProfiler.startSection("jump");
          if (this.isJumping) {
               if (this.isInWater()) {
                    this.handleJumpWater();
               } else if (this.isInLava()) {
                    this.handleJumpLava();
               } else if (this.onGround && this.jumpTicks == 0) {
                    this.jump();
                    this.jumpTicks = 10;
               }
          } else {
               this.jumpTicks = 0;
          }

          this.world.theProfiler.endSection();
          this.world.theProfiler.startSection("travel");
          this.moveStrafing *= 0.98F;
          this.field_191988_bg *= 0.98F;
          this.randomYawVelocity *= 0.9F;
          this.updateElytra();
          this.func_191986_a(this.moveStrafing, this.moveForward, this.field_191988_bg);
          this.world.theProfiler.endSection();
          this.world.theProfiler.startSection("push");
          this.collideWithNearbyEntities();
          this.world.theProfiler.endSection();
     }

     private void updateElytra() {
          boolean flag = this.getFlag(7);
          if (flag && !this.onGround && !this.isRiding()) {
               ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
               if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isBroken(itemstack)) {
                    flag = true;
                    if (!this.world.isRemote && (this.ticksElytraFlying + 1) % 20 == 0) {
                         itemstack.damageItem(1, this);
                    }
               } else {
                    flag = false;
               }
          } else {
               flag = false;
          }

          if (!this.world.isRemote) {
               this.setFlag(7, flag);
          }

     }

     protected void updateEntityActionState() {
     }

     protected void collideWithNearbyEntities() {
          List list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), EntitySelectors.getTeamCollisionPredicate(this));
          if (!list.isEmpty()) {
               int i = this.world.getGameRules().getInt("maxEntityCramming");
               int j;
               if (i > 0 && list.size() > i - 1 && this.rand.nextInt(4) == 0) {
                    j = 0;

                    for(int k = 0; k < list.size(); ++k) {
                         if (!((Entity)list.get(k)).isRiding()) {
                              ++j;
                         }
                    }

                    if (j > i - 1) {
                         this.attackEntityFrom(DamageSource.field_191291_g, 6.0F);
                    }
               }

               for(j = 0; j < list.size(); ++j) {
                    Entity entity = (Entity)list.get(j);
                    this.collideWithEntity(entity);
               }
          }

     }

     protected void collideWithEntity(Entity entityIn) {
          entityIn.applyEntityCollision(this);
     }

     public void dismountRidingEntity() {
          Entity entity = this.getRidingEntity();
          super.dismountRidingEntity();
          if (entity != null && entity != this.getRidingEntity() && !this.world.isRemote) {
               this.dismountEntity(entity);
          }

     }

     public void updateRidden() {
          super.updateRidden();
          this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
          this.onGroundSpeedFactor = 0.0F;
          this.fallDistance = 0.0F;
     }

     public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
          this.interpTargetX = x;
          this.interpTargetY = y;
          this.interpTargetZ = z;
          this.interpTargetYaw = (double)yaw;
          this.interpTargetPitch = (double)pitch;
          this.newPosRotationIncrements = posRotationIncrements;
     }

     public void setJumping(boolean jumping) {
          this.isJumping = jumping;
     }

     public void onItemPickup(Entity entityIn, int quantity) {
          if (!entityIn.isDead && !this.world.isRemote) {
               EntityTracker entitytracker = ((WorldServer)this.world).getEntityTracker();
               if (entityIn instanceof EntityItem || entityIn instanceof EntityArrow || entityIn instanceof EntityXPOrb) {
                    entitytracker.sendToAllTrackingEntity(entityIn, new SPacketCollectItem(entityIn.getEntityId(), this.getEntityId(), quantity));
               }
          }

     }

     public boolean canEntityBeSeen(Entity entityIn) {
          return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), new Vec3d(entityIn.posX, entityIn.posY + (double)entityIn.getEyeHeight(), entityIn.posZ), false, true, false) == null;
     }

     public Vec3d getLook(float partialTicks) {
          if (!Main.instance.featureDirector.getFeatureByClass(KillAura.class).isToggled() && this instanceof EntityPlayerSP) {
               return super.getLook(partialTicks);
          } else if (partialTicks == 1.0F) {
               return getVectorForRotation(this.rotationPitch, this.rotationYawHead);
          } else {
               float f = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
               float f1 = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * partialTicks;
               return getVectorForRotation(f, f1);
          }
     }

     public float getSwingProgress(float partialTickTime) {
          float f = this.swingProgress - this.prevSwingProgress;
          if (f < 0.0F) {
               ++f;
          }

          return this.prevSwingProgress + f * partialTickTime;
     }

     public boolean isServerWorld() {
          return !this.world.isRemote;
     }

     public boolean canBeCollidedWith() {
          return !this.isDead;
     }

     public boolean canBePushed() {
          if (Main.instance.featureDirector.getFeatureByClass(NoPush.class).isToggled() && NoPush.pushplayers.getBoolValue()) {
               return false;
          } else {
               return this.isEntityAlive() && !this.isOnLadder();
          }
     }

     protected void setBeenAttacked() {
          this.velocityChanged = this.rand.nextDouble() >= this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
     }

     public float getRotationYawHead() {
          return this.rotationYawHead;
     }

     public void setRotationYawHead(float rotation) {
          this.rotationYawHead = rotation;
     }

     public void setRenderYawOffset(float offset) {
          this.renderYawOffset = offset;
     }

     public float getAbsorptionAmount() {
          return this.absorptionAmount;
     }

     public void setAbsorptionAmount(float amount) {
          if (amount < 0.0F) {
               amount = 0.0F;
          }

          this.absorptionAmount = amount;
     }

     public void sendEnterCombat() {
     }

     public void sendEndCombat() {
     }

     protected void markPotionsDirty() {
          this.potionsNeedUpdate = true;
     }

     public abstract EnumHandSide getPrimaryHand();

     public boolean isHandActive() {
          return ((Byte)this.dataManager.get(HAND_STATES) & 1) > 0;
     }

     public EnumHand getActiveHand() {
          return ((Byte)this.dataManager.get(HAND_STATES) & 2) > 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
     }

     protected void updateActiveHand() {
          if (this.isHandActive()) {
               ItemStack itemstack = this.getHeldItem(this.getActiveHand());
               if (itemstack == this.activeItemStack) {
                    if (this.getItemInUseCount() <= 25 && this.getItemInUseCount() % 4 == 0) {
                         this.updateItemUse(this.activeItemStack, 5);
                    }

                    if (--this.activeItemStackUseCount == 0 && !this.world.isRemote) {
                         this.onItemUseFinish();
                    }
               } else {
                    this.resetActiveHand();
               }
          }

     }

     public void setActiveHand(EnumHand hand) {
          ItemStack itemstack = this.getHeldItem(hand);
          if (!itemstack.func_190926_b() && !this.isHandActive()) {
               this.activeItemStack = itemstack;
               this.activeItemStackUseCount = itemstack.getMaxItemUseDuration();
               if (!this.world.isRemote) {
                    int i = 1;
                    if (hand == EnumHand.OFF_HAND) {
                         i |= 2;
                    }

                    this.dataManager.set(HAND_STATES, (byte)i);
               }
          }

     }

     public void notifyDataManagerChange(DataParameter key) {
          super.notifyDataManagerChange(key);
          if (HAND_STATES.equals(key) && this.world.isRemote) {
               if (this.isHandActive() && this.activeItemStack.func_190926_b()) {
                    this.activeItemStack = this.getHeldItem(this.getActiveHand());
                    if (!this.activeItemStack.func_190926_b()) {
                         this.activeItemStackUseCount = this.activeItemStack.getMaxItemUseDuration();
                    }
               } else if (!this.isHandActive() && !this.activeItemStack.func_190926_b()) {
                    this.activeItemStack = ItemStack.field_190927_a;
                    this.activeItemStackUseCount = 0;
               }
          }

     }

     protected void updateItemUse(ItemStack stack, int eatingParticleCount) {
          if (!stack.func_190926_b() && this.isHandActive()) {
               if (stack.getItemUseAction() == EnumAction.DRINK) {
                    this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
               }

               if (stack.getItemUseAction() == EnumAction.EAT) {
                    for(int i = 0; i < eatingParticleCount; ++i) {
                         Vec3d vec3d = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                         vec3d = vec3d.rotatePitch(-this.rotationPitch * 0.017453292F);
                         vec3d = vec3d.rotateYaw(-this.rotationYaw * 0.017453292F);
                         double d0 = (double)(-this.rand.nextFloat()) * 0.6D - 0.3D;
                         Vec3d vec3d1 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
                         vec3d1 = vec3d1.rotatePitch(-this.rotationPitch * 0.017453292F);
                         vec3d1 = vec3d1.rotateYaw(-this.rotationYaw * 0.017453292F);
                         vec3d1 = vec3d1.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
                         if (stack.getHasSubtypes()) {
                              this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, vec3d.xCoord, vec3d.yCoord + 0.05D, vec3d.zCoord, Item.getIdFromItem(stack.getItem()), stack.getMetadata());
                         } else {
                              this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, vec3d.xCoord, vec3d.yCoord + 0.05D, vec3d.zCoord, Item.getIdFromItem(stack.getItem()));
                         }
                    }

                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
               }
          }

     }

     protected void onItemUseFinish() {
          if (!this.activeItemStack.func_190926_b() && this.isHandActive()) {
               this.updateItemUse(this.activeItemStack, 16);
               this.setHeldItem(this.getActiveHand(), this.activeItemStack.onItemUseFinish(this.world, this));
               this.resetActiveHand();
          }

     }

     public int getItemInUseDuration() {
          return this.isUsingItem() ? this.activeItemStack.getMaxItemUseDuration() - this.getItemInUseCount() : 0;
     }

     public ItemStack getActiveItemStack() {
          return this.activeItemStack;
     }

     public int getItemInUseCount() {
          return this.activeItemStackUseCount;
     }

     public int getItemInUseMaxCount() {
          return this.isHandActive() ? this.activeItemStack.getMaxItemUseDuration() - this.getItemInUseCount() : 0;
     }

     public void stopActiveHand() {
          if (!this.activeItemStack.func_190926_b()) {
               this.activeItemStack.onPlayerStoppedUsing(this.world, this, this.getItemInUseCount());
          }

          this.resetActiveHand();
     }

     public void resetActiveHand() {
          if (!this.world.isRemote) {
               this.dataManager.set(HAND_STATES, (byte)0);
          }

          this.activeItemStack = ItemStack.field_190927_a;
          this.activeItemStackUseCount = 0;
     }

     public boolean isActiveItemStackBlocking(int i) {
          if (this.isHandActive() && !this.activeItemStack.func_190926_b()) {
               Item item = this.activeItemStack.getItem();
               if (item.getItemUseAction(this.activeItemStack) != EnumAction.BLOCK) {
                    return false;
               } else {
                    return item.getMaxItemUseDuration(this.activeItemStack) - this.activeItemStackUseCount >= 5;
               }
          } else {
               return false;
          }
     }

     public boolean isBlocking() {
          return this.isHandActive() && this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.BLOCK;
     }

     public boolean isEating() {
          return this.isHandActive() && this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.EAT;
     }

     public boolean isUsingItem() {
          return this.isHandActive() && (this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.EAT || this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.BLOCK || this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.BOW || this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.DRINK);
     }

     public boolean isJumping() {
          return Minecraft.getMinecraft().gameSettings.keyBindJump.pressed;
     }

     public boolean isDrinking() {
          return this.isHandActive() && this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.DRINK;
     }

     public boolean isBowing() {
          return this.isHandActive() && this.activeItemStack.getItem().getItemUseAction(this.activeItemStack) == EnumAction.BOW;
     }

     public boolean isElytraFlying() {
          return this.getFlag(7);
     }

     public int getTicksElytraFlying() {
          return this.ticksElytraFlying;
     }

     public boolean attemptTeleport(double x, double y, double z) {
          double d0 = this.posX;
          double d1 = this.posY;
          double d2 = this.posZ;
          this.posX = x;
          this.posY = y;
          this.posZ = z;
          boolean flag = false;
          BlockPos blockpos = new BlockPos(this);
          World world = this.world;
          Random random = this.getRNG();
          boolean flag1;
          if (world.isBlockLoaded(blockpos)) {
               flag1 = false;

               while(!flag1 && blockpos.getY() > 0) {
                    BlockPos blockpos1 = blockpos.down();
                    IBlockState iblockstate = world.getBlockState(blockpos1);
                    if (iblockstate.getMaterial().blocksMovement()) {
                         flag1 = true;
                    } else {
                         --this.posY;
                         blockpos = blockpos1;
                    }
               }

               if (flag1) {
                    this.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                    if (world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !world.containsAnyLiquid(this.getEntityBoundingBox())) {
                         flag = true;
                    }
               }
          }

          if (!flag) {
               this.setPositionAndUpdate(d0, d1, d2);
               return false;
          } else {
               flag1 = true;

               for(int j = 0; j < 128; ++j) {
                    double d6 = (double)j / 127.0D;
                    float f = (random.nextFloat() - 0.5F) * 0.2F;
                    float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                    float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                    double d3 = d0 + (this.posX - d0) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                    double d4 = d1 + (this.posY - d1) * d6 + random.nextDouble() * (double)this.height;
                    double d5 = d2 + (this.posZ - d2) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                    world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, (double)f, (double)f1, (double)f2);
               }

               if (this instanceof EntityCreature) {
                    ((EntityCreature)this).getNavigator().clearPathEntity();
               }

               return true;
          }
     }

     public boolean canBeHitWithPotion() {
          return true;
     }

     public boolean func_190631_cK() {
          return true;
     }

     public void func_191987_a(BlockPos p_191987_1_, boolean p_191987_2_) {
     }

     static {
          SPRINTING_SPEED_BOOST = (new AttributeModifier(SPRINTING_SPEED_BOOST_ID, "Sprinting speed boost", 0.30000001192092896D, 2)).setSaved(false);
          HAND_STATES = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.BYTE);
          HEALTH = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.FLOAT);
          POTION_EFFECTS = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.VARINT);
          HIDE_PARTICLES = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.BOOLEAN);
          ARROW_COUNT_IN_ENTITY = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.VARINT);
     }
}
