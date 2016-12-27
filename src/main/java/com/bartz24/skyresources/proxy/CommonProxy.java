package com.bartz24.skyresources.proxy;

import com.bartz24.skyresources.SkyResources;
import com.bartz24.skyresources.base.HeatSources;
import com.bartz24.skyresources.base.guide.SkyResourcesGuide;
import com.bartz24.skyresources.config.ConfigOptions;
import com.bartz24.skyresources.events.EventHandler;
import com.bartz24.skyresources.events.ModBucketHandler;
import com.bartz24.skyresources.registry.ModBlocks;
import com.bartz24.skyresources.registry.ModCrafting;
import com.bartz24.skyresources.registry.ModEntities;
import com.bartz24.skyresources.registry.ModFluids;
import com.bartz24.skyresources.registry.ModGuiHandler;
import com.bartz24.skyresources.registry.ModGuidePages;
import com.bartz24.skyresources.registry.ModItems;
import com.bartz24.skyresources.world.WorldOverride;
import com.bartz24.skyresources.world.WorldTypeSky;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	EventHandler events = new EventHandler();

	public void preInit(FMLPreInitializationEvent e)
	{
		ConfigOptions.loadConfigThenSave(e);
		ModFluids.init();
		ModBlocks.init();
		ModItems.init();

		//ModPlugins.preInit();

		new HeatSources();
		//new InfusionRecipes();
		new WorldTypeSky();
		new SkyResourcesGuide();

		ModGuidePages.init();

	}

	public void init(FMLInitializationEvent e)
	{
		MinecraftForge.EVENT_BUS.register(events);
		MinecraftForge.EVENT_BUS.register(new ModBucketHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(SkyResources.instance, new ModGuiHandler());
		ModEntities.init();
		ModCrafting.initOreDict();

		//ModPlugins.init();

		WorldOverride.registerWorldProviders();
		//if (Loader.isModLoaded("MineTweaker3"))
			//MinetweakerPlugin.init(e);
	}

	public void postInit(FMLPostInitializationEvent e)
	{
		ModCrafting.init();

		//ModPlugins.postInit();
	}
}
