package net.quantium.energysink.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.quantium.energysink.EnergyLeaderboards;
import net.quantium.energysink.ModProvider;
import net.quantium.energysink.net.MessageChart;
import net.quantium.energysink.stats.StatsClient;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiChart extends GuiScreen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModProvider.MODID, "textures/gui/chart.png");

    private static final int[] COLORS = { 0xffe500, 0xe0cc1d, 0xc4b427, 0xa19427, 0x8f8532, 0x756e34, 0x5c5838 };
    private static final int COLOR_YOU = 0xdc143c;

    private int guiLeft, guiTop, xSize, ySize;
    private boolean rateMode;

    private static final int CHART_LEFT = 9, CHART_TOP = 16, CHART_WIDTH = 133, CHART_HEIGHT = 94;

    @Override
    public void initGui() {
        super.initGui();
        StatsClient.requestChart();

        this.xSize = 176;
        this.ySize = 120;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        final int x = this.guiLeft + 151;
        final int y = this.guiTop + 15;
        final int w = 18;
        final int h = 18;

        //button
        boolean mouse = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        if(mouse) {
            rateMode = !rateMode;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        final int x = this.guiLeft + 151;
        final int y = this.guiTop + 15;
        final int w = 18;
        final int h = 18;

        //button
        boolean info = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        if (info) {
            this.drawTexturedModalRect(x, y, 176, h, w, h);
        } else {
            this.drawTexturedModalRect(x, y, 176, 0, w, h);
        }

        if(rateMode) {
            this.mc.fontRenderer.drawString(I18n.format(ModProvider.MODID + ".chart.name.rate"), this.guiLeft + 8, this.guiTop + 6, 4210752, false);
        } else {
            this.mc.fontRenderer.drawString(I18n.format(ModProvider.MODID + ".chart.name.total"), this.guiLeft + 8, this.guiTop + 6, 4210752, false);
        }

        if(StatsClient.chart == null) {
            this.mc.fontRenderer.drawString(I18n.format(ModProvider.MODID + ".chart.loading"), this.guiLeft + CHART_LEFT + 2, this.guiTop + CHART_TOP, 4210752, false);
        } else {
            drawChart(mouseX, mouseY);
        }

        if(info) {
            if(rateMode) {
                this.drawHoveringText(I18n.format(ModProvider.MODID + ".chart.switch.rate"), mouseX, mouseY);
            } else {
                this.drawHoveringText(I18n.format(ModProvider.MODID + ".chart.switch.total"), mouseX, mouseY);
            }
        }
    }

    private void drawChart(int mouseX, int mouseY) {
        long min = Long.MAX_VALUE;
        long max = 0;

        for(MessageChart.Chart data : StatsClient.chart) {
            if(rateMode) {
                for(int i = 0; i < data.rate.length; i++) {
                    long v = data.rate[i];
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                }
            } else {
                for(int i = 0; i < data.rate.length; i++) {
                    long v = data.total[i];
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                }
            }
        }

        max += 1;
        min -= 1;

        long range = max - min;

        max += range / 10;
        min -= range / 10;

        if(min < 0) min = 0;

        drawGridLine(min, max, min);
        drawGridLine(min, max, max);
        drawGridLine(min, max, max / 2 + min / 2);

        for(int i = 0; i < EnergyLeaderboards.TeamInfo.CHART_SAMPLES; i += 10) {
            drawGridLineTime(i);
        }

        int index = 0;
        for(MessageChart.Chart data : StatsClient.chart) {
            int color = data.name.equals(StatsClient.chartOwner) ? COLOR_YOU : COLORS[Math.min(index, COLORS.length)];
            index++;

            if(rateMode) {
                drawChartLine(data.rate, color, min, max);
            } else {
                drawChartLine(data.total, color, min, max);
            }
        }

        drawCursor(mouseX, mouseY, min, max);
    }

    private void drawGridLineTime(int index) {
        float x = CHART_WIDTH - (float)index * CHART_WIDTH / (EnergyLeaderboards.TeamInfo.CHART_SAMPLES - 1);

        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(1f);
        GlStateManager.color(0.3f, 0.3f, 0.3f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        buf.pos(guiLeft + CHART_LEFT + x, guiTop + CHART_TOP, zLevel + 1).endVertex();
        buf.pos(guiLeft + CHART_LEFT + x, guiTop + CHART_TOP + CHART_HEIGHT, zLevel + 1).endVertex();

        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    private void drawGridLine(long min, long max, long value) {
        int y = (int) (CHART_HEIGHT - (double)(value - min) * CHART_HEIGHT / (max - min));

        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(1f);
        GlStateManager.color(0.3f, 0.3f, 0.3f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        buf.pos(guiLeft + CHART_LEFT, guiTop + CHART_TOP + y, zLevel + 1).endVertex();
        buf.pos(guiLeft + CHART_LEFT + CHART_WIDTH, guiTop + CHART_TOP + y, zLevel + 1).endVertex();

        tessellator.draw();
        GlStateManager.enableTexture2D();

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.translate(0, 0, 100);

        y += 2;
        if(y > CHART_HEIGHT)
            y = CHART_HEIGHT - this.mc.fontRenderer.FONT_HEIGHT / 2 - 2;

        if(rateMode) {
            this.mc.fontRenderer.drawString(GuiSink.readableRate(value), 2 * (guiLeft + CHART_LEFT + 2), 2 * (guiTop + CHART_TOP + y), 4210752);
        } else {
            this.mc.fontRenderer.drawString(GuiSink.readableEnergy(value), 2 * (guiLeft + CHART_LEFT + 2), 2 * (guiTop + CHART_TOP + y), 4210752);
        }

        GlStateManager.popMatrix();
    }

    private void drawChartLine(long[] samples, int color, long min, long max) {
        int len = samples.length;

        GlStateManager.color(((color >> 16) & 0xff) / 255.0f, ((color >> 8) & 0xff) / 255.0f, (color & 0xff) / 255.0f);
        GlStateManager.glLineWidth(2);
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        for(int i = 0; i < len; i++) {
            int y = (int) (CHART_HEIGHT - (samples[i] - min) * CHART_HEIGHT / (max - min));
            if (y < 0) y = 0;
            if (y > CHART_HEIGHT) y = CHART_HEIGHT;

            int x = (int)(CHART_WIDTH - (float)i * CHART_WIDTH / (len - 1));

            buf.pos(guiLeft + CHART_LEFT + x, guiTop + CHART_TOP + y, zLevel + 1).endVertex();
        }

        tessellator.draw();

        GlStateManager.enableTexture2D();
    }

    private void drawCursor(int mouseX, int mouseY, long min, long max) {
        int relX = mouseX - guiLeft - CHART_LEFT;
        int relY = mouseY - guiTop - CHART_TOP;

        if(relX <= 0 || relX >= CHART_WIDTH) return;
        if(relY <= 0 || relY >= CHART_HEIGHT) return;

        int sample = (int)(((float)(CHART_WIDTH - relX) * EnergyLeaderboards.TeamInfo.CHART_SAMPLES) / CHART_WIDTH + 0.5f);
        if (sample < 0) sample = 0;
        if (sample >= EnergyLeaderboards.TeamInfo.CHART_SAMPLES) sample = EnergyLeaderboards.TeamInfo.CHART_SAMPLES - 1;
        long value = (CHART_HEIGHT - relY) * (max - min) / CHART_HEIGHT + min;

        MessageChart.Chart chart = null;
        long di = Long.MAX_VALUE;

        for(MessageChart.Chart data : StatsClient.chart) {
            long distance = Math.abs(value - (rateMode ? data.rate[sample] : data.total[sample]));

            // priority to (you)
            if (data.name.equals(StatsClient.chartOwner))
                distance = distance / 4 * 3;

            if (distance < di) {
                di = distance;
                chart = data;
            }
        }

        long chartValue = chart == null ? 0 : rateMode ? chart.rate[sample] : chart.total[sample];

        //gridline
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(2f);
        GlStateManager.color(1f, 1f, 1f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        float x = CHART_WIDTH - (float)sample * CHART_WIDTH / (EnergyLeaderboards.TeamInfo.CHART_SAMPLES - 1);
        buf.pos(guiLeft + CHART_LEFT + x, guiTop + CHART_TOP, zLevel + 1).endVertex();
        buf.pos(guiLeft + CHART_LEFT + x, guiTop + CHART_TOP + CHART_HEIGHT, zLevel + 1).endVertex();

        int y = (int) (CHART_HEIGHT - ((chart == null ? value : chartValue) - min) * CHART_HEIGHT / (max - min));
        buf.pos(guiLeft + CHART_LEFT, guiTop + CHART_TOP + y, zLevel + 1).endVertex();
        buf.pos(guiLeft + CHART_LEFT + CHART_WIDTH, guiTop + CHART_TOP + y, zLevel + 1).endVertex();

        tessellator.draw();
        GlStateManager.enableTexture2D();

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.translate(mouseX, mouseY, 0);

        if (chart != null) {
            List<String> list = new ArrayList<>();
            list.add(chart.name);
            list.add(I18n.format(ModProvider.MODID + ".chart.hover.days", String.format("%.1f", sample / 10f)));
            list.add(GuiSink.readableEnergy(chart.total[sample]));
            list.add(GuiSink.readableRate(chart.rate[sample]));

            drawHoveringText(list, mouseX, mouseY);
        }

        GlStateManager.popMatrix();
    }
}
