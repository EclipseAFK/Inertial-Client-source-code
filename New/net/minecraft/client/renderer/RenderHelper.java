package net.minecraft.client.renderer;

import java.awt.Color;
import java.nio.FloatBuffer;
import net.minecraft.util.math.Vec3d;

public class RenderHelper {
     private static final FloatBuffer COLOR_BUFFER = GLAllocation.createDirectFloatBuffer(4);
     private static final Vec3d LIGHT0_POS = (new Vec3d(0.20000000298023224D, 1.0D, -0.699999988079071D)).normalize();
     private static final Vec3d LIGHT1_POS = (new Vec3d(-0.20000000298023224D, 1.0D, 0.699999988079071D)).normalize();

     public static void disableStandardItemLighting() {
          GlStateManager.disableLighting();
          GlStateManager.disableLight(0);
          GlStateManager.disableLight(1);
          GlStateManager.disableColorMaterial();
     }

     public static Color getGradientOffset(Color color1, Color color2, double offset, int alpha) {
          double inverse_percent;
          int redPart;
          if (offset > 1.0D) {
               inverse_percent = offset % 1.0D;
               redPart = (int)offset;
               offset = redPart % 2 == 0 ? inverse_percent : 1.0D - inverse_percent;
          }

          inverse_percent = 1.0D - offset;
          redPart = (int)((double)color1.getRed() * inverse_percent + (double)color2.getRed() * offset);
          int greenPart = (int)((double)color1.getGreen() * inverse_percent + (double)color2.getGreen() * offset);
          int bluePart = (int)((double)color1.getBlue() * inverse_percent + (double)color2.getBlue() * offset);
          return new Color(redPart, greenPart, bluePart, alpha);
     }

     public static void enableStandardItemLighting() {
          GlStateManager.enableLighting();
          GlStateManager.enableLight(0);
          GlStateManager.enableLight(1);
          GlStateManager.enableColorMaterial();
          GlStateManager.colorMaterial(1032, 5634);
          GlStateManager.glLight(16384, 4611, setColorBuffer(LIGHT0_POS.xCoord, LIGHT0_POS.yCoord, LIGHT0_POS.zCoord, 0.0D));
          float f = 0.6F;
          GlStateManager.glLight(16384, 4609, setColorBuffer(0.6F, 0.6F, 0.6F, 1.0F));
          GlStateManager.glLight(16384, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
          GlStateManager.glLight(16384, 4610, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
          GlStateManager.glLight(16385, 4611, setColorBuffer(LIGHT1_POS.xCoord, LIGHT1_POS.yCoord, LIGHT1_POS.zCoord, 0.0D));
          GlStateManager.glLight(16385, 4609, setColorBuffer(0.6F, 0.6F, 0.6F, 1.0F));
          GlStateManager.glLight(16385, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
          GlStateManager.glLight(16385, 4610, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
          GlStateManager.shadeModel(7424);
          float f1 = 0.4F;
          GlStateManager.glLightModel(2899, setColorBuffer(0.4F, 0.4F, 0.4F, 1.0F));
     }

     private static FloatBuffer setColorBuffer(double p_74517_0_, double p_74517_2_, double p_74517_4_, double p_74517_6_) {
          return setColorBuffer((float)p_74517_0_, (float)p_74517_2_, (float)p_74517_4_, (float)p_74517_6_);
     }

     public static FloatBuffer setColorBuffer(float p_74521_0_, float p_74521_1_, float p_74521_2_, float p_74521_3_) {
          COLOR_BUFFER.clear();
          COLOR_BUFFER.put(p_74521_0_).put(p_74521_1_).put(p_74521_2_).put(p_74521_3_);
          COLOR_BUFFER.flip();
          return COLOR_BUFFER;
     }

     public static void enableGUIStandardItemLighting() {
          GlStateManager.pushMatrix();
          GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
          GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);
          enableStandardItemLighting();
          GlStateManager.popMatrix();
     }
}
