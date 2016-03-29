package com.bartz24.skymod.alchemy.item;

import java.util.ArrayList;
import java.util.List;

import com.bartz24.skymod.References;
import com.bartz24.skymod.alchemy.infusion.InfusionRecipe;
import com.bartz24.skymod.alchemy.infusion.InfusionRecipes;
import com.bartz24.skymod.registry.ModCreativeTabs;
import com.bartz24.skymod.registry.ModItems;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemInfusionStone extends Item
{
	public ItemInfusionStone(int durability, String unlocalizedName, String registryName)
	{
        this.setMaxDamage(durability);
        this.setUnlocalizedName(References.ModID + "." + unlocalizedName);
        setRegistryName(registryName);
        this.setMaxStackSize(1);
        this.setNoRepair();
		this.setCreativeTab(ModCreativeTabs.tabAlchemy);
	}
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world,
			BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
        
        Block block = world.getBlockState(pos).getBlock();
        
        ItemStack offHand = player.getHeldItemOffhand();

		InfusionRecipe recipe = InfusionRecipes.getRecipe(offHand, block,
				block.getMetaFromState(world.getBlockState(pos)),
				world);

		if (recipe != null && recipe.getOutput() != null)
		{
			if (player.getMaxHealth() < recipe.getHealthReq())
			{
				if (world.isRemote)
					player
							.addChatMessage(new TextComponentString(
									"You are not strong enough to infuse. Your max health is too low."));
			}
			if (player.getHealth() >= recipe.getHealthReq())
			{
				if (!world.isRemote)
				{
					player.attackEntityFrom(DamageSource.magic,
							recipe.getHealthReq());
					world.setBlockToAir(pos);
					player.dropPlayerItemWithRandomChoice(recipe
							.getOutput().copy(), false);
					if (offHand != null)
						offHand.stackSize -= recipe.getInputStack().stackSize;
					
					stack.damageItem(1, player);					
				}
			} else
			{
				if (world.isRemote)
					player.addChatMessage(new TextComponentString(
							"Not enough health to infuse."));
			}
		}

		if(world.isRemote)
			player.swingArm(EnumHand.MAIN_HAND);
		
        return EnumActionResult.PASS;
    }
}
