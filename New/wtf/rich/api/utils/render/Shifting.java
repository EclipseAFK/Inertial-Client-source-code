package wtf.rich.api.utils.render;

public final class Shifting {
     private double x;
     private double y;

     public Shifting(float x, float y) {
          this.x = (double)x;
          this.y = (double)y;
     }

     public final void interpolate(double targetX, double targetY, double smoothing) {
          this.x = AnimationHelper.animate(targetX, this.x, smoothing);
          this.y = AnimationHelper.animate(targetY, this.y, smoothing);
     }

     public void animate(double newX, double newY) {
          this.x = AnimationHelper.animate(this.x, newX, 1.0D);
          this.y = AnimationHelper.animate(this.y, newY, 1.0D);
     }

     public double getX() {
          return this.x;
     }

     public void setX(double x) {
          this.x = x;
     }

     public double getY() {
          return this.y;
     }

     public void setY(double y) {
          this.y = y;
     }
}
