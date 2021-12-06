package net.quantium.energysink.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.energysink.ModProvider;
import net.quantium.energysink.inventory.ContainerSink;
import net.quantium.energysink.stats.StatsClient;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiSink extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModProvider.MODID, "textures/gui/inventory.png");

    public GuiSink(ContainerSink container) {
        super(container);
        this.xSize = 176;
        this.ySize = 120;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        final int x = this.guiLeft + 151;
        final int y = this.guiTop + 15;
        final int w = 18;
        final int h = 18;

        if(mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h)
        {
            mc.displayGuiScreen(new GuiChart());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        final int x = this.guiLeft + 151;
        final int y = this.guiTop + 15;
        final int w = 18;
        final int h = 18;

        if(mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h)
            this.drawHoveringText(buildStats(), mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(
                this.guiLeft,
                this.guiTop,
                0, 0,
                this.xSize, this.ySize);

        final int x = this.guiLeft + 151;
        final int y = this.guiTop + 15;
        final int w = 18;
        final int h = 18;
        boolean info = i >= x && i <= x + w && i1 >= y && i1 <= y + h;

        if (info) {
            this.drawTexturedModalRect(x, y, 176, h, w, h);
        } else {
            this.drawTexturedModalRect(x, y, 176, 0, w, h);
        }

        this.mc.fontRenderer.drawString(I18n.format(ModProvider.MODID + ".inventory.name"), this.guiLeft + 8, this.guiTop + 6, 4210752, false);

        int color = StatsClient.processRate >= StatsClient.throughput - 1 ? 0xff4444 : 4210752;
        this.mc.fontRenderer.drawString(I18n.format(ModProvider.MODID + ".stats.current.process", StatsClient.processRate, StatsClient.throughput), this.guiLeft + 8, this.guiTop + 16, color, false);

        if(!StatsClient.ownership) {
            this.mc.fontRenderer.drawString(I18n.format(ModProvider.MODID + ".stats.current.ownership"), this.guiLeft + 8, this.guiTop + 26, 0xff4444, false);
        }
    }

    private String buildStats() {
        StringBuilder builder = new StringBuilder();
        builder.append(I18n.format(ModProvider.MODID + ".stats.total.rate", readableRate(StatsClient.totalProcessRate))).append("\n");
        builder.append(I18n.format(ModProvider.MODID + ".stats.total.energy", readableEnergy(StatsClient.totalEnergy))).append("\n");
        return builder.toString();
    }

    public static String readableEnergy(long totalEnergy) {
        switch (readableAmountGroup(totalEnergy)) {
            case 0: return I18n.format(ModProvider.MODID + ".stats.total.energy.unit", String.format("%.2f", (double)totalEnergy));
            case 1: return I18n.format(ModProvider.MODID + ".stats.total.energy.kilo", String.format("%.2f", (double)totalEnergy / 1000L));
            case 2: return I18n.format(ModProvider.MODID + ".stats.total.energy.mega", String.format("%.2f", (double)totalEnergy / 1000000L));
            case 3: return I18n.format(ModProvider.MODID + ".stats.total.energy.giga", String.format("%.2f", (double)totalEnergy / 1000000000L));
            default: return I18n.format(ModProvider.MODID + ".stats.total.energy.tera", String.format("%.2f", (double)totalEnergy / 1000000000000L));
        }
    }

    public static String readableRate(long rate) {
        switch (readableAmountGroup(rate)) {
            case 0: return I18n.format(ModProvider.MODID + ".stats.total.rate.unit", String.format("%.2f", (double)rate));
            case 1: return I18n.format(ModProvider.MODID + ".stats.total.rate.kilo", String.format("%.2f", (double)rate / 1000L));
            case 2: return I18n.format(ModProvider.MODID + ".stats.total.rate.mega", String.format("%.2f", (double)rate / 1000000L));
            case 3: return I18n.format(ModProvider.MODID + ".stats.total.rate.giga", String.format("%.2f", (double)rate / 1000000000L));
            default: return I18n.format(ModProvider.MODID + ".stats.total.rate.tera",String.format("%.2f",  (double)rate / 1000000000000L));
        }
    }

    public static int readableAmountGroup(long energy) {
        if(energy <= 0) return 0;
        return (int) (Math.log10(energy) / 3);
    }
}
