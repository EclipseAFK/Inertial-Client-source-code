package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelWither;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerWitherAura;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.ResourceLocation;

public class RenderWither extends RenderLiving {
     private static final ResourceLocation INVULNERABLE_WITHER_TEXTURES = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
     private static final ResourceLocation WITHER_TEXTURES = new ResourceLocation("textures/entity/wither/wither.png");

     public RenderWither(RenderManager renderManagerIn) {
          super(renderManagerIn, new ModelWither(0.0F), 1.0F);
          this.addLayer(new LayerWitherAura(this));
     }

     protected ResourceLocation getEntityTexture(EntityWither entity) {
          int i = entity.getInvulTime();
          return i <= 0 || i <= 80 && i / 5 % 2 == 1 ? WITHER_TEXTURES : INVULNERABLE_WITHER_TEXTURES;
     }

     protected void preRenderCallback(EntityWither entitylivingbaseIn, float partialTickTime) {
          float f = 2.0F;
          int i = entitylivingbaseIn.getInvulTime();
          if (i > 0) {
               f -= ((float)i - partialTickTime) / 220.0F * 0.5F;
          }

          GlStateManager.scale(f, f, f);
     }
}
