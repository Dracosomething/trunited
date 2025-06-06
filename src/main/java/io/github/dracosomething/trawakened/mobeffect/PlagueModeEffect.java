package io.github.dracosomething.trawakened.mobeffect;

import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.capability.ep.TensuraEPCapability;
import com.github.manasmods.tensura.client.particle.TensuraParticleHelper;
import com.github.manasmods.tensura.effect.template.DamageAction;
import com.github.manasmods.tensura.effect.template.SkillMobEffect;
import com.github.manasmods.tensura.effect.template.Transformation;
import com.github.manasmods.tensura.registry.effects.TensuraMobEffects;
import io.github.dracosomething.trawakened.registry.effectRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

import static io.github.dracosomething.trawakened.mobeffect.PlagueEffect.getOwner;

public class PlagueModeEffect extends SkillMobEffect implements Transformation, DamageAction {
    protected static final String PLAGUE = "c585ceb0-3f6a-11ee-be57-0242ac120002";

    public PlagueModeEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "c585ceb0-3f6a-11ee-be57-0242ac120002", -1.0, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "c585ceb0-3f6a-11ee-be57-0242ac120002", 0.2, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ForgeMod.ATTACK_RANGE.get(), "c585ceb0-3f6a-11ee-be57-0242ac120002", 5.0, AttributeModifier.Operation.ADDITION);
    }

    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if(pLivingEntity instanceof Player player) {
            if (player.getAbilities().invulnerable) {
                return;
            }

            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
        }
    }

    public void applyEffectTick(LivingEntity entity, int pAmplifier) {
        if (pAmplifier >= 1 || !this.failedToActivate(entity, this)) {
            TensuraParticleHelper.addParticlesAroundSelf(entity, (ParticleOptions) ParticleTypes.SQUID_INK);
        }
        entity.addEffect(new MobEffectInstance(TensuraMobEffects.FALSIFIER.get(), 5, 10));
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

    public void removeAttributeModifiers(LivingEntity entity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(entity, pAttributeMap, pAmplifier);
        TensuraEPCapability.updateEP(entity);
        if(entity instanceof Player player) {
            if (!player.getAbilities().invulnerable) {
                return;
            }

            player.getAbilities().invulnerable = false;
            player.onUpdateAbilities();
        }
    }
    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
        return pModifier.getId().equals(UUID.fromString("c585ceb0-3f6a-11ee-be56-0242ac120002")) ? pModifier.getAmount() : pModifier.getAmount() * (double)(pAmplifier + 1);
    }

    public boolean isDurationEffectTick(int pDuration, int amplifier) {
        return pDuration % 2 == 0;
    }
}

