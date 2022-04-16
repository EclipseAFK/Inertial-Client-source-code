package wtf.rich.client.features.impl.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import wtf.rich.api.event.EventTarget;
import wtf.rich.api.event.event.EventUpdate;
import wtf.rich.api.utils.world.TimerHelper;
import wtf.rich.client.features.Category;
import wtf.rich.client.features.Feature;
import wtf.rich.client.ui.settings.Setting;
import wtf.rich.client.ui.settings.impl.BooleanSetting;
import wtf.rich.client.ui.settings.impl.NumberSetting;

public class AutoArmor extends Feature {
     public static BooleanSetting openInventory;
     private final NumberSetting delay = new NumberSetting("Equip Delay", 1.0F, 0.0F, 10.0F, 1.0F, () -> {
          return true;
     });
     public TimerHelper timerUtils = new TimerHelper();

     public AutoArmor() {
          super("AutoArmor", "Автоматически одевает броню", 0, Category.COMBAT);
          openInventory = new BooleanSetting("Only Open Inventory", "Только при открытом инвинтаре ", true, () -> {
               return true;
          });
          this.addSettings(new Setting[]{this.delay, openInventory});
     }

     public static boolean isNullOrEmpty(ItemStack stack) {
          return stack == null || stack.isEmpty();
     }

     @EventTarget
     public void onUpdate(EventUpdate event) {
          this.setSuffix("" + this.delay.getNumberValue());
          if (mc.currentScreen instanceof GuiInventory || !openInventory.getBoolValue()) {
               if (!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof InventoryEffectRenderer) {
                    InventoryPlayer inventory = mc.player.inventory;
                    int[] bestArmorSlots = new int[4];
                    int[] bestArmorValues = new int[4];

                    int slot;
                    ItemStack stack;
                    ItemArmor item;
                    for(slot = 0; slot < 4; ++slot) {
                         bestArmorSlots[slot] = -1;
                         stack = inventory.armorItemInSlot(slot);
                         if (!isNullOrEmpty(stack) && stack.getItem() instanceof ItemArmor) {
                              item = (ItemArmor)stack.getItem();
                              bestArmorValues[slot] = this.getArmorValue(item, stack);
                         }
                    }

                    int j;
                    for(slot = 0; slot < 36; ++slot) {
                         stack = inventory.getStackInSlot(slot);
                         if (!isNullOrEmpty(stack) && stack.getItem() instanceof ItemArmor) {
                              item = (ItemArmor)stack.getItem();
                              j = item.armorType.getIndex();
                              int armorValue = this.getArmorValue(item, stack);
                              if (armorValue > bestArmorValues[j]) {
                                   bestArmorSlots[j] = slot;
                                   bestArmorValues[j] = armorValue;
                              }
                         }
                    }

                    ArrayList types = new ArrayList(Arrays.asList(0, 1, 2, 3));
                    Collections.shuffle(types);
                    Iterator var11 = types.iterator();

                    while(var11.hasNext()) {
                         int i = (Integer)var11.next();
                         j = bestArmorSlots[i];
                         if (j != -1) {
                              ItemStack oldArmor = inventory.armorItemInSlot(i);
                              if (isNullOrEmpty(oldArmor) || inventory.getFirstEmptyStack() != -1) {
                                   if (j < 9) {
                                        j += 36;
                                   }

                                   if (this.timerUtils.hasReached((double)(this.delay.getNumberValue() * 100.0F))) {
                                        if (!isNullOrEmpty(oldArmor)) {
                                             mc.playerController.windowClick(0, 8 - i, 0, ClickType.QUICK_MOVE, mc.player);
                                        }

                                        mc.playerController.windowClick(0, j, 0, ClickType.QUICK_MOVE, mc.player);
                                        this.timerUtils.reset();
                                   }
                                   break;
                              }
                         }
                    }

               }
          }
     }

     private int getArmorValue(ItemArmor item, ItemStack stack) {
          int armorPoints = item.damageReduceAmount;
          int prtPoints = false;
          int armorToughness = (int)item.toughness;
          int armorType = item.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.LEGS);
          Enchantment protection = Enchantments.PROTECTION;
          int prtLvl = EnchantmentHelper.getEnchantmentLevel(protection, stack);
          DamageSource dmgSource = DamageSource.causePlayerDamage(mc.player);
          int prtPoints = protection.calcModifierDamage(prtLvl, dmgSource);
          return armorPoints * 5 + prtPoints * 3 + armorToughness + armorType;
     }
}
