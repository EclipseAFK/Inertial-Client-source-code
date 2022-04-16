package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelBlock {
     private static final Logger LOGGER = LogManager.getLogger();
     @VisibleForTesting
     static final Gson SERIALIZER = (new GsonBuilder()).registerTypeAdapter(ModelBlock.class, new ModelBlock.Deserializer()).registerTypeAdapter(BlockPart.class, new BlockPart.Deserializer()).registerTypeAdapter(BlockPartFace.class, new BlockPartFace.Deserializer()).registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer()).registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer()).registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer()).create();
     private final List elements;
     private final boolean gui3d;
     private final boolean ambientOcclusion;
     private final ItemCameraTransforms cameraTransforms;
     private final List overrides;
     public String name = "";
     @VisibleForTesting
     protected final Map textures;
     @VisibleForTesting
     protected ModelBlock parent;
     @VisibleForTesting
     protected ResourceLocation parentLocation;

     public static ModelBlock deserialize(Reader readerIn) {
          return (ModelBlock)JsonUtils.gsonDeserialize(SERIALIZER, readerIn, ModelBlock.class, false);
     }

     public static ModelBlock deserialize(String jsonString) {
          return deserialize((Reader)(new StringReader(jsonString)));
     }

     public ModelBlock(@Nullable ResourceLocation parentLocationIn, List elementsIn, Map texturesIn, boolean ambientOcclusionIn, boolean gui3dIn, ItemCameraTransforms cameraTransformsIn, List overridesIn) {
          this.elements = elementsIn;
          this.ambientOcclusion = ambientOcclusionIn;
          this.gui3d = gui3dIn;
          this.textures = texturesIn;
          this.parentLocation = parentLocationIn;
          this.cameraTransforms = cameraTransformsIn;
          this.overrides = overridesIn;
     }

     public List getElements() {
          return this.elements.isEmpty() && this.hasParent() ? this.parent.getElements() : this.elements;
     }

     private boolean hasParent() {
          return this.parent != null;
     }

     public boolean isAmbientOcclusion() {
          return this.hasParent() ? this.parent.isAmbientOcclusion() : this.ambientOcclusion;
     }

     public boolean isGui3d() {
          return this.gui3d;
     }

     public boolean isResolved() {
          return this.parentLocation == null || this.parent != null && this.parent.isResolved();
     }

     public void getParentFromMap(Map p_178299_1_) {
          if (this.parentLocation != null) {
               this.parent = (ModelBlock)p_178299_1_.get(this.parentLocation);
          }

     }

     public Collection getOverrideLocations() {
          Set set = Sets.newHashSet();
          Iterator var2 = this.overrides.iterator();

          while(var2.hasNext()) {
               ItemOverride itemoverride = (ItemOverride)var2.next();
               set.add(itemoverride.getLocation());
          }

          return set;
     }

     protected List getOverrides() {
          return this.overrides;
     }

     public ItemOverrideList createOverrides() {
          return this.overrides.isEmpty() ? ItemOverrideList.NONE : new ItemOverrideList(this.overrides);
     }

     public boolean isTexturePresent(String textureName) {
          return !"missingno".equals(this.resolveTextureName(textureName));
     }

     public String resolveTextureName(String textureName) {
          if (!this.startsWithHash(textureName)) {
               textureName = '#' + textureName;
          }

          return this.resolveTextureName(textureName, new ModelBlock.Bookkeep(this));
     }

     private String resolveTextureName(String textureName, ModelBlock.Bookkeep p_178302_2_) {
          if (this.startsWithHash(textureName)) {
               if (this == p_178302_2_.modelExt) {
                    LOGGER.warn("Unable to resolve texture due to upward reference: {} in {}", textureName, this.name);
                    return "missingno";
               } else {
                    String s = (String)this.textures.get(textureName.substring(1));
                    if (s == null && this.hasParent()) {
                         s = this.parent.resolveTextureName(textureName, p_178302_2_);
                    }

                    p_178302_2_.modelExt = this;
                    if (s != null && this.startsWithHash(s)) {
                         s = p_178302_2_.model.resolveTextureName(s, p_178302_2_);
                    }

                    return s != null && !this.startsWithHash(s) ? s : "missingno";
               }
          } else {
               return textureName;
          }
     }

     private boolean startsWithHash(String hash) {
          return hash.charAt(0) == '#';
     }

     @Nullable
     public ResourceLocation getParentLocation() {
          return this.parentLocation;
     }

     public ModelBlock getRootModel() {
          return this.hasParent() ? this.parent.getRootModel() : this;
     }

     public ItemCameraTransforms getAllTransforms() {
          ItemTransformVec3f itemtransformvec3f = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
          ItemTransformVec3f itemtransformvec3f1 = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
          ItemTransformVec3f itemtransformvec3f2 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
          ItemTransformVec3f itemtransformvec3f3 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
          ItemTransformVec3f itemtransformvec3f4 = this.getTransform(ItemCameraTransforms.TransformType.HEAD);
          ItemTransformVec3f itemtransformvec3f5 = this.getTransform(ItemCameraTransforms.TransformType.GUI);
          ItemTransformVec3f itemtransformvec3f6 = this.getTransform(ItemCameraTransforms.TransformType.GROUND);
          ItemTransformVec3f itemtransformvec3f7 = this.getTransform(ItemCameraTransforms.TransformType.FIXED);
          return new ItemCameraTransforms(itemtransformvec3f, itemtransformvec3f1, itemtransformvec3f2, itemtransformvec3f3, itemtransformvec3f4, itemtransformvec3f5, itemtransformvec3f6, itemtransformvec3f7);
     }

     private ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType type) {
          return this.parent != null && !this.cameraTransforms.hasCustomTransform(type) ? this.parent.getTransform(type) : this.cameraTransforms.getTransform(type);
     }

     public static void checkModelHierarchy(Map p_178312_0_) {
          Iterator var1 = p_178312_0_.values().iterator();

          while(var1.hasNext()) {
               ModelBlock modelblock = (ModelBlock)var1.next();

               try {
                    ModelBlock modelblock1 = modelblock.parent;

                    for(ModelBlock modelblock2 = modelblock1.parent; modelblock1 != modelblock2; modelblock2 = modelblock2.parent.parent) {
                         modelblock1 = modelblock1.parent;
                    }

                    throw new ModelBlock.LoopException();
               } catch (NullPointerException var5) {
               }
          }

     }

     public static class LoopException extends RuntimeException {
     }

     public static class Deserializer implements JsonDeserializer {
          public ModelBlock deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
               JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
               List list = this.getModelElements(p_deserialize_3_, jsonobject);
               String s = this.getParent(jsonobject);
               Map map = this.getTextures(jsonobject);
               boolean flag = this.getAmbientOcclusionEnabled(jsonobject);
               ItemCameraTransforms itemcameratransforms = ItemCameraTransforms.DEFAULT;
               if (jsonobject.has("display")) {
                    JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonobject, "display");
                    itemcameratransforms = (ItemCameraTransforms)p_deserialize_3_.deserialize(jsonobject1, ItemCameraTransforms.class);
               }

               List list1 = this.getItemOverrides(p_deserialize_3_, jsonobject);
               ResourceLocation resourcelocation = s.isEmpty() ? null : new ResourceLocation(s);
               return new ModelBlock(resourcelocation, list, map, flag, true, itemcameratransforms, list1);
          }

          protected List getItemOverrides(JsonDeserializationContext deserializationContext, JsonObject object) {
               List list = Lists.newArrayList();
               if (object.has("overrides")) {
                    Iterator var4 = JsonUtils.getJsonArray(object, "overrides").iterator();

                    while(var4.hasNext()) {
                         JsonElement jsonelement = (JsonElement)var4.next();
                         list.add((ItemOverride)deserializationContext.deserialize(jsonelement, ItemOverride.class));
                    }
               }

               return list;
          }

          private Map getTextures(JsonObject object) {
               Map map = Maps.newHashMap();
               if (object.has("textures")) {
                    JsonObject jsonobject = object.getAsJsonObject("textures");
                    Iterator var4 = jsonobject.entrySet().iterator();

                    while(var4.hasNext()) {
                         Entry entry = (Entry)var4.next();
                         map.put(entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
                    }
               }

               return map;
          }

          private String getParent(JsonObject object) {
               return JsonUtils.getString(object, "parent", "");
          }

          protected boolean getAmbientOcclusionEnabled(JsonObject object) {
               return JsonUtils.getBoolean(object, "ambientocclusion", true);
          }

          protected List getModelElements(JsonDeserializationContext deserializationContext, JsonObject object) {
               List list = Lists.newArrayList();
               if (object.has("elements")) {
                    Iterator var4 = JsonUtils.getJsonArray(object, "elements").iterator();

                    while(var4.hasNext()) {
                         JsonElement jsonelement = (JsonElement)var4.next();
                         list.add((BlockPart)deserializationContext.deserialize(jsonelement, BlockPart.class));
                    }
               }

               return list;
          }
     }

     static final class Bookkeep {
          public final ModelBlock model;
          public ModelBlock modelExt;

          private Bookkeep(ModelBlock modelIn) {
               this.model = modelIn;
          }

          // $FF: synthetic method
          Bookkeep(ModelBlock x0, Object x1) {
               this(x0);
          }
     }
}