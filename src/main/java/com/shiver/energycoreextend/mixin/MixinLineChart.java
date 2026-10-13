package com.shiver.energycoreextend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sonar.fluxnetworks.common.core.FluxUtils;

import java.util.List;

@Pseudo
@Mixin(targets = "sonar.fluxnetworks.client.gui.LineChart", remap = false)
public abstract class MixinLineChart {
    @Shadow public long maxUnitY;
    @Shadow public String displayUnitY;

    @Inject(method = "calculateUnitY", at = @At("HEAD"), cancellable = true)
    private void ece$calculateUnitY(List<Long> data, CallbackInfo ci) {
        long max = 0L;
        if (data != null) {
            for (Long v : data) {
                if (v != null && v > max) {
                    max = v;
                }
            }
        }
        if (max <= 0L) {
            maxUnitY = 1L;
            displayUnitY = FluxUtils.format(maxUnitY, FluxUtils.TypeNumberFormat.COMPACT, "");
            ci.cancel();
            return;
        }
        int log10 = (int) Math.log10(max);
        switch (log10) {
            case 0:
                maxUnitY = max + 1L;
                break;
            case 1:
                maxUnitY = (max / 5L + 1L) * 5L;
                break;
            case 2:
                maxUnitY = (max / 50L + 1L) * 50L;
                break;
            default:
                if (log10 >= 18) {
                    // 当统计值接近或达到 Long.MAX_VALUE 时，避免 10^17 强转 int 截断并溢出为负数。
                    long unit = 100_000_000_000_000_000L;
                    long div = max / unit;
                    if (div >= 92) {
                        maxUnitY = Long.MAX_VALUE;
                    } else {
                        maxUnitY = (div + 1L) * unit;
                    }
                } else {
                    long unit = (long) Math.pow(10.0, log10 - 1);
                    maxUnitY = (max / unit + 1L) * unit;
                }
                break;
        }
        if (maxUnitY <= 0) {
            maxUnitY = Long.MAX_VALUE;
        }
        displayUnitY = FluxUtils.format(maxUnitY, FluxUtils.TypeNumberFormat.COMPACT, "");
        ci.cancel();
    }
}

