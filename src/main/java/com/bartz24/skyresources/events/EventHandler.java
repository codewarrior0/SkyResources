package com.bartz24.skyresources.events;

import java.util.List;

import com.bartz24.skyresources.RandomHelper;
import com.bartz24.skyresources.References;
import com.bartz24.skyresources.SkyResources;
import com.bartz24.skyresources.alchemy.effects.IHealthBoostItem;
import com.bartz24.skyresources.alchemy.item.AlchemyItemComponent;
import com.bartz24.skyresources.base.ModKeyBindings;
import com.bartz24.skyresources.config.ConfigOptions;
import com.bartz24.skyresources.recipe.ProcessRecipe;
import com.bartz24.skyresources.recipe.ProcessRecipeManager;
import com.bartz24.skyresources.registry.ModGuiHandler;
import com.bartz24.skyresources.registry.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class EventHandler
{
	@SubscribeEvent
	public void onPlayerRightClick(RightClickBlock event)
	{
		if (!event.getWorld().isRemote && event.getHand() == EnumHand.MAIN_HAND)
		{
			ItemStack equip = event.getEntityPlayer().getHeldItem(event.getHand());
			if (event.getPos() == null)
				return;
			Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
			if (block != null && equip.isEmpty() && event.getEntityPlayer().isSneaking())
			{
				if (block == Blocks.CACTUS)
				{
					RandomHelper.spawnItemInWorld(event.getWorld(), new ItemStack(ModItems.alchemyComponent, 1,
							((AlchemyItemComponent) ModItems.alchemyComponent).getNames().indexOf("cactusNeedle")),
							event.getEntityPlayer().getPosition());
					event.getEntityPlayer().attackEntityFrom(DamageSource.CACTUS, 5);
				} else if (block == Blocks.SNOW_LAYER)
				{
					RandomHelper.spawnItemInWorld(event.getWorld(), new ItemStack(Items.SNOWBALL), event.getPos());
					event.getEntityPlayer().addPotionEffect(
							new PotionEffect(MobEffects.MINING_FATIGUE, event.getWorld().rand.nextInt(80) + 20, 1));

					int meta = Blocks.SNOW_LAYER.getMetaFromState(event.getWorld().getBlockState(event.getPos()));

					if (meta < 1)
						event.getWorld().setBlockToAir(event.getPos());
					else
						event.getWorld().setBlockState(event.getPos(), Blocks.SNOW_LAYER.getStateFromMeta(meta - 1));
				} else if (block == Blocks.SNOW)
				{
					RandomHelper.spawnItemInWorld(event.getWorld(), new ItemStack(Items.SNOWBALL), event.getPos());
					event.getEntityPlayer().addPotionEffect(
							new PotionEffect(MobEffects.MINING_FATIGUE, event.getWorld().rand.nextInt(80) + 20, 1));

					event.getWorld().setBlockState(event.getPos(), Blocks.SNOW_LAYER.getStateFromMeta(6));
				}
			} else if (block != null && !equip.isEmpty())
			{
				if (block == Blocks.CAULDRON)
				{

					int i = ((Integer) event.getWorld().getBlockState(event.getPos()).getValue(BlockCauldron.LEVEL))
							.intValue();
					ItemStack item = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);

					if (i > 0)
					{
						List<ProcessRecipe> recipes = ProcessRecipeManager.cauldronCleanRecipes.getRecipes();
						if (recipes.size() > 0)
						{
							boolean worked = false;
							for (ProcessRecipe rec : recipes)
							{
								if (((ItemStack) rec.getInputs().get(0)).isItemEqual(item))
									if (event.getWorld().rand.nextFloat() <= rec.getIntParameter())
									{
										worked = true;
										RandomHelper.spawnItemInWorld(event.getWorld(), rec.getOutputs().get(0),
												event.getPos());
									}
							}
							if (worked)
							{
								if (event.getWorld().rand.nextFloat() < 0.4F)
									new BlockCauldron().setWaterLevel(event.getWorld(), event.getPos(),
											event.getWorld().getBlockState(event.getPos()), i - 1);
								item.shrink(1);
								if (item.getCount() == 0)
									event.getEntityPlayer().setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
							}
						}
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void onPlayerTickEvent(PlayerTickEvent event)
	{
		if (!event.player.world.isRemote)
		{
			EntityPlayer player = event.player;

			int healthToAdd = 0;

			for (int i = 0; i <= player.inventory.mainInventory.size(); i++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);

				if (stack.isEmpty())
					continue;

				if (stack.getItem() instanceof IHealthBoostItem)
				{
					healthToAdd += ((IHealthBoostItem) stack.getItem()).getHealthBoost(stack);
				}

				if (stack.isItemEqual(new ItemStack(ModItems.healthGem, 1)))
				{
					if (stack.getTagCompound().getInteger("cooldown") > 0)
					{
						stack.getTagCompound().setInteger("cooldown",
								stack.getTagCompound().getInteger("cooldown") - 1);
					}
				}
			}

			player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20 + healthToAdd);
			if (player.getHealth() > player.getMaxHealth())
				player.setHealth(player.getMaxHealth());
		}

	}

	@SubscribeEvent
	public void onPlayerJoinEvent(PlayerLoggedInEvent event)
	{
		EntityPlayer player = event.player;

		if (ConfigOptions.displayFirstChatInfo && ConfigOptions.allowGuide)
		{
			TextComponentString text = new TextComponentString(
					"Need help or a guide? \nPress your " + TextFormatting.AQUA + "Open Guide Key (Default: G)"
							+ TextFormatting.WHITE + " to open the Sky Resources in-game guide!");
			player.sendMessage(text);
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event)
	{
		if (ModKeyBindings.guideKey.isPressed() && ConfigOptions.allowGuide)
		{
			EntityPlayerSP player = Minecraft.getMinecraft().player;

			if (player.world.isRemote)
			{
				player.openGui(SkyResources.instance, ModGuiHandler.GuideGUI, player.world, player.getPosition().getX(),
						player.getPosition().getY(), player.getPosition().getZ());
			}
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equalsIgnoreCase(References.ModID))
		{
			ConfigOptions.reloadConfigs();
		}
	}
}
