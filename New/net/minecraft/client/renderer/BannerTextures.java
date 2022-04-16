package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;

public class BannerTextures {
     public static final BannerTextures.Cache BANNER_DESIGNS = new BannerTextures.Cache("B", new ResourceLocation("textures/entity/banner_base.png"), "textures/entity/banner/");
     public static final BannerTextures.Cache SHIELD_DESIGNS = new BannerTextures.Cache("S", new ResourceLocation("textures/entity/shield_base.png"), "textures/entity/shield/");
     public static final ResourceLocation SHIELD_BASE_TEXTURE = new ResourceLocation("textures/entity/shield_base_nopattern.png");
     public static final ResourceLocation BANNER_BASE_TEXTURE = new ResourceLocation("textures/entity/banner/base.png");

     static class CacheEntry {
          public long lastUseMillis;
          public ResourceLocation textureLocation;

          private CacheEntry() {
          }

          // $FF: synthetic method
          CacheEntry(Object x0) {
               this();
          }
     }

     public static class Cache {
          private final Map cacheMap = Maps.newLinkedHashMap();
          private final ResourceLocation cacheResourceLocation;
          private final String cacheResourceBase;
          private final String cacheId;

          public Cache(String id, ResourceLocation baseResource, String resourcePath) {
               this.cacheId = id;
               this.cacheResourceLocation = baseResource;
               this.cacheResourceBase = resourcePath;
          }

          @Nullable
          public ResourceLocation getResourceLocation(String id, List patternList, List colorList) {
               if (id.isEmpty()) {
                    return null;
               } else {
                    id = this.cacheId + id;
                    BannerTextures.CacheEntry bannertextures$cacheentry = (BannerTextures.CacheEntry)this.cacheMap.get(id);
                    if (bannertextures$cacheentry == null) {
                         if (this.cacheMap.size() >= 256 && !this.freeCacheSlot()) {
                              return BannerTextures.BANNER_BASE_TEXTURE;
                         }

                         List list = Lists.newArrayList();
                         Iterator var6 = patternList.iterator();

                         while(var6.hasNext()) {
                              BannerPattern bannerpattern = (BannerPattern)var6.next();
                              list.add(this.cacheResourceBase + bannerpattern.func_190997_a() + ".png");
                         }

                         bannertextures$cacheentry = new BannerTextures.CacheEntry();
                         bannertextures$cacheentry.textureLocation = new ResourceLocation(id);
                         Minecraft.getMinecraft().getTextureManager().loadTexture(bannertextures$cacheentry.textureLocation, new LayeredColorMaskTexture(this.cacheResourceLocation, list, colorList));
                         this.cacheMap.put(id, bannertextures$cacheentry);
                    }

                    bannertextures$cacheentry.lastUseMillis = System.currentTimeMillis();
                    return bannertextures$cacheentry.textureLocation;
               }
          }

          private boolean freeCacheSlot() {
               long i = System.currentTimeMillis();
               Iterator iterator = this.cacheMap.keySet().iterator();

               BannerTextures.CacheEntry bannertextures$cacheentry;
               do {
                    if (!iterator.hasNext()) {
                         return this.cacheMap.size() < 256;
                    }

                    String s = (String)iterator.next();
                    bannertextures$cacheentry = (BannerTextures.CacheEntry)this.cacheMap.get(s);
               } while(i - bannertextures$cacheentry.lastUseMillis <= 5000L);

               Minecraft.getMinecraft().getTextureManager().deleteTexture(bannertextures$cacheentry.textureLocation);
               iterator.remove();
               return true;
          }
     }
}
