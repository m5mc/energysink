package net.quantium.energysink.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.quantium.energysink.EnergyLeaderboards;
import net.quantium.energysink.stats.StatsClient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MessageChart implements IMessage {

    public List<Chart> data;
    public String owner;

    public MessageChart() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        owner = ByteBufUtils.readUTF8String(buf);

        int len = buf.readInt();

        List<Chart> data = new ArrayList<Chart>();

        for(int i = 0; i < len; i++) {
            String name = ByteBufUtils.readUTF8String(buf);
            long[] total = new long[EnergyLeaderboards.TeamInfo.CHART_SAMPLES];
            long[] rate = new long[EnergyLeaderboards.TeamInfo.CHART_SAMPLES];

            for(int j = 0; j < EnergyLeaderboards.TeamInfo.CHART_SAMPLES; j++) {
                total[j] = buf.readLong();
                rate[j] = buf.readInt();
            }

            data.add(new Chart(name, total, rate));
        }

        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, owner);

        buf.writeInt(data.size());

        for(Chart e : data) {
            ByteBufUtils.writeUTF8String(buf, e.name);

            for(int j = 0; j < EnergyLeaderboards.TeamInfo.CHART_SAMPLES; j++) {
                buf.writeLong(e.total[j]);
                buf.writeInt((int)e.rate[j]);
            }
        }
    }

    public static class Handler implements IMessageHandler<MessageChart, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(MessageChart message, MessageContext ctx) {
            StatsClient.chart = message.data;
            StatsClient.chartOwner = message.owner;
            return null;
        }
    }

    public static class Chart {
        public final String name;
        public final long[] total, rate;

        public Chart(String name, long[] total, long[] rate) {
            this.name = name;
            this.total = total;
            this.rate = rate;
        }
    }
}
