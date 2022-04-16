package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDoor extends Block {
     public static final PropertyDirection FACING;
     public static final PropertyBool OPEN;
     public static final PropertyEnum HINGE;
     public static final PropertyBool POWERED;
     public static final PropertyEnum HALF;
     protected static final AxisAlignedBB SOUTH_AABB;
     protected static final AxisAlignedBB NORTH_AABB;
     protected static final AxisAlignedBB WEST_AABB;
     protected static final AxisAlignedBB EAST_AABB;

     protected BlockDoor(Material materialIn) {
          super(materialIn);
          this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, false).withProperty(HINGE, BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, false).withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER));
     }

     public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
          state = state.getActualState(source, pos);
          EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
          boolean flag = !(Boolean)state.getValue(OPEN);
          boolean flag1 = state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT;
          switch(enumfacing) {
          case EAST:
          default:
               return flag ? EAST_AABB : (flag1 ? NORTH_AABB : SOUTH_AABB);
          case SOUTH:
               return flag ? SOUTH_AABB : (flag1 ? EAST_AABB : WEST_AABB);
          case WEST:
               return flag ? WEST_AABB : (flag1 ? SOUTH_AABB : NORTH_AABB);
          case NORTH:
               return flag ? NORTH_AABB : (flag1 ? WEST_AABB : EAST_AABB);
          }
     }

     public String getLocalizedName() {
          return I18n.translateToLocal((this.getUnlocalizedName() + ".name").replaceAll("tile", "item"));
     }

     public boolean isOpaqueCube(IBlockState state) {
          return false;
     }

     public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
          return isOpen(combineMetadata(worldIn, pos));
     }

     public boolean isFullCube(IBlockState state) {
          return false;
     }

     private int getCloseSound() {
          return this.blockMaterial == Material.IRON ? 1011 : 1012;
     }

     private int getOpenSound() {
          return this.blockMaterial == Material.IRON ? 1005 : 1006;
     }

     public MapColor getMapColor(IBlockState state, IBlockAccess p_180659_2_, BlockPos p_180659_3_) {
          if (state.getBlock() == Blocks.IRON_DOOR) {
               return MapColor.IRON;
          } else if (state.getBlock() == Blocks.OAK_DOOR) {
               return BlockPlanks.EnumType.OAK.getMapColor();
          } else if (state.getBlock() == Blocks.SPRUCE_DOOR) {
               return BlockPlanks.EnumType.SPRUCE.getMapColor();
          } else if (state.getBlock() == Blocks.BIRCH_DOOR) {
               return BlockPlanks.EnumType.BIRCH.getMapColor();
          } else if (state.getBlock() == Blocks.JUNGLE_DOOR) {
               return BlockPlanks.EnumType.JUNGLE.getMapColor();
          } else if (state.getBlock() == Blocks.ACACIA_DOOR) {
               return BlockPlanks.EnumType.ACACIA.getMapColor();
          } else {
               return state.getBlock() == Blocks.DARK_OAK_DOOR ? BlockPlanks.EnumType.DARK_OAK.getMapColor() : super.getMapColor(state, p_180659_2_, p_180659_3_);
          }
     }

     public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY) {
          if (this.blockMaterial == Material.IRON) {
               return false;
          } else {
               BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
               IBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);
               if (iblockstate.getBlock() != this) {
                    return false;
               } else {
                    state = iblockstate.cycleProperty(OPEN);
                    worldIn.setBlockState(blockpos, state, 10);
                    worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
                    worldIn.playEvent(playerIn, (Boolean)state.getValue(OPEN) ? this.getOpenSound() : this.getCloseSound(), pos, 0);
                    return true;
               }
          }
     }

     public void toggleDoor(World worldIn, BlockPos pos, boolean open) {
          IBlockState iblockstate = worldIn.getBlockState(pos);
          if (iblockstate.getBlock() == this) {
               BlockPos blockpos = iblockstate.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
               IBlockState iblockstate1 = pos == blockpos ? iblockstate : worldIn.getBlockState(blockpos);
               if (iblockstate1.getBlock() == this && (Boolean)iblockstate1.getValue(OPEN) != open) {
                    worldIn.setBlockState(blockpos, iblockstate1.withProperty(OPEN, open), 10);
                    worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
                    worldIn.playEvent((EntityPlayer)null, open ? this.getOpenSound() : this.getCloseSound(), pos, 0);
               }
          }

     }

     public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos p_189540_5_) {
          if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
               BlockPos blockpos = pos.down();
               IBlockState iblockstate = worldIn.getBlockState(blockpos);
               if (iblockstate.getBlock() != this) {
                    worldIn.setBlockToAir(pos);
               } else if (blockIn != this) {
                    iblockstate.neighborChanged(worldIn, blockpos, blockIn, p_189540_5_);
               }
          } else {
               boolean flag1 = false;
               BlockPos blockpos1 = pos.up();
               IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);
               if (iblockstate1.getBlock() != this) {
                    worldIn.setBlockToAir(pos);
                    flag1 = true;
               }

               if (!worldIn.getBlockState(pos.down()).isFullyOpaque()) {
                    worldIn.setBlockToAir(pos);
                    flag1 = true;
                    if (iblockstate1.getBlock() == this) {
                         worldIn.setBlockToAir(blockpos1);
                    }
               }

               if (flag1) {
                    if (!worldIn.isRemote) {
                         this.dropBlockAsItem(worldIn, pos, state, 0);
                    }
               } else {
                    boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos1);
                    if (blockIn != this && (flag || blockIn.getDefaultState().canProvidePower()) && flag != (Boolean)iblockstate1.getValue(POWERED)) {
                         worldIn.setBlockState(blockpos1, iblockstate1.withProperty(POWERED, flag), 2);
                         if (flag != (Boolean)state.getValue(OPEN)) {
                              worldIn.setBlockState(pos, state.withProperty(OPEN, flag), 2);
                              worldIn.markBlockRangeForRenderUpdate(pos, pos);
                              worldIn.playEvent((EntityPlayer)null, flag ? this.getOpenSound() : this.getCloseSound(), pos, 0);
                         }
                    }
               }
          }

     }

     public Item getItemDropped(IBlockState state, Random rand, int fortune) {
          return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? Items.field_190931_a : this.getItem();
     }

     public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
          if (pos.getY() >= 255) {
               return false;
          } else {
               return worldIn.getBlockState(pos.down()).isFullyOpaque() && super.canPlaceBlockAt(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos.up());
          }
     }

     public EnumPushReaction getMobilityFlag(IBlockState state) {
          return EnumPushReaction.DESTROY;
     }

     public static int combineMetadata(IBlockAccess worldIn, BlockPos pos) {
          IBlockState iblockstate = worldIn.getBlockState(pos);
          int i = iblockstate.getBlock().getMetaFromState(iblockstate);
          boolean flag = isTop(i);
          IBlockState iblockstate1 = worldIn.getBlockState(pos.down());
          int j = iblockstate1.getBlock().getMetaFromState(iblockstate1);
          int k = flag ? j : i;
          IBlockState iblockstate2 = worldIn.getBlockState(pos.up());
          int l = iblockstate2.getBlock().getMetaFromState(iblockstate2);
          int i1 = flag ? i : l;
          boolean flag1 = (i1 & 1) != 0;
          boolean flag2 = (i1 & 2) != 0;
          return removeHalfBit(k) | (flag ? 8 : 0) | (flag1 ? 16 : 0) | (flag2 ? 32 : 0);
     }

     public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
          return new ItemStack(this.getItem());
     }

     private Item getItem() {
          if (this == Blocks.IRON_DOOR) {
               return Items.IRON_DOOR;
          } else if (this == Blocks.SPRUCE_DOOR) {
               return Items.SPRUCE_DOOR;
          } else if (this == Blocks.BIRCH_DOOR) {
               return Items.BIRCH_DOOR;
          } else if (this == Blocks.JUNGLE_DOOR) {
               return Items.JUNGLE_DOOR;
          } else if (this == Blocks.ACACIA_DOOR) {
               return Items.ACACIA_DOOR;
          } else {
               return this == Blocks.DARK_OAK_DOOR ? Items.DARK_OAK_DOOR : Items.OAK_DOOR;
          }
     }

     public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
          BlockPos blockpos = pos.down();
          BlockPos blockpos1 = pos.up();
          if (player.capabilities.isCreativeMode && state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this) {
               worldIn.setBlockToAir(blockpos);
          }

          if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER && worldIn.getBlockState(blockpos1).getBlock() == this) {
               if (player.capabilities.isCreativeMode) {
                    worldIn.setBlockToAir(pos);
               }

               worldIn.setBlockToAir(blockpos1);
          }

     }

     public BlockRenderLayer getBlockLayer() {
          return BlockRenderLayer.CUTOUT;
     }

     public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
          IBlockState iblockstate;
          if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER) {
               iblockstate = worldIn.getBlockState(pos.up());
               if (iblockstate.getBlock() == this) {
                    state = state.withProperty(HINGE, iblockstate.getValue(HINGE)).withProperty(POWERED, iblockstate.getValue(POWERED));
               }
          } else {
               iblockstate = worldIn.getBlockState(pos.down());
               if (iblockstate.getBlock() == this) {
                    state = state.withProperty(FACING, iblockstate.getValue(FACING)).withProperty(OPEN, iblockstate.getValue(OPEN));
               }
          }

          return state;
     }

     public IBlockState withRotation(IBlockState state, Rotation rot) {
          return state.getValue(HALF) != BlockDoor.EnumDoorHalf.LOWER ? state : state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
     }

     public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
          return mirrorIn == Mirror.NONE ? state : state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING))).cycleProperty(HINGE);
     }

     public IBlockState getStateFromMeta(int meta) {
          return (meta & 8) > 0 ? this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.UPPER).withProperty(HINGE, (meta & 1) > 0 ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, (meta & 2) > 0) : this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER).withProperty(FACING, EnumFacing.getHorizontal(meta & 3).rotateYCCW()).withProperty(OPEN, (meta & 4) > 0);
     }

     public int getMetaFromState(IBlockState state) {
          int i = 0;
          int i;
          if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
               i = i | 8;
               if (state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT) {
                    i |= 1;
               }

               if ((Boolean)state.getValue(POWERED)) {
                    i |= 2;
               }
          } else {
               i = i | ((EnumFacing)state.getValue(FACING)).rotateY().getHorizontalIndex();
               if ((Boolean)state.getValue(OPEN)) {
                    i |= 4;
               }
          }

          return i;
     }

     protected static int removeHalfBit(int meta) {
          return meta & 7;
     }

     public static boolean isOpen(IBlockAccess worldIn, BlockPos pos) {
          return isOpen(combineMetadata(worldIn, pos));
     }

     public static EnumFacing getFacing(IBlockAccess worldIn, BlockPos pos) {
          return getFacing(combineMetadata(worldIn, pos));
     }

     public static EnumFacing getFacing(int combinedMeta) {
          return EnumFacing.getHorizontal(combinedMeta & 3).rotateYCCW();
     }

     protected static boolean isOpen(int combinedMeta) {
          return (combinedMeta & 4) != 0;
     }

     protected static boolean isTop(int meta) {
          return (meta & 8) != 0;
     }

     protected BlockStateContainer createBlockState() {
          return new BlockStateContainer(this, new IProperty[]{HALF, FACING, OPEN, HINGE, POWERED});
     }

     public BlockFaceShape func_193383_a(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_) {
          return BlockFaceShape.UNDEFINED;
     }

     static {
          FACING = BlockHorizontal.FACING;
          OPEN = PropertyBool.create("open");
          HINGE = PropertyEnum.create("hinge", BlockDoor.EnumHingePosition.class);
          POWERED = PropertyBool.create("powered");
          HALF = PropertyEnum.create("half", BlockDoor.EnumDoorHalf.class);
          SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
          NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
          WEST_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
          EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
     }

     public static enum EnumHingePosition implements IStringSerializable {
          LEFT,
          RIGHT;

          public String toString() {
               return this.getName();
          }

          public String getName() {
               return this == LEFT ? "left" : "right";
          }
     }

     public static enum EnumDoorHalf implements IStringSerializable {
          UPPER,
          LOWER;

          public String toString() {
               return this.getName();
          }

          public String getName() {
               return this == UPPER ? "upper" : "lower";
          }
     }
}
