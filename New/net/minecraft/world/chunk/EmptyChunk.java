package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class EmptyChunk extends Chunk {
     public EmptyChunk(World worldIn, int x, int z) {
          super(worldIn, x, z);
     }

     public boolean isAtLocation(int x, int z) {
          return x == this.xPosition && z == this.zPosition;
     }

     public int getHeightValue(int x, int z) {
          return 0;
     }

     public void generateHeightMap() {
     }

     public void generateSkylightMap() {
     }

     public IBlockState getBlockState(BlockPos pos) {
          return Blocks.AIR.getDefaultState();
     }

     public int getBlockLightOpacity(BlockPos pos) {
          return 255;
     }

     public int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos) {
          return p_177413_1_.defaultLightValue;
     }

     public void setLightFor(EnumSkyBlock p_177431_1_, BlockPos pos, int value) {
     }

     public int getLightSubtracted(BlockPos pos, int amount) {
          return 0;
     }

     public void addEntity(Entity entityIn) {
     }

     public void removeEntity(Entity entityIn) {
     }

     public void removeEntityAtIndex(Entity entityIn, int index) {
     }

     public boolean canSeeSky(BlockPos pos) {
          return false;
     }

     @Nullable
     public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType p_177424_2_) {
          return null;
     }

     public void addTileEntity(TileEntity tileEntityIn) {
     }

     public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
     }

     public void removeTileEntity(BlockPos pos) {
     }

     public void onChunkLoad() {
     }

     public void onChunkUnload() {
     }

     public void setChunkModified() {
     }

     public void getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, AxisAlignedBB aabb, List listToFill, Predicate p_177414_4_) {
     }

     public void getEntitiesOfTypeWithinAAAB(Class entityClass, AxisAlignedBB aabb, List listToFill, Predicate filter) {
     }

     public boolean needsSaving(boolean p_76601_1_) {
          return false;
     }

     public Random getRandomWithSeed(long seed) {
          return new Random(this.getWorld().getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ seed);
     }

     public boolean isEmpty() {
          return true;
     }

     public boolean getAreLevelsEmpty(int startY, int endY) {
          return true;
     }
}
