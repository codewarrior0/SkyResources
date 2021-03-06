package com.bartz24.skyresources.technology.item;

import java.util.List;

import com.bartz24.skyresources.ItemHelper;
import com.bartz24.skyresources.RandomHelper;
import com.bartz24.skyresources.References;
import com.bartz24.skyresources.config.ConfigOptions;
import com.bartz24.skyresources.recipe.ProcessRecipe;
import com.bartz24.skyresources.recipe.ProcessRecipeManager;
import com.bartz24.skyresources.registry.ModCreativeTabs;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemRockGrinder extends ItemPickaxe
{
	private float damageVsEntity;

	ToolMaterial toolMaterial;

	public ItemRockGrinder(ToolMaterial material, String unlocalizedName, String registryName)
	{
		super(material);
		toolMaterial = material;
		this.setMaxDamage((int) (material.getMaxUses() * ConfigOptions.rockGrinderBaseDurability));
		this.damageVsEntity = ConfigOptions.rockGrinderBaseDamage + material.getDamageVsEntity();
		this.setUnlocalizedName(References.ModID + "." + unlocalizedName);
		setRegistryName(registryName);
		this.setMaxStackSize(1);
		this.setCreativeTab(ModCreativeTabs.tabTech);
		this.setHarvestLevel("rockGrinder", material.getHarvestLevel());

		ItemHelper.addRockGrinder(this);
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state)
	{
		Block block = state.getBlock();

		List<ProcessRecipe> recipes = ProcessRecipeManager.rockGrinderRecipes.getRecipes();
		boolean worked = false;
		for (ProcessRecipe r : recipes)

		{
			ItemStack stackIn = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
			ItemStack recIn = (ItemStack) r.getInputs().get(0);

			if (r != null
					&& ((recIn.getMetadata() == OreDictionary.WILDCARD_VALUE && stackIn.getItem() == recIn.getItem())
							|| (stackIn.isItemEqual(recIn) && !r.getOutputs().get(0).isEmpty())))
			{
				if (toolMaterial.getHarvestLevel() < block.getHarvestLevel(state))
					return 0.5F;
				else
				{
					return toolMaterial.getEfficiencyOnProperMaterial();
				}
			}
		}
		return 0.5F;

	}

	@Override
	public boolean onBlockStartBreak(ItemStack item, BlockPos pos, EntityPlayer player)
	{
		World world = player.world;
		IBlockState state = world.getBlockState(pos);
		if (item.attemptDamageItem(1, this.itemRand))
		{
			player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
		}

		List<ProcessRecipe> recipes = ProcessRecipeManager.rockGrinderRecipes.getRecipes();
		boolean worked = false;
		for (ProcessRecipe r : recipes)

		{
			ItemStack stackIn = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
			ItemStack recIn = (ItemStack) r.getInputs().get(0);

			if (r != null
					&& ((recIn.getMetadata() == OreDictionary.WILDCARD_VALUE && stackIn.getItem() == recIn.getItem())
							|| (stackIn.isItemEqual(recIn) && !r.getOutputs().get(0).isEmpty())))
			{
				worked = true;
				if (!world.isRemote)
				{
					int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, item);
					float chance = r.getIntParameter() * (((float) level + 3F) / 3F);
					while (chance >= 1)
					{
						RandomHelper.spawnItemInWorld(world, r.getOutputs().get(0).copy(), pos);
						chance -= 1;
					}
					if (itemRand.nextFloat() <= chance)
						RandomHelper.spawnItemInWorld(world, r.getOutputs().get(0).copy(), pos);
				}
			}
		}
		if (!world.isRemote)
			world.destroyBlock(pos, !worked);
		return worked;

	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot)
	{

		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

		if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
		{
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
					new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.damageVsEntity, 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
					new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
		}

		return multimap;
	}
}
