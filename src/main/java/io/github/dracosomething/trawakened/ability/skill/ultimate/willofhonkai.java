package io.github.dracosomething.trawakened.ability.skill.ultimate;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.event.UnlockSkillEvent;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.ability.TensuraSkill;
import com.github.manasmods.tensura.ability.TensuraSkillInstance;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.capability.ep.TensuraEPCapability;
import com.github.manasmods.tensura.capability.race.TensuraPlayerCapability;
import com.github.manasmods.tensura.capability.skill.TensuraSkillCapability;
import com.github.manasmods.tensura.client.particle.TensuraParticleHelper;
import com.github.manasmods.tensura.effect.template.Transformation;
import com.github.manasmods.tensura.event.SkillPlunderEvent;
import com.github.manasmods.tensura.race.Race;
import com.github.manasmods.tensura.registry.attribute.TensuraAttributeRegistry;
import com.github.manasmods.tensura.registry.particle.TensuraParticles;
import com.github.manasmods.tensura.registry.race.TensuraRaces;
import io.github.dracosomething.trawakened.registry.effectRegistry;
import io.github.dracosomething.trawakened.registry.raceRegistry;
import io.github.dracosomething.trawakened.registry.skillRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class willofhonkai extends Skill implements Transformation {
    protected static final UUID MULTILAYER = UUID.fromString("2c03b682-5705-11ee-8c99-0242ac120002");

    public ResourceLocation getSkillIcon() {
        return new ResourceLocation("trawakened", "textures/skill/ultimate/will_of_honkai.png");
    }

    public willofhonkai() {
        super(SkillType.ULTIMATE);
    }

    public double getObtainingEpCost() {
        return 5000.0;
    }

    public double learningCost() {
        return 5000.0;
    }

    @Override
    public int modes() {
        return 4;
    }

    @Override
    public int nextMode(LivingEntity entity, TensuraSkillInstance instance, boolean reverse) {
        if (reverse) {
            return instance.getMode() == 1 ? 4 : instance.getMode() - 1;
        } else {
            return instance.getMode() == 4 ? 1 : instance.getMode() + 1;
        }
    }

    public Component getModeName(int mode) {
        MutableComponent var10000;
        switch (mode) {
            case 1 -> var10000 = Component.translatable("trawakened.skill.mode.willofhonkai.copy");
            case 2 -> var10000 = Component.translatable("trawakened.skill.mode.willofhonkai.punch");
            case 3 -> var10000 = Component.translatable("trawakened.skill.mode.willofhonkai.selfbarrier");
            case 4 -> var10000 = Component.translatable("trawakened.skill.mode.willofhonkai.honkaimode");
            default -> var10000 = Component.empty();
        }

        return var10000;
    }

    public double magiculeCost(LivingEntity entity, ManasSkillInstance instance) {
        double var10000;
        switch (instance.getMode()) {
            case 1:
                var10000 = 100.0;
                break;
            case 2:
                var10000 = 500.0;
                break;
            case 3:
                var10000 = 50.0;
                break;
            case 4:
                var10000 = 2500.0;
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
    public void onLearnSkill(ManasSkillInstance instance, LivingEntity living, UnlockSkillEvent event) {
        TensuraEPCapability.getFrom(living).ifPresent((cap) -> {
            instance.getOrCreateTag().putBoolean("isUserMajin", cap.isMajin());
        });
        TensuraEPCapability.sync(living);
    }

    @Override
    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        analize(instance, entity, true);
        if (instance.getOrCreateTag().getBoolean("isUserMajin")) {
            TensuraEPCapability.getFrom(entity).ifPresent((cap) -> {
                cap.setMajin(true);
            });
            TensuraEPCapability.sync(entity);
        }
    }

    @Override
    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        analize(instance, entity, false);
        if (instance.getOrCreateTag().getBoolean("isUserMajin")) {
            TensuraEPCapability.getFrom(entity).ifPresent((cap) -> {
                cap.setMajin(false);
            });
            TensuraEPCapability.sync(entity);
        }
    }

    public void analize(ManasSkillInstance instance, LivingEntity entity, boolean on) {
        if (entity instanceof Player) {
            if (on) {
                Player player = (Player) entity;
                TensuraSkillCapability.getFrom(player).ifPresent((cap) -> {
                    int level;
                    level = 5;
                    if (cap.getAnalysisLevel() != level) {
                        cap.setAnalysisLevel(level);
                        cap.setAnalysisDistance(instance.isMastered(entity) ? 15 : 5);
                        entity.getLevel().playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    } else {
                        cap.setAnalysisLevel(0);
                        entity.getLevel().playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                    TensuraSkillCapability.sync(player);
                });
            }
        }
    }

    @Override
    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        LivingEntity target = SkillHelper.getTargetingEntity(entity, 10.0, false);
        switch (instance.getMode()) {
            case 1:
                if(!SkillHelper.outOfMagicule(entity, instance)) {
                    if (target != null) {
                        label52:
                        {
                            entity.swing(InteractionHand.MAIN_HAND, true);
                            ServerLevel level = (ServerLevel) entity.getLevel();
                            int chance = 50;
                            boolean failed = true;
                            if (entity.getRandom().nextInt(100) <= chance) {
                                List<ManasSkillInstance> collection = SkillAPI.getSkillsFrom(target).getLearnedSkills().stream()
                                        .filter(this::canCopy)
                                        .filter((instance1 -> {
                                            return !SkillUtils.hasSkill(entity, instance1.getSkill());
                                        })).toList();                                if (!collection.isEmpty()) {
                                    ManasSkill skill = ((ManasSkillInstance) collection.get(target.getRandom().nextInt(collection.size()))).getSkill();
                                    SkillPlunderEvent event = new SkillPlunderEvent(target, entity, false, skill);
                                    if (!MinecraftForge.EVENT_BUS.post(event) && SkillUtils.learnSkill(entity, event.getSkill(), instance.getRemoveTime())) {                                        this.addMasteryPoint(instance, entity);
                                        instance.setCoolDown(10);
                                        failed = false;
                                        if (entity instanceof Player) {
                                            Player player = (Player) entity;
                                            player.displayClientMessage(Component.translatable("tensura.skill.acquire", new Object[]{skill.getName()}).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
                                        }

                                        level.playSound((Player) null, entity.getX(), entity.getY(), entity.getY(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
                                    }
                                } else if (entity instanceof Player player) {
                                    player.displayClientMessage(Component.translatable("trawakened.messages.copy.no_left").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
                                }
                            }

                            if (failed && entity instanceof Player) {
                                Player player = (Player) entity;
                                player.displayClientMessage(Component.translatable("tensura.ability.activation_failed").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
                                level.playSound((Player) null, entity.getX(), entity.getY(), entity.getY(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                                instance.setCoolDown(5);
                            }

                            return;
                        }
                    } else if (entity instanceof Player) {
                        Player player = (Player) entity;
                        player.displayClientMessage(Component.translatable("tensura.targeting.not_targeted").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
                    }
                }
                break;

            case 2:
                if(!SkillHelper.outOfMagicule(entity, instance)) {
                    if (target != null) {
                        entity.swing(InteractionHand.MAIN_HAND, true);
                        Explosion.BlockInteraction interaction = Explosion.BlockInteraction.BREAK;
                        target.level.explode(entity, target.getX(), target.getY(), target.getZ(), 20F, interaction);
                        instance.setCoolDown(25);
                    }
                }
                break;
            case 3:
                if (!SkillHelper.outOfMagicule(entity, instance)) {
                    AttributeInstance attributeInstance = (AttributeInstance) Objects.requireNonNull(entity.getAttribute((Attribute)TensuraAttributeRegistry.BARRIER.get()));
                    if (attributeInstance.getModifier(MULTILAYER) != null) {
                        attributeInstance.removePermanentModifier(MULTILAYER);
                        entity.getLevel().playSound((Player)null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
                        instance.setCoolDown(10);
                    } else {
                        this.addMasteryPoint(instance, entity);
                        double barrierPoints = instance.isMastered(entity) ? 50 : 25;
                        attributeInstance.addPermanentModifier(new AttributeModifier(MULTILAYER, "Multilayer Barrier", barrierPoints, AttributeModifier.Operation.ADDITION));
                        entity.getLevel().playSound((Player)null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                }
                break;
            case 4:
                if(!SkillHelper.outOfMagicule(entity, instance)) {
                    if (!this.failedToActivate(entity, (MobEffect) effectRegistry.HONKAIEFFECT.get())) {
                        if (!entity.hasEffect((MobEffect) effectRegistry.HONKAIEFFECT.get())) {
                            if (SkillHelper.outOfMagicule(entity, instance)) {
                                return;
                            }

                            this.addMasteryPoint(instance, entity);
                            instance.setCoolDown(1200);
                            entity.setHealth(entity.getMaxHealth());
                            entity.getLevel().playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                            entity.addEffect(new MobEffectInstance((MobEffect) effectRegistry.HONKAIEFFECT.get(), this.isMastered(instance, entity) ? 1200 : 780, 1, true, true, true));
                            TensuraParticleHelper.addServerParticlesAroundSelf(entity, ParticleTypes.EXPLOSION_EMITTER);
                            TensuraParticleHelper.spawnServerParticles(entity.level, (ParticleOptions) TensuraParticles.LIGHTNING_SPARK.get(), entity.getX(), entity.getY(), entity.getZ(), 55, 0.08, 0.08, 0.08, 0.5, true);
                        } else {
                            entity.removeEffect((MobEffect) effectRegistry.HONKAIEFFECT.get());
                            entity.getLevel().playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                }
                break;
        }
    }


    public boolean canCopy(ManasSkillInstance instance) {
        if (!instance.isTemporarySkill() && instance.getMastery() >= 0) {
            ManasSkill var3 = instance.getSkill();
            if (!(var3 instanceof Skill)) {
                return false;
            } else {
                Skill skill = (Skill) var3;
                return skill.getType().equals(SkillType.INTRINSIC) ||
                        skill.getType().equals(SkillType.COMMON) ||
                        skill.getType().equals(SkillType.EXTRA) ||
                        skill.getType().equals(SkillType.UNIQUE) ||
                        skill.getType().equals(SkillType.RESISTANCE) ||
                        skill.getClass().equals(azazel.class) ||
                        !skill.equals(skillRegistry.VOICEOFHONKAI.get());
            }
        } else {
            return false;
        }
    }

    private void gainMastery(ManasSkillInstance instance, LivingEntity entity) {
        CompoundTag tag = instance.getOrCreateTag();
        int Time = tag.getInt("activatedTimes");
        if (Time % 200 == 0) {
            int chance = 3;
            if (entity.getRandom().nextInt(100) <= chance) {
                this.addMasteryPoint(instance, entity);
            }
        }

        tag.putInt("artivatedTimes", Time + 1);
    }

    @Override
    public void onDamageEntity(ManasSkillInstance instance, LivingEntity entity, LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        AttributeInstance attributeInstance = target.getAttribute((Attribute) TensuraAttributeRegistry.BARRIER.get());
        if(attributeInstance != null){
            attributeInstance.removeModifiers();
        }
        entity.level.playSound((Player) null, entity.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.AMBIENT, 1.0F, 1.0F);
    }

    @Override
    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return true;
    }

    @Override
    public void onTick(ManasSkillInstance instance, LivingEntity living) {
        if (instance.isToggled()) {
            this.gainMastery(instance, living);
        }

        if (living instanceof Player) {
            Player player = (Player) living;
            if(!player.isCreative()) {
                if (!TensuraPlayerCapability.getRace(player).equals((Race) ((IForgeRegistry<?>) TensuraRaces.RACE_REGISTRY.get()).getValue(raceRegistry.ENSLAVED_APOSTLE)) &&
                        !TensuraPlayerCapability.getRace(player).equals((Race) ((IForgeRegistry<?>) TensuraRaces.RACE_REGISTRY.get()).getValue(raceRegistry.HERRSCHER_SEED_ENSLAVED)) &&
                        !TensuraPlayerCapability.getRace(player).equals((Race) ((IForgeRegistry<?>) TensuraRaces.RACE_REGISTRY.get()).getValue(raceRegistry.HERRSCHER_OF_PLAGUE)) &&
                        !TensuraPlayerCapability.getRace(player).equals((Race) ((IForgeRegistry<?>) TensuraRaces.RACE_REGISTRY.get()).getValue(raceRegistry.HERRSCHER_OF_TIME))
                ) {
                    SkillAPI.getSkillsFrom(player).forgetSkill((TensuraSkill) SkillAPI.getSkillRegistry().getValue(new ResourceLocation("trawakened:willofhonkai")));
                    player.displayClientMessage(Component.translatable("unworthy").setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)), false);
                }
            }
        }
    }
}
