package net.minecraft.client.gui;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.ArrayUtils;

public class GuiKeyBindingList extends GuiListExtended {
     private final GuiControls controlsScreen;
     private final Minecraft mc;
     private final GuiListExtended.IGuiListEntry[] listEntries;
     private int maxListLabelWidth;

     public GuiKeyBindingList(GuiControls controls, Minecraft mcIn) {
          super(mcIn, controls.width + 45, controls.height, 63, controls.height - 32, 20);
          this.controlsScreen = controls;
          this.mc = mcIn;
          KeyBinding[] akeybinding = (KeyBinding[])((KeyBinding[])ArrayUtils.clone(mcIn.gameSettings.keyBindings));
          this.listEntries = new GuiListExtended.IGuiListEntry[akeybinding.length + KeyBinding.getKeybinds().size()];
          Arrays.sort((Object[])akeybinding);
          int i = 0;
          String s = null;
          KeyBinding[] var6 = akeybinding;
          int var7 = akeybinding.length;

          for(int var8 = 0; var8 < var7; ++var8) {
               KeyBinding keybinding = var6[var8];
               String s1 = keybinding.getKeyCategory();
               if (!s1.equals(s)) {
                    s = s1;
                    this.listEntries[i++] = new GuiKeyBindingList.CategoryEntry(s1);
               }

               int j = Minecraft.fontRendererObj.getStringWidth(I18n.format(keybinding.getKeyDescription()));
               if (j > this.maxListLabelWidth) {
                    this.maxListLabelWidth = j;
               }

               this.listEntries[i++] = new GuiKeyBindingList.KeyEntry(keybinding);
          }

     }

     protected int getSize() {
          return this.listEntries.length;
     }

     public GuiListExtended.IGuiListEntry getListEntry(int index) {
          return this.listEntries[index];
     }

     protected int getScrollBarX() {
          return super.getScrollBarX() + 15;
     }

     public int getListWidth() {
          return super.getListWidth() + 32;
     }

     public class KeyEntry implements GuiListExtended.IGuiListEntry {
          private final KeyBinding keybinding;
          private final String keyDesc;
          private final GuiButton btnChangeKeyBinding;
          private final GuiButton btnReset;

          private KeyEntry(KeyBinding name) {
               this.keybinding = name;
               this.keyDesc = I18n.format(name.getKeyDescription());
               this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 75, 20, I18n.format(name.getKeyDescription()));
               this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
          }

          public void func_192634_a(int p_192634_1_, int p_192634_2_, int p_192634_3_, int p_192634_4_, int p_192634_5_, int p_192634_6_, int p_192634_7_, boolean p_192634_8_, float p_192634_9_) {
               boolean flag = GuiKeyBindingList.this.controlsScreen.buttonId == this.keybinding;
               GuiKeyBindingList.this.mc;
               FontRenderer var10000 = Minecraft.fontRendererObj;
               String var10001 = this.keyDesc;
               float var10002 = (float)(p_192634_2_ + 90 - GuiKeyBindingList.this.maxListLabelWidth);
               int var10003 = p_192634_3_ + p_192634_5_ / 2;
               GuiKeyBindingList.this.mc;
               var10000.drawString(var10001, var10002, (float)(var10003 - Minecraft.fontRendererObj.FONT_HEIGHT / 2), 16777215);
               this.btnReset.xPosition = p_192634_2_ + 190;
               this.btnReset.yPosition = p_192634_3_;
               this.btnReset.enabled = this.keybinding.getKeyCode() != this.keybinding.getKeyCodeDefault();
               this.btnReset.drawButton(GuiKeyBindingList.this.mc, p_192634_6_, p_192634_7_, p_192634_9_);
               this.btnChangeKeyBinding.xPosition = p_192634_2_ + 105;
               this.btnChangeKeyBinding.yPosition = p_192634_3_;
               this.btnChangeKeyBinding.displayString = GameSettings.getKeyDisplayString(this.keybinding.getKeyCode());
               boolean flag1 = false;
               if (this.keybinding.getKeyCode() != 0) {
                    KeyBinding[] var12 = GuiKeyBindingList.this.mc.gameSettings.keyBindings;
                    int var13 = var12.length;

                    for(int var14 = 0; var14 < var13; ++var14) {
                         KeyBinding keybinding = var12[var14];
                         if (keybinding != this.keybinding && keybinding.getKeyCode() == this.keybinding.getKeyCode()) {
                              flag1 = true;
                              break;
                         }
                    }
               }

               if (flag) {
                    this.btnChangeKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.displayString + TextFormatting.WHITE + " <";
               } else if (flag1) {
                    this.btnChangeKeyBinding.displayString = TextFormatting.RED + this.btnChangeKeyBinding.displayString;
               }

               this.btnChangeKeyBinding.drawButton(GuiKeyBindingList.this.mc, p_192634_6_, p_192634_7_, p_192634_9_);
          }

          public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
               if (this.btnChangeKeyBinding.mousePressed(GuiKeyBindingList.this.mc, mouseX, mouseY)) {
                    GuiKeyBindingList.this.controlsScreen.buttonId = this.keybinding;
                    return true;
               } else if (this.btnReset.mousePressed(GuiKeyBindingList.this.mc, mouseX, mouseY)) {
                    GuiKeyBindingList.this.mc.gameSettings.setOptionKeyBinding(this.keybinding, this.keybinding.getKeyCodeDefault());
                    KeyBinding.resetKeyBindingArrayAndHash();
                    return true;
               } else {
                    return false;
               }
          }

          public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
               this.btnChangeKeyBinding.mouseReleased(x, y);
               this.btnReset.mouseReleased(x, y);
          }

          public void func_192633_a(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {
          }

          // $FF: synthetic method
          KeyEntry(KeyBinding x1, Object x2) {
               this(x1);
          }
     }

     public class CategoryEntry implements GuiListExtended.IGuiListEntry {
          private final String labelText;
          private final int labelWidth;

          public CategoryEntry(String name) {
               this.labelText = I18n.format(name);
               GuiKeyBindingList.this.mc;
               this.labelWidth = Minecraft.fontRendererObj.getStringWidth(this.labelText);
          }

          public void func_192634_a(int p_192634_1_, int p_192634_2_, int p_192634_3_, int p_192634_4_, int p_192634_5_, int p_192634_6_, int p_192634_7_, boolean p_192634_8_, float p_192634_9_) {
               GuiKeyBindingList.this.mc;
               FontRenderer var10000 = Minecraft.fontRendererObj;
               String var10001 = this.labelText;
               float var10002 = (float)(GuiKeyBindingList.this.mc.currentScreen.width / 2 - this.labelWidth / 2);
               int var10003 = p_192634_3_ + p_192634_5_;
               GuiKeyBindingList.this.mc;
               var10000.drawString(var10001, var10002, (float)(var10003 - Minecraft.fontRendererObj.FONT_HEIGHT - 1), 16777215);
          }

          public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
               return false;
          }

          public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
          }

          public void func_192633_a(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {
          }
     }
}
