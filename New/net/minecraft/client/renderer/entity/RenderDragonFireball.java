package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.util.ResourceLocation;

public class RenderDragonFireball extends Render {
     private static final ResourceLocation DRAGON_FIREBALL_TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

     public RenderDragonFireball(RenderManager renderManagerIn) {
          super(renderManagerIn);
     }

     public void doRender(EntityDragonFireball entity, double x, double y, double z, float entityYaw, float partialTicks) {
          GlStateManager.pushMatrix();
          this.bindEntityTexture(entity);
          GlStateManager.translate((float)x, (float)y, (float)z);
          GlStateManager.enableRescaleNormal();
          GlStateManager.scale(2.0F, 2.0F, 2.0F);
          Tessellator tessellator = Tessellator.getInstance();
          BufferBuilder bufferbuilder = tessellator.getBuffer();
          float f = 1.0F;
          float f1 = 0.5F;
          float f2 = 0.25F;
          RenderManager var10001 = this.renderManager;
          GlStateManager.rotate(180.0F - RenderManager.playerViewY, 0.0F, 1.0F, 0.0F);
          float var10000 = (float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1);
          var10001 = this.renderManager;
          GlStateManager.rotate(var10000 * -RenderManager.playerViewX, 1.0F, 0.0F, 0.0F);
          if (this.renderOutlines) {
               GlStateManager.enableColorMaterial();
               GlStateManager.enableOutlineMode(this.getTeamColor(entity));
          }

          bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
          bufferbuilder.pos(-0.5D, -0.25D, 0.0D).tex(0.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
          bufferbuilder.pos(0.5D, -0.25D, 0.0D).tex(1.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
          bufferbuilder.pos(0.5D, 0.75D, 0.0D).tex(1.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
          bufferbuilder.pos(-0.5D, 0.75D, 0.0D).tex(0.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
          tessellator.draw();
          if (this.renderOutlines) {
               GlStateManager.disableOutlineMode();
               GlStateManager.disableColorMaterial();
          }

          GlStateManager.disableRescaleNormal();
          GlStateManager.popMatrix();
          super.doRender(entity, x, y, z, entityYaw, partialTicks);
     }

     protected ResourceLocation getEntityTexture(EntityDragonFireball entity) {
          return DRAGON_FIREBALL_TEXTURE;
     }
}
