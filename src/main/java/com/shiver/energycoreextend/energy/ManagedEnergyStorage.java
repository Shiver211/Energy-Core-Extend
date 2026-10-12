package com.shiver.energycoreextend.energy;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.datamanager.AbstractManagedData;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigInteger;

/** 基础值和溢出次数一起同步，防止客户端短暂拼出错误的总能量。 */
public final class ManagedEnergyStorage extends AbstractManagedData {
    public final OverflowEnergyStorage storage = new OverflowEnergyStorage();
    private long lastBase;
    private BigInteger lastOverflow = BigInteger.ZERO;

    @Override
    public boolean detectChanges() {
        if (lastBase == storage.getBase() && lastOverflow.equals(storage.getOverflow())) return false;
        lastBase = storage.getBase();
        lastOverflow = storage.getOverflow();
        return true;
    }

    @Override
    public void toBytes(MCDataOutput output) {
        byte[] overflow = storage.getOverflow().toByteArray();
        output.writeLong(storage.getBase());
        output.writeVarInt(overflow.length);
        output.writeArray(overflow);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        long base = input.readLong();
        BigInteger overflow = new BigInteger(input.readArray(input.readVarInt()));
        storage.restore(base, overflow);
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        if (storage.getBase() == 0 && storage.getOverflow().signum() == 0) {
            compound.removeTag(name);
            return;
        }
        NBTTagCompound value = new NBTTagCompound();
        value.setLong("base", storage.getBase());
        value.setByteArray("overflow", storage.getOverflow().toByteArray());
        compound.setTag(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        if (!compound.hasKey(name, 10)) {
            storage.restore(0, BigInteger.ZERO);
            return;
        }
        NBTTagCompound value = compound.getCompoundTag(name);
        storage.restore(value.getLong("base"), new BigInteger(value.getByteArray("overflow")));
    }
}
