package dev.gasharaujo.behindclick;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import org.lwjgl.glfw.GLFW;

public final class GashsBehindClick {
    private static final KeyMapping CONTEXTUAL =
        new KeyMapping(BehindClick.CONTEXTUAL_KEY, InputConstants.KEY_LALT, "key.categories.gashs_behind_click");
    private static final KeyMapping BLOCK_ONLY =
        new KeyMapping(BehindClick.BLOCK_ONLY_KEY, InputConstants.KEY_RALT, "key.categories.gashs_behind_click");
    private static final BehindClick CLICK = new BehindClick(CONTEXTUAL, BLOCK_ONLY);
    private static TimelesssIntegration timelesss;

    private GashsBehindClick() {}

    static void register(IEventBus modBus) {
        modBus.addListener(GashsBehindClick::registerKeyMappings);
        MinecraftForge.EVENT_BUS.addListener(GashsBehindClick::onInteractionKey);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                endTick();
            }
        });
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CONTEXTUAL);
        event.register(BLOCK_ONLY);
        if (ModList.get().isLoaded(TimelesssIntegration.MOD_ID)) {
            KeyMapping key = new KeyMapping(TimelesssIntegration.KEY, KeyConflictContext.IN_GAME,
                KeyModifier.ALT, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_KP_ENTER), "key.categories.gashs_behind_click");
            timelesss = TimelesssIntegration.load(key, "ActivateMessage");
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
