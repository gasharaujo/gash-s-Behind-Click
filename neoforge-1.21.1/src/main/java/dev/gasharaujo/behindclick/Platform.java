package dev.gasharaujo.behindclick;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

final class Platform {
    private Platform() {}

    static double blockReach(LocalPlayer player) {
        return player.blockInteractionRange();
    }

    static int getSelectedSlot(Inventory inventory) {
        return inventory.selected;
    }

    static void setSelectedSlot(Inventory inventory, int slot) {
        inventory.selected = slot;
    }

    static Runnable stopSneaking(LocalPlayer player) {
        if (!player.isShiftKeyDown()) {
            return () -> {};
        }

        player.input.shiftKeyDown = false;
        player.connection.send(new ServerboundPlayerCommandPacket(player,
            ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        return () -> {
            player.input.shiftKeyDown = true;
            player.connection.send(new ServerboundPlayerCommandPacket(player,
                ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        };
    }

    static void swingIfNeeded(LocalPlayer player, InteractionResult result) {
        if (result.shouldSwing()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    static void sendTimelesss(LocalPlayer player, Object packet) {
        CustomPacketPayload payload = (CustomPacketPayload) packet;
        if (NetworkRegistry.hasChannel(player.connection, payload.type().id())) {
            PacketDistributor.sendToServer(payload);
        }
    }

    static void showOverlay(LocalPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}
