package net.quantium.energysink.stats;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.quantium.energysink.EnergyLeaderboards;
import net.quantium.energysink.net.MessageStats;
import net.quantium.energysink.net.NetManager;
import net.quantium.energysink.tileentity.TileEntitySink;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class StatsServer {
    private static final HashMap<EntityPlayerMP, TileEntitySink> trackers = new HashMap<>();

    private static int counter;

    @SubscribeEvent
    public static void onUpdate(TickEvent.ServerTickEvent e) {
        if(e.phase == TickEvent.Phase.END) {
            if(++counter > 10) {
                counter = 0;

                sendUpdates();
            }
        }
    }

    private static void sendUpdates() {
        for(Map.Entry<EntityPlayerMP, TileEntitySink> entry : trackers.entrySet()) {
            sendUpdate(entry.getKey(), entry.getValue());
        }
    }

    private static void sendUpdate(EntityPlayerMP player, TileEntitySink tile) {
        MessageStats message = new MessageStats();
        EnergyLeaderboards.TeamInfo team = EnergyLeaderboards.get().get(tile.getOwner());

        message.throughput = tile.getThroughput();
        message.processRate = (int)tile.getAverageProcessRate();
        message.ownership = EnergyLeaderboards.getOwnerId(player).equals(tile.getOwner());
        message.totalEnergy = team == null ? 0 : team.getEnergy();
        message.totalProcessRate = (int)(team == null ? 0 : team.getRate());

        NetManager.send(player, message);
    }

    public static void addTracker(EntityPlayerMP player, TileEntitySink tile) {
        trackers.put(player, tile);
        sendUpdate(player, tile);
    }

    public static void removeTracker(EntityPlayerMP player) {
        trackers.remove(player);
    }
}
