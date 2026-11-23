package dev.gasharaujo.behindclick;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Input;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

final class Platform {
    private Platform() {}

    static double blockReach(LocalPlayer player) {
        return player.blockInteractionRange();
    }

    static int getSelectedSlot(Inventory inventory) {
        return inventory.getSelectedSlot();
    }

    static void setSelectedSlot(Inventory inventory, int slot) {
        inventory.setSelectedSlot(slot);
    }

    static Runnable stopSneaking(LocalPlayer player) {
        Input controls = player.input.keyPresses;
        Input sent = player.getLastSentInput();
        if (!controls.shift() && !sent.shift()) {
            return () -> {};
        }

        Input released = new Input(sent.forward(), sent.backward(), sent.left(), sent.right(),
            sent.jump(), false, sent.sprint());
        if (controls.shift()) {
            player.input.keyPresses = new Input(controls.forward(), controls.backward(),
                controls.left(), controls.right(), controls.jump(), false, controls.sprint());
        }
        if (sent.shift()) {
            player.connection.send(new ServerboundPlayerInputPacket(released));
        }
        return () -> {
            player.input.keyPresses = controls;
            if (sent.shift()) {
                player.connection.send(new ServerboundPlayerInputPacket(sent));
            }
        };
    }

    static void swingIfNeeded(LocalPlayer player, InteractionResult result) {
        if (result instanceof InteractionResult.Success success
            && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    static void sendTimelesss(LocalPlayer player, Object packet) {
        CustomPacketPayload payload = (CustomPacketPayload) packet;
        if (NetworkRegistry.hasChannel(player.connection, payload.type().id())) {
            ClientPacketDistributor.sendToServer(payload);
        }
    }

    static void showOverlay(LocalPlayer player, Component message) {
        player.sendOverlayMessage(message);
    }
}
