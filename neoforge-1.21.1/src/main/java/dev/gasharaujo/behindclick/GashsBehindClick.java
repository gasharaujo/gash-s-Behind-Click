package dev.gasharaujo.behindclick;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = BehindClick.MOD_ID, dist = Dist.CLIENT)
public final class GashsBehindClick {
    private static final KeyMapping CONTEXTUAL =
        new KeyMapping(BehindClick.CONTEXTUAL_KEY, InputConstants.KEY_LALT, "key.categories.gashs_behind_click");
    private static final KeyMapping BLOCK_ONLY =
        new KeyMapping(BehindClick.BLOCK_ONLY_KEY, InputConstants.KEY_RALT, "key.categories.gashs_behind_click");
    private static final BehindClick CLICK = new BehindClick(CONTEXTUAL, BLOCK_ONLY);
    private static TimelesssIntegration timelesss;

    public GashsBehindClick(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, GashsBehindClick::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(InputEvent.InteractionKeyMappingTriggered.class, GashsBehindClick::onInteractionKey);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> endTick());
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CONTEXTUAL);
        event.register(BLOCK_ONLY);
        if (ModList.get().isLoaded(TimelesssIntegration.MOD_ID)) {
            KeyMapping key = new KeyMapping(TimelesssIntegration.KEY, KeyConflictContext.IN_GAME,
                KeyModifier.ALT, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_KP_ENTER), "key.categories.gashs_behind_click");
            timelesss = TimelesssIntegration.load(key, "ActivatePayload");
            if (timelesss != null) {
                event.register(key);
            }
        }
    }

    private static void endTick() {
        Minecraft minecraft = Minecraft.getInstance();
        CLICK.restoreHit(minecraft);
        if (timelesss != null) {
            timelesss.tick(minecraft);
        }
    }

    private static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!event.isUseItem()) {
            CLICK.restoreHit(Minecraft.getInstance());
        } else if (CLICK.onUse()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }
}
