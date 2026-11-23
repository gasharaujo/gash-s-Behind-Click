package dev.gasharaujo.behindclick;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BehindClick.MOD_ID)
public final class ForgeEntrypoint {
    public ForgeEntrypoint(FMLJavaModLoadingContext context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GashsBehindClick.register(context.getModEventBus()));
    }
}
