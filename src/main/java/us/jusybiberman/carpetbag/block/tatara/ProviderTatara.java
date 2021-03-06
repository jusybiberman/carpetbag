package us.jusybiberman.carpetbag.block.tatara;

import com.google.common.collect.Lists;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import us.jusybiberman.carpetbag.api.capability.ICarpetbagPlayer;
import us.jusybiberman.carpetbag.capability.CPBCapabilityManager;
import us.jusybiberman.carpetbag.crafting.manager.CraftingManagerTatara;
import us.jusybiberman.carpetbag.crafting.recipes.SmeltingRecipe;
import us.jusybiberman.carpetbag.item.ModItems;
import us.jusybiberman.carpetbag.storage.PlayerSideItemStackHandler;

public class ProviderTatara {
	public int furnaceBurnTime;
	public int currentItemBurnTime;
	public int cookTime;
	public int totalCookTime;

	public PlayerSideItemStackHandler inventory = createItemStackHandler();

	private final TileEntityTatara tile;
	private final ICarpetbagPlayer player;

	public ProviderTatara(TileEntityTatara tileEntityTatara, EntityPlayer p) {
		tile = tileEntityTatara;
		player = CPBCapabilityManager.asCarpetbagPlayer(p);
	}

	public PlayerSideItemStackHandler createItemStackHandler() {
		return new PlayerSideItemStackHandler(true, 3);
	}

	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return tile.hasCapability(capability, facing);
	}

	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T) inventory;
		return tile.getCapability(capability, facing);
	}

	public void writeDataToNBT(NBTTagCompound compound) {
		compound.merge(inventory.serializeNBT());

		compound.setInteger("BurnTime", this.furnaceBurnTime);
		compound.setInteger("CookTime", this.cookTime);
		compound.setInteger("CookTimeTotal", this.totalCookTime);
	}

	public void readDataFromNBT(NBTTagCompound compound) {
		inventory = createItemStackHandler();
		inventory.deserializeNBT(compound);

		this.furnaceBurnTime = compound.getInteger("BurnTime");
		this.cookTime = compound.getInteger("CookTime");
		this.totalCookTime = compound.getInteger("CookTimeTotal");
		this.currentItemBurnTime = getItemBurnTime(this.inventory.getStackInSlot(1));

	}



	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	public static int getItemBurnTime(ItemStack stack) {
		if(!stack.isEmpty() && stack.isItemEqual(new ItemStack(ModItems.rice_ash)))
			return 1600;

		return 0;
	}

	public static boolean isItemFuel(ItemStack stack) {
		return getItemBurnTime(stack) > 0;
	}

	public int getCookProgressScaled(int width) {
		return totalCookTime != 0 && cookTime != 0 ? cookTime * width / totalCookTime:0;
	}

	public int getBurnLeftScaled(int height) {
		return currentItemBurnTime != 0 ? furnaceBurnTime * height / currentItemBurnTime : 0;
	}

	public int getCookTime(ItemStack stack) {
		return 1000;
	}

	private boolean canSmelt() {
		ItemStack inputstack = inventory.getStackInSlot(0);
		ItemStack outputstack = inventory.getStackInSlot(2);
		SmeltingRecipe recipe = CraftingManagerTatara.getInstance().getSmeltingRecipe(inputstack);
		if (inputstack.isEmpty() || recipe == null) {
			return false;
		} else if (recipe.getLevelReq() > player.getSkillStorage().getSkill("smithing").getLevel()) {
			return false;
		} else {
			ItemStack itemstack = recipe.getOutput(Lists.newArrayList(inputstack), tile).get(0);
			if (itemstack.isEmpty()) {
				return false;
			} else if (outputstack.isEmpty()) {
				return true;
			} else if (!outputstack.isItemEqual(itemstack)) {
				return false;
			} else {
				int result = outputstack.getCount() + itemstack.getCount();
				return result <= this.getInventoryStackLimit() && result <= outputstack.getMaxStackSize();
			}
		}
	}

	public void smeltItem() {
		ItemStack inputstack = inventory.getStackInSlot(0);
		ItemStack outputstack = inventory.getStackInSlot(2);

		if(this.canSmelt()) {
			SmeltingRecipe recipe = CraftingManagerTatara.getInstance().getSmeltingRecipe(inputstack);
			if(recipe != null) {
				ItemStack itemstack = recipe.getOutput(Lists.newArrayList(inputstack), tile).get(0);
				if (outputstack.isEmpty()) {
					inventory.setStackInSlot(2, itemstack.copy());
				} else if (outputstack.getItem() == itemstack.getItem()) {
					outputstack.grow(itemstack.getCount());
				}

				inputstack.shrink(recipe.getInputCount());
				if (inputstack.getCount() <= 0) {
					inventory.setStackInSlot(0, ItemStack.EMPTY);
				}
			}
		}

	}

	private int getInventoryStackLimit()
	{
		return 64;
	}

	public void update() {
		boolean burning = this.isBurning();
		boolean flag1 = false;
		if(this.isBurning()) {
			--this.furnaceBurnTime;
		}

		if(!this.tile.getWorld().isRemote) {
			if(!tile.isValidStructure())
				return;

			ItemStack inputstack = inventory.getStackInSlot(0);
			ItemStack fuelstack = inventory.getStackInSlot(1);
			if(this.isBurning() || !fuelstack.isEmpty() && !inputstack.isEmpty()) {
				if(!this.isBurning() && this.canSmelt()) {
					this.furnaceBurnTime = getItemBurnTime(fuelstack);
					this.currentItemBurnTime = this.furnaceBurnTime;
					if(this.isBurning()) {
						flag1 = true;
						if(!fuelstack.isEmpty()) {
							fuelstack.shrink(1);
							if(fuelstack.getCount() == 0) {
								inventory.setStackInSlot(1,fuelstack.getItem().getContainerItem(fuelstack));
							}
						}
					}
				}

				if(this.isBurning() && this.canSmelt()) {
					++this.cookTime;
					this.totalCookTime = this.getCookTime(inputstack);
					if(this.cookTime >= this.totalCookTime) {
						this.cookTime = 0;
						this.totalCookTime = this.getCookTime(inputstack);
						this.smeltItem();
						flag1 = true;
					}
				} else {
					this.cookTime = 0;
				}
			} else if(!this.isBurning() && this.cookTime > 0) {
				this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
			}

			if(burning != this.isBurning()) {
				flag1 = true;
			}
		}

		if(flag1) {
			tile.markDirty();
		}
	}
}
