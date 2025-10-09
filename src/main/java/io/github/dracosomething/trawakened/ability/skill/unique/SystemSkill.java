package io.github.dracosomething.trawakened.ability.skill.unique;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.event.UnlockSkillEvent;
import com.github.manasmods.tensura.ability.ISpatialStorage;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.entity.human.OtherworlderEntity;
import com.github.manasmods.tensura.menu.SpatialStorageMenu;
import com.github.manasmods.tensura.menu.container.SpatialStorageContainer;
import com.github.manasmods.tensura.network.TensuraNetwork;
import com.github.manasmods.tensura.network.play2client.ClientboundSpatialStorageOpenPacket;
import com.github.manasmods.tensura.registry.effects.TensuraMobEffects;
import com.github.manasmods.tensura.registry.entity.TensuraEntityTypes;
import io.github.dracosomething.trawakened.ability.skill.ultimate.ShadowMonarch;
import io.github.dracosomething.trawakened.event.SystemLevelUpEvent;
import io.github.dracosomething.trawakened.registry.items.runeStones;
import io.github.dracosomething.trawakened.registry.skillRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SystemSkill extends Skill implements ISpatialStorage {
    private static final UUID SYSTEM_SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SYSTEM_DAMAGE_UUID = UUID.fromString("b2c3d4e5-f617-8901-bcde-f23456789012");
    private static final UUID SYSTEM_ARMOR_UUID = UUID.fromString("c3d4e5f6-a718-9012-cdef-345678901234");
    
    private CompoundTag tag;

    public SystemSkill() {
        super(SkillType.UNIQUE);
        tag = new CompoundTag();
    }

    public ResourceLocation getSkillIcon() {
        return new ResourceLocation("trawakened", "textures/skill/unique/system.png");
    }

    public @Nullable MutableComponent getName() {
        if (tag.getBoolean("isGui")) {
            return Component.translatable("trawakened.skill.system", new Object[]{tag.getInt("level")});
        } else {
            return Component.translatable("trawakened.skill.system_normal");
        }
    }

    public int getMaxMastery() {
        return 140;
    }

    private double getBonusMultiplier(LivingEntity user, boolean mastered) {
        if (!mastered) return 1.0;
        
        boolean hasShadowMonarch = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).isPresent();
        
        if (!hasShadowMonarch) {
            return 1.2;
        }
        
        ManasSkillInstance shadowInstance = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).get();
        boolean hasShadowMonarchMastery = shadowInstance.getMastery() >= 1.0;
        
        boolean isShadowMonarchAwakened = false;
        if (shadowInstance.getSkill() instanceof ShadowMonarch skill) {
            isShadowMonarchAwakened = skill.getData().getBoolean("awakened");
        }
        
        if (isShadowMonarchAwakened) {
            return 3.0;
        } else if (hasShadowMonarchMastery) {
            return 2.0;
        } else {
            return 1.5;
        }
    }

    private double getSystemSpeedBonus(int level, boolean mastered, LivingEntity user) {
        double baseBonus = level * 0.000285714; // Reduced multiplier for 0.04 cap
        double multiplier = getBonusMultiplier(user, mastered);
        double finalBonus = baseBonus * multiplier;
        return Math.min(finalBonus, 0.04); // Reduced speed cap
    }

    private double getSystemAttackBonus(int level, boolean mastered, LivingEntity user) {
        double baseBonus = level * 0.5;
        double multiplier = getBonusMultiplier(user, mastered);
        return baseBonus * multiplier;
    }

    private double getSystemArmorBonus(int level, boolean mastered, LivingEntity user) {
        if (level < 30) return 0.0;
        double baseBonus = ((level - 30) / 30.0 + 1) * 12.5;
        double multiplier = getBonusMultiplier(user, mastered);
        return baseBonus * multiplier;
    }

    private void applySystemModifiers(LivingEntity entity, int level, boolean mastered) {
        AttributeInstance speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
        
        if (speedAttr != null) {
            speedAttr.removeModifier(SYSTEM_SPEED_UUID);
            double speedBonus = getSystemSpeedBonus(level, mastered, entity);
            if (speedBonus > 0) {
                AttributeModifier speedModifier = new AttributeModifier(
                    SYSTEM_SPEED_UUID, 
                    "System Movement Speed", 
                    speedBonus, 
                    AttributeModifier.Operation.ADDITION
                );
                speedAttr.addPermanentModifier(speedModifier);
            }
        }
        
        if (damageAttr != null) {
            damageAttr.removeModifier(SYSTEM_DAMAGE_UUID);
            double damageBonus = getSystemAttackBonus(level, mastered, entity);
            if (damageBonus > 0) {
                AttributeModifier damageModifier = new AttributeModifier(
                    SYSTEM_DAMAGE_UUID, 
                    "System Attack Damage", 
                    damageBonus, 
                    AttributeModifier.Operation.ADDITION
                );
                damageAttr.addPermanentModifier(damageModifier);
            }
        }
        
        // Apply armor modifier (following Tensura pattern)
        if (armorAttr != null) {
            armorAttr.removeModifier(SYSTEM_ARMOR_UUID);
            double armorBonus = getSystemArmorBonus(level, mastered, entity);
            if (armorBonus > 0) {
                AttributeModifier armorModifier = new AttributeModifier(
                    SYSTEM_ARMOR_UUID,
                    "System Armor",
                    armorBonus,
                    AttributeModifier.Operation.ADDITION
                );
                armorAttr.addPermanentModifier(armorModifier);
            }
        }
    }

    public List<MobEffect> getImmuneEffects(ManasSkillInstance instance, LivingEntity entity) {
        return List.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS, MobEffects.BLINDNESS, MobEffects.DARKNESS, MobEffects.POISON, MobEffects.WITHER, MobEffects.HUNGER, TensuraMobEffects.PARALYSIS.get(), TensuraMobEffects.INFECTION.get());
    }

    public int modes() {
        return 1;
    }

    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        openSpatialStorage(entity, instance);
    }

    public SpatialStorageContainer getSpatialStorage(ManasSkillInstance instance) {
        SpatialStorageContainer container = new SpatialStorageContainer(90, 999);
        container.fromTag(instance.getOrCreateTag().getList("SpatialStorage", 10));
        return container;
    }

    public void openSpatialStorage(LivingEntity entity, ManasSkillInstance instance) {
        if (entity instanceof ServerPlayer player) {
            player.closeContainer();
            player.nextContainerCounter();
            player.playNotifySound(SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 1.0F, 1.0F);
            ManasSkill skill = instance.getSkill();
            SpatialStorageContainer container = this.getSpatialStorage(instance);
            tag.putBoolean("isGui", true);
            instance.getOrCreateTag().put("data", this.tag);
            TensuraNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> {
                return player;
            }), new ClientboundSpatialStorageOpenPacket(player.getId(), player.containerCounter, container.getContainerSize(), container.getMaxStackSize(), SkillUtils.getSkillId(skill)));
            player.containerMenu = new SpatialStorageMenu(player.containerCounter, player.getInventory(), player, container, skill);
            player.initMenu(player.containerMenu);
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
        }
    }

    public void onLearnSkill(ManasSkillInstance instance, LivingEntity living, UnlockSkillEvent event) {
        CompoundTag tag = instance.getOrCreateTag();
        tag.putInt("level", 1);
        tag.putInt("nextLevel", 1150);
        tag.putInt("maxLevel", 140);
        tag.putInt("accumulatedEP", 0);
        tag.putBoolean("isGui", false);
        this.tag.putBoolean("isGui", tag.getBoolean("isGui"));
        this.tag.putInt("level", tag.getInt("level"));
        tag.put("data", this.tag);
    }

    public void onLevelUp(ManasSkillInstance instance, LivingEntity entity, SystemLevelUpEvent event) {
        int level = instance.getOrCreateTag().getInt("level");
        boolean mastered = instance.isMastered(entity);
        
        // Apply speed, damage and armor modifiers using AttributeModifiers
        applySystemModifiers(entity, level, mastered);
        
        instance.addMasteryPoint(entity);
    }

    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return true;
    }

    public void onTick(ManasSkillInstance instance, LivingEntity living) {
        int level = instance.getOrCreateTag().getInt("level");
        boolean mastered = instance.isMastered(living);
        
        // Apply speed, damage and armor modifiers using AttributeModifiers
        applySystemModifiers(living, level, mastered);
    }

    public void onTakenDamage(ManasSkillInstance instance, LivingDamageEvent event) {
        if (isInSlot(event.getEntity())) {
            int level = instance.getOrCreateTag().getInt("level");
            if (level >= 30) {
                if (event.getEntity().getHealth() < event.getEntity().getMaxHealth() * 0.3) {
                    event.setAmount(event.getAmount() / 2);
                }
            }
        }
    }

    public void onDamageEntity(ManasSkillInstance instance, LivingEntity entity, LivingHurtEvent event) {
        int level = instance.getOrCreateTag().getInt("level");
        if (level >= 20) {
            Random random = new Random();
            float dmg = event.getEntity().getHealth() - event.getAmount();
            if (dmg <= 0.0) {
                if (event.getEntity().getType().equals(EntityType.CREEPER) || event.getEntity().getType().equals(EntityType.WANDERING_TRADER)) {
                    if (random.nextInt(0, 100) <= (event.getEntity().getType().equals(EntityType.CREEPER) ? 3 : 33)) {
                        newDrop(runeStones.STEALTH_STONE.get(), event, entity);
                    }
                }
                if (event.getEntity().getType().getCategory().equals(MobCategory.MONSTER)) {
                    if (random.nextDouble(0, 100) <= 0.5) {
                        newDrop(runeStones.BLOODLUST_STONE.get(), event, entity);
                    }
                }
                if (event.getEntity().getType().equals(EntityType.SILVERFISH) ||
                        event.getEntity().getType().equals(EntityType.CAT) ||
                        event.getEntity().getType().equals(TensuraEntityTypes.WINGED_CAT.get()) ||
                        event.getEntity().getType().equals(TensuraEntityTypes.EVIL_CENTIPEDE.get()) ||
                        event.getEntity().getType().equals(TensuraEntityTypes.EVIL_CENTIPEDE_BODY.get())) {
                    if (random.nextInt(0, 100) <= 3) {
                        newDrop(runeStones.QUICKSILVER_STONE.get(), event, entity);
                    }
                }
                if (event.getEntity() instanceof OtherworlderEntity || event.getEntity().getType().equals(EntityType.EVOKER) || event.getEntity().getType().equals(EntityType.VINDICATOR)) {
                    if (random.nextInt(0, 100) <= 20) {
                        newDrop(runeStones.MUTILATION_STONE.get(), event, entity);
                    }
                }
                if (event.getEntity() instanceof OtherworlderEntity || event.getEntity().getType().equals(EntityType.VILLAGER) || event.getEntity().getMobType().equals(MobType.ILLAGER)) {
                    if (event.getEntity().getType().equals(TensuraEntityTypes.HINATA_SAKAGUCHI.get())) {
                        newDrop(runeStones.RULERS_AUTHORITY_STONE.get(), event, entity);
                    } else {
                        if (random.nextInt(0, 100) <= (event.getEntity() instanceof OtherworlderEntity ? 20 : 3)) {
                            newDrop(runeStones.RULERS_AUTHORITY_STONE.get(), event, entity);
                        }
                    }
                }
                if (event.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
                    if (random.nextInt(0, 100) <= 25) {
                        newDrop(runeStones.DRAGONS_FEAR_STONE.get(), event, entity);
                    }
                }
            }
        }
    }

    private void newDrop(Item drop, LivingHurtEvent event, LivingEntity source) {
        Vec3 pos = event.getEntity().position();
        ItemEntity itemDrop = new ItemEntity(event.getEntity().level, pos.x, pos.y, pos.z, drop.getDefaultInstance());
        source.level.addFreshEntity(itemDrop);
    }

    public CompoundTag getTag() {
        return tag;
    }

    public int getLevel() {
        return this.tag.getInt("level");
    }
}
