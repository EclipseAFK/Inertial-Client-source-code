package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenerator {
     private final boolean doBlockNotify;

     public WorldGenerator() {
          this(false);
     }

     public WorldGenerator(boolean notify) {
          this.doBlockNotify = notify;
     }

     public abstract boolean generate(World var1, Random var2, BlockPos var3);

     public void setDecorationDefaults() {
     }

     protected void setBlockAndNotifyAdequately(World worldIn, BlockPos pos, IBlockState state) {
          if (this.doBlockNotify) {
               worldIn.setBlockState(pos, state, 3);
          } else {
               worldIn.setBlockState(pos, state, 2);
          }

     }
}
