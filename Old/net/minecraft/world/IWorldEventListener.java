package net.minecraft.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public interface IWorldEventListener {
      void notifyBlockUpdate(World var1, BlockPos var2, IBlockState var3, IBlockState var4, int var5);

      void notifyLightSet(BlockPos var1);

      void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6);

      void playSoundToAllNearExcept(EntityPlayer var1, SoundEvent var2, SoundCategory var3, double var4, double var6, double var8, float var10, float var11);

      void playRecord(SoundEvent var1, BlockPos var2);

      void spawnParticle(int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15);

      void func_190570_a(int var1, boolean var2, boolean var3, double var4, double var6, double var8, double var10, double var12, double var14, int... var16);

      void onEntityAdded(Entity var1);

      void onEntityRemoved(Entity var1);

      void broadcastSound(int var1, BlockPos var2, int var3);

      void playEvent(EntityPlayer var1, int var2, BlockPos var3, int var4);

      void sendBlockBreakProgress(int var1, BlockPos var2, int var3);
}
