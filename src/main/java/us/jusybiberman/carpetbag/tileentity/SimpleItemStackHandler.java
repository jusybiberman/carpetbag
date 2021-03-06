package us.jusybiberman.carpetbag.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

public class SimpleItemStackHandler extends ItemStackHandler {

	private final boolean allowWrite;
	private final TileEntity tile;

	public SimpleItemStackHandler(TileEntity inv, boolean allowWrite, int size) {
		super(size);
		this.allowWrite = allowWrite;
		this.tile = inv;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (allowWrite) {
			return super.insertItem(slot, stack, simulate);
		} else return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (allowWrite) {
			return super.extractItem(slot, amount, simulate);
		} else return ItemStack.EMPTY;
	}

	@Override
	public void onContentsChanged(int slot) {
		tile.markDirty();
	}

}
