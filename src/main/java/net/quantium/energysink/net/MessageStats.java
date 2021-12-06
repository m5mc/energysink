package net.quantium.energysink.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.quantium.energysink.stats.StatsClient;

import javax.annotation.Nullable;

public class MessageStats implements IMessage {

    public int throughput;
    public int processRate;
    public boolean ownership;
    public long totalEnergy;
    public int totalProcessRate;

    public MessageStats() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        throughput = buf.readInt();
        processRate = buf.readInt();
        ownership = buf.readBoolean();
        totalEnergy = buf.readLong();
        totalProcessRate = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(throughput);
        buf.writeInt(processRate);
        buf.writeBoolean(ownership);
        buf.writeLong(totalEnergy);
        buf.writeInt(totalProcessRate);
    }

    public static class Handler implements IMessageHandler<MessageStats, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(MessageStats message, MessageContext ctx) {
            StatsClient.throughput = message.throughput;
            StatsClient.processRate = message.processRate;
            StatsClient.totalEnergy = message.totalEnergy;
            StatsClient.totalProcessRate = message.totalProcessRate;
            StatsClient.ownership = message.ownership;
            return null;
        }
    }
}
