package com.shiver.energycoreextend.mixin;

import it.unimi.dsi.fastutil.longs.LongList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sonar.fluxnetworks.api.network.FluxLogicType;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.api.tiles.IFluxConnector;

import java.math.BigInteger;

@Pseudo
@Mixin(targets = "sonar.fluxnetworks.common.connection.NetworkStatistics", remap = false)
public abstract class MixinFluxNetworkStatistics {
    @Shadow @Final private IFluxNetwork network;
    @Shadow public long energyInput;
    @Shadow public long energyOutput;
    @Shadow public long totalBuffer;
    @Shadow public long totalEnergy;
    @Shadow @Final public LongList energyChange;

    @Unique private BigInteger ece$input = BigInteger.ZERO;
    @Unique private BigInteger ece$output = BigInteger.ZERO;
    @Unique private BigInteger ece$history = BigInteger.ZERO;

    @Inject(method = "weakTick", at = @At("HEAD"), cancellable = true)
    private void ece$sample(CallbackInfo ci) {
        for (IFluxConnector plug : network.getConnections(FluxLogicType.PLUG)) {
            if (!plug.getConnectionType().isStorage()) {
                ece$input = ece$input.add(BigInteger.valueOf(plug.getTransferChange()));
            }
        }
        for (IFluxConnector point : network.getConnections(FluxLogicType.POINT)) {
            if (!point.getConnectionType().isStorage()) {
                ece$output = ece$output.subtract(BigInteger.valueOf(point.getTransferChange()));
            }
        }
        ci.cancel();
    }

    @Inject(method = "weakerTick", at = @At("RETURN"))
    private void ece$average(CallbackInfo ci) {
        // 保留原来的连接数、耗时统计；能量必须先求平均，再限制到显示范围。
        BigInteger input = ece$input.divide(BigInteger.valueOf(4));
        BigInteger output = ece$output.divide(BigInteger.valueOf(4));
        energyInput = ece$limit(input);
        energyOutput = ece$limit(output);
        ece$history = ece$history.add(input.max(output));
        ece$input = BigInteger.ZERO;
        ece$output = BigInteger.ZERO;

        BigInteger buffer = BigInteger.ZERO;
        BigInteger stored = BigInteger.ZERO;
        for (IFluxConnector device : network.getConnections(FluxLogicType.ANY)) {
            if (!device.getConnectionType().isStorage()) {
                buffer = buffer.add(BigInteger.valueOf(device.getTransferBuffer()));
            }
        }
        for (IFluxConnector storage : network.getConnections(FluxLogicType.STORAGE)) {
            stored = stored.add(BigInteger.valueOf(storage.getTransferBuffer()));
        }
        totalBuffer = ece$limit(buffer);
        totalEnergy = ece$limit(stored);
    }

    @Inject(method = "weakestTick", at = @At("RETURN"))
    private void ece$history(CallbackInfo ci) {
        energyChange.set(energyChange.size() - 1, ece$limit(ece$history.divide(BigInteger.valueOf(5))));
        ece$history = BigInteger.ZERO;
    }

    @Unique
    private static long ece$limit(BigInteger amount) {
        return amount.max(BigInteger.ZERO).min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }
}
