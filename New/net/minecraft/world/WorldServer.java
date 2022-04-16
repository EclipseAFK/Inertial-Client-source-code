package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ReportedException;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataCallableSave;
import net.minecraft.world.storage.loot.LootTableManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldServer extends World implements IThreadListener {
     private static final Logger LOGGER = LogManager.getLogger();
     private final MinecraftServer mcServer;
     private final EntityTracker theEntityTracker;
     private final PlayerChunkMap thePlayerManager;
     private final Set pendingTickListEntriesHashSet = Sets.newHashSet();
     private final TreeSet pendingTickListEntriesTreeSet = new TreeSet();
     private final Map entitiesByUuid = Maps.newHashMap();
     public boolean disableLevelSaving;
     private boolean allPlayersSleeping;
     private int updateEntityTick;
     private final Teleporter worldTeleporter;
     private final WorldEntitySpawner entitySpawner = new WorldEntitySpawner();
     protected final VillageSiege villageSiege = new VillageSiege(this);
     private final WorldServer.ServerBlockEventList[] blockEventQueue = new WorldServer.ServerBlockEventList[]{new WorldServer.ServerBlockEventList(), new WorldServer.ServerBlockEventList()};
     private int blockEventCacheIndex;
     private final List pendingTickListEntriesThisTick = Lists.newArrayList();

     public WorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn) {
          super(saveHandlerIn, info, DimensionType.getById(dimensionId).createDimension(), profilerIn, false);
          this.mcServer = server;
          this.theEntityTracker = new EntityTracker(this);
          this.thePlayerManager = new PlayerChunkMap(this);
          this.provider.registerWorld(this);
          this.chunkProvider = this.createChunkProvider();
          this.worldTeleporter = new Teleporter(this);
          this.calculateInitialSkylight();
          this.calculateInitialWeather();
          this.getWorldBorder().setSize(server.getMaxWorldSize());
     }

     public World init() {
          this.mapStorage = new MapStorage(this.saveHandler);
          String s = VillageCollection.fileNameForProvider(this.provider);
          VillageCollection villagecollection = (VillageCollection)this.mapStorage.getOrLoadData(VillageCollection.class, s);
          if (villagecollection == null) {
               this.villageCollectionObj = new VillageCollection(this);
               this.mapStorage.setData(s, this.villageCollectionObj);
          } else {
               this.villageCollectionObj = villagecollection;
               this.villageCollectionObj.setWorldsForAll(this);
          }

          this.worldScoreboard = new ServerScoreboard(this.mcServer);
          ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData)this.mapStorage.getOrLoadData(ScoreboardSaveData.class, "scoreboard");
          if (scoreboardsavedata == null) {
               scoreboardsavedata = new ScoreboardSaveData();
               this.mapStorage.setData("scoreboard", scoreboardsavedata);
          }

          scoreboardsavedata.setScoreboard(this.worldScoreboard);
          ((ServerScoreboard)this.worldScoreboard).addDirtyRunnable(new WorldSavedDataCallableSave(scoreboardsavedata));
          this.lootTable = new LootTableManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "loot_tables"));
          this.field_191951_C = new AdvancementManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "advancements"));
          this.field_193036_D = new FunctionManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "functions"), this.mcServer);
          this.getWorldBorder().setCenter(this.worldInfo.getBorderCenterX(), this.worldInfo.getBorderCenterZ());
          this.getWorldBorder().setDamageAmount(this.worldInfo.getBorderDamagePerBlock());
          this.getWorldBorder().setDamageBuffer(this.worldInfo.getBorderSafeZone());
          this.getWorldBorder().setWarningDistance(this.worldInfo.getBorderWarningDistance());
          this.getWorldBorder().setWarningTime(this.worldInfo.getBorderWarningTime());
          if (this.worldInfo.getBorderLerpTime() > 0L) {
               this.getWorldBorder().setTransition(this.worldInfo.getBorderSize(), this.worldInfo.getBorderLerpTarget(), this.worldInfo.getBorderLerpTime());
          } else {
               this.getWorldBorder().setTransition(this.worldInfo.getBorderSize());
          }

          return this;
     }

     public void tick() {
          super.tick();
          if (this.getWorldInfo().isHardcoreModeEnabled() && this.getDifficulty() != EnumDifficulty.HARD) {
               this.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
          }

          this.provider.getBiomeProvider().cleanupCache();
          if (this.areAllPlayersAsleep()) {
               if (this.getGameRules().getBoolean("doDaylightCycle")) {
                    long i = this.worldInfo.getWorldTime() + 24000L;
                    this.worldInfo.setWorldTime(i - i % 24000L);
               }

               this.wakeAllPlayers();
          }

          this.theProfiler.startSection("mobSpawner");
          if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD) {
               this.entitySpawner.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
          }

          this.theProfiler.endStartSection("chunkSource");
          this.chunkProvider.unloadQueuedChunks();
          int j = this.calculateSkylightSubtracted(1.0F);
          if (j != this.getSkylightSubtracted()) {
               this.setSkylightSubtracted(j);
          }

          this.worldInfo.setWorldTotalTime(this.worldInfo.getWorldTotalTime() + 1L);
          if (this.getGameRules().getBoolean("doDaylightCycle")) {
               this.worldInfo.setWorldTime(this.worldInfo.getWorldTime() + 1L);
          }

          this.theProfiler.endStartSection("tickPending");
          this.tickUpdates(false);
          this.theProfiler.endStartSection("tickBlocks");
          this.updateBlocks();
          this.theProfiler.endStartSection("chunkMap");
          this.thePlayerManager.tick();
          this.theProfiler.endStartSection("village");
          this.villageCollectionObj.tick();
          this.villageSiege.tick();
          this.theProfiler.endStartSection("portalForcer");
          this.worldTeleporter.removeStalePortalLocations(this.getTotalWorldTime());
          this.theProfiler.endSection();
          this.sendQueuedBlockEvents();
     }

     @Nullable
     public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType creatureType, BlockPos pos) {
          List list = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
          return list != null && !list.isEmpty() ? (Biome.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, list) : null;
     }

     public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, Biome.SpawnListEntry spawnListEntry, BlockPos pos) {
          List list = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
          return list != null && !list.isEmpty() ? list.contains(spawnListEntry) : false;
     }

     public void updateAllPlayersSleepingFlag() {
          this.allPlayersSleeping = false;
          if (!this.playerEntities.isEmpty()) {
               int i = 0;
               int j = 0;
               Iterator var3 = this.playerEntities.iterator();

               while(var3.hasNext()) {
                    EntityPlayer entityplayer = (EntityPlayer)var3.next();
                    if (entityplayer.isSpectator()) {
                         ++i;
                    } else if (entityplayer.isPlayerSleeping()) {
                         ++j;
                    }
               }

               this.allPlayersSleeping = j > 0 && j >= this.playerEntities.size() - i;
          }

     }

     protected void wakeAllPlayers() {
          this.allPlayersSleeping = false;
          Iterator var1 = ((List)this.playerEntities.stream().filter(EntityPlayer::isPlayerSleeping).collect(Collectors.toList())).iterator();

          while(var1.hasNext()) {
               EntityPlayer entityplayer = (EntityPlayer)var1.next();
               entityplayer.wakeUpPlayer(false, false, true);
          }

          if (this.getGameRules().getBoolean("doWeatherCycle")) {
               this.resetRainAndThunder();
          }

     }

     private void resetRainAndThunder() {
          this.worldInfo.setRainTime(0);
          this.worldInfo.setRaining(false);
          this.worldInfo.setThunderTime(0);
          this.worldInfo.setThundering(false);
     }

     public boolean areAllPlayersAsleep() {
          if (this.allPlayersSleeping && !this.isRemote) {
               Iterator var1 = this.playerEntities.iterator();

               EntityPlayer entityplayer;
               do {
                    if (!var1.hasNext()) {
                         return true;
                    }

                    entityplayer = (EntityPlayer)var1.next();
               } while(entityplayer.isSpectator() || entityplayer.isPlayerFullyAsleep());

               return false;
          } else {
               return false;
          }
     }

     public void setInitialSpawnLocation() {
          if (this.worldInfo.getSpawnY() <= 0) {
               this.worldInfo.setSpawnY(this.getSeaLevel() + 1);
          }

          int i = this.worldInfo.getSpawnX();
          int j = this.worldInfo.getSpawnZ();
          int k = 0;

          while(this.getGroundAboveSeaLevel(new BlockPos(i, 0, j)).getMaterial() == Material.AIR) {
               i += this.rand.nextInt(8) - this.rand.nextInt(8);
               j += this.rand.nextInt(8) - this.rand.nextInt(8);
               ++k;
               if (k == 10000) {
                    break;
               }
          }

          this.worldInfo.setSpawnX(i);
          this.worldInfo.setSpawnZ(j);
     }

     protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
          return this.getChunkProvider().chunkExists(x, z);
     }

     protected void playerCheckLight() {
          this.theProfiler.startSection("playerCheckLight");
          if (!this.playerEntities.isEmpty()) {
               int i = this.rand.nextInt(this.playerEntities.size());
               EntityPlayer entityplayer = (EntityPlayer)this.playerEntities.get(i);
               int j = MathHelper.floor(entityplayer.posX) + this.rand.nextInt(11) - 5;
               int k = MathHelper.floor(entityplayer.posY) + this.rand.nextInt(11) - 5;
               int l = MathHelper.floor(entityplayer.posZ) + this.rand.nextInt(11) - 5;
               this.checkLight(new BlockPos(j, k, l));
          }

          this.theProfiler.endSection();
     }

     protected void updateBlocks() {
          this.playerCheckLight();
          if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
               Iterator iterator1 = this.thePlayerManager.getChunkIterator();

               while(iterator1.hasNext()) {
                    ((Chunk)iterator1.next()).onTick(false);
               }
          } else {
               int i = this.getGameRules().getInt("randomTickSpeed");
               boolean flag = this.isRaining();
               boolean flag1 = this.isThundering();
               this.theProfiler.startSection("pollingChunks");

               for(Iterator iterator = this.thePlayerManager.getChunkIterator(); iterator.hasNext(); this.theProfiler.endSection()) {
                    this.theProfiler.startSection("getChunk");
                    Chunk chunk = (Chunk)iterator.next();
                    int j = chunk.xPosition * 16;
                    int k = chunk.zPosition * 16;
                    this.theProfiler.endStartSection("checkNextLight");
                    chunk.enqueueRelightChecks();
                    this.theProfiler.endStartSection("tickChunk");
                    chunk.onTick(false);
                    this.theProfiler.endStartSection("thunder");
                    int j2;
                    BlockPos blockpos1;
                    if (flag && flag1 && this.rand.nextInt(100000) == 0) {
                         this.updateLCG = this.updateLCG * 3 + 1013904223;
                         j2 = this.updateLCG >> 2;
                         blockpos1 = this.adjustPosToNearbyEntity(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
                         if (this.isRainingAt(blockpos1)) {
                              DifficultyInstance difficultyinstance = this.getDifficultyForLocation(blockpos1);
                              if (this.getGameRules().getBoolean("doMobSpawning") && this.rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.01D) {
                                   EntitySkeletonHorse entityskeletonhorse = new EntitySkeletonHorse(this);
                                   entityskeletonhorse.func_190691_p(true);
                                   entityskeletonhorse.setGrowingAge(0);
                                   entityskeletonhorse.setPosition((double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
                                   this.spawnEntityInWorld(entityskeletonhorse);
                                   this.addWeatherEffect(new EntityLightningBolt(this, (double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ(), true));
                              } else {
                                   this.addWeatherEffect(new EntityLightningBolt(this, (double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ(), false));
                              }
                         }
                    }

                    this.theProfiler.endStartSection("iceandsnow");
                    if (this.rand.nextInt(16) == 0) {
                         this.updateLCG = this.updateLCG * 3 + 1013904223;
                         j2 = this.updateLCG >> 2;
                         blockpos1 = this.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
                         BlockPos blockpos2 = blockpos1.down();
                         if (this.canBlockFreezeNoWater(blockpos2)) {
                              this.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
                         }

                         if (flag && this.canSnowAt(blockpos1, true)) {
                              this.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState());
                         }

                         if (flag && this.getBiome(blockpos2).canRain()) {
                              this.getBlockState(blockpos2).getBlock().fillWithRain(this, blockpos2);
                         }
                    }

                    this.theProfiler.endStartSection("tickBlocks");
                    if (i > 0) {
                         ExtendedBlockStorage[] var20 = chunk.getBlockStorageArray();
                         int var21 = var20.length;

                         for(int var23 = 0; var23 < var21; ++var23) {
                              ExtendedBlockStorage extendedblockstorage = var20[var23];
                              if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.getNeedsRandomTick()) {
                                   for(int i1 = 0; i1 < i; ++i1) {
                                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                                        int j1 = this.updateLCG >> 2;
                                        int k1 = j1 & 15;
                                        int l1 = j1 >> 8 & 15;
                                        int i2 = j1 >> 16 & 15;
                                        IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
                                        Block block = iblockstate.getBlock();
                                        this.theProfiler.startSection("randomTick");
                                        if (block.getTickRandomly()) {
                                             block.randomTick(this, new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), iblockstate, this.rand);
                                        }

                                        this.theProfiler.endSection();
                                   }
                              }
                         }
                    }
               }

               this.theProfiler.endSection();
          }

     }

     protected BlockPos adjustPosToNearbyEntity(BlockPos pos) {
          BlockPos blockpos = this.getPrecipitationHeight(pos);
          AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos, new BlockPos(blockpos.getX(), this.getHeight(), blockpos.getZ()))).expandXyz(3.0D);
          List list = this.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb, new Predicate() {
               public boolean apply(@Nullable EntityLivingBase p_apply_1_) {
                    return p_apply_1_ != null && p_apply_1_.isEntityAlive() && WorldServer.this.canSeeSky(p_apply_1_.getPosition());
               }
          });
          if (!list.isEmpty()) {
               return ((EntityLivingBase)list.get(this.rand.nextInt(list.size()))).getPosition();
          } else {
               if (blockpos.getY() == -1) {
                    blockpos = blockpos.up(2);
               }

               return blockpos;
          }
     }

     public boolean isBlockTickPending(BlockPos pos, Block blockType) {
          NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockType);
          return this.pendingTickListEntriesThisTick.contains(nextticklistentry);
     }

     public boolean isUpdateScheduled(BlockPos pos, Block blk) {
          NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blk);
          return this.pendingTickListEntriesHashSet.contains(nextticklistentry);
     }

     public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
          this.updateBlockTick(pos, blockIn, delay, 0);
     }

     public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
          Material material = blockIn.getDefaultState().getMaterial();
          if (this.scheduledUpdatesAreImmediate && material != Material.AIR) {
               if (blockIn.requiresUpdates()) {
                    if (this.isAreaLoaded(pos.add(-8, -8, -8), pos.add(8, 8, 8))) {
                         IBlockState iblockstate = this.getBlockState(pos);
                         if (iblockstate.getMaterial() != Material.AIR && iblockstate.getBlock() == blockIn) {
                              iblockstate.getBlock().updateTick(this, pos, iblockstate, this.rand);
                         }
                    }

                    return;
               }

               delay = 1;
          }

          NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
          if (this.isBlockLoaded(pos)) {
               if (material != Material.AIR) {
                    nextticklistentry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
                    nextticklistentry.setPriority(priority);
               }

               if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
                    this.pendingTickListEntriesHashSet.add(nextticklistentry);
                    this.pendingTickListEntriesTreeSet.add(nextticklistentry);
               }
          }

     }

     public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
          NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
          nextticklistentry.setPriority(priority);
          Material material = blockIn.getDefaultState().getMaterial();
          if (material != Material.AIR) {
               nextticklistentry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
          }

          if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
               this.pendingTickListEntriesHashSet.add(nextticklistentry);
               this.pendingTickListEntriesTreeSet.add(nextticklistentry);
          }

     }

     public void updateEntities() {
          if (this.playerEntities.isEmpty()) {
               if (this.updateEntityTick++ >= 300) {
                    return;
               }
          } else {
               this.resetUpdateEntityTick();
          }

          this.provider.onWorldUpdateEntities();
          super.updateEntities();
     }

     protected void tickPlayers() {
          super.tickPlayers();
          this.theProfiler.endStartSection("players");

          for(int i = 0; i < this.playerEntities.size(); ++i) {
               Entity entity = (Entity)this.playerEntities.get(i);
               Entity entity1 = entity.getRidingEntity();
               if (entity1 != null) {
                    if (!entity1.isDead && entity1.isPassenger(entity)) {
                         continue;
                    }

                    entity.dismountRidingEntity();
               }

               this.theProfiler.startSection("tick");
               if (!entity.isDead) {
                    try {
                         this.updateEntity(entity);
                    } catch (Throwable var7) {
                         CrashReport crashreport = CrashReport.makeCrashReport(var7, "Ticking player");
                         CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
                         entity.addEntityCrashInfo(crashreportcategory);
                         throw new ReportedException(crashreport);
                    }
               }

               this.theProfiler.endSection();
               this.theProfiler.startSection("remove");
               if (entity.isDead) {
                    int j = entity.chunkCoordX;
                    int k = entity.chunkCoordZ;
                    if (entity.addedToChunk && this.isChunkLoaded(j, k, true)) {
                         this.getChunkFromChunkCoords(j, k).removeEntity(entity);
                    }

                    this.loadedEntityList.remove(entity);
                    this.onEntityRemoved(entity);
               }

               this.theProfiler.endSection();
          }

     }

     public void resetUpdateEntityTick() {
          this.updateEntityTick = 0;
     }

     public boolean tickUpdates(boolean p_72955_1_) {
          if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
               return false;
          } else {
               int i = this.pendingTickListEntriesTreeSet.size();
               if (i != this.pendingTickListEntriesHashSet.size()) {
                    throw new IllegalStateException("TickNextTick list out of synch");
               } else {
                    if (i > 65536) {
                         i = 65536;
                    }

                    this.theProfiler.startSection("cleaning");

                    NextTickListEntry nextticklistentry1;
                    for(int j = 0; j < i; ++j) {
                         nextticklistentry1 = (NextTickListEntry)this.pendingTickListEntriesTreeSet.first();
                         if (!p_72955_1_ && nextticklistentry1.scheduledTime > this.worldInfo.getWorldTotalTime()) {
                              break;
                         }

                         this.pendingTickListEntriesTreeSet.remove(nextticklistentry1);
                         this.pendingTickListEntriesHashSet.remove(nextticklistentry1);
                         this.pendingTickListEntriesThisTick.add(nextticklistentry1);
                    }

                    this.theProfiler.endSection();
                    this.theProfiler.startSection("ticking");
                    Iterator iterator = this.pendingTickListEntriesThisTick.iterator();

                    while(iterator.hasNext()) {
                         nextticklistentry1 = (NextTickListEntry)iterator.next();
                         iterator.remove();
                         int k = false;
                         if (this.isAreaLoaded(nextticklistentry1.position.add(0, 0, 0), nextticklistentry1.position.add(0, 0, 0))) {
                              IBlockState iblockstate = this.getBlockState(nextticklistentry1.position);
                              if (iblockstate.getMaterial() != Material.AIR && Block.isEqualTo(iblockstate.getBlock(), nextticklistentry1.getBlock())) {
                                   try {
                                        iblockstate.getBlock().updateTick(this, nextticklistentry1.position, iblockstate, this.rand);
                                   } catch (Throwable var10) {
                                        CrashReport crashreport = CrashReport.makeCrashReport(var10, "Exception while ticking a block");
                                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                                        CrashReportCategory.addBlockInfo(crashreportcategory, nextticklistentry1.position, iblockstate);
                                        throw new ReportedException(crashreport);
                                   }
                              }
                         } else {
                              this.scheduleUpdate(nextticklistentry1.position, nextticklistentry1.getBlock(), 0);
                         }
                    }

                    this.theProfiler.endSection();
                    this.pendingTickListEntriesThisTick.clear();
                    return !this.pendingTickListEntriesTreeSet.isEmpty();
               }
          }
     }

     @Nullable
     public List getPendingBlockUpdates(Chunk chunkIn, boolean p_72920_2_) {
          ChunkPos chunkpos = chunkIn.getChunkCoordIntPair();
          int i = (chunkpos.chunkXPos << 4) - 2;
          int j = i + 16 + 2;
          int k = (chunkpos.chunkZPos << 4) - 2;
          int l = k + 16 + 2;
          return this.getPendingBlockUpdates(new StructureBoundingBox(i, 0, k, j, 256, l), p_72920_2_);
     }

     @Nullable
     public List getPendingBlockUpdates(StructureBoundingBox structureBB, boolean p_175712_2_) {
          List list = null;

          for(int i = 0; i < 2; ++i) {
               Iterator iterator;
               if (i == 0) {
                    iterator = this.pendingTickListEntriesTreeSet.iterator();
               } else {
                    iterator = this.pendingTickListEntriesThisTick.iterator();
               }

               while(iterator.hasNext()) {
                    NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
                    BlockPos blockpos = nextticklistentry.position;
                    if (blockpos.getX() >= structureBB.minX && blockpos.getX() < structureBB.maxX && blockpos.getZ() >= structureBB.minZ && blockpos.getZ() < structureBB.maxZ) {
                         if (p_175712_2_) {
                              if (i == 0) {
                                   this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                              }

                              iterator.remove();
                         }

                         if (list == null) {
                              list = Lists.newArrayList();
                         }

                         list.add(nextticklistentry);
                    }
               }
          }

          return list;
     }

     public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
          if (!this.canSpawnAnimals() && (entityIn instanceof EntityAnimal || entityIn instanceof EntityWaterMob)) {
               entityIn.setDead();
          }

          if (!this.canSpawnNPCs() && entityIn instanceof INpc) {
               entityIn.setDead();
          }

          super.updateEntityWithOptionalForce(entityIn, forceUpdate);
     }

     private boolean canSpawnNPCs() {
          return this.mcServer.getCanSpawnNPCs();
     }

     private boolean canSpawnAnimals() {
          return this.mcServer.getCanSpawnAnimals();
     }

     protected IChunkProvider createChunkProvider() {
          IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
          return new ChunkProviderServer(this, ichunkloader, this.provider.createChunkGenerator());
     }

     public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
          return !this.mcServer.isBlockProtected(this, pos, player) && this.getWorldBorder().contains(pos);
     }

     public void initialize(WorldSettings settings) {
          if (!this.worldInfo.isInitialized()) {
               try {
                    this.createSpawnPosition(settings);
                    if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
                         this.setDebugWorldSettings();
                    }

                    super.initialize(settings);
               } catch (Throwable var6) {
                    CrashReport crashreport = CrashReport.makeCrashReport(var6, "Exception initializing level");

                    try {
                         this.addWorldInfoToCrashReport(crashreport);
                    } catch (Throwable var5) {
                    }

                    throw new ReportedException(crashreport);
               }

               this.worldInfo.setServerInitialized(true);
          }

     }

     private void setDebugWorldSettings() {
          this.worldInfo.setMapFeaturesEnabled(false);
          this.worldInfo.setAllowCommands(true);
          this.worldInfo.setRaining(false);
          this.worldInfo.setThundering(false);
          this.worldInfo.setCleanWeatherTime(1000000000);
          this.worldInfo.setWorldTime(6000L);
          this.worldInfo.setGameType(GameType.SPECTATOR);
          this.worldInfo.setHardcore(false);
          this.worldInfo.setDifficulty(EnumDifficulty.PEACEFUL);
          this.worldInfo.setDifficultyLocked(true);
          this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
     }

     private void createSpawnPosition(WorldSettings settings) {
          if (!this.provider.canRespawnHere()) {
               this.worldInfo.setSpawn(BlockPos.ORIGIN.up(this.provider.getAverageGroundLevel()));
          } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
               this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
          } else {
               this.findingSpawnPoint = true;
               BiomeProvider biomeprovider = this.provider.getBiomeProvider();
               List list = biomeprovider.getBiomesToSpawnIn();
               Random random = new Random(this.getSeed());
               BlockPos blockpos = biomeprovider.findBiomePosition(0, 0, 256, list, random);
               int i = 8;
               int j = this.provider.getAverageGroundLevel();
               int k = 8;
               if (blockpos != null) {
                    i = blockpos.getX();
                    k = blockpos.getZ();
               } else {
                    LOGGER.warn("Unable to find spawn biome");
               }

               int l = 0;

               while(!this.provider.canCoordinateBeSpawn(i, k)) {
                    i += random.nextInt(64) - random.nextInt(64);
                    k += random.nextInt(64) - random.nextInt(64);
                    ++l;
                    if (l == 1000) {
                         break;
                    }
               }

               this.worldInfo.setSpawn(new BlockPos(i, j, k));
               this.findingSpawnPoint = false;
               if (settings.isBonusChestEnabled()) {
                    this.createBonusChest();
               }
          }

     }

     protected void createBonusChest() {
          WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest();

          for(int i = 0; i < 10; ++i) {
               int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
               int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
               BlockPos blockpos = this.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();
               if (worldgeneratorbonuschest.generate(this, this.rand, blockpos)) {
                    break;
               }
          }

     }

     @Nullable
     public BlockPos getSpawnCoordinate() {
          return this.provider.getSpawnCoordinate();
     }

     public void saveAllChunks(boolean p_73044_1_, @Nullable IProgressUpdate progressCallback) throws MinecraftException {
          ChunkProviderServer chunkproviderserver = this.getChunkProvider();
          if (chunkproviderserver.canSave()) {
               if (progressCallback != null) {
                    progressCallback.displaySavingString("Saving level");
               }

               this.saveLevel();
               if (progressCallback != null) {
                    progressCallback.displayLoadingString("Saving chunks");
               }

               chunkproviderserver.saveChunks(p_73044_1_);
               Iterator var4 = Lists.newArrayList(chunkproviderserver.getLoadedChunks()).iterator();

               while(var4.hasNext()) {
                    Chunk chunk = (Chunk)var4.next();
                    if (chunk != null && !this.thePlayerManager.contains(chunk.xPosition, chunk.zPosition)) {
                         chunkproviderserver.unload(chunk);
                    }
               }
          }

     }

     public void saveChunkData() {
          ChunkProviderServer chunkproviderserver = this.getChunkProvider();
          if (chunkproviderserver.canSave()) {
               chunkproviderserver.saveExtraData();
          }

     }

     protected void saveLevel() throws MinecraftException {
          this.checkSessionLock();
          WorldServer[] var1 = this.mcServer.worldServers;
          int var2 = var1.length;

          for(int var3 = 0; var3 < var2; ++var3) {
               WorldServer worldserver = var1[var3];
               if (worldserver instanceof WorldServerMulti) {
                    ((WorldServerMulti)worldserver).saveAdditionalData();
               }
          }

          this.worldInfo.setBorderSize(this.getWorldBorder().getDiameter());
          this.worldInfo.getBorderCenterX(this.getWorldBorder().getCenterX());
          this.worldInfo.getBorderCenterZ(this.getWorldBorder().getCenterZ());
          this.worldInfo.setBorderSafeZone(this.getWorldBorder().getDamageBuffer());
          this.worldInfo.setBorderDamagePerBlock(this.getWorldBorder().getDamageAmount());
          this.worldInfo.setBorderWarningDistance(this.getWorldBorder().getWarningDistance());
          this.worldInfo.setBorderWarningTime(this.getWorldBorder().getWarningTime());
          this.worldInfo.setBorderLerpTarget(this.getWorldBorder().getTargetSize());
          this.worldInfo.setBorderLerpTime(this.getWorldBorder().getTimeUntilTarget());
          this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.mcServer.getPlayerList().getHostPlayerData());
          this.mapStorage.saveAllData();
     }

     public boolean spawnEntityInWorld(Entity entityIn) {
          return this.canAddEntity(entityIn) ? super.spawnEntityInWorld(entityIn) : false;
     }

     public void loadEntities(Collection entityCollection) {
          Iterator var2 = Lists.newArrayList(entityCollection).iterator();

          while(var2.hasNext()) {
               Entity entity = (Entity)var2.next();
               if (this.canAddEntity(entity)) {
                    this.loadedEntityList.add(entity);
                    this.onEntityAdded(entity);
               }
          }

     }

     private boolean canAddEntity(Entity entityIn) {
          if (entityIn.isDead) {
               LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityList.func_191301_a(entityIn));
               return false;
          } else {
               UUID uuid = entityIn.getUniqueID();
               if (this.entitiesByUuid.containsKey(uuid)) {
                    Entity entity = (Entity)this.entitiesByUuid.get(uuid);
                    if (this.unloadedEntityList.contains(entity)) {
                         this.unloadedEntityList.remove(entity);
                    } else {
                         if (!(entityIn instanceof EntityPlayer)) {
                              LOGGER.warn("Keeping entity {} that already exists with UUID {}", EntityList.func_191301_a(entity), uuid.toString());
                              return false;
                         }

                         LOGGER.warn("Force-added player with duplicate UUID {}", uuid.toString());
                    }

                    this.removeEntityDangerously(entity);
               }

               return true;
          }
     }

     protected void onEntityAdded(Entity entityIn) {
          super.onEntityAdded(entityIn);
          this.entitiesById.addKey(entityIn.getEntityId(), entityIn);
          this.entitiesByUuid.put(entityIn.getUniqueID(), entityIn);
          Entity[] aentity = entityIn.getParts();
          if (aentity != null) {
               Entity[] var3 = aentity;
               int var4 = aentity.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                    Entity entity = var3[var5];
                    this.entitiesById.addKey(entity.getEntityId(), entity);
               }
          }

     }

     protected void onEntityRemoved(Entity entityIn) {
          super.onEntityRemoved(entityIn);
          this.entitiesById.removeObject(entityIn.getEntityId());
          this.entitiesByUuid.remove(entityIn.getUniqueID());
          Entity[] aentity = entityIn.getParts();
          if (aentity != null) {
               Entity[] var3 = aentity;
               int var4 = aentity.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                    Entity entity = var3[var5];
                    this.entitiesById.removeObject(entity.getEntityId());
               }
          }

     }

     public boolean addWeatherEffect(Entity entityIn) {
          if (super.addWeatherEffect(entityIn)) {
               this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, this.provider.getDimensionType().getId(), new SPacketSpawnGlobalEntity(entityIn));
               return true;
          } else {
               return false;
          }
     }

     public void setEntityState(Entity entityIn, byte state) {
          this.getEntityTracker().sendToTrackingAndSelf(entityIn, new SPacketEntityStatus(entityIn, state));
     }

     public ChunkProviderServer getChunkProvider() {
          return (ChunkProviderServer)super.getChunkProvider();
     }

     public Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking) {
          Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, isFlaming, isSmoking);
          explosion.doExplosionA();
          explosion.doExplosionB(false);
          if (!isSmoking) {
               explosion.clearAffectedBlockPositions();
          }

          Iterator var12 = this.playerEntities.iterator();

          while(var12.hasNext()) {
               EntityPlayer entityplayer = (EntityPlayer)var12.next();
               if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                    ((EntityPlayerMP)entityplayer).connection.sendPacket(new SPacketExplosion(x, y, z, strength, explosion.getAffectedBlockPositions(), (Vec3d)explosion.getPlayerKnockbackMap().get(entityplayer)));
               }
          }

          return explosion;
     }

     public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
          BlockEventData blockeventdata = new BlockEventData(pos, blockIn, eventID, eventParam);
          Iterator var6 = this.blockEventQueue[this.blockEventCacheIndex].iterator();

          BlockEventData blockeventdata1;
          do {
               if (!var6.hasNext()) {
                    this.blockEventQueue[this.blockEventCacheIndex].add(blockeventdata);
                    return;
               }

               blockeventdata1 = (BlockEventData)var6.next();
          } while(!blockeventdata1.equals(blockeventdata));

     }

     private void sendQueuedBlockEvents() {
          while(!this.blockEventQueue[this.blockEventCacheIndex].isEmpty()) {
               int i = this.blockEventCacheIndex;
               this.blockEventCacheIndex ^= 1;
               Iterator var2 = this.blockEventQueue[i].iterator();

               while(var2.hasNext()) {
                    BlockEventData blockeventdata = (BlockEventData)var2.next();
                    if (this.fireBlockEvent(blockeventdata)) {
                         this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, this.provider.getDimensionType().getId(), new SPacketBlockAction(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
                    }
               }

               this.blockEventQueue[i].clear();
          }

     }

     private boolean fireBlockEvent(BlockEventData event) {
          IBlockState iblockstate = this.getBlockState(event.getPosition());
          return iblockstate.getBlock() == event.getBlock() ? iblockstate.onBlockEventReceived(this, event.getPosition(), event.getEventID(), event.getEventParameter()) : false;
     }

     public void flush() {
          this.saveHandler.flush();
     }

     protected void updateWeather() {
          boolean flag = this.isRaining();
          super.updateWeather();
          if (this.prevRainingStrength != this.rainingStrength) {
               this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(7, this.rainingStrength), this.provider.getDimensionType().getId());
          }

          if (this.prevThunderingStrength != this.thunderingStrength) {
               this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimensionType().getId());
          }

          if (flag != this.isRaining()) {
               if (flag) {
                    this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketChangeGameState(2, 0.0F));
               } else {
                    this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketChangeGameState(1, 0.0F));
               }

               this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketChangeGameState(7, this.rainingStrength));
               this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketChangeGameState(8, this.thunderingStrength));
          }

     }

     @Nullable
     public MinecraftServer getMinecraftServer() {
          return this.mcServer;
     }

     public EntityTracker getEntityTracker() {
          return this.theEntityTracker;
     }

     public PlayerChunkMap getPlayerChunkMap() {
          return this.thePlayerManager;
     }

     public Teleporter getDefaultTeleporter() {
          return this.worldTeleporter;
     }

     public TemplateManager getStructureTemplateManager() {
          return this.saveHandler.getStructureTemplateManager();
     }

     public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
          this.spawnParticle(particleType, false, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, particleArguments);
     }

     public void spawnParticle(EnumParticleTypes particleType, boolean longDistance, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments) {
          SPacketParticles spacketparticles = new SPacketParticles(particleType, longDistance, (float)xCoord, (float)yCoord, (float)zCoord, (float)xOffset, (float)yOffset, (float)zOffset, (float)particleSpeed, numberOfParticles, particleArguments);

          for(int i = 0; i < this.playerEntities.size(); ++i) {
               EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntities.get(i);
               this.sendPacketWithinDistance(entityplayermp, longDistance, xCoord, yCoord, zCoord, spacketparticles);
          }

     }

     public void spawnParticle(EntityPlayerMP player, EnumParticleTypes particle, boolean longDistance, double x, double y, double z, int count, double xOffset, double yOffset, double zOffset, double speed, int... arguments) {
          Packet packet = new SPacketParticles(particle, longDistance, (float)x, (float)y, (float)z, (float)xOffset, (float)yOffset, (float)zOffset, (float)speed, count, arguments);
          this.sendPacketWithinDistance(player, longDistance, x, y, z, packet);
     }

     private void sendPacketWithinDistance(EntityPlayerMP player, boolean longDistance, double x, double y, double z, Packet packetIn) {
          BlockPos blockpos = player.getPosition();
          double d0 = blockpos.distanceSq(x, y, z);
          if (d0 <= 1024.0D || longDistance && d0 <= 262144.0D) {
               player.connection.sendPacket(packetIn);
          }

     }

     @Nullable
     public Entity getEntityFromUuid(UUID uuid) {
          return (Entity)this.entitiesByUuid.get(uuid);
     }

     public ListenableFuture addScheduledTask(Runnable runnableToSchedule) {
          return this.mcServer.addScheduledTask(runnableToSchedule);
     }

     public boolean isCallingFromMinecraftThread() {
          return this.mcServer.isCallingFromMinecraftThread();
     }

     @Nullable
     public BlockPos func_190528_a(String p_190528_1_, BlockPos p_190528_2_, boolean p_190528_3_) {
          return this.getChunkProvider().getStrongholdGen(this, p_190528_1_, p_190528_2_, p_190528_3_);
     }

     public AdvancementManager func_191952_z() {
          return this.field_191951_C;
     }

     public FunctionManager func_193037_A() {
          return this.field_193036_D;
     }

     static class ServerBlockEventList extends ArrayList {
          private ServerBlockEventList() {
          }

          // $FF: synthetic method
          ServerBlockEventList(Object x0) {
               this();
          }
     }
}
