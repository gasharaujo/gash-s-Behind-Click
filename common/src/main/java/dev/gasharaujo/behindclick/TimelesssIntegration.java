package dev.gasharaujo.behindclick;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TimelesssIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelesssIntegration.class);
    static final String MOD_ID = "timelesss";
    static final String KEY = "key.gashs_behind_click.accelerate_behind";
    private static final String PACKAGE = "net.gasharaujo.timelesss.";

    private final KeyMapping key;
    private final Method findPreferred;
    private final Method sourceType;
    private final Method sourceIndex;
    private final Constructor<?> activationPacket;
    private boolean enabled = true;

    private TimelesssIntegration(KeyMapping key, String packetName) throws ReflectiveOperationException {
        this.key = key;
        ClassLoader loader = TimelesssIntegration.class.getClassLoader();
        Class<?> locator = Class.forName(PACKAGE + "item.TimelesssLocator", false, loader);
        Class<?> found = Class.forName(PACKAGE + "item.TimelesssLocator$Found", false, loader);
        Class<?> packet = Class.forName(PACKAGE + "TimelesssMod$" + packetName, false, loader);
        findPreferred = locator.getMethod("findPreferred", Player.class);
        sourceType = found.getMethod("sourceType");
        sourceIndex = found.getMethod("sourceIndex");
        activationPacket = packet.getConstructor(int.class, int.class, int.class, long.class, int.class);
    }

    static TimelesssIntegration load(KeyMapping key, String packetName) {
        try {
            return new TimelesssIntegration(key, packetName);
        } catch (ReflectiveOperationException | LinkageError failure) {
            LOGGER.warn("Could not load the Timelesss integration", failure);
            return null;
        }
    }

    void tick(Minecraft minecraft) {
        while (key.consumeClick()) {
            if (enabled) {
                activate(minecraft);
            }
        }
    }

    private void activate(Minecraft minecraft) {
        BlockHitResult target = BehindClick.findTarget(minecraft);
        if (target == null) {
            return;
        }

        try {
            // Use Timelesss' own selection order, including its optional Curios support.
            Optional<?> preferred = (Optional<?>) findPreferred.invoke(null, minecraft.player);
            if (preferred.isEmpty()) {
                return;
            }
            Object source = preferred.get();
            Object packet = activationPacket.newInstance(sourceType.invoke(source), sourceIndex.invoke(source),
                0, target.getBlockPos().asLong(), target.getDirection().get3DDataValue());
            Platform.sendTimelesss(minecraft.player, packet);
        } catch (ReflectiveOperationException | LinkageError failure) {
            enabled = false;
            LOGGER.warn("Could not activate Timelesss behind the targeted block", failure);
        }
    }
}
