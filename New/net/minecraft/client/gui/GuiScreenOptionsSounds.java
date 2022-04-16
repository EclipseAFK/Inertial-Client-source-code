package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class GuiScreenOptionsSounds extends GuiScreen {
     private final GuiScreen parent;
     private final GameSettings game_settings_4;
     protected String title = "Options";
     private String offDisplayString;

     public GuiScreenOptionsSounds(GuiScreen parentIn, GameSettings settingsIn) {
          this.parent = parentIn;
          this.game_settings_4 = settingsIn;
     }

     public void initGui() {
          this.title = I18n.format("options.sounds.title");
          this.offDisplayString = I18n.format("options.off");
          int i = 0;
          this.buttonList.add(new GuiScreenOptionsSounds.Button(SoundCategory.MASTER.ordinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), SoundCategory.MASTER, true));
          int i = i + 2;
          SoundCategory[] var2 = SoundCategory.values();
          int k = var2.length;

          for(int var4 = 0; var4 < k; ++var4) {
               SoundCategory soundcategory = var2[var4];
               if (soundcategory != SoundCategory.MASTER) {
                    this.buttonList.add(new GuiScreenOptionsSounds.Button(soundcategory.ordinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), soundcategory, false));
                    ++i;
               }
          }

          int j = this.width / 2 - 75;
          k = this.height / 6 - 12;
          ++i;
          this.buttonList.add(new GuiOptionButton(201, j, k + 24 * (i >> 1), GameSettings.Options.SHOW_SUBTITLES, this.game_settings_4.getKeyBinding(GameSettings.Options.SHOW_SUBTITLES)));
          this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done")));
     }

     protected void keyTyped(char typedChar, int keyCode) throws IOException {
          if (keyCode == 1) {
               this.mc.gameSettings.saveOptions();
          }

          super.keyTyped(typedChar, keyCode);
     }

     protected void actionPerformed(GuiButton button) throws IOException {
          if (button.enabled) {
               if (button.id == 200) {
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(this.parent);
               } else if (button.id == 201) {
                    this.mc.gameSettings.setOptionValue(GameSettings.Options.SHOW_SUBTITLES, 1);
                    button.displayString = this.mc.gameSettings.getKeyBinding(GameSettings.Options.SHOW_SUBTITLES);
                    this.mc.gameSettings.saveOptions();
               }
          }

     }

     public void drawScreen(int mouseX, int mouseY, float partialTicks) {
          this.drawDefaultBackground();
          this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
          super.drawScreen(mouseX, mouseY, partialTicks);
     }

     protected String getDisplayString(SoundCategory category) {
          float f = this.game_settings_4.getSoundLevel(category);
          return f == 0.0F ? this.offDisplayString : (int)(f * 100.0F) + "%";
     }

     class Button extends GuiButton {
          private final SoundCategory category;
          private final String categoryName;
          public float volume = 1.0F;
          public boolean pressed;

          public Button(int p_i46744_2_, int x, int y, SoundCategory categoryIn, boolean master) {
               super(p_i46744_2_, x, y, master ? 310 : 150, 20, "");
               this.category = categoryIn;
               this.categoryName = I18n.format("soundCategory." + categoryIn.getName());
               this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getDisplayString(categoryIn);
               this.volume = GuiScreenOptionsSounds.this.game_settings_4.getSoundLevel(categoryIn);
          }

          protected int getHoverState(boolean mouseOver) {
               return 0;
          }

          protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
               if (this.visible) {
                    if (this.pressed) {
                         this.volume = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                         this.volume = MathHelper.clamp(this.volume, 0.0F, 1.0F);
                         mc.gameSettings.setSoundLevel(this.category, this.volume);
                         mc.gameSettings.saveOptions();
                         this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getDisplayString(this.category);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.drawTexturedModalRect(this.xPosition + (int)(this.volume * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
                    this.drawTexturedModalRect(this.xPosition + (int)(this.volume * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
               }

          }

          public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
               if (super.mousePressed(mc, mouseX, mouseY)) {
                    this.volume = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                    this.volume = MathHelper.clamp(this.volume, 0.0F, 1.0F);
                    mc.gameSettings.setSoundLevel(this.category, this.volume);
                    mc.gameSettings.saveOptions();
                    this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getDisplayString(this.category);
                    this.pressed = true;
                    return true;
               } else {
                    return false;
               }
          }

          public void playPressSound(SoundHandler soundHandlerIn) {
          }

          public void mouseReleased(int mouseX, int mouseY) {
               if (this.pressed) {
                    GuiScreenOptionsSounds.this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               }

               this.pressed = false;
          }
     }
}