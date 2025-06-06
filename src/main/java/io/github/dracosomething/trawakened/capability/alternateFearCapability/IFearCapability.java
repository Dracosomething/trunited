package io.github.dracosomething.trawakened.capability.alternateFearCapability;

import io.github.dracosomething.trawakened.library.FearTypes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface IFearCapability extends INBTSerializable<CompoundTag> {
    /**
     * returns the fear of an entity
     * @param entity
     * @return the fear of entity
     */
    FearTypes getFear(LivingEntity entity);

    /**
     * sets the fear of an entity
     * @param entity
     * @param types
     */
    void setFear(LivingEntity entity, FearTypes types);

    /**
     * return the times an entity has been scared
     * @param entity
     * @return scared amount of entity
     */
    int getScaredAmount(LivingEntity entity);

    /**
     * sets the amount an entity has been scared
     * @param amount
     * @param entity
     */
    void setScaredAmount(int amount, LivingEntity entity);

    /**
     * returns the fear cooldown of an entity
     * @param entity
     * @return cooldown of an entities fear
     */
    int getCooldown(LivingEntity entity);

    /**
     * sets the cooldown of an entities fear
     * @param entity
     * @param amount
     */
    void setCooldown(LivingEntity entity, int amount);

    /**
     * @param entity
     * @return returns the isAlternate boolean
     */
    boolean getIsAlternate(LivingEntity entity);

    /**
     * sets the is alternate boolean
     * @param entity
     * @param isAlternate
     */
    void setIsAlternate(LivingEntity entity, boolean isAlternate);

    boolean getIsSlim(LivingEntity entity);

    void setIsSlim(LivingEntity entity, boolean isSlim);
}
