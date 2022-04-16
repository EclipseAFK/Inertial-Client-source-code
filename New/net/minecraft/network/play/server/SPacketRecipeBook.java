package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketRecipeBook implements Packet {
     private SPacketRecipeBook.State field_193646_a;
     private List field_192596_a;
     private List field_193647_c;
     private boolean field_192598_c;
     private boolean field_192599_d;

     public SPacketRecipeBook() {
     }

     public SPacketRecipeBook(SPacketRecipeBook.State p_i47597_1_, List p_i47597_2_, List p_i47597_3_, boolean p_i47597_4_, boolean p_i47597_5_) {
          this.field_193646_a = p_i47597_1_;
          this.field_192596_a = p_i47597_2_;
          this.field_193647_c = p_i47597_3_;
          this.field_192598_c = p_i47597_4_;
          this.field_192599_d = p_i47597_5_;
     }

     public void processPacket(INetHandlerPlayClient handler) {
          handler.func_191980_a(this);
     }

     public void readPacketData(PacketBuffer buf) throws IOException {
          this.field_193646_a = (SPacketRecipeBook.State)buf.readEnumValue(SPacketRecipeBook.State.class);
          this.field_192598_c = buf.readBoolean();
          this.field_192599_d = buf.readBoolean();
          int i = buf.readVarIntFromBuffer();
          this.field_192596_a = Lists.newArrayList();

          int k;
          for(k = 0; k < i; ++k) {
               this.field_192596_a.add(CraftingManager.func_193374_a(buf.readVarIntFromBuffer()));
          }

          if (this.field_193646_a == SPacketRecipeBook.State.INIT) {
               i = buf.readVarIntFromBuffer();
               this.field_193647_c = Lists.newArrayList();

               for(k = 0; k < i; ++k) {
                    this.field_193647_c.add(CraftingManager.func_193374_a(buf.readVarIntFromBuffer()));
               }
          }

     }

     public void writePacketData(PacketBuffer buf) throws IOException {
          buf.writeEnumValue(this.field_193646_a);
          buf.writeBoolean(this.field_192598_c);
          buf.writeBoolean(this.field_192599_d);
          buf.writeVarIntToBuffer(this.field_192596_a.size());
          Iterator var2 = this.field_192596_a.iterator();

          IRecipe irecipe1;
          while(var2.hasNext()) {
               irecipe1 = (IRecipe)var2.next();
               buf.writeVarIntToBuffer(CraftingManager.func_193375_a(irecipe1));
          }

          if (this.field_193646_a == SPacketRecipeBook.State.INIT) {
               buf.writeVarIntToBuffer(this.field_193647_c.size());
               var2 = this.field_193647_c.iterator();

               while(var2.hasNext()) {
                    irecipe1 = (IRecipe)var2.next();
                    buf.writeVarIntToBuffer(CraftingManager.func_193375_a(irecipe1));
               }
          }

     }

     public List func_192595_a() {
          return this.field_192596_a;
     }

     public List func_193644_b() {
          return this.field_193647_c;
     }

     public boolean func_192593_c() {
          return this.field_192598_c;
     }

     public boolean func_192594_d() {
          return this.field_192599_d;
     }

     public SPacketRecipeBook.State func_194151_e() {
          return this.field_193646_a;
     }

     public static enum State {
          INIT,
          ADD,
          REMOVE;
     }
}
