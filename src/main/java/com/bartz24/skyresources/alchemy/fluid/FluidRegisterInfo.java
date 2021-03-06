package com.bartz24.skyresources.alchemy.fluid;

public class FluidRegisterInfo
{
	public enum CrystalFluidType
	{
		NORMAL, MOLTEN
	}

	public String name;
	public int color;
	public int rarity;
	public int crystalIndex;
	public CrystalFluidType type;

	public FluidRegisterInfo(String nameIn, int colorIn, int rarityIn, int index, CrystalFluidType typeIn)
	{
		name = nameIn;
		color = colorIn;
		rarity = rarityIn;
		crystalIndex=index;
		type = typeIn;
	}
}
