package net.minecraft.client.renderer.block.statemap;

import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public abstract class StateMapperBase implements IStateMapper {
     protected Map mapStateModelLocations = Maps.newLinkedHashMap();

     public String getPropertyString(Map values) {
          StringBuilder stringbuilder = new StringBuilder();
          Iterator var3 = values.entrySet().iterator();

          while(var3.hasNext()) {
               Entry entry = (Entry)var3.next();
               if (stringbuilder.length() != 0) {
                    stringbuilder.append(",");
               }

               IProperty iproperty = (IProperty)entry.getKey();
               stringbuilder.append(iproperty.getName());
               stringbuilder.append("=");
               stringbuilder.append(this.getPropertyName(iproperty, (Comparable)entry.getValue()));
          }

          if (stringbuilder.length() == 0) {
               stringbuilder.append("normal");
          }

          return stringbuilder.toString();
     }

     private String getPropertyName(IProperty property, Comparable value) {
          return property.getName(value);
     }

     public Map putStateModelLocations(Block blockIn) {
          UnmodifiableIterator unmodifiableiterator = blockIn.getBlockState().getValidStates().iterator();

          while(unmodifiableiterator.hasNext()) {
               IBlockState iblockstate = (IBlockState)unmodifiableiterator.next();
               this.mapStateModelLocations.put(iblockstate, this.getModelResourceLocation(iblockstate));
          }

          return this.mapStateModelLocations;
     }

     protected abstract ModelResourceLocation getModelResourceLocation(IBlockState var1);
}
