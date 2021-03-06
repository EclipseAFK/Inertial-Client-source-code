package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.util.math.MathHelper;
import optifine.MathUtils;

public class Stitcher {
      private final int mipmapLevelStitcher;
      private final Set setStitchHolders = Sets.newHashSetWithExpectedSize(256);
      private final List stitchSlots = Lists.newArrayListWithCapacity(256);
      private int currentWidth;
      private int currentHeight;
      private final int maxWidth;
      private final int maxHeight;
      private final int maxTileDimension;

      public Stitcher(int maxWidthIn, int maxHeightIn, int maxTileDimensionIn, int mipmapLevelStitcherIn) {
            this.mipmapLevelStitcher = mipmapLevelStitcherIn;
            this.maxWidth = maxWidthIn;
            this.maxHeight = maxHeightIn;
            this.maxTileDimension = maxTileDimensionIn;
      }

      public int getCurrentWidth() {
            return this.currentWidth;
      }

      public int getCurrentHeight() {
            return this.currentHeight;
      }

      public void addSprite(TextureAtlasSprite textureAtlas) {
            Stitcher.Holder stitcher$holder = new Stitcher.Holder(textureAtlas, this.mipmapLevelStitcher);
            if (this.maxTileDimension > 0) {
                  stitcher$holder.setNewDimension(this.maxTileDimension);
            }

            this.setStitchHolders.add(stitcher$holder);
      }

      public void doStitch() {
            Stitcher.Holder[] astitcher$holder = (Stitcher.Holder[])((Stitcher.Holder[])this.setStitchHolders.toArray(new Stitcher.Holder[this.setStitchHolders.size()]));
            Arrays.sort(astitcher$holder);
            Stitcher.Holder[] var2 = astitcher$holder;
            int var3 = astitcher$holder.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                  Stitcher.Holder stitcher$holder = var2[var4];
                  if (!this.allocateSlot(stitcher$holder)) {
                        String s = String.format("Unable to fit: %s, size: %dx%d, atlas: %dx%d, atlasMax: %dx%d - Maybe try a lower resolution resourcepack?", stitcher$holder.getAtlasSprite().getIconName(), stitcher$holder.getAtlasSprite().getIconWidth(), stitcher$holder.getAtlasSprite().getIconHeight(), this.currentWidth, this.currentHeight, this.maxWidth, this.maxHeight);
                        throw new StitcherException(stitcher$holder, s);
                  }
            }

