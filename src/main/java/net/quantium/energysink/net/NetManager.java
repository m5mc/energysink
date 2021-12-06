package net.quantium.energysink.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.quantium.energysink.ModProvider;

public class NetManager {
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ModProvider.MODID);

    static {
        CHANNEL.registerMessage(MessageStats.Handler.class, MessageStats.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(MessageChart.Handler.class, MessageChart.class, 2, Side.CLIENT);
        CHANNEL.registerMessage(MessageChartRequest.Handler.class, MessageChartRequest.class, 3, Side.SERVER);
    }

    public static void sendToServer(IMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void send(EntityPlayerMP ply, IMessage message) {
        CHANNEL.sendTo(message, ply);
    }
}
