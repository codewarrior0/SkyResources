package com.bartz24.skymod.alchemy.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.bartz24.skymod.References;
import com.bartz24.skymod.registry.ModCreativeTabs;
import com.bartz24.skymod.registry.ModItems;

public class AlchemyItemComponent extends Item
{
    private static ArrayList<String> names = new ArrayList<String>();

    public static final String cactusNeedle = "cactusNeedle";
    public static final String cactusNeedleBloody = "cactusNeedleBloody";

    public AlchemyItemComponent()
    {
        super();
        
        setUnlocalizedName(References.ModID + ".alchemyItemComponent.");
        setRegistryName("AlchemyItemComponent");
        setHasSubtypes(true);
		this.setCreativeTab(ModCreativeTabs.tabAlchemy);

        itemList();
    }

    private void itemList()
    {
        names.add(0, cactusNeedle);
        names.add(1, cactusNeedleBloody);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName(stack) + names.get(stack.getItemDamage());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item id, CreativeTabs creativeTab, List<ItemStack> list)
    {
        for (int i = 0; i < names.size(); i++)
            list.add(new ItemStack(id, 1, i));
    }

    public static ItemStack getStack(String name)
    {
        return new ItemStack(ModItems.alchemyComponent, 1, names.indexOf(name));
    }

    public static ArrayList<String> getNames()
    {
    	return names;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        super.onItemRightClick(stack, world, player, hand);
        
        if(player.isSneaking())
        {
            if(stack.getMetadata() == names.indexOf(cactusNeedle))
            {
            	stack.stackSize--;
            	
            	if (!player.inventory.addItemStackToInventory(new ItemStack(ModItems.alchemyComponent, 1, names.indexOf(cactusNeedleBloody))))
                {
                    player.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.alchemyComponent, 1, names.indexOf(cactusNeedleBloody)), false);
                }
            	
            	player.attackEntityFrom(DamageSource.cactus, 2);
            }
        }

        return new ActionResult(EnumActionResult.SUCCESS, stack);
    }
}
