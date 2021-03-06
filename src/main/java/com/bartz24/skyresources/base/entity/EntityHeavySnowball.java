package com.bartz24.skyresources.base.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityHeavySnowball extends EntityThrowable
{
    public EntityHeavySnowball(World worldIn)
    {
        super(worldIn);
    }

    public EntityHeavySnowball(World worldIn, EntityLivingBase throwerIn)
    {
        super(worldIn, throwerIn);
        this.moveRelative(1, .5f, 1);
    }

    public EntityHeavySnowball(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id)
    {
        if (id == 3)
        {
            for (int i = 0; i < 8; ++i)
            {
                this.world.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
            }
        }
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(RayTraceResult result)
    {
        if (result.entityHit != null)
        {
            int i = 8;

            if (result.entityHit instanceof EntityBlaze)
            {
                i = 14;
            }

            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)i);
        }

        if (!this.world.isRemote)
        {
            this.setDead();
        }
    }

}
