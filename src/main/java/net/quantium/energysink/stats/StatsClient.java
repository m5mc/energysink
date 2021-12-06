package net.quantium.energysink.stats;

import net.quantium.energysink.net.MessageChart;
import net.quantium.energysink.net.MessageChartRequest;
import net.quantium.energysink.net.NetManager;

import java.util.List;

public class StatsClient {
    public static int throughput;
    public static int processRate;
    public static boolean ownership;
    public static long totalEnergy;
    public static int totalProcessRate;

    public static List<MessageChart.Chart> chart;
    public static String chartOwner;

    public static void requestChart() {
        chart = null;
        chartOwner = null;
        NetManager.sendToServer(new MessageChartRequest());
    }
}
