package net.quantium.energysink.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.quantium.energysink.ModProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemModule extends Item {

    public static final ItemModule INSTANCE = new ItemModule();

    public enum Level {
        LOW("low", 32, 60 * 30 * 5, EnumRarity.COMMON),
        MEDIUM("medium", 128, 60 * 50 * 5, EnumRarity.UNCOMMON),
        HIGH("high", 512, 60 * 60 * 5, EnumRarity.RARE),
        EXTREME("extreme", 2048, 60 * 70 * 5, EnumRarity.EPIC),
        INSANE("insane", 8192, 60 * 80 * 5, new IRarity() {

            @Override
            public TextFormatting getColor() {
                return TextFormatting.RED;
            }

            @Override
            public String getName() {
                return "Insane";
            }
        });

        private final String name;
        private final int energy;
        private final int durability;
        private final IRarity rarity;

        Level(String name, int energy, int durability, IRarity rarity) {
            this.name = name;
            this.energy = energy;
            this.durability = durability;
            this.rarity = rarity;
        }

        public String getName() {
            return name;
        }

        public int getEnergy() {
            return energy;
        }

        public int getDurability() {
            return durability;
        }

        public IRarity getRarity() { return rarity; }
    }

    public ItemModule() {
        this.setCreativeTab(CreativeTabs.MISC);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setRegistryName(ModProvider.MODID, "module");

        this.addPropertyOverride(new ResourceLocation("level"), (stack, worldIn, entityIn) -> getLevel(stack).map(Enum::ordinal).orElse(-1));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return String.format("item.%s.module.%s", ModProvider.MODID, getLevel(stack).orElse(Level.LOW).getName());
    }

    public static Optional<Level> getLevel(ItemStack stack) {
        if (stack.getTagCompound() != null) {
            int level = stack.getTagCompound().getInteger("Level");
            if(level >= 0 && level < Level.values().length) {
                return Optional.of(Level.values()[level]);
            }
        }

        return Optional.empty();
    }

    public static ItemStack getStack(Level level) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Level", level.ordinal());
        ItemStack stack = new ItemStack(INSTANCE, 1, 0);
        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab != this.getCreativeTab()) return;

        for (Level level : Level.values()) {
            items.add(getStack(level));
        }
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getLevel(stack).orElse(Level.LOW).getDurability();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format(ModProvider.MODID + ".tooltip.module.info"));
        tooltip.add(I18n.format(ModProvider.MODID + ".tooltip.module.energy", getLevel(stack).orElse(Level.LOW).getEnergy()));
    }

    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return getLevel(stack).map(Level::getRarity).orElse(super.getForgeRarity(stack));
    }
}
