package net.quantium.energysink.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.quantium.energysink.EnergyLeaderboards;
import net.quantium.energysink.stats.StatsClient;

import javax.annotation.Nullable;
import java.util.*;

public class MessageChartRequest implements IMessage {

    public MessageChartRequest() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<MessageChartRequest, MessageChart> {

        @Override
        @Nullable
        public MessageChart onMessage(MessageChartRequest message, MessageContext ctx) {

            List<MessageChart.Chart> data = new ArrayList<>();

            EnergyLeaderboards.get().sorted().limit(7).forEach(info -> {
                long[] rate = new long[EnergyLeaderboards.TeamInfo.CHART_SAMPLES],
                       total = new long[EnergyLeaderboards.TeamInfo.CHART_SAMPLES];

                for(int i = 0; i < EnergyLeaderboards.TeamInfo.CHART_SAMPLES; i++) {
                    rate[i] = info.sampleRate(i);
                    total[i] = info.sampleEnergy(i);
                }

                data.add(new MessageChart.Chart(info.getName(), total, rate));
            });

            MessageChart msg = new MessageChart();
            msg.data = data;
            msg.owner = EnergyLeaderboards.getOwnerId(ctx.getServerHandler().player);
            return msg;
        }
    }
}
