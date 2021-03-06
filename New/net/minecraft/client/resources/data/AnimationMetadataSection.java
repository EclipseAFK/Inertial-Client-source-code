package net.minecraft.client.resources.data;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AnimationMetadataSection implements IMetadataSection {
     private final List animationFrames;
     private final int frameWidth;
     private final int frameHeight;
     private final int frameTime;
     private final boolean interpolate;

     public AnimationMetadataSection(List animationFramesIn, int frameWidthIn, int frameHeightIn, int frameTimeIn, boolean interpolateIn) {
          this.animationFrames = animationFramesIn;
          this.frameWidth = frameWidthIn;
          this.frameHeight = frameHeightIn;
          this.frameTime = frameTimeIn;
          this.interpolate = interpolateIn;
     }

     public int getFrameHeight() {
          return this.frameHeight;
     }

     public int getFrameWidth() {
          return this.frameWidth;
     }

     public int getFrameCount() {
          return this.animationFrames.size();
     }

     public int getFrameTime() {
          return this.frameTime;
     }

     public boolean isInterpolate() {
          return this.interpolate;
     }

     private AnimationFrame getAnimationFrame(int frame) {
          return (AnimationFrame)this.animationFrames.get(frame);
     }

     public int getFrameTimeSingle(int frame) {
          AnimationFrame animationframe = this.getAnimationFrame(frame);
          return animationframe.hasNoTime() ? this.frameTime : animationframe.getFrameTime();
     }

     public boolean frameHasTime(int frame) {
          return !((AnimationFrame)this.animationFrames.get(frame)).hasNoTime();
     }

     public int getFrameIndex(int frame) {
          return ((AnimationFrame)this.animationFrames.get(frame)).getFrameIndex();
     }

     public Set getFrameIndexSet() {
          Set set = Sets.newHashSet();
          Iterator var2 = this.animationFrames.iterator();

          while(var2.hasNext()) {
               AnimationFrame animationframe = (AnimationFrame)var2.next();
               set.add(animationframe.getFrameIndex());
          }

          return set;
     }
}
