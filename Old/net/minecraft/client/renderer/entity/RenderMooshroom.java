package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelCow;
import net.minecraft.client.renderer.entity.layers.LayerMooshroomMushroom;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.util.ResourceLocation;

public class RenderMooshroom extends RenderLiving {
      private static final ResourceLocation MOOSHROOM_TEXTURES = new ResourceLocation("textures/entity/cow/mooshroom.png");

      public RenderMooshroom(RenderManager p_i47200_1_) {
            super(p_i47200_1_, new ModelCow(), 0.7F);
            this.addLayer(new LayerMooshroomMushroom(this));
      }

      public ModelCow getMainModel() {
            return (ModelCow)super.getMainModel();
      }

      protected ResourceLocation getEntityTexture(EntityMooshroom entity) {
            return MOOSHROOM_TEXTURES;
      }
}
