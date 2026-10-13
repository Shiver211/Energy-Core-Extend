package com.shiver.energycoreextend;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import com.shiver.energycoreextend.integration.FluxNetworksIntegration;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:draconicevolution;required-after:brandonscore;required-after:mixinbooter;after:fluxnetworks")
public final class EnergyCoreExtend {
    public static final int EXTENDED_TIER = 9;

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("fluxnetworks")) FluxNetworksIntegration.register();
    }
}
