package com.bartz24.skymod.base.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.bartz24.skymod.References;
import com.bartz24.skymod.SkyMod;
import com.bartz24.skymod.registry.ModBlocks;
import com.bartz24.skymod.registry.ModCreativeTabs;

public class ItemWaterExtractor extends Item implements IFluidHandler
{
	int maxAmount = 1000;

	FluidTank tank;

	public static final String[] extractorIcons = new String[]
	{ "empty", "full1", "full2", "full3", "full4", "full5", "full6" };

	public ItemWaterExtractor()
	{
		this.maxStackSize = 1;
		this.setCreativeTab(ModCreativeTabs.tabMain);
		tank = new FluidTank(new FluidStack(FluidRegistry.WATER, 0), maxAmount);
		setUnlocalizedName(References.ModID + ".waterExtractor");
		setRegistryName("WaterExtractor");
	}

	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BOW;
	}

	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 70000;
	}

	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityPlayer playerIn)
	{
		return stack;
	}

	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world,
			EntityPlayer player, EnumHand hand)
	{

		player.setActiveHand(hand);
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity,
			int timeLeft)
	{
		SkyMod.logger.debug("HERE");
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entity;
			if (!world.isRemote && timeLeft <= getMaxItemUseDuration(stack) - 50)
			{
				if ((player.rayTrace(200, 1.0F) != null))
				{
					BlockPos pos = player.rayTrace(200, 1.0F).getBlockPos();

					EnumFacing blockHitSide = player.rayTrace(200, 1.0F).sideHit;

					Block block = world.getBlockState(pos).getBlock();
					if (block == Blocks.cactus)
					{
						getCompound(stack)
								.setInteger(
										"amount",
										stack.getTagCompound().getInteger("amount")
												+ fill(null,
														new FluidStack(FluidRegistry.WATER, 50),
														true));
						tank.getFluid().amount = stack.getTagCompound().getInteger("amount");
						world.setBlockState(pos, ModBlocks.dryCactus.getDefaultState());
						world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
								SoundEvents.entity_player_splash, SoundCategory.NEUTRAL, 1.0F,
								1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
						return;
					}
					else if(world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.water && getCompound(stack).getInteger("amount") < 1000)
					{
						world.setBlockToAir(pos.add(0, 1, 0));
						getCompound(stack).setInteger("amount", 1000);		
						world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
								SoundEvents.entity_player_splash, SoundCategory.NEUTRAL, 1.0F,
								1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));				
					}
								
				}
			}
		}
	}

	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn,
			BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();
		

		if (worldIn.getTileEntity(pos) instanceof IFluidHandler)
		{
			IFluidHandler tile = (IFluidHandler) worldIn.getTileEntity(pos);
			getCompound(stack).setInteger(
					"amount",
					stack.getTagCompound().getInteger("amount")
							- tile.fill(side, tank.getFluid(), true));
			tank.getFluid().amount = stack.getTagCompound().getInteger("amount");
			return EnumActionResult.FAIL;
		}
		
		if (getCompound(stack).getInteger("amount") >= 1000)
		{
			if (block == Blocks.snow_layer
					&& ((Integer) iblockstate.getValue(BlockSnow.LAYERS)).intValue() < 1)
			{
				side = EnumFacing.UP;
			} else if (!block.isReplaceable(worldIn, pos))
			{
				pos = pos.offset(side);
			}

			if (!playerIn.canPlayerEdit(pos, side, stack) || stack.stackSize == 0)
			{
				return EnumActionResult.FAIL;
			} else
			{
				if (worldIn.canBlockBePlaced(Blocks.water, pos, false, side, (Entity) null, stack))
				{
					IBlockState iblockstate1 = Blocks.water.onBlockPlaced(worldIn, pos, side, hitX,
							hitY, hitZ, 0, playerIn);

					worldIn.setBlockState(pos, iblockstate1, 3);
					getCompound(stack).setInteger("amount",
							stack.getTagCompound().getInteger("amount") - 1000);
					tank.getFluid().amount = stack.getTagCompound().getInteger("amount");
					worldIn.playSound((EntityPlayer) null, playerIn.posX, playerIn.posY, playerIn.posZ,
							SoundEvents.entity_player_splash, SoundCategory.NEUTRAL, 1.0F,
							1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));
					return EnumActionResult.SUCCESS;
				}

				return EnumActionResult.FAIL;
			}
		}
		return EnumActionResult.FAIL;
	}

	public NBTTagCompound getCompound(ItemStack stack)
	{
		NBTTagCompound com = stack.getTagCompound();
		if (com == null)
			onCreated(stack, null, null);
		com = stack.getTagCompound();

		return com;
	}

	public void onCreated(ItemStack itemStack, World world, EntityPlayer player)
	{
		itemStack.setTagCompound(new NBTTagCompound());
		itemStack.getTagCompound().setInteger("amount", 0);
		tank.getFluid().amount = itemStack.getTagCompound().getInteger("amount");

	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{

		if (resource != null && canFill(from, resource.getFluid()))
		{
			int filled = tank.fill(resource, doFill);

			return filled;
		}

		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if (resource != null && canDrain(from, resource.getFluid()))
		{
			return tank.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if (canDrain(from, null))
		{
			return tank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return tank.getFluid() == null || tank.getFluid().getFluid() == fluid;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		if (tank != null)
		{
			if (fluid == null || tank.getFluid() != null && tank.getFluid().getFluid() == fluid)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return new FluidTankInfo[]
		{ tank.getInfo() };
	}

	public FluidTank getTank()
	{
		return tank;
	}

	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4)
	{
		if (itemStack.getTagCompound() != null)
		{
			list.add("Water: " + itemStack.getTagCompound().getInteger("amount"));
		} else
			list.add("Water: " + 0);
	}

	public int getMaxAmount()
	{
		return maxAmount;
	}
}
