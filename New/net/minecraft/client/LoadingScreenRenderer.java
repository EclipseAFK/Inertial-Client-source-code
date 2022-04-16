package net.minecraft.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MinecraftError;
import wtf.rich.api.utils.render.ClientHelper;
import wtf.rich.api.utils.render.DrawHelper;

public class LoadingScreenRenderer implements IProgressUpdate {
     private String message = "";
     private final Minecraft mc;
     private String currentlyDisplayedText = "";
     private long systemTime = Minecraft.getSystemTime();
     private boolean loadingSuccess;
     private final ScaledResolution scaledResolution;
     private final Framebuffer framebuffer;

     public LoadingScreenRenderer(Minecraft mcIn) {
          this.mc = mcIn;
          this.scaledResolution = new ScaledResolution(mcIn);
          this.framebuffer = new Framebuffer(mcIn.displayWidth, mcIn.displayHeight, false);
          this.framebuffer.setFramebufferFilter(9728);
     }

     public void resetProgressAndMessage(String message) {
          this.loadingSuccess = false;
          this.displayString(message);
     }

     public void displaySavingString(String message) {
          this.loadingSuccess = true;
          this.displayString(message);
     }

     private void displayString(String message) {
          this.currentlyDisplayedText = message;
          if (!this.mc.running) {
               if (!this.loadingSuccess) {
                    throw new MinecraftError();
               }
          } else {
               GlStateManager.clear(256);
               GlStateManager.matrixMode(5889);
               GlStateManager.loadIdentity();
               if (OpenGlHelper.isFramebufferEnabled()) {
                    ScaledResolution var10000 = this.scaledResolution;
                    int i = ScaledResolution.getScaleFactor();
                    GlStateManager.ortho(0.0D, (double)(this.scaledResolution.getScaledWidth() * i), (double)(this.scaledResolution.getScaledHeight() * i), 0.0D, 100.0D, 300.0D);
               } else {
                    ScaledResolution scaledresolution = new ScaledResolution(this.mc);
                    GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
               }

               GlStateManager.matrixMode(5888);
               GlStateManager.loadIdentity();
               GlStateManager.translate(0.0F, 0.0F, -200.0F);
          }

     }

     public void displayLoadingString(String message) {
          if (!this.mc.running) {
               if (!this.loadingSuccess) {
                    throw new MinecraftError();
               }
          } else {
               this.systemTime = 0L;
               this.message = message;
               this.setLoadingProgress(-1);
               this.systemTime = 0L;
          }

     }

     public void setLoadingProgress(int progress) {
          if (!this.mc.running) {
               if (!this.loadingSuccess) {
                    throw new MinecraftError();
               }
          } else {
               long i = Minecraft.getSystemTime();
               if (i - this.systemTime >= 100L) {
                    this.systemTime = i;
                    ScaledResolution scaledresolution = new ScaledResolution(this.mc);
                    int j = ScaledResolution.getScaleFactor();
                    int k = scaledresolution.getScaledWidth();
                    int l = scaledresolution.getScaledHeight();
                    if (OpenGlHelper.isFramebufferEnabled()) {
                         this.framebuffer.framebufferClear();
                    } else {
                         GlStateManager.clear(256);
                    }

                    this.framebuffer.bindFramebuffer(false);
                    GlStateManager.matrixMode(5889);
                    GlStateManager.loadIdentity();
                    GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
                    GlStateManager.matrixMode(5888);
                    GlStateManager.loadIdentity();
                    GlStateManager.translate(0.0F, 0.0F, -200.0F);
                    if (!OpenGlHelper.isFramebufferEnabled()) {
                         GlStateManager.clear(16640);
                    }

                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
                    float f = 32.0F;
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    bufferbuilder.pos(0.0D, (double)l, 0.0D).tex(0.0D, (double)((float)l / 32.0F)).color(64, 64, 64, 255).endVertex();
                    bufferbuilder.pos((double)k, (double)l, 0.0D).tex((double)((float)k / 32.0F), (double)((float)l / 32.0F)).color(64, 64, 64, 255).endVertex();
                    bufferbuilder.pos((double)k, 0.0D, 0.0D).tex((double)((float)k / 32.0F), 0.0D).color(64, 64, 64, 255).endVertex();
                    bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 255).endVertex();
                    tessellator.draw();
                    if (progress >= 0) {
                         int i1 = true;
                         int j1 = true;
                         int k1 = k / 2 - 50;
                         int l1 = l / 2 + 16;
                         GlStateManager.disableTexture2D();
                         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                         bufferbuilder.pos((double)k1, (double)l1, 0.0D).color(128, 128, 128, 255).endVertex();
                         bufferbuilder.pos((double)k1, (double)(l1 + 2), 0.0D).color(128, 128, 128, 255).endVertex();
                         bufferbuilder.pos((double)(k1 + 100), (double)(l1 + 2), 0.0D).color(128, 128, 128, 255).endVertex();
                         bufferbuilder.pos((double)(k1 + 100), (double)l1, 0.0D).color(128, 128, 128, 255).endVertex();
                         bufferbuilder.pos((double)k1, (double)l1, 0.0D).color(128, 255, 128, 255).endVertex();
                         bufferbuilder.pos((double)k1, (double)(l1 + 2), 0.0D).color(128, 255, 128, 255).endVertex();
                         bufferbuilder.pos((double)(k1 + progress), (double)(l1 + 2), 0.0D).color(128, 255, 128, 255).endVertex();
                         bufferbuilder.pos((double)(k1 + progress), (double)l1, 0.0D).color(128, 255, 128, 255).endVertex();
                         tessellator.draw();
                         GlStateManager.enableTexture2D();
                    }

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    Minecraft var10000 = this.mc;
                    Minecraft var10003 = this.mc;
                    Minecraft.fontRendererObj.drawStringWithShadow(this.currentlyDisplayedText, (float)((k - Minecraft.fontRendererObj.getStringWidth(this.currentlyDisplayedText)) / 2), (float)(l / 2 - 4 - 16), 16777215);
                    var10000 = this.mc;
                    var10003 = this.mc;
                    Minecraft.fontRendererObj.drawStringWithShadow(this.message, (float)((k - Minecraft.fontRendererObj.getStringWidth(this.message)) / 2), (float)(l / 2 - 4 + 8), 16777215);
                    this.framebuffer.unbindFramebuffer();
                    if (OpenGlHelper.isFramebufferEnabled()) {
                         this.framebuffer.framebufferRender(k * j, l * j);
                    }

                    this.mc.updateDisplay();

                    try {
                         Thread.yield();
                    } catch (Exception var15) {
                    }

                    for(int a = 0; a <= 360; a += 5) {
                         DrawHelper.drawCircle((float)(scaledresolution.getScaledWidth() / 2), (float)(scaledresolution.getScaledHeight() / 2), (float)(0 + a), 360.0F, 50.0F, 2.0F, false, ClientHelper.getClientColor(5.0F, (float)a, 5));
                    }
               }
          }

     }

     public void setDoneWorking() {
     }
}
