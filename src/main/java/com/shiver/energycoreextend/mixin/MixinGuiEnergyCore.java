package com.shiver.energycoreextend.mixin;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import com.brandon3055.draconicevolution.client.gui.GuiEnergyCore;
import com.shiver.energycoreextend.api.IExtendedEnergyCore;
import com.shiver.energycoreextend.client.EnergyFormat;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigInteger;

import static com.shiver.energycoreextend.EnergyCoreExtend.EXTENDED_TIER;

@Mixin(value = GuiEnergyCore.class, remap = false)
public abstract class MixinGuiEnergyCore extends GuiContainer {
    @Shadow public TileEnergyStorageCore tile;
    @Shadow private GuiButton tierUp;
    @Shadow private GuiButton tierDown;
    @Shadow public static int layer;

    protected MixinGuiEnergyCore(Container container) {
        super(container);
    }

    @Unique
    private IExtendedEnergyCore ece$core() {
        return (IExtendedEnergyCore) tile;
    }

    @Inject(method = "updateButtonStates", at = @At("RETURN"))
    private void ece$updateTierButtons(CallbackInfo ci) {
        if (tile.active.value) return;
        tierUp.enabled = tile.tier.value < EXTENDED_TIER;
        if (ece$core().isUnlimited()) {
            tierDown.enabled = ece$core().getStoredBig()
                    .compareTo(BigInteger.valueOf((long) DEConfig.coreCapacity[7])) <= 0;
        }
    }

    @Inject(method = {"drawGuiContainerBackgroundLayer", "func_146976_a"},
            at = @At("HEAD"), cancellable = true)
    private void ece$drawEnergy(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        if (!ece$core().isUnlimited() || !tile.active.value) return;
        GuiHelper.drawGuiBaseBackground(this, guiLeft, guiTop, xSize, ySize);
        GuiHelper.drawPlayerSlots(this, guiLeft + xSize / 2, guiTop + 125, true);
        drawCenteredString(fontRenderer, I18n.format("gui.de.energyStorageCore.name", tile.tier.toString()),
                guiLeft + xSize / 2, guiTop + 5, InfoHelper.GUI_TITLE);
        ece$line(I18n.format("gui.de.capacity.txt"), 16, 0xFFAA00, true);
        ece$line(I18n.format("gui.energycoreextend.unlimited"), 27, 0x555555, false);
        ece$line(I18n.format("info.bc.charge.txt"), 38, 0xFFAA00, true);
        ece$line(EnergyFormat.compact(ece$core().getStoredBig()) + " RF", 49, 0x555555, false);

        BigInteger rate = ece$core().getTransferRateBig();
        int sign = rate.signum();
        ece$line(I18n.format(sign < 0 ? "gui.energycoreextend.transfer_discharge"
                : "gui.energycoreextend.transfer"), 59, 0xFFAA00, true);
        String signedRate = (sign > 0 ? "+" : sign < 0 ? "-" : "")
                + EnergyFormat.compact(rate.abs()) + " RF/t";
        ece$line(signedRate, 70, sign > 0 ? 0x00FF00 : sign < 0 ? 0xFF0000 : 0x222222, sign > 0);
        if (sign < 0) ece$line(ece$dischargeTime(rate), 80, 0x555555, false);
        ci.cancel();
    }

    @Unique
    private void ece$line(String text, int y, int colour, boolean shadow) {
        GuiHelper.drawCenteredString(fontRenderer, text, guiLeft + xSize / 2, guiTop + y, colour, shadow);
    }

    @Unique
    private String ece$dischargeTime(BigInteger rate) {
        return EnergyFormat.duration(EnergyFormat.dischargeTicks(ece$core().getStoredBig(), rate),
                I18n.format("gui.energycoreextend.years"), I18n.format("gui.energycoreextend.days"));
    }

    @Inject(method = {"drawScreen", "func_73863_a"}, at = @At("HEAD"), cancellable = true)
    private void ece$drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!ece$core().isUnlimited()) return;
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (tile.active.value) {
            GuiEnergyCore.drawEnergyBar(this, guiLeft + 5, guiTop + 92, 170,
                    true, 0, 1, false, mouseX, mouseY);
            ece$line(I18n.format("gui.energycoreextend.unlimited"), 95, 0xFFFFFF, true);

            if (GuiHelper.isInRect(guiLeft + 40, guiTop + 27, xSize - 80, 8, mouseX, mouseY)) {
                ece$tooltip(I18n.format("gui.energycoreextend.unlimited_capacity"), mouseX, mouseY);
            } else if (GuiHelper.isInRect(guiLeft + 5, guiTop + 48, xSize - 10, 9, mouseX, mouseY)
                    || GuiHelper.isInRect(guiLeft + 5, guiTop + 92, 170, 14, mouseX, mouseY)) {
                ece$tooltip(EnergyFormat.grouped(ece$core().getStoredBig()) + " RF", mouseX, mouseY);
            } else if (GuiHelper.isInRect(guiLeft + 5, guiTop + 69, xSize - 10, 10, mouseX, mouseY)) {
                BigInteger rate = ece$core().getTransferRateBig();
                String prefix = rate.signum() > 0 ? "+" : rate.signum() < 0 ? "-" : "";
                ece$tooltip(prefix + EnergyFormat.grouped(rate.abs()) + " RF/t", mouseX, mouseY);
            } else if (ece$core().getTransferRateBig().signum() < 0
                    && GuiHelper.isInRect(guiLeft + 5, guiTop + 80, xSize - 10, 9, mouseX, mouseY)) {
                ece$tooltip(I18n.format("gui.energycoreextend.discharge_eta",
                        ece$dischargeTime(ece$core().getTransferRateBig())), mouseX, mouseY);
            }
        } else if (!tierDown.enabled && GuiHelper.isInRect(tierDown.x, tierDown.y,
                tierDown.width, tierDown.height, mouseX, mouseY)) {
            ece$tooltip(I18n.format("message.energycoreextend.discharge_first"), mouseX, mouseY);
        }

        if (tile.buildGuide.value) {
            drawCenteredString(fontRenderer, layer == -1 ? "All" : Integer.toString(layer),
                    guiLeft + xSize / 2, guiTop - 10, 0xFFFFFF);
        }
        renderHoveredToolTip(mouseX, mouseY);
        ci.cancel();
    }

    @Unique
    private void ece$tooltip(String text, int mouseX, int mouseY) {
        drawHoveringText(fontRenderer.listFormattedStringToWidth(text, Math.min(320, width - 20)), mouseX, mouseY);
    }
}
