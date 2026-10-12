package com.shiver.energycoreextend.mixin;

import com.brandon3055.brandonscore.lib.datamanager.ManagedByte;
import com.brandon3055.draconicevolution.client.render.tile.RenderTileEnergyStorageCore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import static com.shiver.energycoreextend.EnergyCoreExtend.EXTENDED_TIER;

@Mixin(value = RenderTileEnergyStorageCore.class, remap = false)
public abstract class MixinRenderTileEnergyStorageCore {
    @Redirect(method = "render(Lcom/brandon3055/draconicevolution/blocks/tileentity/TileEnergyStorageCore;DDDFIF)V",
            slice = @Slice(from = @At(value = "FIELD",
                    target = "Lcom/brandon3055/draconicevolution/client/render/tile/RenderTileEnergyStorageCore;SCALES:[D")),
            at = @At(value = "FIELD", target = "Lcom/brandon3055/brandonscore/lib/datamanager/ManagedByte;value:B"))
    private byte ece$coreAppearance(ManagedByte tier) {
        // 切片从模型缩放开始，前面的建造指引仍然读取 9 级结构。
        return tier.value == EXTENDED_TIER ? (byte) 8 : tier.value;
    }

    @Redirect(method = {"renderStabilizers", "renderStabilizerBeam"}, at = @At(value = "FIELD",
            target = "Lcom/brandon3055/brandonscore/lib/datamanager/ManagedByte;value:B"))
    private byte ece$stabilizerAppearance(ManagedByte tier) {
        return tier.value == EXTENDED_TIER ? (byte) 8 : tier.value;
    }
}
