package io.github.dracosomething.trawakened.ability.skill.ultimate;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.TensuraSkill;
import com.github.manasmods.tensura.ability.TensuraSkillInstance;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.capability.race.TensuraPlayerCapability;
import com.github.manasmods.tensura.race.Race;
import com.github.manasmods.tensura.registry.race.TensuraRaces;
import io.github.dracosomething.trawakened.capability.trawakenedPlayerCapability;
import io.github.dracosomething.trawakened.registry.effectRegistry;
import io.github.dracosomething.trawakened.registry.raceRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class herrscherofplague extends Skill {
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation("trawakened", "textures/skill/ultimate/herrscherofpestilence.png");
    }

    @Override
    public List<MobEffect> getImmuneEffects(ManasSkillInstance instance, LivingEntity entity) {
        return List.of(effectRegistry.PLAGUEEFFECT.get());
    }

    public herrscherofplague() {
        super(SkillType.ULTIMATE);
    }

    public static boolean active = false;
    public boolean infection = false;

    public double getObtainingEpCost() {
        return 5000.0;
    }

    public double learningCost() {
        return 5000.0;
    }

    @Override
    public int modes() {
        return 3;
    }

    @Override
    public Component getModeName(int mode) {
        MutableComponent var10000;
        switch (mode) {
            case 1 -> var10000 = Component.translatable("trawakened.skill.mode.herrscherofplagueskill.toggleplague");
            case 2 -> var10000 = Component.translatable("trawakened.skill.mode.herrscherofplagueskill.toggleplaguehit");
            case 3 -> var10000 = Component.translatable("trawakened.skill.mode.herrscherofplagueskill.plaguecloud");
            case 4 -> var10000 = Component.translatable("trawakened.skill.mode.herrscherofplagueskill.plagueform");
            case 5 -> var10000 = Component.translatable("trawakened.skill.mode.herrscherofplagueskill.plaguepound");
            default -> var10000 = Component.empty();
        }

        return var10000;
    }

    @Override
    public int nextMode(LivingEntity entity, TensuraSkillInstance instance, boolean reverse) {
        int var10000;
        if (reverse) {
            switch (instance.getMode()) {
                case 1 -> var10000 = 5;
                case 2 -> var10000 = 1;
                case 3 -> var10000 = 2;
                case 4 -> var10000 = 3;
                case 5 -> var10000 = 4;
                default -> var10000 = 0;
            }

            return var10000;
        } else {
            switch (instance.getMode()) {
                case 1 -> var10000 = 2;
                case 2 -> var10000 = 3;
                case 3 -> var10000 = 4;
                case 4 -> var10000 = 5;
                case 5 -> var10000 = 1;
                default -> var10000 = 0;
            }

            return var10000;
        }
    }

    public double magiculeCost(LivingEntity entity, ManasSkillInstance instance) {
        double var10000;
        switch (instance.getMode()) {
            case 1:
                var10000 = 0.0;
                break;
            case 2:
                var10000 = 0.0;
                break;
            case 3:
                var10000 = 100.0;
                break;
            case 4:
                var10000 = 150.0;
                break;
            case 5:
                var10000 = 250.0;
                break;
            default:
                var10000 = 0.0;
                break;
        }

        return var10000;
    }

    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        return true;
    }

    @Override
    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            ((Player) entity).onUpdateAbilities();
        }
    }

    @Override
    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            ((Player) entity).onUpdateAbilities();
        }
    }

    @Override
    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        LivingEntity target = SkillHelper.getTargetingEntity(entity, 10.0, false);
        switch (instance.getMode()) {
            case 1:
                if (active) {
                    if (entity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Disabled plague"), true);

                    }
                    active = false;
                } else {
                    if (entity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Enabled plague"), true);
                    }
                    active = true;
                }
                break;
            case 2:
                if (infection) {
                    if (entity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Disabled Infection"), true);
                    }
                    infection = false;
                } else {
                    if (entity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Enabled Infection"), true);
                    }
                    infection = true;
                }
                break;
            case 3:
                if (!SkillHelper.outOfMagicule(entity, instance)) {
                    AABB aabb = new AABB((double) (entity.getX() - 8), (double) (entity.getY() - 8), (double) (entity.getZ() - 8), (double) (entity.getX() + 8), (double) (entity.getY() + 8), (double) (entity.getZ() + 8));
                    for(float x = (float) (entity.getX() - (float)8); x < entity.getX() + (float)8 + 1.0F; ++x) {
                        for(float y = (float) (entity.getY() - (float)8); y < entity.getY() + (float)8 + 1.0F; ++y) {
                            for(float z = (float) (entity.getZ() - (float)8); z < entity.getZ() + (float)8 + 1.0F; ++z) {
                                if (entity.level instanceof ServerLevel world) {
                                    world.sendParticles(ParticleTypes.SQUID_INK, (double) x, (double) y, (double) z, 5, 1.0, 1.0, 1.0, 1.0);
                                }
                            }
                        }
                    }
                    List<Entity> entities = entity.level.getEntities((Entity) null, aabb, Entity::isAlive);
                    List<Entity> ret = new ArrayList();
                    new Vec3((double) entity.getX(), (double) entity.getY(), (double) entity.getZ());
                    Iterator var16 = entities.iterator();

                    while (var16.hasNext()) {
                        Entity entity2 = (Entity) var16.next();

                        int radius = 10;

                        double x = entity2.getX();
                        double y = entity2.getY();
                        double z = entity2.getZ();
                        double cmp = (double) (radius * radius) - ((double) entity2.getX() - x) * ((double) entity2.getX() - x) - ((double) entity2.getY() - y) * ((double) entity2.getY() - y) - ((double) entity2.getZ() - z) * ((double) entity2.getZ() - z);
                        if (cmp > 0.0) {
                            ret.add(entity2);
                        }
                    }

                    for (Entity entity2 : ret) {
                        if (entity2 instanceof LivingEntity) {
                            if (entity2 != entity) {
                                SkillHelper.checkThenAddEffectSource((LivingEntity) entity2, entity, (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 32767, 3);
                                Owner = trawakenedPlayerCapability.setOwnerSkill(entity, instance);
                            }
                        }
                    }
                    instance.setCoolDown(350);
                }
                break;
            case 4:
                if(!SkillHelper.outOfMagicule(entity, instance)){
                    if (entity.hasEffect(effectRegistry.PLAGUE_MODE_EFFECT.get())){
                        return;
                    } else {
                        entity.addEffect(new MobEffectInstance(effectRegistry.PLAGUE_MODE_EFFECT.get(), 5326, 2, false, false, false));
                        Owner = trawakenedPlayerCapability.setOwnerSkill(entity, instance);
                        infection = true;
                        instance.setCoolDown(200);
                    }
                }
            case 5:
                if(!SkillHelper.outOfMagicule(entity, instance)){
                    SkillHelper.checkThenAddEffectSource((LivingEntity) target, entity, (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 32767, 3);
                    Owner = trawakenedPlayerCapability.setOwnerSkill(entity, instance);
                    instance.setCoolDown(50);
                }
                break;
        }
    }

    @Override
    public boolean canIgnoreCoolDown(ManasSkillInstance instance, LivingEntity entity) {
        switch (instance.getMode()) {
            case 1, 2 -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public void onDamageEntity(ManasSkillInstance instance, LivingEntity entity, LivingHurtEvent e) {
        if (infection) {
            LivingEntity target = e.getEntity();
            SkillHelper.checkThenAddEffectSource(target, entity, (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 32767,
                    3);
            Owner = trawakenedPlayerCapability.setOwnerSkill(entity, instance);
        }
    }

    public static LivingEntity Owner = null;

    private void gainMastery(ManasSkillInstance instance, LivingEntity entity) {
        CompoundTag tag = instance.getOrCreateTag();
        int Time = tag.getInt("activatedTimes");
        if (Time % 150 == 0) {
            int chance = 10;
            if (entity.getRandom().nextInt(100) <= chance) {
                this.addMasteryPoint(instance, entity);
            }
        }
        tag.putInt("artivatedTimes", Time + 1);
    }



    @Override
    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return true;
    }

    @Override
    public void onTick(ManasSkillInstance instance, LivingEntity living) {
        if (living instanceof Player) {
            Player player = (Player) living;
            if(!player.isCreative()) {
                if (!TensuraPlayerCapability.getRace(player)
                        .equals((Race) ((IForgeRegistry<?>) TensuraRaces.RACE_REGISTRY.get())
                                .getValue(raceRegistry.HERRSCHER_OF_PLAGUE))) {
                    SkillAPI.getSkillsFrom(player).forgetSkill((TensuraSkill) SkillAPI.getSkillRegistry()
                            .getValue(new ResourceLocation("trawakened:herrscherofpestilenceskill")));
                    player.displayClientMessage(Component.translatable("unworthy").setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)), false);
                }
            }
        }
    }
}
