package me.roundaround.armorstands.client.gui.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import me.roundaround.armorstands.ArmorStandsMod;
import me.roundaround.armorstands.client.gui.widget.FlagToggleWidget;
import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.roundalib.client.gui.GuiUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class ArmorStandInventoryScreen extends AbstractArmorStandScreen {
  private static final int BACKGROUND_WIDTH = 176;
  private static final int BACKGROUND_HEIGHT = 166;
  private static final Identifier CUSTOM_TEXTURE = new Identifier(
      ArmorStandsMod.MOD_ID, "textures/gui/container/inventory.png");

  private float mouseX;
  private float mouseY;
  private FlagToggleWidget showArmsToggle;
  private FlagToggleWidget lockInventoryToggle;

  public ArmorStandInventoryScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.INVENTORY.getDisplayName());

    this.utilizesInventory = true;
    this.passEvents = false;
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.INVENTORY;
  }

  protected void populateLayout() {
//    this.showArmsToggle = this.layout.bottomRight.add(new FlagToggleWidget(this.textRenderer, ArmorStandFlag.SHOW_ARMS,
//        ArmorStandFlag.SHOW_ARMS.getValue(this.armorStand), this.width - GuiUtil.PADDING,
//        this.height - GuiUtil.PADDING - 2 * FlagToggleWidget.WIDGET_HEIGHT - (GuiUtil.PADDING / 2)
//    ));
//    this.lockInventoryToggle = this.layout.bottomRight.add(new FlagToggleWidget(this.textRenderer, ArmorStandFlag.LOCK_INVENTORY,
//        ArmorStandFlag.LOCK_INVENTORY.getValue(this.armorStand), this.width - GuiUtil.PADDING,
//        this.height - GuiUtil.PADDING - FlagToggleWidget.WIDGET_HEIGHT
//    ));
  }

  @Override
  public void handledScreenTick() {
    super.handledScreenTick();

//    this.showArmsToggle.setValue(ArmorStandFlag.SHOW_ARMS.getValue(this.armorStand));
//    this.lockInventoryToggle.setValue(ArmorStandFlag.LOCK_INVENTORY.getValue(this.armorStand));
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    renderBackground(context, mouseX, mouseY, delta);

    this.mouseX = mouseX;
    this.mouseY = mouseY;
    super.render(context, mouseX, mouseY, delta);

    drawMouseoverTooltip(context, mouseX, mouseY);
  }

  @Override
  protected void drawBackground(DrawContext drawContext, float delta, int mouseX, int mouseY) {
    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

    drawContext.drawTexture(CUSTOM_TEXTURE, this.x, this.y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

    ImmutableList<Pair<Slot, EquipmentSlot>> armorSlots = this.handler.getArmorSlots();
    for (int index = 0; index < armorSlots.size(); index++) {
      Slot slot = armorSlots.get(index).getFirst();
      EquipmentSlot equipmentSlot = armorSlots.get(index).getSecond();
      if (ArmorStandScreenHandler.isSlotDisabled(armorStand, equipmentSlot)) {
        drawContext.fill(x + slot.x, this.y + slot.y, this.x + slot.x + 16, y + slot.y + 16, 0x80000000);
      }
    }

    InventoryScreen.drawEntity(drawContext, this.x + 62, this.y + 8, this.x + 114, this.y + 78, 30, 0.0625f,
        this.mouseX, this.mouseY, this.armorStand
    );
  }
}
