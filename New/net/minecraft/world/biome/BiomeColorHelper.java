package net.minecraft.world.biome;

import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BiomeColorHelper {
     private static final BiomeColorHelper.ColorResolver GRASS_COLOR = new BiomeColorHelper.ColorResolver() {
          public int getColorAtPos(Biome biome, BlockPos blockPosition) {
               return biome.getGrassColorAtPos(blockPosition);
          }
     };
     private static final BiomeColorHelper.ColorResolver FOLIAGE_COLOR = new BiomeColorHelper.ColorResolver() {
          public int getColorAtPos(Biome biome, BlockPos blockPosition) {
               return biome.getFoliageColorAtPos(blockPosition);
          }
     };
     private static final BiomeColorHelper.ColorResolver WATER_COLOR = new BiomeColorHelper.ColorResolver() {
          public int getColorAtPos(Biome biome, BlockPos blockPosition) {
               return biome.getWaterColor();
          }
     };

     private static int getColorAtPos(IBlockAccess blockAccess, BlockPos pos, BiomeColorHelper.ColorResolver colorResolver) {
          int i = 0;
          int j = 0;
          int k = 0;

          int l;
          for(Iterator var6 = BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1)).iterator(); var6.hasNext(); k += l & 255) {
               BlockPos.MutableBlockPos blockpos$mutableblockpos = (BlockPos.MutableBlockPos)var6.next();
               l = colorResolver.getColorAtPos(blockAccess.getBiome(blockpos$mutableblockpos), blockpos$mutableblockpos);
               i += (l & 16711680) >> 16;
               j += (l & '\uff00') >> 8;
          }

          return (i / 9 & 255) << 16 | (j / 9 & 255) << 8 | k / 9 & 255;
     }

     public static int getGrassColorAtPos(IBlockAccess blockAccess, BlockPos pos) {
          return getColorAtPos(blockAccess, pos, GRASS_COLOR);
     }

     public static int getFoliageColorAtPos(IBlockAccess blockAccess, BlockPos pos) {
          return getColorAtPos(blockAccess, pos, FOLIAGE_COLOR);
     }

     public static int getWaterColorAtPos(IBlockAccess blockAccess, BlockPos pos) {
          return getColorAtPos(blockAccess, pos, WATER_COLOR);
     }

     interface ColorResolver {
          int getColorAtPos(Biome var1, BlockPos var2);
     }
}