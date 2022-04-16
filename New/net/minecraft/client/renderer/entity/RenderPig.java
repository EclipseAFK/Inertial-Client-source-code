package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelPig;
import net.minecraft.client.renderer.entity.layers.LayerSaddle;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;

public class RenderPig extends RenderLiving {
     private static final ResourceLocation PIG_TEXTURES = new ResourceLocation("textures/entity/pig/pig.png");

     public RenderPig(RenderManager p_i47198_1_) {
          super(p_i47198_1_, new ModelPig(), 0.7F);
          this.addLayer(new LayerSaddle(this));
     }

     protected ResourceLocation getEntityTexture(EntityPig entity) {
          return PIG_TEXTURES;
     }
}
