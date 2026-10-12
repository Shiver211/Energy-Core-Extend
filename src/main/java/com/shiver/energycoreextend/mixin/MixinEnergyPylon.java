package com.shiver.energycoreextend.mixin;

import com.brandon3055.draconicevolution.blocks.machines.EnergyPylon;
import com.shiver.energycoreextend.api.IExtendedEnergyCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnergyPylon.class, remap = false)
public abstract class MixinEnergyPylon {
    @Inject(method = {"getComparatorInputOverride", "func_180641_l"},
            at = @At("HEAD"), cancellable = true)
    private void ece$unlimitedComparator(IBlockState state, World world, BlockPos pos,
                                        CallbackInfoReturnable<Integer> cir) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IExtendedEnergyCore && ((IExtendedEnergyCore) tile).isUnlimited()) {
            cir.setReturnValue(0);
        }
    }
}
