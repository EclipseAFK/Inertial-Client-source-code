package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class ChunkPrimer {
     private static final IBlockState DEFAULT_STATE;
     private final char[] data = new char[65536];

     public IBlockState getBlockState(int x, int y, int z) {
          IBlockState iblockstate = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(this.data[getBlockIndex(x, y, z)]);
          return iblockstate == null ? DEFAULT_STATE : iblockstate;
     }

     public void setBlockState(int x, int y, int z, IBlockState state) {
          this.data[getBlockIndex(x, y, z)] = (char)Block.BLOCK_STATE_IDS.get(state);
     }

     private static int getBlockIndex(int x, int y, int z) {
          return x << 12 | z << 8 | y;
     }

     public int findGroundBlockIdx(int x, int z) {
          int i = (x << 12 | z << 8) + 256 - 1;

          for(int j = 255; j >= 0; --j) {
               IBlockState iblockstate = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(this.data[i + j]);
               if (iblockstate != null && iblockstate != DEFAULT_STATE) {
                    return j;
               }
          }

          return 0;
     }

     static {
          DEFAULT_STATE = Blocks.AIR.getDefaultState();
     }
}
