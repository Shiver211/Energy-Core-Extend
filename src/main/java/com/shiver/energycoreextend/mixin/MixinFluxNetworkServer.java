package com.shiver.energycoreextend.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sonar.fluxnetworks.common.connection.FluxNetworkServer;

@Pseudo
@Mixin(targets = "sonar.fluxnetworks.common.connection.FluxNetworkServer", remap = false)
public abstract class MixinFluxNetworkServer {
    @Redirect(method = "onEndServerTick", at = @At(value = "FIELD",
            target = "Lsonar/fluxnetworks/common/connection/FluxNetworkServer;bufferLimiter:J",
            opcode = Opcodes.PUTFIELD, ordinal = 1))
    private void ece$saturateDemand(FluxNetworkServer network, long total) {
        // 两个非负 long 相加变成负数时，需求已经超过 long 上限。
        network.bufferLimiter = total < 0 ? Long.MAX_VALUE : total;
    }
}
