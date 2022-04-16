package net.minecraft.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCactus extends Block {
     public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
     protected static final AxisAlignedBB CACTUS_COLLISION_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.9375D, 0.9375D);
     protected static final AxisAlignedBB CACTUS_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

     protected BlockCactus() {
          super(Material.CACTUS);
          this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
          this.setTickRandomly(true);
          this.setCreativeTab(CreativeTabs.DECORATIONS);
     }

     public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
          BlockPos blockpos = pos.up();
          if (worldIn.isAirBlock(blockpos)) {
               int i;
               for(i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; ++i) {
               }

               if (i < 3) {
                    int j = (Integer)state.getValue(AGE);
                    if (j == 15) {
                         worldIn.setBlockState(blockpos, this.getDefaultState());
                         IBlockState iblockstate = state.withProperty(AGE, 0);
                         worldIn.setBlockState(pos, iblockstate, 4);
                         iblockstate.neighborChanged(worldIn, blockpos, this, pos);
                    } else {
                         worldIn.setBlockState(pos, state.withProperty(AGE, j + 1), 4);
                    }
               }
          }

     }

     public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
          return CACTUS_COLLISION_AABB;
     }

     public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
          return CACTUS_AABB.offset(pos);
     }

     public boolean isFullCube(IBlockState state) {
          return false;
     }

     public boolean isOpaqueCube(IBlockState state) {
          return false;
     }

     public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
          return super.canPlaceBlockAt(worldIn, pos) ? this.canBlockStay(worldIn, pos) : false;
     }

     public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos p_189540_5_) {
          if (!this.canBlockStay(worldIn, pos)) {
               worldIn.destroyBlock(pos, true);
          }

     }

     public boolean canBlockStay(World worldIn, BlockPos pos) {
          Iterator var3 = EnumFacing.Plane.HORIZONTAL.iterator();

          Material material;
          do {
               if (!var3.hasNext()) {
                    Block block = worldIn.getBlockState(pos.down()).getBlock();
                    return block == Blocks.CACTUS || block == Blocks.SAND && !worldIn.getBlockState(pos.up()).getMaterial().isLiquid();
               }

               EnumFacing enumfacing = (EnumFacing)var3.next();
               material = worldIn.getBlockState(pos.offset(enumfacing)).getMaterial();
          } while(!material.isSolid() && material != Material.LAVA);

          return false;
     }

     public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
          entityIn.attackEntityFrom(DamageSource.cactus, 1.0F);
     }

     public BlockRenderLayer getBlockLayer() {
          return BlockRenderLayer.CUTOUT;
     }

     public IBlockState getStateFromMeta(int meta) {
          return this.getDefaultState().withProperty(AGE, meta);
     }

     public int getMetaFromState(IBlockState state) {
          return (Integer)state.getValue(AGE);
     }

     protected BlockStateContainer createBlockState() {
          return new BlockStateContainer(this, new IProperty[]{AGE});
     }

     public BlockFaceShape func_193383_a(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_) {
          return BlockFaceShape.UNDEFINED;
     }
}
