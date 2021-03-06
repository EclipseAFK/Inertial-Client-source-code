package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketCooldown implements Packet {
     private Item item;
     private int ticks;

     public SPacketCooldown() {
     }

     public SPacketCooldown(Item itemIn, int ticksIn) {
          this.item = itemIn;
          this.ticks = ticksIn;
     }

     public void readPacketData(PacketBuffer buf) throws IOException {
          this.item = Item.getItemById(buf.readVarIntFromBuffer());
          this.ticks = buf.readVarIntFromBuffer();
     }

     public void writePacketData(PacketBuffer buf) throws IOException {
          buf.writeVarIntToBuffer(Item.getIdFromItem(this.item));
          buf.writeVarIntToBuffer(this.ticks);
     }

     public void processPacket(INetHandlerPlayClient handler) {
          handler.handleCooldown(this);
     }

     public Item getItem() {
          return this.item;
     }

     public int getTicks() {
          return this.ticks;
     }
}
