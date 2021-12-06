package net.quantium.energysink;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.energysink.block.BlockSink;
import net.quantium.energysink.client.GuiSink;
import net.quantium.energysink.commands.LeaderboardsCommand;
import net.quantium.energysink.inventory.ContainerSink;
import net.quantium.energysink.item.ItemModule;
import net.quantium.energysink.tileentity.TileEntitySink;

import javax.annotation.Nullable;

@Mod(modid = ModProvider.MODID, name = ModProvider.NAME, version = ModProvider.VERSION)
public class ModProvider
{
    public static final String MODID = "qenergysink";
    public static final String NAME = "Energy Sink";
    public static final String VERSION = "1.0";

    @Mod.Instance(MODID)
    private static ModProvider instance;
    public static ModProvider instance() {
        return instance;
    }

    private float conversionRate = 0.25f;
    private boolean isIC2Available = false;

    public static float getConversionRate() {
        return instance.conversionRate;
    }

    public static boolean isIC2Available() {
        return instance.isIC2Available;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        Configuration c = new Configuration(e.getSuggestedConfigurationFile());
        conversionRate = c.getFloat("rfConversionRate", "conversion", 0.25f, 0, 1000, "RF -> EU conversion rate");
        c.save();
        
        GameRegistry.registerTileEntity(TileEntitySink.class, new ResourceLocation(MODID, "sink"));
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        isIC2Available = Loader.isModLoaded("ic2");
        System.out.println("[QEnergySink] Conversion rate [RF -> EU]: " + conversionRate + "; IC2 support = " + isIC2Available);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new LeaderboardsCommand());
    }

    @Mod.EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> e) {
            e.getRegistry().register(BlockSink.INSTANCE);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> e) {
            e.getRegistry().register(ItemModule.INSTANCE);
            e.getRegistry().register(BlockSink.ITEM);
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(ItemModule.INSTANCE, 0, new ModelResourceLocation(MODID + ":module", "inventory"));
            ModelLoader.setCustomModelResourceLocation(BlockSink.ITEM, 0, new ModelResourceLocation(MODID + ":sink", "inventory"));
        }
    }

    private static class GuiHandler implements IGuiHandler {

        @Override @Nullable
        public ContainerSink getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
            if (entity instanceof TileEntitySink) {
                return new ContainerSink((TileEntitySink) entity, player);
            }

            return null;
        }

        @Override @Nullable
        public GuiSink getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            ContainerSink container = getServerGuiElement(id, player, world, x, y, z);
            if(container != null) {
                return new GuiSink(container);
            }
            return null;
        }
    }
}
