package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class SimpleBakedModel implements IBakedModel {
     protected final List generalQuads;
     protected final Map faceQuads;
     protected final boolean ambientOcclusion;
     protected final boolean gui3d;
     protected final TextureAtlasSprite texture;
     protected final ItemCameraTransforms cameraTransforms;
     protected final ItemOverrideList itemOverrideList;

     public SimpleBakedModel(List generalQuadsIn, Map faceQuadsIn, boolean ambientOcclusionIn, boolean gui3dIn, TextureAtlasSprite textureIn, ItemCameraTransforms cameraTransformsIn, ItemOverrideList itemOverrideListIn) {
          this.generalQuads = generalQuadsIn;
          this.faceQuads = faceQuadsIn;
          this.ambientOcclusion = ambientOcclusionIn;
          this.gui3d = gui3dIn;
          this.texture = textureIn;
          this.cameraTransforms = cameraTransformsIn;
          this.itemOverrideList = itemOverrideListIn;
     }

     public List getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
          return side == null ? this.generalQuads : (List)this.faceQuads.get(side);
     }

     public boolean isAmbientOcclusion() {
          return this.ambientOcclusion;
     }

     public boolean isGui3d() {
          return this.gui3d;
     }

     public boolean isBuiltInRenderer() {
          return false;
     }

     public TextureAtlasSprite getParticleTexture() {
          return this.texture;
     }

     public ItemCameraTransforms getItemCameraTransforms() {
          return this.cameraTransforms;
     }

     public ItemOverrideList getOverrides() {
          return this.itemOverrideList;
     }

     public static class Builder {
          private final List builderGeneralQuads;
          private final Map builderFaceQuads;
          private final ItemOverrideList builderItemOverrideList;
          private final boolean builderAmbientOcclusion;
          private TextureAtlasSprite builderTexture;
          private final boolean builderGui3d;
          private final ItemCameraTransforms builderCameraTransforms;

          public Builder(ModelBlock model, ItemOverrideList overrides) {
               this(model.isAmbientOcclusion(), model.isGui3d(), model.getAllTransforms(), overrides);
          }

          public Builder(IBlockState state, IBakedModel model, TextureAtlasSprite texture, BlockPos pos) {
               this(model.isAmbientOcclusion(), model.isGui3d(), model.getItemCameraTransforms(), model.getOverrides());
               this.builderTexture = model.getParticleTexture();
               long i = MathHelper.getPositionRandom(pos);
               EnumFacing[] var7 = EnumFacing.values();
               int var8 = var7.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                    EnumFacing enumfacing = var7[var9];
                    this.addFaceQuads(state, model, texture, enumfacing, i);
               }

               this.addGeneralQuads(state, model, texture, i);
          }

          private Builder(boolean ambientOcclusion, boolean gui3d, ItemCameraTransforms transforms, ItemOverrideList overrides) {
               this.builderGeneralQuads = Lists.newArrayList();
               this.builderFaceQuads = Maps.newEnumMap(EnumFacing.class);
               EnumFacing[] var5 = EnumFacing.values();
               int var6 = var5.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                    EnumFacing enumfacing = var5[var7];
                    this.builderFaceQuads.put(enumfacing, Lists.newArrayList());
               }

               this.builderItemOverrideList = overrides;
               this.builderAmbientOcclusion = ambientOcclusion;
               this.builderGui3d = gui3d;
               this.builderCameraTransforms = transforms;
          }

          private void addFaceQuads(IBlockState p_188644_1_, IBakedModel p_188644_2_, TextureAtlasSprite p_188644_3_, EnumFacing p_188644_4_, long p_188644_5_) {
               Iterator var7 = p_188644_2_.getQuads(p_188644_1_, p_188644_4_, p_188644_5_).iterator();

               while(var7.hasNext()) {
                    BakedQuad bakedquad = (BakedQuad)var7.next();
                    this.addFaceQuad(p_188644_4_, new BakedQuadRetextured(bakedquad, p_188644_3_));
               }

          }

          private void addGeneralQuads(IBlockState p_188645_1_, IBakedModel p_188645_2_, TextureAtlasSprite p_188645_3_, long p_188645_4_) {
               Iterator var6 = p_188645_2_.getQuads(p_188645_1_, (EnumFacing)null, p_188645_4_).iterator();

               while(var6.hasNext()) {
                    BakedQuad bakedquad = (BakedQuad)var6.next();
                    this.addGeneralQuad(new BakedQuadRetextured(bakedquad, p_188645_3_));
               }

          }

          public SimpleBakedModel.Builder addFaceQuad(EnumFacing facing, BakedQuad quad) {
               ((List)this.builderFaceQuads.get(facing)).add(quad);
               return this;
          }

          public SimpleBakedModel.Builder addGeneralQuad(BakedQuad quad) {
               this.builderGeneralQuads.add(quad);
               return this;
          }

          public SimpleBakedModel.Builder setTexture(TextureAtlasSprite texture) {
               this.builderTexture = texture;
               return this;
          }

          public IBakedModel makeBakedModel() {
               if (this.builderTexture == null) {
                    throw new RuntimeException("Missing particle!");
               } else {
                    return new SimpleBakedModel(this.builderGeneralQuads, this.builderFaceQuads, this.builderAmbientOcclusion, this.builderGui3d, this.builderTexture, this.builderCameraTransforms, this.builderItemOverrideList);
               }
          }
     }
}
