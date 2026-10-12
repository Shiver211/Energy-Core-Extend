package com.shiver.energycoreextend.mixin;

import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import com.shiver.energycoreextend.api.IExtendedEnergyCore;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.math.BigInteger;

@Mixin(value = TileEnergyPylon.class, remap = false)
public abstract class MixinTileEnergyPylon extends TileBCBase implements IExtendedEnergyCore {
    @Shadow @Final public ManagedBool isOutputMode;
    @Shadow @Final private ManagedBool hasCoreLock;
    @Shadow private int lastCompOverride;
    @Shadow private TileEnergyStorageCore getCore() { throw new AssertionError(); }

    @Override
    public boolean isUnlimited() {
        TileEnergyStorageCore core = hasCoreLock.value ? getCore() : null;
        return core != null && ((IExtendedEnergyCore) core).isUnlimited();
    }

    @Override
    public BigInteger getStoredBig() {
        TileEnergyStorageCore core = hasCoreLock.value ? getCore() : null;
        return core == null ? BigInteger.ZERO : ((IExtendedEnergyCore) core).getStoredBig();
    }

    @Inject(method = "getEnergyStored", at = @At("RETURN"), cancellable = true)
    private void ece$leaveInputSpace(EnumFacing side, CallbackInfoReturnable<Integer> cir) {
        // 仅输入端预留空间；输出端仍能每次提供 Integer.MAX_VALUE。
        if (!isOutputMode.value && isUnlimited()) {
            cir.setReturnValue(Math.min(cir.getReturnValueI(), Integer.MAX_VALUE / 2));
        }
    }

    @Inject(method = "updateComparators", at = @At("HEAD"), cancellable = true)
    private void ece$unlimitedComparator(CallbackInfo ci) {
        if (!isUnlimited()) return;
        if (lastCompOverride != 0) {
            lastCompOverride = 0;
            world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
        }
        ci.cancel();
    }
}
