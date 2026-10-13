package com.shiver.energycoreextend.energy;

import com.brandon3055.brandonscore.lib.EnergyHelper;
import com.shiver.energycoreextend.integration.FluxNetworksIntegration;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Loader;

/** 每个方向只调用一次接收端；通量插头使用原生 long 入口。 */
public final class EnergyTransfer {
    private EnergyTransfer() {}

    public static long sendTo(TileEntity source, EnumFacing side, long amount) {
        TileEntity target = source.getWorld().getTileEntity(source.getPos().offset(side));
        if (target == null || amount <= 0) return 0;
        EnumFacing receivingSide = side.getOpposite();
        if (Loader.isModLoaded("fluxnetworks") && FluxNetworksIntegration.isPlug(target)) {
            return FluxNetworksIntegration.receive(target, receivingSide, amount);
        }
        // insertEnergy 已检查 RF 接收接口和 FE 能力，避免旧预检查漏掉纯 RF 接收端。
        return EnergyHelper.insertEnergy(target, (int) Math.min(amount, Integer.MAX_VALUE), receivingSide, false);
    }
}
