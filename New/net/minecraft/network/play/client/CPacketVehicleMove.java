package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketVehicleMove implements Packet {
     private double x;
     private double y;
     private double z;
     private float yaw;
     private float pitch;

     public CPacketVehicleMove() {
     }

     public CPacketVehicleMove(Entity entityIn) {
          this.x = entityIn.posX;
          this.y = entityIn.posY;
          this.z = entityIn.posZ;
          this.yaw = entityIn.rotationYaw;
          this.pitch = entityIn.rotationPitch;
     }

     public void readPacketData(PacketBuffer buf) throws IOException {
          this.x = buf.readDouble();
          this.y = buf.readDouble();
          this.z = buf.readDouble();
          this.yaw = buf.readFloat();
          this.pitch = buf.readFloat();
     }

     public void writePacketData(PacketBuffer buf) throws IOException {
          buf.writeDouble(this.x);
          buf.writeDouble(this.y);
          buf.writeDouble(this.z);
          buf.writeFloat(this.yaw);
          buf.writeFloat(this.pitch);
     }

     public void processPacket(INetHandlerPlayServer handler) {
          handler.processVehicleMove(this);
     }

     public double getX() {
          return this.x;
     }

     public double getY() {
          return this.y;
     }

     public double getZ() {
          return this.z;
     }

     public float getYaw() {
          return this.yaw;
     }

     public float getPitch() {
          return this.pitch;
     }
}
