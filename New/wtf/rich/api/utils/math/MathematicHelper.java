package wtf.rich.api.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathematicHelper {
     public static BigDecimal round(float f, int times) {
          BigDecimal bd = new BigDecimal(Float.toString(f));
          bd = bd.setScale(times, RoundingMode.HALF_UP);
          return bd;
     }

     public static int getMiddle(int old, int newValue) {
          return (old + newValue) / 2;
     }

     public static double round(double num, double increment) {
          double v = (double)Math.round(num / increment) * increment;
          BigDecimal bd = new BigDecimal(v);
          bd = bd.setScale(2, RoundingMode.HALF_UP);
          return bd.doubleValue();
     }

     public static float checkAngle(float one, float two, float three) {
          float f = net.minecraft.util.math.MathHelper.wrapDegrees(one - two);
          if (f < -three) {
               f = -three;
          }

          if (f >= three) {
               f = three;
          }

          return one - f;
     }

     public static float clamp(float val, float min, float max) {
          if (val <= min) {
               val = min;
          }

          if (val >= max) {
               val = max;
          }

          return val;
     }

     public static float randomizeFloat(float min, float max) {
          return (float)((double)min + (double)(max - min) * Math.random());
     }
}
