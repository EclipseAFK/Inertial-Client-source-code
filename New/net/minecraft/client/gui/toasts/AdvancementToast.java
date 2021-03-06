package net.minecraft.client.gui.toasts;

import java.util.Iterator;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class AdvancementToast implements IToast {
     private final Advancement field_193679_c;
     private boolean field_194168_d = false;

     public AdvancementToast(Advancement p_i47490_1_) {
          this.field_193679_c = p_i47490_1_;
     }

     public IToast.Visibility func_193653_a(GuiToast p_193653_1_, long p_193653_2_) {
          p_193653_1_.func_192989_b().getTextureManager().bindTexture(field_193654_a);
          GlStateManager.color(1.0F, 1.0F, 1.0F);
          DisplayInfo displayinfo = this.field_193679_c.func_192068_c();
          p_193653_1_.drawTexturedModalRect(0, 0, 0, 0, 160, 32);
          if (displayinfo != null) {
               p_193653_1_.func_192989_b();
               List list = Minecraft.fontRendererObj.listFormattedStringToWidth(displayinfo.func_192297_a().getFormattedText(), 125);
               int i = displayinfo.func_192291_d() == FrameType.CHALLENGE ? 16746751 : 16776960;
               if (list.size() == 1) {
                    p_193653_1_.func_192989_b();
                    Minecraft.fontRendererObj.drawString(I18n.format("advancements.toast." + displayinfo.func_192291_d().func_192307_a()), 30.0F, 7.0F, i | -16777216);
                    p_193653_1_.func_192989_b();
                    Minecraft.fontRendererObj.drawString(displayinfo.func_192297_a().getFormattedText(), 30.0F, 18.0F, -1);
               } else {
                    int j = true;
                    float f = 300.0F;
                    int i1;
                    if (p_193653_2_ < 1500L) {
                         i1 = MathHelper.floor(MathHelper.clamp((float)(1500L - p_193653_2_) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                         p_193653_1_.func_192989_b();
                         Minecraft.fontRendererObj.drawString(I18n.format("advancements.toast." + displayinfo.func_192291_d().func_192307_a()), 30.0F, 11.0F, i | i1);
                    } else {
                         i1 = MathHelper.floor(MathHelper.clamp((float)(p_193653_2_ - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                         int var10001 = list.size();
                         p_193653_1_.func_192989_b();
                         int l = 16 - var10001 * Minecraft.fontRendererObj.FONT_HEIGHT / 2;

                         for(Iterator var11 = list.iterator(); var11.hasNext(); l += Minecraft.fontRendererObj.FONT_HEIGHT) {
                              String s = (String)var11.next();
                              p_193653_1_.func_192989_b();
                              Minecraft.fontRendererObj.drawString(s, 30.0F, (float)l, 16777215 | i1);
                              p_193653_1_.func_192989_b();
                         }
                    }
               }

               if (!this.field_194168_d && p_193653_2_ > 0L) {
                    this.field_194168_d = true;
                    if (displayinfo.func_192291_d() == FrameType.CHALLENGE) {
                         p_193653_1_.func_192989_b().getSoundHandler().playSound(PositionedSoundRecord.func_194007_a(SoundEvents.field_194228_if, 1.0F, 1.0F));
                    }
               }

               RenderHelper.enableGUIStandardItemLighting();
               p_193653_1_.func_192989_b().getRenderItem().renderItemAndEffectIntoGUI((EntityLivingBase)null, displayinfo.func_192298_b(), 8, 8);
               return p_193653_2_ >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
          } else {
               return IToast.Visibility.HIDE;
          }
     }
}
