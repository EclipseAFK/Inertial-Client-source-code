package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.ResourceLocation;

public class SPacketAdvancementInfo implements Packet {
      private boolean field_192605_a;
      private Map field_192606_b;
      private Set field_192607_c;
      private Map field_192608_d;

      public SPacketAdvancementInfo() {
      }

      public SPacketAdvancementInfo(boolean p_i47519_1_, Collection p_i47519_2_, Set p_i47519_3_, Map p_i47519_4_) {
            this.field_192605_a = p_i47519_1_;
            this.field_192606_b = Maps.newHashMap();
            Iterator var5 = p_i47519_2_.iterator();

            while(var5.hasNext()) {
                  Advancement advancement = (Advancement)var5.next();
                  this.field_192606_b.put(advancement.func_192067_g(), advancement.func_192075_a());
            }

            this.field_192607_c = p_i47519_3_;
            this.field_192608_d = Maps.newHashMap(p_i47519_4_);
      }

      public void processPacket(INetHandlerPlayClient handler) {
            handler.func_191981_a(this);
      }

      public void readPacketData(PacketBuffer buf) throws IOException {
            this.field_192605_a = buf.readBoolean();
            this.field_192606_b = Maps.newHashMap();
            this.field_192607_c = Sets.newLinkedHashSet();
            this.field_192608_d = Maps.newHashMap();
            int i = buf.readVarIntFromBuffer();

            int l;
            ResourceLocation resourcelocation2;
            for(l = 0; l < i; ++l) {
                  resourcelocation2 = buf.func_192575_l();
                  Advancement.Builder advancement$builder = Advancement.Builder.func_192060_b(buf);
                  this.field_192606_b.put(resourcelocation2, advancement$builder);
            }

            i = buf.readVarIntFromBuffer();

            for(l = 0; l < i; ++l) {
                  resourcelocation2 = buf.func_192575_l();
                  this.field_192607_c.add(resourcelocation2);
            }

            i = buf.readVarIntFromBuffer();

            for(l = 0; l < i; ++l) {
                  resourcelocation2 = buf.func_192575_l();
                  this.field_192608_d.put(resourcelocation2, AdvancementProgress.func_192100_b(buf));
            }

      }

      public void writePacketData(PacketBuffer buf) throws IOException {
            buf.writeBoolean(this.field_192605_a);
            buf.writeVarIntToBuffer(this.field_192606_b.size());
            Iterator var2 = this.field_192606_b.entrySet().iterator();

            Entry entry1;
            while(var2.hasNext()) {
                  entry1 = (Entry)var2.next();
                  ResourceLocation resourcelocation = (ResourceLocation)entry1.getKey();
                  Advancement.Builder advancement$builder = (Advancement.Builder)entry1.getValue();
                  buf.func_192572_a(resourcelocation);
                  advancement$builder.func_192057_a(buf);
            }

            buf.writeVarIntToBuffer(this.field_192607_c.size());
            var2 = this.field_192607_c.iterator();

            while(var2.hasNext()) {
                  ResourceLocation resourcelocation1 = (ResourceLocation)var2.next();
                  buf.func_192572_a(resourcelocation1);
            }

            buf.writeVarIntToBuffer(this.field_192608_d.size());
            var2 = this.field_192608_d.entrySet().iterator();

            while(var2.hasNext()) {
                  entry1 = (Entry)var2.next();
                  buf.func_192572_a((ResourceLocation)entry1.getKey());
                  ((AdvancementProgress)entry1.getValue()).func_192104_a(buf);
            }

      }

      public Map func_192603_a() {
            return this.field_192606_b;
      }

      public Set func_192600_b() {
            return this.field_192607_c;
      }

      public Map func_192604_c() {
            return this.field_192608_d;
      }

      public boolean func_192602_d() {
            return this.field_192605_a;
      }
}
