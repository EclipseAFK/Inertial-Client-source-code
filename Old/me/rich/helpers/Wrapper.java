package me.rich.helpers;

import com.sun.jna.platform.mac.MacFileUtils.FileManager;
import java.lang.reflect.Field;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Wrapper {
      private static Wrapper theWrapper = new Wrapper();
      public static Minecraft mc = Minecraft.getMinecraft();
      public static volatile Wrapper INSTANCE = new Wrapper();
      public static FontRenderer fr;
      public static FileManager fileManager;

      public static Wrapper getInstance() {
            return theWrapper;
      }

      public static float getCooldown() {
            return Minecraft.player.getCooledAttackStrength(0.0F);
      }

      public static FileManager getFileManager() {
            return fileManager;
      }

      public static Entity getRenderEntity() {
            return mc.getRenderViewEntity();
      }

      public static Block getBlock(BlockPos pos) {
            return Minecraft.getMinecraft().world.getBlockState(pos).getBlock();
      }

      public static Minecraft getMinecraft() {
            return Minecraft.getMinecraft();
      }

      public static EntityPlayerSP getPlayer() {
            getMinecraft();
            return Minecraft.player;
      }

      public static World getWorld() {
            return getMinecraft().world;
      }

      public static void setPrivateValue(Class classToAccess, Object instance, Object value, String... fieldNames) {
            try {
                  findField(classToAccess, fieldNames).set(instance, value);
            } catch (Exception var5) {
            }

      }

      private static Field findField(Class clazz, String... fieldNames) {
            Exception failed = null;
            int length = fieldNames.length;
            int i = 0;

            while(i < length) {
                  String fieldName = fieldNames[i];

                  try {
                        Field f = clazz.getDeclaredField(fieldName);
                        f.setAccessible(true);
                        return f;
                  } catch (Exception var7) {
                        ++i;
                  }
            }

            return null;
      }

      public static Minecraft mc() {
            return Minecraft.getMinecraft();
      }

      public static EntityPlayerSP player() {
            mc();
            return Minecraft.player;
      }

      public WorldClient world() {
            return mc().world;
      }

      public GameSettings mcSettings() {
            return mc().gameSettings;
      }

      public FontRenderer fontRenderer() {
            mc();
            return Minecraft.fontRendererObj;
      }

      public void sendPacket(CPacketAnimation cPacketAnimation) {
            player().connection.sendPacket(cPacketAnimation);
      }

      public InventoryPlayer inventory() {
            return player().inventory;
      }

      public PlayerControllerMP controller() {
            return mc().playerController;
      }

      public void sendPacket(CPacketPlayer cPacketPlayer) {
      }

      static {
            Minecraft.getMinecraft();
            fr = Minecraft.fontRendererObj;
      }
}
