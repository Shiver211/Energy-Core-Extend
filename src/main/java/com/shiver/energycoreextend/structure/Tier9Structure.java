package com.shiver.energycoreextend.structure;

import com.brandon3055.brandonscore.lib.MultiBlockStorage;
import com.brandon3055.brandonscore.utils.MultiBlockHelper;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public final class Tier9Structure {
    public static final int SIZE = 13;
    public static final BlockPos OFFSET = new BlockPos(-6, -6, -6);
    public static final String DRACONIUM = "draconicevolution:draconium_block";
    public static final String AWAKENED_DRACONIUM = "draconicevolution:draconic_block";

    private Tier9Structure() {}

    /** 复制形状并替换材料，原版结构数据保持不变。 */
    public static MultiBlockStorage create(MultiBlockStorage tier8, MultiBlockHelper helper) {
        String[][][] blocks = new String[SIZE][SIZE][SIZE];
        for (String[][] layer : blocks) {
            for (String[] row : layer) Arrays.fill(row, "");
        }
        tier8.forEachBlock(BlockPos.ORIGIN, (pos, block) ->
                blocks[pos.getX()][pos.getY()][pos.getZ()] =
                        DRACONIUM.equals(block) ? AWAKENED_DRACONIUM : block);

        MultiBlockStorage result = new MultiBlockStorage(SIZE, helper);
        for (int y = 0; y < SIZE; y++) {
            if (y > 0) result.newLayer();
            for (int x = 0; x < SIZE; x++) result.addRow(blocks[x][y]);
        }
        return result;
    }
}
