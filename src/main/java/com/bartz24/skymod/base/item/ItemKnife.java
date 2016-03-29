package com.bartz24.skymod.base.item;

import com.bartz24.skymod.References;
import com.bartz24.skymod.registry.ModCreativeTabs;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemKnife extends Item
{
    private float damageVsEntity;
	public ItemKnife(Item.ToolMaterial material, String unlocalizedName, String registryName)
	{
        this.setMaxDamage((int)(material.getMaxUses() * 0.8F));
        this.damageVsEntity = 1.5F + material.getDamageVsEntity();
        this.setUnlocalizedName(References.ModID + "." + unlocalizedName);
        setRegistryName(registryName);
        this.setMaxStackSize(1);
        this.setNoRepair();
		this.setCreativeTab(ModCreativeTabs.tabMain);
	}

	public float getStrVsBlock(ItemStack stack, IBlockState state)
	{
        Block block = state.getBlock();
		Material material = block.getMaterial(state);
		return material != Material.plants && material != Material.vine
				&& material != Material.coral && material != Material.leaves
				&& material != Material.gourd ? 1.0F : 3F;
	}
	
	public boolean canHarvestBlock(Block blockIn)
    {
        return false;
    }
	
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot)
    {
		
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
        
        if(equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
        multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.damageVsEntity, 0));
        multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -1.4000000953674316D, 0));
        }

        return multimap;
    }
}
