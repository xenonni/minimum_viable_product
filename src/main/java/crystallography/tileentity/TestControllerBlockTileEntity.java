package crystallography.tileentity;

import crystallography.init.ModBlocks;
import crystallography.init.ModItems;
import crystallography.init.ModTileEntityTypes;
import crystallography.libs.Util;
import crystallography.libs.multiblock.MultiBlockComponent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestControllerBlockTileEntity extends TileEntity{

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String INVENTORY_TAG = "vat_data";

    private Collection<BlockPos> structure;

    // private HashMap<Item, Integer> data = new HashMap<>();

    private VatData vatData = new VatData();

    // Store the capability lazy optionals as fields to keep the amount of objects we use to a minimum
    private final LazyOptional<ItemStackHandler> inventoryCapabilityExternal = LazyOptional.of(() -> this.inventory);

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
            return true;
        }

        @Override
        protected void onContentsChanged(final int slot) {
            super.onContentsChanged(slot);
            // Mark the tile entity as having changed whenever its inventory changes.
            // "markDirty" tells vanilla that the chunk containing the tile entity has
            // changed and means the game will save the chunk to disk later.
            markDirty();
        }
    };

    public TestControllerBlockTileEntity() {
        super(ModTileEntityTypes.TEST_CONTROLLER_BLOCK_TILE_ENTITY.get());
    }

    public void setStructure(Collection<BlockPos> structure)
    {
        this.structure = structure;
        // TODO set max capacity based on the number of fluid blocks
    }

    public Collection<BlockPos> getStructure()
    {
        if(structure == null)
            return new ArrayList<>(); // why are you calling me without having a structure?
        return structure;
    }

    public void addItem(Item item, int count)
    {

         vatData.add(item, count);

    }

    //DEBUG
    public void reportContents()
    {
        vatData.reportContents();
    }

    /**
     * Read saved data from disk into the tile.
     */
    @Override
    public void read(CompoundNBT tag) {
        super.read(tag); // does this go before or after?
        this.inventory.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        // TODO vatData
        // CompoundNBT vatDataTag = tag.getCompound("vatData");
        // vatData.deserializeNBT(vatDataTag);
    }

    /**
     * Write data from the tile into a compound tag for saving to disk.
     */
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag); // does this go before or after?
        tag.put(INVENTORY_TAG, inventory.serializeNBT());
        // TODO vatData
        // CompoundNBT compound = vatData.serializeNBT();
        // tag.put("vatData", compound);
        return tag;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventoryCapabilityExternal.cast();
        }
        return super.getCapability(cap, side);
    }

    //DEBUG
    public void craft() {
//        if(items.get(Items.IRON_ORE) != null && items.get(Items.IRON_ORE) > 0 && items.get(ModItems.EXAMPLE_ITEM) != null && items.get(ModItems.EXAMPLE_ITEM) > 0)
//        {
//            BlockPos crystallizingPos;
//
//            for (BlockPos p : structure)
//            {
//                if (getWorld().getBlockState(p).getBlock().equals(ModBlocks.NUCLEATION_BLOCK.get()))
//                {
//                    // check for any eligible neighbors, which include water or NotFluid
//                    Map<Direction, Block> neighbors = Util.getNeighbors(getWorld(), p);
//                    for(Direction d : neighbors.keySet())
//                    {
//                        if(neighbors.get(d).equals(Blocks.WATER) || neighbors.get(d).equals(ModBlocks.NOT_FLUID.get()))
//                        {
//                            crystallizingPos = p.offset(d);
//
//                            getWorld().setBlockState(crystallizingPos, ModBlocks.IRON_CRYSTAL_BLOCK.get().getDefaultState());
//                            items.put(Items.IRON_ORE, items.get(Items.IRON_ORE) - 1);
//                            items.put(ModItems.EXAMPLE_ITEM, items.get(ModItems.EXAMPLE_ITEM) - 1);
//                            return;
//                        }
//                    }
//                }
//            }
//            LOGGER.info("No valid crystallizing position was found.");
//            return;
//        }
//        LOGGER.info("Insufficient crafting materials for recipe: IRON_CRYSTAL_BLOCK");
        vatData.craft();
    }

    private class VatData implements INBTSerializable<CompoundNBT>
    {
        protected NonNullList<ItemStack> stacks;

        protected HashMap<Item, Integer> items = new HashMap<>();

        private int maxSize;

        public VatData()
        {
            this(2);
        }

        public VatData(int maxSize)
        {
            this.maxSize = maxSize;
        }

        public void setSize(int size)
        {
            maxSize = size;
        }

        public void add(Item item, int count)
        {
            if(items.containsKey(item))
                items.put(item, items.get(item) + count);
            else
                items.put(item, count);

            LOGGER.info("I received data. Item: " + item + ", count: " + count);
        }

        @Override
        public CompoundNBT serializeNBT() {
            // TODO implement
            //copied from ItemStackHandler
            ListNBT nbtTagList = new ListNBT();
            for (int i = 0; i < stacks.size(); i++)
            {
                if (!stacks.get(i).isEmpty())
                {
                    CompoundNBT itemTag = new CompoundNBT();
                    itemTag.putInt("Slot", i);
                    stacks.get(i).write(itemTag);
                    nbtTagList.add(itemTag);
                }
            }
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("Items", nbtTagList);
            nbt.putInt("Size", stacks.size());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            // TODO implement
            //copied from ItemStackHandler
            setSize(nbt.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size") : stacks.size());
            ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++)
            {
                CompoundNBT itemTags = tagList.getCompound(i);
                int slot = itemTags.getInt("Slot");

                if (slot >= 0 && slot < stacks.size())
                {
                    stacks.set(slot, ItemStack.read(itemTags));
                }
            }
            onLoad();
        }

        // protected void onLoad() {} // I'm not sure why this exists

        public void reportContents() {
            for(Item item : items.keySet())
                LOGGER.info("I contain Item: " + item + ", count: " + items.get(item));
        }

        public void craft() {
            // if I have at least one iron ore and at least one example item, place an iron crystal block on one of the fluid (or NotFluid) neighbors of any nucleation block
            if(items.get(Items.IRON_ORE) != null && items.get(Items.IRON_ORE) > 0 && items.get(ModItems.EXAMPLE_ITEM) != null && items.get(ModItems.EXAMPLE_ITEM) > 0)
            {
                BlockPos crystallizingPos;

                for (BlockPos p : structure)
                {
                    if (getWorld().getBlockState(p).getBlock().equals(ModBlocks.NUCLEATION_BLOCK.get()))
                    {
                        // check for any eligible neighbors, which include water or NotFluid
                        Map<Direction, Block> neighbors = Util.getNeighbors(getWorld(), p);
                        for(Direction d : neighbors.keySet())
                        {
                            if(neighbors.get(d).equals(Blocks.WATER) || neighbors.get(d).equals(ModBlocks.NOT_FLUID.get()))
                            {
                                crystallizingPos = p.offset(d);

                                getWorld().setBlockState(crystallizingPos, ModBlocks.IRON_CRYSTAL_BLOCK.get().getDefaultState());
                                items.put(Items.IRON_ORE, items.get(Items.IRON_ORE) - 1);
                                items.put(ModItems.EXAMPLE_ITEM, items.get(ModItems.EXAMPLE_ITEM) - 1);
                                return;
                            }
                        }
                    }
                }
                LOGGER.info("No valid crystallizing position was found.");
                return;
            }
            LOGGER.info("Insufficient crafting materials for recipe: IRON_CRYSTAL_BLOCK");
        }
    }
}

