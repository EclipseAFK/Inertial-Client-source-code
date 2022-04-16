package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import optifine.Config;
import shadersmod.client.Shaders;

public class LayerSpiderEyes implements LayerRenderer {
     private static final ResourceLocation SPIDER_EYES = new ResourceLocation("textures/entity/spider_eyes.png");
     private final RenderSpider spiderRenderer;

     public LayerSpiderEyes(RenderSpider spiderRendererIn) {
          this.spiderRenderer = spiderRendererIn;
     }

     public void doRenderLayer(EntitySpider entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
          this.spiderRenderer.bindTexture(SPIDER_EYES);
          GlStateManager.enableBlend();
          GlStateManager.disableAlpha();
          GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
          if (entitylivingbaseIn.isInvisible()) {
               GlStateManager.depthMask(false);
          } else {
               GlStateManager.depthMask(true);
          }

          int i = '\uf0f0';
          int j = i % 65536;
          int k = i / 65536;
          OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
          GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
          Minecraft.getMinecraft().entityRenderer.func_191514_d(true);
          if (Config.isShaders()) {
               Shaders.beginSpiderEyes();
          }

          Config.getRenderGlobal().renderOverlayEyes = true;
          this.spiderRenderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
          Config.getRenderGlobal().renderOverlayEyes = false;
          if (Config.isShaders()) {
               Shaders.endSpiderEyes();
          }

          Minecraft.getMinecraft().entityRenderer.func_191514_d(false);
          int i = entitylivingbaseIn.getBrightnessForRender();
          j = i % 65536;
          k = i / 65536;
          OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
          this.spiderRenderer.setLightmap(entitylivingbaseIn);
          GlStateManager.disableBlend();
          GlStateManager.enableAlpha();
     }

     public boolean shouldCombineTextures() {
          return false;
     }
}
