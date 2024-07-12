package me.roundaround.armorstands.server.network;

import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.util.ArmorStandEditor;
import me.roundaround.armorstands.util.HasArmorStandEditor;
import me.roundaround.armorstands.util.LastUsedScreen;
import me.roundaround.armorstands.util.actions.AdjustPosAction;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Supplier;

public final class ServerNetworking {
  private ServerNetworking() {
  }

  public static void sendClientUpdatePacket(ServerPlayerEntity player, ArmorStandEntity armorStand) {
    if (ServerPlayNetworking.canSend(player, Networking.ClientUpdateS2C.ID)) {
      ServerPlayNetworking.send(player, new Networking.ClientUpdateS2C(armorStand));
    }
  }

  public static void sendMessagePacket(ServerPlayerEntity player, String message) {
    if (ServerPlayNetworking.canSend(player, Networking.MessageS2C.ID)) {
      ServerPlayNetworking.send(player, new Networking.MessageS2C(message));
    }
  }

  public static void sendMessagePacket(ServerPlayerEntity player, String message, int color) {
    if (ServerPlayNetworking.canSend(player, Networking.MessageS2C.ID)) {
      ServerPlayNetworking.send(player, new Networking.MessageS2C(message, color));
    }
  }

  public static void sendPongPacket(ServerPlayerEntity player) {
    ServerPlayNetworking.send(player, new Networking.PongS2C(player.getUuid()));
  }

  public static void registerReceivers() {
    ServerPlayNetworking.registerGlobalReceiver(Networking.AdjustPoseC2S.ID, ServerNetworking::handleAdjustPose);
    ServerPlayNetworking.registerGlobalReceiver(Networking.AdjustPosC2S.ID, ServerNetworking::handleAdjustPos);
    ServerPlayNetworking.registerGlobalReceiver(Networking.AdjustYawC2S.ID, ServerNetworking::handleAdjustYaw);
    ServerPlayNetworking.registerGlobalReceiver(Networking.PingC2S.ID, ServerNetworking::handlePing);
    ServerPlayNetworking.registerGlobalReceiver(Networking.RequestScreenC2S.ID, ServerNetworking::handleRequestScreen);
    ServerPlayNetworking.registerGlobalReceiver(Networking.SetFlagC2S.ID, ServerNetworking::handleSetFlag);
    ServerPlayNetworking.registerGlobalReceiver(Networking.SetPoseC2S.ID, ServerNetworking::handleSetPose);
    ServerPlayNetworking.registerGlobalReceiver(Networking.SetPosePresetC2S.ID, ServerNetworking::handleSetPosePreset);
    ServerPlayNetworking.registerGlobalReceiver(Networking.SetYawC2S.ID, ServerNetworking::handleSetYaw);
    ServerPlayNetworking.registerGlobalReceiver(Networking.UndoC2S.ID, ServerNetworking::handleUndo);
    ServerPlayNetworking.registerGlobalReceiver(Networking.UtilityActionC2S.ID, ServerNetworking::handleUtilityAction);
  }

  private static void handleAdjustPose(Networking.AdjustPoseC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      editor.adjustPose(payload.part(), payload.parameter(), payload.amount());
    });
  }

  private static void handleAdjustPos(Networking.AdjustPosC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      editor.applyAction(payload.mode().isLocal() ?
          AdjustPosAction.local(payload.direction(), payload.amount(), payload.units(),
              payload.mode().isLocalToPlayer()
          ) :
          AdjustPosAction.relative(payload.direction(), payload.amount(), payload.units()));
    });
  }

  private static void handleAdjustYaw(Networking.AdjustYawC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      editor.rotate(payload.amount());
    });
  }

  private static void handlePing(Networking.PingC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      sendPongPacket(context.player());
    });
  }

  private static void handleRequestScreen(Networking.RequestScreenC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ServerPlayerEntity player = context.player();
      Entity entity = player.getServerWorld().getEntityById(payload.armorStandId());
      if (!(entity instanceof ArmorStandEntity armorStand)) {
        return;
      }

      if (player.currentScreenHandler instanceof ArmorStandScreenHandler) {
        // Bypass the normal screen closing logic, as we don't want to send a
        // close packet to the client.
        player.onHandledScreenClosed();
      }

      LastUsedScreen.set(player, armorStand, payload.screenType());
      player.openHandledScreen(ArmorStandScreenHandler.Factory.create(payload.screenType(), armorStand));
    });
  }

  private static void handleSetFlag(Networking.SetFlagC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ServerPlayerEntity player = context.player();
      ScreenHandler currentScreenHandler = player.currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      editor.setFlag(payload.flag(), payload.value());

      sendClientUpdatePacket(player, editor.getArmorStand());
    });
  }

  private static void handleSetPose(Networking.SetPoseC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      editor.setPose(payload.head(), payload.body(), payload.rightArm(), payload.leftArm(), payload.rightLeg(),
          payload.leftLeg()
      );
    });
  }

  private static void handleSetPosePreset(Networking.SetPosePresetC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      editor.setPose(payload.pose().toPose());
    });
  }

  private static void handleSetYaw(Networking.SetYawC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      editor.setRotation(payload.angle());
    });
  }

  private static void handleUndo(Networking.UndoC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ServerPlayerEntity player = context.player();
      ScreenHandler currentScreenHandler = player.currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      Supplier<Boolean> action = payload.redo() ? editor::redo : editor::undo;
      String successMessage = payload.redo() ? "armorstands.message.redo" : "armorstands.message.undo";
      String failureMessage = payload.redo() ? "armorstands.message.redo.fail" : "armorstands.message.undo.fail";

      if (action.get()) {
        sendMessagePacket(player, successMessage);
      } else {
        sendMessagePacket(player, failureMessage);
      }
    });
  }

  private static void handleUtilityAction(Networking.UtilityActionC2S payload, ServerPlayNetworking.Context context) {
    context.player().server.execute(() -> {
      ServerPlayerEntity player = context.player();
      ScreenHandler currentScreenHandler = player.currentScreenHandler;
      if (!(currentScreenHandler instanceof HasArmorStandEditor screenHandler)) {
        return;
      }

      ArmorStandEditor editor = screenHandler.getEditor();
      payload.action().apply(editor, player);
    });
  }
}