            this.currentWidth = MathHelper.smallestEncompassingPowerOfTwo(this.currentWidth);
            this.currentHeight = MathHelper.smallestEncompassingPowerOfTwo(this.currentHeight);
      }

      public List getStichSlots() {
            List list = Lists.newArrayList();
            Iterator var2 = this.stitchSlots.iterator();

            while(var2.hasNext()) {
                  Stitcher.Slot stitcher$slot = (Stitcher.Slot)var2.next();
                  stitcher$slot.getAllStitchSlots(list);
            }

            List list1 = Lists.newArrayList();
            Iterator var8 = list.iterator();

            while(var8.hasNext()) {
                  Stitcher.Slot stitcher$slot1 = (Stitcher.Slot)var8.next();
                  Stitcher.Holder stitcher$holder = stitcher$slot1.getStitchHolder();
                  TextureAtlasSprite textureatlassprite = stitcher$holder.getAtlasSprite();
                  textureatlassprite.initSprite(this.currentWidth, this.currentHeight, stitcher$slot1.getOriginX(), stitcher$slot1.getOriginY(), stitcher$holder.isRotated());
                  list1.add(textureatlassprite);
            }

            return list1;
      }

      private static int getMipmapDimension(int p_147969_0_, int p_147969_1_) {
            return (p_147969_0_ >> p_147969_1_) + ((p_147969_0_ & (1 << p_147969_1_) - 1) == 0 ? 0 : 1) << p_147969_1_;
      }

      private boolean allocateSlot(Stitcher.Holder p_94310_1_) {
            TextureAtlasSprite textureatlassprite = p_94310_1_.getAtlasSprite();
            boolean flag = textureatlassprite.getIconWidth() != textureatlassprite.getIconHeight();

            for(int i = 0; i < this.stitchSlots.size(); ++i) {
                  if (((Stitcher.Slot)this.stitchSlots.get(i)).addSlot(p_94310_1_)) {
                        return true;
                  }

                  if (flag) {
                        p_94310_1_.rotate();
                        if (((Stitcher.Slot)this.stitchSlots.get(i)).addSlot(p_94310_1_)) {
                              return true;
                        }

                        p_94310_1_.rotate();
                  }
            }

            return this.expandAndAllocateSlot(p_94310_1_);
      }

      private boolean expandAndAllocateSlot(Stitcher.Holder p_94311_1_) {
            int i = Math.min(p_94311_1_.getWidth(), p_94311_1_.getHeight());
            int j = Math.max(p_94311_1_.getWidth(), p_94311_1_.getHeight());
            int k = MathHelper.smallestEncompassingPowerOfTwo(this.currentWidth);
            int l = MathHelper.smallestEncompassingPowerOfTwo(this.currentHeight);
            int i1 = MathHelper.smallestEncompassingPowerOfTwo(this.currentWidth + i);
            int j1 = MathHelper.smallestEncompassingPowerOfTwo(this.currentHeight + i);
            boolean flag = i1 <= this.maxWidth;
            boolean flag1 = j1 <= this.maxHeight;
            if (!flag && !flag1) {
                  return false;
            } else {
                  int k1 = MathUtils.roundDownToPowerOfTwo(this.currentHeight);
                  boolean flag2 = flag && i1 <= 2 * k1;
                  if (this.currentWidth == 0 && this.currentHeight == 0) {
                        flag2 = true;
                  }

                  Stitcher.Slot stitcher$slot;
                  if (flag2) {
                        if (p_94311_1_.getWidth() > p_94311_1_.getHeight()) {
                              p_94311_1_.rotate();
                        }

                        if (this.currentHeight == 0) {
                              this.currentHeight = p_94311_1_.getHeight();
                        }

                        stitcher$slot = new Stitcher.Slot(this.currentWidth, 0, p_94311_1_.getWidth(), this.currentHeight);
                        this.currentWidth += p_94311_1_.getWidth();
                  } else {
                        stitcher$slot = new Stitcher.Slot(0, this.currentHeight, this.currentWidth, p_94311_1_.getHeight());
                        this.currentHeight += p_94311_1_.getHeight();
                  }

                  stitcher$slot.addSlot(p_94311_1_);
                  this.stitchSlots.add(stitcher$slot);
                  return true;
            }
      }

      public static class Slot {
            private final int originX;
            private final int originY;
            private final int width;
            private final int height;
            private List subSlots;
            private Stitcher.Holder holder;

            public Slot(int originXIn, int originYIn, int widthIn, int heightIn) {
                  this.originX = originXIn;
                  this.originY = originYIn;
                  this.width = widthIn;
                  this.height = heightIn;
            }

            public Stitcher.Holder getStitchHolder() {
                  return this.holder;
            }

            public int getOriginX() {
                  return this.originX;
            }

            public int getOriginY() {
                  return this.originY;
            }

            public boolean addSlot(Stitcher.Holder holderIn) {
                  if (this.holder != null) {
                        return false;
                  } else {
                        int i = holderIn.getWidth();
                        int j = holderIn.getHeight();
                        if (i <= this.width && j <= this.height) {
                              if (i == this.width && j == this.height) {
                                    this.holder = holderIn;
                                    return true;
                              } else {
                                    if (this.subSlots == null) {
                                          this.subSlots = Lists.newArrayListWithCapacity(1);
                                          this.subSlots.add(new Stitcher.Slot(this.originX, this.originY, i, j));
                                          int k = this.width - i;
                                          int l = this.height - j;
                                          if (l > 0 && k > 0) {
                                                int i1 = Math.max(this.height, k);
                                                int j1 = Math.max(this.width, l);
                                                if (i1 >= j1) {
                                                      this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + j, i, l));
                                                      this.subSlots.add(new Stitcher.Slot(this.originX + i, this.originY, k, this.height));
                                                } else {
                                                      this.subSlots.add(new Stitcher.Slot(this.originX + i, this.originY, k, j));
                                                      this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + j, this.width, l));
                                                }
                                          } else if (k == 0) {
                                                this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + j, i, l));
                                          } else if (l == 0) {
                                                this.subSlots.add(new Stitcher.Slot(this.originX + i, this.originY, k, j));
                                          }
                                    }

                                    Iterator var8 = this.subSlots.iterator();

                                    Stitcher.Slot stitcher$slot;
                                    do {
                                          if (!var8.hasNext()) {
                                                return false;
                                          }

                                          stitcher$slot = (Stitcher.Slot)var8.next();
                                    } while(!stitcher$slot.addSlot(holderIn));

                                    return true;
                              }
                        } else {
                              return false;
                        }
                  }
            }

            public void getAllStitchSlots(List p_94184_1_) {
                  if (this.holder != null) {
                        p_94184_1_.add(this);
                  } else if (this.subSlots != null) {
                        Iterator var2 = this.subSlots.iterator();

                        while(var2.hasNext()) {
                              Stitcher.Slot stitcher$slot = (Stitcher.Slot)var2.next();
                              stitcher$slot.getAllStitchSlots(p_94184_1_);
                        }
                  }

            }

            public String toString() {
                  return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.holder + ", subSlots=" + this.subSlots + '}';
            }
      }

      public static class Holder implements Comparable {
            private final TextureAtlasSprite theTexture;
            private final int width;
            private final int height;
            private final int mipmapLevelHolder;
            private boolean rotated;
            private float scaleFactor = 1.0F;

            public Holder(TextureAtlasSprite theTextureIn, int mipmapLevelHolderIn) {
                  this.theTexture = theTextureIn;
                  this.width = theTextureIn.getIconWidth();
                  this.height = theTextureIn.getIconHeight();
                  this.mipmapLevelHolder = mipmapLevelHolderIn;
                  this.rotated = Stitcher.getMipmapDimension(this.height, mipmapLevelHolderIn) > Stitcher.getMipmapDimension(this.width, mipmapLevelHolderIn);
            }

            public TextureAtlasSprite getAtlasSprite() {
                  return this.theTexture;
            }

            public int getWidth() {
                  int i = this.rotated ? this.height : this.width;
                  return Stitcher.getMipmapDimension((int)((float)i * this.scaleFactor), this.mipmapLevelHolder);
            }

            public int getHeight() {
                  int i = this.rotated ? this.width : this.height;
                  return Stitcher.getMipmapDimension((int)((float)i * this.scaleFactor), this.mipmapLevelHolder);
            }

            public void rotate() {
                  this.rotated = !this.rotated;
            }

            public boolean isRotated() {
                  return this.rotated;
            }

            public void setNewDimension(int p_94196_1_) {
                  if (this.width > p_94196_1_ && this.height > p_94196_1_) {
                        this.scaleFactor = (float)p_94196_1_ / (float)Math.min(this.width, this.height);
                  }

            }

            public String toString() {
                  return "Holder{width=" + this.width + ", height=" + this.height + ", name=" + this.theTexture.getIconName() + '}';
            }

            public int compareTo(Stitcher.Holder p_compareTo_1_) {
                  int i;
                  if (this.getHeight() == p_compareTo_1_.getHeight()) {
                        if (this.getWidth() == p_compareTo_1_.getWidth()) {
                              if (this.theTexture.getIconName() == null) {
                                    return p_compareTo_1_.theTexture.getIconName() == null ? 0 : -1;
                              }

                              return this.theTexture.getIconName().compareTo(p_compareTo_1_.theTexture.getIconName());
                        }

                        i = this.getWidth() < p_compareTo_1_.getWidth() ? 1 : -1;
                  } else {
                        i = this.getHeight() < p_compareTo_1_.getHeight() ? 1 : -1;
                  }

                  return i;
            }
      }
}
