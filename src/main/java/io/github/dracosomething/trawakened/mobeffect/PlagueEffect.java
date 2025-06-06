package io.github.dracosomething.trawakened.mobeffect;

import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.capability.effects.TensuraEffectsCapability;
import com.github.manasmods.tensura.capability.ep.TensuraEPCapability;
import com.github.manasmods.tensura.capability.race.TensuraPlayerCapability;
import com.github.manasmods.tensura.effect.template.DamageAction;
import com.github.manasmods.tensura.effect.template.SkillMobEffect;
import com.github.manasmods.tensura.world.TensuraGameRules;
import io.github.dracosomething.trawakened.ability.skill.ultimate.herrscherofplague;
import io.github.dracosomething.trawakened.registry.effectRegistry;
import io.github.dracosomething.trawakened.world.trawakenedDamage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PlagueEffect extends SkillMobEffect implements DamageAction {
    public PlagueEffect(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
    }

    public void applyEffectTick(LivingEntity entity, int pAmplifier) {
        if (herrscherofplague.active) {
            Player source = TensuraEffectsCapability.getEffectSource(entity, this);
            if(getOwner(entity) == source){
                for(int i = 0; i < 2; i++){
                    if(i==1){
                        SkillHelper.checkThenAddEffectSource(entity, source, (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 1000, 3);
                    }
                }
            }
            if (source != null) {
                if (getOwner(entity) == source) {
                    entity.hurt(trawakenedDamage.PLAGUE, (float) pAmplifier * (SkillUtils.isSkillMastered(Objects.requireNonNull(getOwner(entity)), Objects.requireNonNull(SkillAPI.getSkillRegistry().getValue(new ResourceLocation("trawakened:herrscherofpestilenceskill"))))? 6 : 5));
                }
            }
        } else {
            SkillHelper.checkThenAddEffectSource(entity, getOwner(entity), (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 32767, 3);
        }
        if(entity.isDeadOrDying()){
            AABB aabb = new AABB((double) (entity.getX() - 7), (double) (entity.getY() - 7), (double) (entity.getZ() - 7), (double) (entity.getX() + 7), (double) (entity.getY() + 7), (double) (entity.getZ() + 7));
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
                    if (entity.getRandom().nextInt(100) <= 10) {
                        SkillHelper.checkThenAddEffectSource((LivingEntity) entity2, getOwner(entity), (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 32767, 3);
                        if (entity2 instanceof Player player && player == getOwner(entity)) {
                            player.removeEffect(effectRegistry.PLAGUEEFFECT.get());
                        }
                    }
                }
            }

            if (entity.level instanceof ServerLevel world) {
                world.sendParticles(ParticleTypes.SQUID_INK, (double) entity.getX(), (double) entity.getY(), (double) entity.getZ(), 50, 1.0, 1.0, 1.0, 1.0);
            }
            if(herrscherofplague.active) {
                Player source = getOwner(entity);
                if (source != null) {

                    TensuraEPCapability.getFrom(source).ifPresent((cap) -> {
                        double OwnerEP = TensuraEPCapability.getEP(source);
                        double TargetEP = TensuraEPCapability.getEP(entity);
                        double EPgain = entity.getLevel().getGameRules().getInt(TensuraGameRules.EP_GAIN);
                        double newstats = (TargetEP * (EPgain / 100))/2;
                        cap.setEP(source, OwnerEP + (TargetEP * (EPgain / 100)), true);
                        cap.setCurrentEP(source, OwnerEP + (TargetEP * (EPgain / 100)));
                        TensuraPlayerCapability.getFrom(source).ifPresent((capability) -> {
                            capability.setBaseMagicule(capability.getBaseMagicule() + newstats, source);
                            capability.setMagicule(capability.getBaseMagicule());
                            capability.setBaseAura(capability.getBaseAura() + newstats, source);
                            capability.setAura(capability.getBaseAura());
                        });
                        TensuraPlayerCapability.sync(source);

                    });
                    TensuraEPCapability.updateEP(source, true);
                }
            }
        }
    }

    public static Player getOwner(LivingEntity entity) {
        Player source = TensuraEffectsCapability.getEffectSource(entity, effectRegistry.PLAGUEEFFECT.get());
        if (source != null) {
            return source;
        } else {
            return null;
        }
    }

    @Override
    public void onDamagingEntity(LivingEntity source, LivingHurtEvent e) {
        if (source.getRandom().nextInt(100) <= 20) {
            if(e.getEntity() != getOwner(source)) {
                LivingEntity target = e.getEntity();
                SkillHelper.checkThenAddEffectSource(target, getOwner(source), (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 32767, 3);
            }
        }
    }

    @Override
    public void onBeingDamaged(LivingHurtEvent e) {
        if (e.getEntity().getRandom().nextInt(100) <= 15) {
            if(e.getEntity() != getOwner(e.getEntity())) {
                LivingEntity target = e.getEntity();
                SkillHelper.checkThenAddEffectSource(target, getOwner(e.getEntity()), (MobEffect) effectRegistry.PLAGUEEFFECT.get(), 32767, 3);
            }
        }
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 5 == 0;
    }
}
