package dev.gasharaujo.behindclick;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class BehindClick {
    static final String MOD_ID = "gashs_behind_click";
    static final String CONTEXTUAL_KEY = "key.gashs_behind_click.modifier";
    static final String BLOCK_ONLY_KEY = "key.gashs_behind_click.block_only";

    private final KeyMapping contextual;
    private final KeyMapping blockOnly;
    private HitResult originalHit;
    private BlockHitResult redirectedHit;

    BehindClick(KeyMapping contextual, KeyMapping blockOnly) {
        this.contextual = contextual;
        this.blockOnly = blockOnly;
    }

    boolean onUse() {
        Minecraft minecraft = Minecraft.getInstance();
        restoreHit(minecraft);
        boolean onlyBlock = blockOnly.isDown();
        if (!onlyBlock && !contextual.isDown()) {
            return false;
        }

        BlockHitResult nextHit = findTarget(minecraft);
        if (nextHit == null) {
            return true;
        }
        if (onlyBlock) {
            useBlockOnly(minecraft, minecraft.player, nextHit);
            return true;
        }

        // Both hands follow Minecraft's normal use pipeline against the new target.
        originalHit = minecraft.hitResult;
        redirectedHit = nextHit;
        minecraft.hitResult = nextHit;
        return false;
    }

    void restoreHit(Minecraft minecraft) {
        if (redirectedHit != null && minecraft.hitResult == redirectedHit) {
            minecraft.hitResult = originalHit;
        }
        originalHit = null;
        redirectedHit = null;
    }

    static BlockHitResult findTarget(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.gameMode == null
            || !(minecraft.hitResult instanceof BlockHitResult firstHit)
            || firstHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return findNextBlock(level, player, firstHit);
    }

    private static BlockHitResult findNextBlock(ClientLevel level, LocalPlayer player, BlockHitResult firstHit) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getViewVector(1.0F).scale(Platform.blockReach(player)));
        BlockPos firstPosition = firstHit.getBlockPos();
        boolean[] passedFirstBlock = {false};
        ClipContext context = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);

        return BlockGetter.traverseBlocks(from, to, context, (clipContext, position) -> {
            if (!passedFirstBlock[0]) {
                passedFirstBlock[0] = position.equals(firstPosition);
                return null;
            }
            BlockState state = level.getBlockState(position);
            VoxelShape shape = clipContext.getBlockShape(state, level, position);
            return level.clipWithInteractionOverride(from, to, position, shape, state);
        }, ignored -> null);
    }

    private static void useBlockOnly(Minecraft minecraft, LocalPlayer player, BlockHitResult target) {
        Inventory inventory = player.getInventory();
        int originalSlot = Platform.getSelectedSlot(inventory);
        int emptySlot = findEmptySlot(inventory, originalSlot);
        if (emptySlot < 0) {
            Platform.showOverlay(player, Component.translatable("message.gashs_behind_click.empty_slot"));
            return;
        }

        // A vanilla use-on packet uses the server's selected item and sneak state.
        // Synchronize both around the click so no held item is used on either side.
        Runnable restoreSneaking = Platform.stopSneaking(player);
        try {
            if (emptySlot != originalSlot) {
                selectSlot(player, inventory, emptySlot);
            }
            InteractionResult result = minecraft.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, target);
            Platform.swingIfNeeded(player, result);
        } finally {
            if (emptySlot != originalSlot) {
                selectSlot(player, inventory, originalSlot);
            }
            restoreSneaking.run();
        }
    }

    private static int findEmptySlot(Inventory inventory, int selected) {
        if (inventory.getItem(selected).isEmpty()) {
            return selected;
        }
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private static void selectSlot(LocalPlayer player, Inventory inventory, int slot) {
        Platform.setSelectedSlot(inventory, slot);
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }
}
