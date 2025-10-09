package io.github.dracosomething.trawakened.handler;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.tensura.event.SkillPlunderEvent;
import com.github.manasmods.tensura.event.UpdateEPEvent;
import com.github.manasmods.tensura.menu.SpatialStorageMenu;
import io.github.dracosomething.trawakened.ability.skill.unique.SystemSkill;
import io.github.dracosomething.trawakened.ability.skill.ultimate.ShadowMonarch;
import io.github.dracosomething.trawakened.event.SystemLevelUpEvent;
import io.github.dracosomething.trawakened.registry.skillRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "trawakened")
public class SystemHandler {
    private static boolean isThunder;
    private static boolean isNight;

    @SubscribeEvent
    public static void CantPlunder(SkillPlunderEvent event) {
        if (event.getSkill().equals(skillRegistry.SYSTEM.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void Regen(TickEvent.PlayerTickEvent event) {
        SkillStorage storage = SkillAPI.getSkillsFrom(event.player);
        if (storage.getSkill(skillRegistry.SYSTEM.get()).isPresent()) {
            ManasSkillInstance skill = storage.getSkill(skillRegistry.SYSTEM.get()).get();
            skill.getOrCreateTag().putInt("seconds", skill.getOrCreateTag().getInt("seconds")+1);
            if (skill.getOrCreateTag().getInt("seconds") == 60) {
                event.player.setHealth(event.player.getHealth()+1);
            }
        }
    }

    @SubscribeEvent
    public static void Recovery(PlayerSleepInBedEvent event) {
        isThunder = event.getEntity().level.isThundering();
        isNight = event.getEntity().level.isNight();
    }

    @SubscribeEvent
    public static void WakeUpHeal(PlayerWakeUpEvent event) {
        if (!isThunder || isNight) {
            float p40 = (float) (event.getEntity().getHealth()*0.40);
            float p35 = (float) (event.getEntity().getHealth()*0.35);
            if (p40 > p35) {
                event.getEntity().setHealth(p40);
            } else {
                event.getEntity().setHealth(p35);
            }
        }
    }

    private static int getMaxLevel(LivingEntity user) {
        boolean hasSystemMastery = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).isPresent() &&
                SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).get().getMastery() >= 1.0;
        
        boolean hasShadowMonarch = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).isPresent();
        boolean hasShadowMonarchMastery = hasShadowMonarch &&
                SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).get().getMastery() >= 1.0;
        
        boolean isShadowMonarchAwakened = false;
        if (hasShadowMonarch) {
            ManasSkillInstance shadowInstance = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).get();
            if (shadowInstance.getSkill() instanceof ShadowMonarch skill) {
                isShadowMonarchAwakened = skill.getData().getBoolean("awakened");
            }
        }

        if (isShadowMonarchAwakened) {
            return 500;
        } else if (hasShadowMonarchMastery) {
            return 360;
        } else if (hasShadowMonarch) {
            return 240;
        } else {
            return 140;
        }
    }

    @SubscribeEvent
    public static void LevelUpMechanic(UpdateEPEvent event) {
        LivingEntity user = event.getEntity();
        if (SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).isPresent()) {
            ManasSkillInstance instance = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).get();
            if (instance.getMastery() >= 0.0) {
                CompoundTag tag = instance.getOrCreateTag();
                
                if (!tag.contains("accumulatedEP")) {
                    tag.putInt("accumulatedEP", 0);
                }
                if (!tag.contains("nextLevel")) {
                    tag.putInt("nextLevel", 1150);
                }
                if (!tag.contains("level")) {
                    tag.putInt("level", 1);
                }
                
                int maxLevel = getMaxLevel(user);
                tag.putInt("maxLevel", maxLevel);
                
                int epGained = (int) (event.getNewEP() - event.getOldEP());
                int accumulatedEP = tag.getInt("accumulatedEP") + epGained;
                tag.putInt("accumulatedEP", accumulatedEP);
                
                int nextLevel = tag.getInt("nextLevel");
                
                while (accumulatedEP >= nextLevel && tag.getInt("level") < maxLevel) {
                    int oldLevel = tag.getInt("level");
                    tag.putInt("level", tag.getInt("level") + 1);
                    int epForNext = 1000 + 150 * (tag.getInt("level"));
                    tag.putInt("nextLevel", tag.getInt("nextLevel") + epForNext);
                    nextLevel = tag.getInt("nextLevel");
                    
                    if (instance.getSkill() instanceof SystemSkill skill) {
                        skill.getTag().putInt("level", tag.getInt("level"));
                    }
                    
                    SystemLevelUpEvent systemLevelUpEvent = new SystemLevelUpEvent(instance, user, oldLevel, tag.getInt("level"));
                    MinecraftForge.EVENT_BUS.post(systemLevelUpEvent);
                    
                    if (instance.getSkill() instanceof SystemSkill skill) {
                        skill.onLevelUp(instance, user, systemLevelUpEvent);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        Player user = event.getEntity();
        if (SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).isPresent()) {
            ManasSkillInstance instance = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).get();
            if (instance.getSkill() instanceof SystemSkill skill && skill != null) {
                skill.getTag().putInt("level", instance.getOrCreateTag().getInt("level"));
                skill.getTag().putBoolean("isGui", instance.getOrCreateTag().getBoolean("isGui"));
                instance.getOrCreateTag().put("data", skill.getTag());
            }
        }
    }

    @SubscribeEvent
    public static void onCloseInventory(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof SpatialStorageMenu) {
            Player user = event.getEntity();
            if (SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).isPresent()) {
                ManasSkillInstance instance = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SYSTEM.get()).get();
                if (instance.getSkill() instanceof SystemSkill skill && skill != null) {
                    instance.getOrCreateTag().putBoolean("isGui", false);
                    skill.getTag().putBoolean("isGui", instance.getOrCreateTag().getBoolean("isGui"));
                }
            }
        }
    }
}
