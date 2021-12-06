package net.quantium.energysink.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.quantium.energysink.EnergyLeaderboards;
import net.quantium.energysink.ModProvider;
import net.quantium.energysink.item.ItemModule;
import net.quantium.energysink.util.RingBuffer;

import javax.annotation.Nullable;

//@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "ic2", striprefs = true)
public class TileEntitySink extends TileEntity implements ICapabilityProvider, IEnergyStorage, ITickable, IEnergySink, IEnergyTile {
    private final InventoryBasic inventory = new InventoryBasic(ModProvider.MODID + ".inventory.name", true, 1);
    private String owner = null;
    private boolean addedToNet;

    public IInventory getInventory() {
        return inventory;
    }

    public boolean canInteractWith(EntityPlayer player) {
        return getDistanceSq(player.posX, player.posY, player.posZ) < 64f;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        inventory.setInventorySlotContents(0, new ItemStack(nbt.getCompoundTag("Slot")));
        owner = nbt.getString("Owner");
        super.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("Slot", inventory.getStackInSlot(0).serializeNBT());
        nbt.setString("Owner", owner);
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(this);
        }

        return super.getCapability(capability, facing);
    }

    public int getThroughput() {
        if(owner == null) return 0;
        return ItemModule.getLevel(inventory.getStackInSlot(0)).map(ItemModule.Level::getEnergy).orElse(0);
    }

    public double process(double energy, boolean simulate) {
        int throughput = getThroughput();
        double capacity = throughput - processedSinceLastTick + .4d;
        if(energy > capacity) {
            energy = (int)capacity;
            if(energy < 0) energy = 0; //that may happen i guess
        }

        if(!simulate) processedSinceLastTick += energy;
        return energy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int)(process((double)maxReceive * ModProvider.getConversionRate(), simulate) / ModProvider.getConversionRate());
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public long getAverageProcessRate() {
        return processRate.average();
    }

    private double processedSinceLastTick = 0;
    public final RingBuffer processRate = new RingBuffer(20);

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        ic2RemoveFromNet();
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        ic2RemoveFromNet();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        ic2AddToNet();
    }

    private void ic2RemoveFromNet() {
        if (this.addedToNet && ModProvider.isIC2Available()) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            this.addedToNet = false;
        }
    }

    private void ic2AddToNet() {
        if(!world.isRemote && !addedToNet && ModProvider.isIC2Available()) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToNet = true;
        }
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            ic2AddToNet();

            int processed = (int)processedSinceLastTick;
            EnergyLeaderboards.get().add(owner, processed);

            double damageChance = (processed / (double) getThroughput()) + 0.005f; //the epic bias
            if (world.rand.nextFloat() < damageChance * 0.2f)
                inventory.getStackInSlot(0).attemptDamageItem(1, world.rand, null);

            processRate.push(processed);
            processedSinceLastTick -= processed;
        }
    }

    @Override
    //@Optional.Method(modid = "ic2")
    public double getDemandedEnergy() {
        return process(getThroughput(), true);
    }

    @Override
    //@Optional.Method(modid = "ic2")
    public int getSinkTier() {
        return EnergyNet.instance.getTierFromPower(getThroughput());
    }

    @Override
    //@Optional.Method(modid = "ic2")
    public double injectEnergy(EnumFacing paramEnumFacing, double amount, double paramDouble2) {
        return process(amount, false);
    }

    @Override
    //@Optional.Method(modid = "ic2")
    public boolean acceptsEnergyFrom(IEnergyEmitter paramIEnergyEmitter, EnumFacing paramEnumFacing) {
        return true;
    }
}
