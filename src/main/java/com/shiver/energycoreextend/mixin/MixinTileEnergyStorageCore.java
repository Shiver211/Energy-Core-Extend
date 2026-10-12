package com.shiver.energycoreextend.mixin;

import codechicken.lib.data.MCDataInput;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.lib.datamanager.ManagedByte;
import com.brandon3055.brandonscore.lib.datamanager.ManagedLong;
import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import com.shiver.energycoreextend.api.IExtendedEnergyCore;
import com.shiver.energycoreextend.energy.EnergyFlowTracker;
import com.shiver.energycoreextend.energy.ManagedEnergyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
import java.util.Arrays;

import static com.shiver.energycoreextend.EnergyCoreExtend.EXTENDED_TIER;

@Mixin(value = TileEnergyStorageCore.class, remap = false)
public abstract class MixinTileEnergyStorageCore extends TileBCBase implements IExtendedEnergyCore {
    @Shadow @Final public ManagedBool active;
    @Shadow @Final public ManagedByte tier;
    @Shadow @Final public ManagedLong energy;
    @Shadow @Final public ManagedLong transferRate;
    @Shadow private long[] flowArray;
    @Shadow public abstract boolean validateStructure();

    @Unique private ManagedEnergyStorage ece$energy;
    @Unique private final EnergyFlowTracker ece$flow = new EnergyFlowTracker();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ece$registerEnergy(CallbackInfo ci) {
        ece$energy = register("energycoreextend:energy", new ManagedEnergyStorage())
                .saveToTile().syncViaTile().finish();
    }

    @Override
    public boolean isUnlimited() {
        return tier.value == EXTENDED_TIER;
    }

    @Override
    public BigInteger getStoredBig() {
        return isUnlimited() ? ece$energy.storage.getStoredBig() : BigInteger.valueOf(energy.value);
    }

    @Override
    public void readExtraNBT(NBTTagCompound compound) {
        super.readExtraNBT(compound);
        if (isUnlimited()) ece$flow.reset(getStoredBig());
    }

    @Inject(method = "getCapacity", at = @At("HEAD"), cancellable = true)
    private void ece$legacyCapacity(CallbackInfoReturnable<Long> cir) {
        if (isUnlimited()) cir.setReturnValue(Long.MAX_VALUE);
    }

    @Inject(method = "getExtendedStorage", at = @At("HEAD"), cancellable = true)
    private void ece$legacyStored(CallbackInfoReturnable<Long> cir) {
        if (isUnlimited()) cir.setReturnValue(ece$energy.storage.getStoredLimited(Long.MAX_VALUE / 2));
    }

    @Inject(method = "receiveEnergy", at = @At("HEAD"), cancellable = true)
    private void ece$receive(int amount, boolean simulate, CallbackInfoReturnable<Integer> cir) {
        if (!isUnlimited()) return;
        int received = world.isRemote ? 0 : (int) ece$energy.storage.receive(amount, simulate);
        if (!simulate && received > 0) ece$energyChanged();
        cir.setReturnValue(received);
    }

    @Inject(method = "extractEnergy", at = @At("HEAD"), cancellable = true)
    private void ece$extract(int amount, boolean simulate, CallbackInfoReturnable<Integer> cir) {
        if (!isUnlimited()) return;
        int extracted = world.isRemote ? 0 : (int) ece$energy.storage.extract(amount, simulate);
        if (!simulate && extracted > 0) ece$energyChanged();
        cir.setReturnValue(extracted);
    }

    @Unique
    private void ece$energyChanged() {
        // 原字段只作为兼容视图，9 级的真实能量由专用对象保存。
        energy.value = ece$energy.storage.getStoredLimited(Long.MAX_VALUE);
        markDirty();
    }

    @Inject(method = "receivePacketFromClient", at = @At("HEAD"), cancellable = true)
    private void ece$changeTier(MCDataInput data, EntityPlayerMP player, int id, CallbackInfo ci) {
        if (active.value) return;
        if (id == 1 && tier.value == 8) {
            ece$energy.storage.setStoredBig(BigInteger.valueOf(energy.value));
            tier.value = EXTENDED_TIER;
            ece$flow.reset(getStoredBig());
        } else if (id == 2 && isUnlimited()) {
            BigInteger stored = getStoredBig();
            if (stored.compareTo(BigInteger.valueOf((long) DEConfig.coreCapacity[7])) > 0) {
                TextComponentTranslation message = new TextComponentTranslation("message.energycoreextend.discharge_first");
                message.getStyle().setColor(TextFormatting.RED);
                player.sendMessage(message);
                ci.cancel();
                return;
            }
            energy.value = stored.longValueExact();
            ece$energy.storage.restore(0, BigInteger.ZERO);
            tier.value = 8;
        } else {
            return;
        }
        energy.lastTickValue = energy.value;
        Arrays.fill(flowArray, 0);
        transferRate.value = 0;
        validateStructure();
        markDirty();
        dataManager.forceSync();
        dataManager.forceSync(transferRate);
        ci.cancel();
    }

    // 同时列出开发名和发行版 SRG 名；TileBCBase.update 本身没有改名。
    @Inject(method = {"update", "func_73660_a"}, at = @At(value = "INVOKE",
            target = "Lcom/brandon3055/brandonscore/blocks/TileBCBase;update()V"))
    private void ece$trackActualFlow(CallbackInfo ci) {
        if (!world.isRemote && isUnlimited()) transferRate.value = ece$flow.sample(getStoredBig());
    }

    @Redirect(method = {"update", "func_73660_a"}, at = @At(value = "FIELD",
            target = "Lcom/brandon3055/brandonscore/lib/datamanager/ManagedByte;value:B"))
    private byte ece$physicalTier(ManagedByte value) {
        // update 中对 tier 的读取仅用于玩家排斥距离；结构仍使用真实的 9 级。
        return value.value == EXTENDED_TIER ? (byte) 8 : value.value;
    }
}
