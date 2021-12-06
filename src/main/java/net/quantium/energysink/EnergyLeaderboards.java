package net.quantium.energysink;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.quantium.energysink.util.RingBuffer;

import java.util.Comparator;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber
public class EnergyLeaderboards extends WorldSavedData {
    public static final String ID = ModProvider.MODID + ":energy";

    public static String getOwnerId(EntityPlayer player) {
        if (player.getTeam() == null) {
            return "player:" + player.getName();
        }

        return "team:" + player.getTeam().getName();
    }

    public static EnergyLeaderboards get() {
        MapStorage storage = Objects.requireNonNull(DimensionManager.getWorld(0).getMapStorage());
        EnergyLeaderboards instance = (EnergyLeaderboards) storage.getOrLoadData(EnergyLeaderboards.class, ID);

        if (instance == null) {
            instance = new EnergyLeaderboards();
            storage.setData(ID, instance);
        }

        return instance;
    }

    public EnergyLeaderboards(String id) {
        super(id);
    }

    public EnergyLeaderboards() {
        super(ID);
    }

    private int sampleCounter = 0;
    private long total = 0;
    private final HashMap<String, TeamInfo> teams = new HashMap<>();

    public long getTotal() {
        return total;
    }

    @Nullable
    public TeamInfo get(@Nullable String team) {
        if(team == null) team = "null";
        return teams.get(team);
    }

    public long set(@Nullable String team, long energy) {
        if(team == null) team = "null";
        TeamInfo info = teams.get(team);
        if(info == null) {
            info = new TeamInfo(team, 0);
            teams.put(team, info);
        }

        long delta = energy - info.getEnergy();
        total += delta;
        info.addEnergy(delta);

        this.markDirty();
        return info.energy;
    }

    public long add(@Nullable String team, long energy) {
        TeamInfo info = get(team);
        long current = info == null ? 0 : info.getEnergy();
        return set(team, current + energy);
    }

    public Stream<TeamInfo> iter() {
        return teams.values().stream();
    }

    public Stream<TeamInfo> sorted() {
        return this.iter().sorted(Comparator.comparingLong(e -> -e.energy));
    }

    private void doUpdate() {
        for(TeamInfo team : teams.values()) {
            team.update();
        }

        if(++sampleCounter >= TeamInfo.CHART_SAMPLE_PERIOD) {
            sampleCounter = 0;

            for(TeamInfo team : teams.values()) {
                team.sample();
            }

            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        teams.clear();
        total = 0;

        NBTTagList list = nbt.getTagList("Teams", 10);

        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            TeamInfo team = new TeamInfo(tag);
            if (!teams.containsKey(team.getName())) {
                total += team.getEnergy();
                teams.put(team.getName(), team);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();

        for(TeamInfo entry : teams.values()) {
            list.appendTag(entry.serialize());
        }

        nbt.setTag("Teams", list);
        return nbt;
    }

    public static class TeamInfo {
        public static final int CHART_SAMPLES = 40;
        public static final int CHART_SAMPLE_PERIOD = 2400;

        private final String name;
        private long energy;

        private long processedSinceLastTick;
        private final RingBuffer processRate = new RingBuffer(20);

        private final RingBuffer chartTotal;
        private final RingBuffer chartRate;

        private TeamInfo(String name, long energy) {
            this.name = name;
            this.energy = energy;
            this.chartRate = new RingBuffer(CHART_SAMPLES);
            this.chartTotal = new RingBuffer(CHART_SAMPLES);
        }

        private TeamInfo(NBTTagCompound nbt) {
            this.name = nbt.getString("Name");
            this.energy = nbt.getLong("Energy");
            this.chartRate = RingBuffer.deserialize(CHART_SAMPLES, nbt.getTagList("ChartRate", 4));
            this.chartTotal = RingBuffer.deserialize(CHART_SAMPLES, nbt.getTagList("ChartTotal", 4));
        }

        private NBTTagCompound serialize() {
            NBTTagCompound tag = new NBTTagCompound();

            tag.setString("Name", name);
            tag.setLong("Energy", energy);
            tag.setTag("ChartRate", chartRate.serialize());
            tag.setTag("ChartTotal", chartTotal.serialize());

            return tag;
        }

        public long getEnergy() {
            return energy;
        }

        public long getRate() {
            return processRate.average();
        }

        public String getName() {
            return name;
        }

        public long sampleEnergy(int idx) {
            return chartTotal.get(idx);
        }

        public long sampleRate(int idx) {
            return chartRate.get(idx);
        }

        private void addEnergy(long energz) {
            energy += energz;
            processedSinceLastTick += energz;
        }

        private void update() {
            processRate.push(processedSinceLastTick);
            processedSinceLastTick = 0;
        }

        private void sample() {
            chartTotal.push(getEnergy());
            chartRate.push(getRate());
        }
    }

    @SubscribeEvent
    public static void onUpdate(TickEvent.ServerTickEvent e) {
        if(e.phase == TickEvent.Phase.END) {
            get().doUpdate();
        }
    }
}
