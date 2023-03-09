package me.roundaround.armorstands.network.packet.s2c;

import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.mixin.ArmorStandEntityAccessor;
import me.roundaround.armorstands.network.packet.NetworkPackets;
import me.roundaround.armorstands.util.HasArmorStand;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ClientUpdatePacket {
  private final double x;
  private final double y;
  private final double z;
  private final float yaw;
  private final float pitch;
  private final boolean invulnerable;
  private final int disabledSlots;

  private ClientUpdatePacket(PacketByteBuf buf) {
    this.x = buf.readDouble();
    this.y = buf.readDouble();
    this.z = buf.readDouble();
    this.yaw = buf.readFloat();
    this.pitch = buf.readFloat();
    this.invulnerable = buf.readBoolean();
    this.disabledSlots = buf.readInt();
  }

  private ClientUpdatePacket(ArmorStandEntity armorStand) {
    this.x = armorStand.getX();
    this.y = armorStand.getY();
    this.z = armorStand.getZ();
    this.yaw = armorStand.getYaw();
    this.pitch = armorStand.getPitch();
    this.invulnerable = armorStand.isInvulnerable();
    this.disabledSlots = ((ArmorStandEntityAccessor) armorStand).getDisabledSlots();
  }

  private PacketByteBuf toPacket() {
    PacketByteBuf buf = new PacketByteBuf(PacketByteBufs.create());
    buf.writeDouble(this.x);
    buf.writeDouble(this.y);
    buf.writeDouble(this.z);
    buf.writeFloat(this.yaw);
    buf.writeFloat(this.pitch);
    buf.writeBoolean(this.invulnerable);
    buf.writeInt(this.disabledSlots);
    return buf;
  }

  private void handleOnClient(
      MinecraftClient client,
      ClientPlayNetworkHandler handler,
      PacketSender responseSender) {
    if (!(client.player.currentScreenHandler instanceof HasArmorStand)) {
      return;
    }

    if (!(client.currentScreen instanceof AbstractArmorStandScreen)) {
      return;
    }

    HasArmorStand screenHandler = (HasArmorStand) client.player.currentScreenHandler;
    AbstractArmorStandScreen screen = (AbstractArmorStandScreen) client.currentScreen;
    ArmorStandEntity armorStand = screenHandler.getArmorStand();

    armorStand.setPos(this.x, this.y, this.z);
    screen.updateYawOnClient(this.yaw % 360f);
    armorStand.setPitch(this.pitch % 360f);
    armorStand.setInvulnerable(this.invulnerable);
    ((ArmorStandEntityAccessor) armorStand).setDisabledSlots(this.disabledSlots);
  }

  public static void sendToClient(ServerPlayerEntity player, ArmorStandEntity armorStand) {
    ServerPlayNetworking.send(
        player,
        NetworkPackets.CLIENT_UPDATE_PACKET,
        new ClientUpdatePacket(armorStand).toPacket());
  }

  public static void registerClientReceiver() {
    ClientPlayNetworking.registerGlobalReceiver(
        NetworkPackets.CLIENT_UPDATE_PACKET,
        (client, handler, buf, responseSender) -> {
          ClientUpdatePacket packet = new ClientUpdatePacket(buf);
          client.execute(() -> {
            packet.handleOnClient(client, handler, responseSender);
          });
        });
  }
}
