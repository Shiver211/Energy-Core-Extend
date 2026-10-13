package com.shiver.energycoreextend.mixin;

import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.lib.datamanager.ManagedByte;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import com.shiver.energycoreextend.api.IExtendedEnergyCore;
import com.shiver.energycoreextend.energy.EnergyTransfer;
import com.shiver.energycoreextend.energy.TransferBudget;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.math.BigInteger;

@Mixin(value = TileEnergyPylon.class, remap = false)
public abstract class MixinTileEnergyPylon extends TileBCBase implements IExtendedEnergyCore {
    @Shadow @Final public ManagedBool isOutputMode;
    @Shadow @Final public ManagedBool structureValid;
    @Shadow @Final private ManagedBool hasCoreLock;
    @Shadow @Final private ManagedByte particleRate;
    @Shadow private int lastCompOverride;
    @Shadow private TileEnergyStorageCore getCore() { throw new AssertionError(); }

    @Unique private final TransferBudget ece$budget = new TransferBudget();

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

    @Override
    public BigInteger getTransferRateBig() {
        TileEnergyStorageCore core = hasCoreLock.value ? getCore() : null;
        return core == null ? BigInteger.ZERO : ((IExtendedEnergyCore) core).getTransferRateBig();
    }

    @Override
    public long receiveEnergyLong(long amount, boolean simulate) {
        return ece$transfer(amount, simulate, false);
    }

    @Override
    public long extractEnergyLong(long amount, boolean simulate) {
        return ece$transfer(amount, simulate, true);
    }

    @Unique
    private long ece$transfer(long amount, boolean simulate, boolean extracting) {
        if (world.isRemote || amount <= 0 || !structureValid.value || !hasCoreLock.value
                || isOutputMode.value != extracting) return 0;
        TileEnergyStorageCore core = getCore();
        if (core == null || !core.active.value) return 0;

        long tick = world.getTotalWorldTime();
        long available = ece$budget.available(tick, amount);
        IExtendedEnergyCore storage = (IExtendedEnergyCore) core;
        long transferred = extracting ? storage.extractEnergyLong(available, simulate)
                : storage.receiveEnergyLong(available, simulate);
        if (!simulate && transferred > 0) {
            ece$budget.record(tick, transferred);
            particleRate.value = (byte) Math.min(20, Math.max(1, transferred / 500));
        }
        return transferred;
    }

    @Inject(method = "receiveEnergy", at = @At("HEAD"), cancellable = true)
    private void ece$receive(EnumFacing side, int amount, boolean simulate,
                             CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) receiveEnergyLong(amount, simulate));
    }

    @Inject(method = "extractEnergy", at = @At("HEAD"), cancellable = true)
    private void ece$extract(EnumFacing side, int amount, boolean simulate,
                             CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) extractEnergyLong(amount, simulate));
    }

    @Redirect(method = {"update", "func_73660_a"}, at = @At(value = "INVOKE",
            target = "Lcom/brandon3055/brandonscore/blocks/TileEnergyBase;sendEnergyToAll(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;I)I"))
    private int ece$sendEnergy(IBlockAccess access, BlockPos position, int legacyAmount) {
        for (EnumFacing side : EnumFacing.VALUES) {
            long available = extractEnergyLong(Long.MAX_VALUE, true);
            if (available == 0) break;
            long sent = EnergyTransfer.sendTo(this, side, available);
            extractEnergyLong(sent, false);
        }
        // long 路径已经扣能并更新粒子，原 int 路径无需再次扣除。
        return 0;
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
