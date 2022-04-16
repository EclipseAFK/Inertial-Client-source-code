package net.minecraft.client.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;

public class RecipeBookClient extends RecipeBook {
     public static final Map field_194086_e = Maps.newHashMap();
     public static final List field_194087_f = Lists.newArrayList();

     private static RecipeList func_194082_a(CreativeTabs p_194082_0_) {
          RecipeList recipelist = new RecipeList();
          field_194087_f.add(recipelist);
          ((List)field_194086_e.computeIfAbsent(p_194082_0_, (p_194085_0_) -> {
               return new ArrayList();
          })).add(recipelist);
          ((List)field_194086_e.computeIfAbsent(CreativeTabs.SEARCH, (p_194083_0_) -> {
               return new ArrayList();
          })).add(recipelist);
          return recipelist;
     }

     private static CreativeTabs func_194084_a(ItemStack p_194084_0_) {
          CreativeTabs creativetabs = p_194084_0_.getItem().getCreativeTab();
          if (creativetabs != CreativeTabs.BUILDING_BLOCKS && creativetabs != CreativeTabs.TOOLS && creativetabs != CreativeTabs.REDSTONE) {
               return creativetabs == CreativeTabs.COMBAT ? CreativeTabs.TOOLS : CreativeTabs.MISC;
          } else {
               return creativetabs;
          }
     }

     static {
          Table table = HashBasedTable.create();
          Iterator var1 = CraftingManager.field_193380_a.iterator();

          while(var1.hasNext()) {
               IRecipe irecipe = (IRecipe)var1.next();
               if (!irecipe.func_192399_d()) {
                    CreativeTabs creativetabs = func_194084_a(irecipe.getRecipeOutput());
                    String s = irecipe.func_193358_e();
                    RecipeList recipelist1;
                    if (s.isEmpty()) {
                         recipelist1 = func_194082_a(creativetabs);
                    } else {
                         recipelist1 = (RecipeList)table.get(creativetabs, s);
                         if (recipelist1 == null) {
                              recipelist1 = func_194082_a(creativetabs);
                              table.put(creativetabs, s, recipelist1);
                         }
                    }

                    recipelist1.func_192709_a(irecipe);
               }
          }

     }
}
