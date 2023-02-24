package me.roundaround.armorstands.network;

import java.util.Arrays;

import me.roundaround.armorstands.ArmorStandsMod;
import me.roundaround.armorstands.mixin.ArmorStandEntityAccessor;
import me.roundaround.armorstands.util.actions.MoveAction;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public enum ArmorStandFlag {
  HIDE_BASE_PLATE("base"),
  SHOW_ARMS("arms"),
  SMALL("small"),
  NO_GRAVITY("gravity"),
  INVISIBLE("visible"),
  NAME("name"),
  INVULNERABLE("invulnerable"),
  UNKNOWN("unknown");

  private final String id;

  private ArmorStandFlag(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return id;
  }

  public boolean getValue(ArmorStandEntity armorStand) {
    switch (this) {
      case HIDE_BASE_PLATE:
        return armorStand.shouldHideBasePlate();
      case SHOW_ARMS:
        return armorStand.shouldShowArms();
      case SMALL:
        return armorStand.isSmall();
      case NO_GRAVITY:
        return armorStand.hasNoGravity();
      case INVISIBLE:
        return armorStand.isInvisible();
      case NAME:
        return armorStand.isCustomNameVisible();
      case INVULNERABLE:
        return armorStand.isInvulnerable();
      default:
        return false;
    }
  }

  public void setValue(ArmorStandEntity armorStand, boolean value) {
    ArmorStandEntityAccessor accessor = (ArmorStandEntityAccessor) armorStand;

    switch (this) {
      case HIDE_BASE_PLATE:
        accessor.invokeSetHideBasePlate(value);
        break;
      case SHOW_ARMS:
        accessor.invokeSetShowArms(value);
        break;
      case SMALL:
        accessor.invokeSetSmall(value);
        break;
      case NO_GRAVITY:
        if (!value) {
          Vec3d pos = armorStand.getPos();
          double blockY = (double) armorStand.getBlockY();
          boolean atBlockPos = Math.abs(pos.y - blockY) < MathHelper.EPSILON;
          if (atBlockPos) {
            MoveAction.setPosition(armorStand, pos.x, pos.y + 0.001, pos.z);
          }
        }
        armorStand.setNoGravity(value);
        break;
      case INVISIBLE:
        armorStand.setInvisible(value);
        break;
      case NAME:
        armorStand.setCustomNameVisible(value);
        break;
      case INVULNERABLE:
        armorStand.setInvulnerable(value);
        break;
      default:
        // Do nothing for unknown
        ArmorStandsMod.LOGGER.warn("Tried to set value to flag {}. Ignoring.", name());
    }
  }

  public static ArmorStandFlag fromString(String value) {
    return Arrays.stream(ArmorStandFlag.values())
        .filter((flag) -> flag.id.equals(value))
        .findFirst()
        .orElseGet(() -> {
          ArmorStandsMod.LOGGER.warn("Unknown flag id '{}'. Returning UNKNOWN.", value);
          return UNKNOWN;
        });
  }
}
