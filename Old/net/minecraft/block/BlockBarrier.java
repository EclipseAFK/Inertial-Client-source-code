package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBarrier extends Block {
      protected BlockBarrier() {
            super(Material.BARRIER);
            this.setBlockUnbreakable();
            this.setResistance(6000001.0F);
            this.disableStats();
            this.translucent = true;
      }

      public EnumBlockRenderType getRenderType(IBlockState state) {
            return EnumBlockRenderType.INVISIBLE;
      }

      public boolean isOpaqueCube(IBlockState state) {
            return false;
      }

      public float getAmbientOcclusionLightValue(IBlockState state) {
            return 1.0F;
      }

      public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
      }
}
