package com.shiver.energycoreextend.energy;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.datamanager.AbstractManagedData;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigInteger;

public final class ManagedBigInteger extends AbstractManagedData {
    public BigInteger value = BigInteger.ZERO;
    private BigInteger lastValue = BigInteger.ZERO;

    public ManagedBigInteger() {}

    public ManagedBigInteger(BigInteger initial) {
        this.value = initial == null ? BigInteger.ZERO : initial;
        this.lastValue = this.value;
    }

    public void setValue(BigInteger val) {
        this.value = val == null ? BigInteger.ZERO : val;
    }

    @Override
    public boolean detectChanges() {
        if (value == null) value = BigInteger.ZERO;
        if (lastValue != null && lastValue.equals(value)) return false;
        lastValue = value;
        return true;
    }

    @Override
    public void toBytes(MCDataOutput output) {
        if (value == null) value = BigInteger.ZERO;
        byte[] bytes = value.toByteArray();
        output.writeVarInt(bytes.length);
        output.writeArray(bytes);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        int length = input.readVarInt();
        byte[] bytes = input.readArray(length);
        value = new BigInteger(bytes);
        lastValue = value;
    }

    @Override
    public void toNBT(NBTTagCompound compound) {}

    @Override
    public void fromNBT(NBTTagCompound compound) {}
}

