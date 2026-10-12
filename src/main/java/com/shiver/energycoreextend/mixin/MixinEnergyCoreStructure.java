package com.shiver.energycoreextend.mixin;

import com.brandon3055.brandonscore.lib.MultiBlockStorage;
import com.brandon3055.brandonscore.utils.MultiBlockHelper;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import com.brandon3055.draconicevolution.world.EnergyCoreStructure;
import com.shiver.energycoreextend.structure.Tier9Structure;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

import static com.shiver.energycoreextend.EnergyCoreExtend.EXTENDED_TIER;

@Mixin(value = EnergyCoreStructure.class, remap = false)
public abstract class MixinEnergyCoreStructure extends MultiBlockHelper {
    @Shadow private MultiBlockStorage[] structureTiers;
    @Shadow private TileEnergyStorageCore core;

    @Inject(method = "initialize", at = @At("RETURN"))
    private void ece$addTier(TileEnergyStorageCore core, CallbackInfoReturnable<EnergyCoreStructure> cir) {
        structureTiers = Arrays.copyOf(structureTiers, EXTENDED_TIER);
        structureTiers[EXTENDED_TIER - 1] = Tier9Structure.create(structureTiers[7], this);
    }

    @Inject(method = "checkTier", at = @At("HEAD"), cancellable = true)
    private void ece$checkTier(int tier, CallbackInfoReturnable<Boolean> cir) {
        if (tier == EXTENDED_TIER) {
            cir.setReturnValue(structureTiers[tier - 1]
                    .checkStructure(core.getWorld(), core.getPos().add(Tier9Structure.OFFSET)));
        }
    }

    @Inject(method = "placeTier", at = @At("HEAD"), cancellable = true)
    private void ece$placeTier(int tier, CallbackInfo ci) {
        if (tier == EXTENDED_TIER) {
            structureTiers[tier - 1].placeStructure(core.getWorld(), core.getPos().add(Tier9Structure.OFFSET));
            ci.cancel();
        }
    }

    @Inject(method = "getCoreOffset", at = @At("HEAD"), cancellable = true)
    private void ece$keepOffset(int tier, CallbackInfoReturnable<BlockPos> cir) {
        if (tier == EXTENDED_TIER) cir.setReturnValue(Tier9Structure.OFFSET);
    }
}
