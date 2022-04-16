package net.minecraft.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

public class Timer {
     float ticksPerSecond;
     private double lastHRTime;
     public int elapsedTicks;
     public double speed = 1.0D;
     public float renderPartialTicks;
     public float timerSpeed = 1.0F;
     public float elapsedPartialTicks;
     private long lastSyncSysClock;
     private long lastSyncHRClock;
     private long field_74285_i;
     private double timeSyncAdjustment = 1.0D;

     public Timer(float p_i1018_1_) {
          this.ticksPerSecond = p_i1018_1_;
          this.lastSyncSysClock = Minecraft.getSystemTime();
          this.lastSyncHRClock = System.nanoTime() / 1000000L;
     }

     public void updateTimer() {
          long i = Minecraft.getSystemTime();
          long j = i - this.lastSyncSysClock;
          long k = System.nanoTime() / 1000000L;
          double d0 = (double)k / 1000.0D;
          if (j <= 1000L && j >= 0L) {
               this.field_74285_i += j;
               if (this.field_74285_i > 1000L) {
                    long l = k - this.lastSyncHRClock;
                    double d1 = (double)this.field_74285_i / (double)l;
                    this.timeSyncAdjustment += (d1 - this.timeSyncAdjustment) * 0.20000000298023224D;
                    this.lastSyncHRClock = k;
                    this.field_74285_i = 0L;
               }

               if (this.field_74285_i < 0L) {
                    this.lastSyncHRClock = k;
               }
          } else {
               this.lastHRTime = d0;
          }

          this.lastSyncSysClock = i;
          double d2 = (d0 - this.lastHRTime) * this.timeSyncAdjustment;
          this.lastHRTime = d0;
          d2 = MathHelper.clamp(d2, 0.0D, 1.0D);
          this.elapsedPartialTicks = (float)((double)this.elapsedPartialTicks + d2 * (double)this.timerSpeed * (double)this.ticksPerSecond);
          this.elapsedTicks = (int)this.elapsedPartialTicks;
          this.elapsedPartialTicks -= (float)this.elapsedTicks;
          if (this.elapsedTicks > 10) {
               this.elapsedTicks = 10;
          }

          this.renderPartialTicks = this.elapsedPartialTicks;
     }
}
