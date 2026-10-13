package com.shiver.energycoreextend.integration;

import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.shiver.energycoreextend.api.IExtendedEnergyCore;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import sonar.fluxnetworks.api.energy.ITileEnergyHandler;
import sonar.fluxnetworks.api.network.ConnectionType;
import sonar.fluxnetworks.api.tiles.IFluxPlug;
import sonar.fluxnetworks.common.handler.TileEntityHandler;

import javax.annotation.Nonnull;

/** 仅在通量网络存在时加载，防止可选依赖影响普通 RF / FE 环境。 */
public final class FluxNetworksIntegration implements ITileEnergyHandler {
    private FluxNetworksIntegration() {}

    public static void register() {
        // 通用 FE 处理器会将数量截断为 int，专用处理器必须优先匹配。
        TileEntityHandler.tileEnergyHandlers.add(0, new FluxNetworksIntegration());
    }

    public static boolean isPlug(TileEntity tile) {
        return tile instanceof IFluxPlug && ((IFluxPlug) tile).getConnectionType() == ConnectionType.PLUG;
    }

    public static long receive(TileEntity tile, EnumFacing side, long amount) {
        IFluxPlug plug = (IFluxPlug) tile;
        return plug.isActive() ? plug.getTransferHandler().receiveFromSupplier(amount, side, false) : 0;
    }

    @Override
    public boolean hasCapability(@Nonnull TileEntity tile, EnumFacing side) {
        return tile instanceof TileEnergyPylon;
    }

    @Override
    public boolean canAddEnergy(@Nonnull TileEntity tile, EnumFacing side) {
        return ((IExtendedEnergyCore) tile).receiveEnergyLong(1, true) > 0;
    }

    @Override
    public boolean canRemoveEnergy(@Nonnull TileEntity tile, EnumFacing side) {
        return ((IExtendedEnergyCore) tile).extractEnergyLong(1, true) > 0;
    }

    @Override
    public long addEnergy(long amount, @Nonnull TileEntity tile, EnumFacing side, boolean simulate) {
        return ((IExtendedEnergyCore) tile).receiveEnergyLong(amount, simulate);
    }

    @Override
    public long removeEnergy(long amount, @Nonnull TileEntity tile, EnumFacing side) {
        return ((IExtendedEnergyCore) tile).extractEnergyLong(amount, false);
    }
}
