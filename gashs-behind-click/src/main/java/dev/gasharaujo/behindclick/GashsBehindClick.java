package dev.gasharaujo.behindclick;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = GashsBehindClick.MOD_ID, dist = Dist.CLIENT)
public final class GashsBehindClick {
    public static final String MOD_ID = "gashs_behind_click";

    private static final KeyMapping BEHIND_CLICK_MODIFIER = new KeyMapping(
        "key.gashs_behind_click.modifier",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_LALT,
        "key.categories.gashs_behind_click"
    );

    public GashsBehindClick(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, GashsBehindClick::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(
            InputEvent.InteractionKeyMappingTriggered.class,
            GashsBehindClick::onInteractionKey
        );
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BEHIND_CLICK_MODIFIER);
    }

    private static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()
            || event.getHand() != InteractionHand.MAIN_HAND
            || !BEHIND_CLICK_MODIFIER.isDown()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            cancelInteraction(event);
            return;
        }

        if (!(minecraft.hitResult instanceof BlockHitResult firstHit)
            || firstHit.getType() != HitResult.Type.BLOCK) {
            cancelInteraction(event);
            return;
        }

        BlockHitResult nextHit = findNextBlock(
            minecraft.level,
            minecraft.player,
            firstHit
        );
        if (nextHit == null) {
            cancelInteraction(event);
            return;
        }

        // The normal Minecraft interaction continues from here. Only its target changes,
        // so the packet sent to the server remains completely vanilla.
        minecraft.hitResult = nextHit;
    }

    private static BlockHitResult findNextBlock(
        ClientLevel level,
        LocalPlayer player,
        BlockHitResult firstHit
    ) {
        Vec3 from = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0F);
        Vec3 to = from.add(direction.scale(player.blockInteractionRange()));
        BlockPos firstPosition = firstHit.getBlockPos();
        boolean[] passedFirstBlock = {false};

        ClipContext context = new ClipContext(
            from,
            to,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            player
        );

        BlockHitResult result = BlockGetter.traverseBlocks(
            from,
            to,
            context,
            (clipContext, position) -> {
                if (!passedFirstBlock[0]) {
                    if (position.equals(firstPosition)) {
                        passedFirstBlock[0] = true;
                    }
                    return null;
                }

                BlockState state = level.getBlockState(position);
                VoxelShape shape = clipContext.getBlockShape(state, level, position);
                return level.clipWithInteractionOverride(from, to, position, shape, state);
            },
            ignored -> BlockHitResult.miss(
                to,
                Direction.getNearest(direction),
                BlockPos.containing(to)
            )
        );

        if (result.getType() != HitResult.Type.BLOCK
            || result.getBlockPos().equals(firstPosition)) {
            return null;
        }

        return result;
    }

    private static void cancelInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        event.setSwingHand(false);
        event.setCanceled(true);
    }
}
