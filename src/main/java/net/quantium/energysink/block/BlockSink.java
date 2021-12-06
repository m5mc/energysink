package net.quantium.energysink.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.quantium.energysink.EnergyLeaderboards;
import net.quantium.energysink.ModProvider;
import net.quantium.energysink.tileentity.TileEntitySink;

import javax.annotation.Nullable;
import java.util.Objects;

public class BlockSink extends BlockContainer {

    public static final BlockSink INSTANCE = new BlockSink();
    public static final Item ITEM = new ItemBlock(INSTANCE).setRegistryName(Objects.requireNonNull(INSTANCE.getRegistryName()));

    public BlockSink() {
        super(Material.IRON);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setHardness(36.0f);
        this.setResistance(999999f);
        this.setLightLevel(0.95f);
        this.setRegistryName(ModProvider.MODID, "sink");
        this.setHarvestLevel("pickaxe", 2);
        this.setUnlocalizedName(ModProvider.MODID + ".sink");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySink();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {
            playerIn.openGui(ModProvider.instance(), 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if(!worldIn.isRemote && placer instanceof EntityPlayer) {
            String id = EnergyLeaderboards.getOwnerId((EntityPlayer)placer);
            ((TileEntitySink) Objects.requireNonNull(worldIn.getTileEntity(pos))).setOwner(id);
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(!worldIn.isRemote) {
            ItemStack module = ((TileEntitySink) Objects.requireNonNull(worldIn.getTileEntity(pos))).getInventory().getStackInSlot(0);
            if(!module.isEmpty()) {
                EntityItem item = new EntityItem(worldIn, pos.getX() + .5d, pos.getY() + .5d, pos.getZ() + .5d, module);
                item.motionY += worldIn.rand.nextFloat() * 0.3f;
                worldIn.spawnEntity(item);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }
}
