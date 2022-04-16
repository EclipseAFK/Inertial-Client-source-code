package net.minecraft.client.gui.inventory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

public class GuiFurnace extends GuiContainer {
     private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/furnace.png");
     private final InventoryPlayer playerInventory;
     private final IInventory tileFurnace;

     public GuiFurnace(InventoryPlayer playerInv, IInventory furnaceInv) {
          super(new ContainerFurnace(playerInv, furnaceInv));
          this.playerInventory = playerInv;
          this.tileFurnace = furnaceInv;
     }

     public void drawScreen(int mouseX, int mouseY, float partialTicks) {
          this.drawDefaultBackground();
          super.drawScreen(mouseX, mouseY, partialTicks);
          this.func_191948_b(mouseX, mouseY);
     }

     protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
          String s = this.tileFurnace.getDisplayName().getUnformattedText();
          this.fontRendererObj.drawString(s, (float)(this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2), 6.0F, 4210752);
          this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8.0F, (float)(this.ySize - 96 + 2), 4210752);
     }

     protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
          GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
          this.mc.getTextureManager().bindTexture(FURNACE_GUI_TEXTURES);
          int i = (this.width - this.xSize) / 2;
          int j = (this.height - this.ySize) / 2;
          this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
          int l;
          if (TileEntityFurnace.isBurning(this.tileFurnace)) {
               l = this.getBurnLeftScaled(13);
               this.drawTexturedModalRect(i + 56, j + 36 + 12 - l, 176, 12 - l, 14, l + 1);
          }

          l = this.getCookProgressScaled(24);
          this.drawTexturedModalRect(i + 79, j + 34, 176, 14, l + 1, 16);
     }

     private int getCookProgressScaled(int pixels) {
          int i = this.tileFurnace.getField(2);
          int j = this.tileFurnace.getField(3);
          return j != 0 && i != 0 ? i * pixels / j : 0;
     }

     private int getBurnLeftScaled(int pixels) {
          int i = this.tileFurnace.getField(1);
          if (i == 0) {
               i = 200;
          }

          return this.tileFurnace.getField(0) * pixels / i;
     }
}