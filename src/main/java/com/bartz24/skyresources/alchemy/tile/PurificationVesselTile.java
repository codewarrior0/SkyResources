package com.bartz24.skyresources.alchemy.tile;

import com.bartz24.skyresources.base.HeatSources;
import com.bartz24.skyresources.registry.ModFluids;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class PurificationVesselTile extends TileEntity implements ITickable, IFluidHandler
{
	FluidTank lowerTank;
	FluidTank upperTank;

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return new IFluidTankProperties[]
		{ new FluidTankPropertiesWrapper(lowerTank), new FluidTankPropertiesWrapper(upperTank) };
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		return lowerTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		return upperTank.drain(resource, doDrain);
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return upperTank.drain(maxDrain, doDrain);
	}

	public FluidTank getLowerTank()
	{
		return lowerTank;
	}

	public FluidTank getUpperTank()
	{
		return upperTank;
	}

	public PurificationVesselTile()
	{
		lowerTank = new FluidTank(3000);
		upperTank = new FluidTank(1000);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		super.onDataPacket(net, packet);
		this.readFromNBT(packet.getNbtCompound());
	}

	int getHeatSourceVal()
	{
		if (HeatSources.isValidHeatSource(pos.down(), worldObj))
		{
			if (HeatSources.getHeatSourceValue(pos.down(), worldObj) > 0)
				return Math.max(HeatSources.getHeatSourceValue(pos.down(), worldObj) / 5, 1);
		}
		return 0;
	}

	@Override
	public void update()
	{
		if (!worldObj.isRemote)
		{
			if (lowerTank.getFluid() != null && lowerTank.getFluid().getFluid() != null)
			{
				int type = -1;
				if (ModFluids.dirtyCrystalFluids.contains(lowerTank.getFluid().getFluid()))
					type = ModFluids.dirtyCrystalFluids.indexOf(lowerTank.getFluid().getFluid());

				if (type >= 0)
				{
					if (HeatSources.isValidHeatSource(pos.down(), worldObj))
					{
						int rate = Math.min(getHeatSourceVal(), lowerTank.getFluidAmount());

						int transferAmount = upperTank.fill(
								new FluidStack(ModFluids.crystalFluids.get(type), rate), true);
						lowerTank.drain(transferAmount, true);
					}
				}
			}
		}

		markDirty();
		worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(getPos()),
				worldObj.getBlockState(getPos()), 3);

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);

		NBTTagList list = new NBTTagList();
		for (int i = 0; i < 2; i++)
		{
			NBTTagCompound stackTag = new NBTTagCompound();
			stackTag.setInteger("Slot", i);
			if (i == 0)
				lowerTank.writeToNBT(stackTag);
			else if (i == 1)
				upperTank.writeToNBT(stackTag);
			list.appendTag(stackTag);
		}
		compound.setTag("Tanks", list);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		NBTTagList list = compound.getTagList("Tanks", 10);
		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);
			int slot = stackTag.getInteger("Slot");

			if (slot == 0)
				lowerTank.readFromNBT(stackTag);
			else if (slot == 1)
				upperTank.readFromNBT(stackTag);
		}
	}

	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState,
			IBlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}
}
