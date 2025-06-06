package io.github.dracosomething.trawakened.handler;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.ability.skill.unique.MercilessSkill;
import com.github.manasmods.tensura.capability.ep.TensuraEPCapability;
import com.github.manasmods.tensura.capability.race.TensuraPlayerCapability;
import com.github.manasmods.tensura.client.particle.TensuraParticleHelper;
import com.github.manasmods.tensura.config.TensuraConfig;
import com.github.manasmods.tensura.data.pack.GearEPCount;
import com.github.manasmods.tensura.data.pack.TensuraData;
import com.github.manasmods.tensura.enchantment.EngravingEnchantment;
import com.github.manasmods.tensura.entity.template.HumanoidNPCEntity;
import com.github.manasmods.tensura.event.UpdateEPEvent;
import com.github.manasmods.tensura.menu.HumanoidNPCMenu;
import com.github.manasmods.tensura.race.Race;
import com.github.manasmods.tensura.registry.effects.TensuraMobEffects;
import com.github.manasmods.tensura.registry.skill.UniqueSkills;
import com.github.manasmods.tensura.util.damage.DamageSourceHelper;
import com.github.manasmods.tensura.world.TensuraGameRules;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.dracosomething.trawakened.ability.skill.ultimate.ShadowMonarch;
import io.github.dracosomething.trawakened.capability.ShadowCapability.AwakenedShadowCapability;
import io.github.dracosomething.trawakened.commands.argument.rankArgument;
import io.github.dracosomething.trawakened.helper.skillHelper;
import io.github.dracosomething.trawakened.item.amplificationOrb;
import io.github.dracosomething.trawakened.library.MonarchsDomain;
import io.github.dracosomething.trawakened.library.shadowRank;
import io.github.dracosomething.trawakened.network.TRAwakenedNetwork;
import io.github.dracosomething.trawakened.network.play2client.openWordScreen;
import io.github.dracosomething.trawakened.registry.dimensionRegistry;
import io.github.dracosomething.trawakened.registry.effectRegistry;
import io.github.dracosomething.trawakened.registry.particleRegistry;
import io.github.dracosomething.trawakened.registry.skillRegistry;
import io.github.dracosomething.trawakened.trawakened;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = trawakened.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShadowHandler {
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof LivingEntity entity) {
            LivingEntity target = event.getEntity();
            AtomicReference<LivingEntity> user = new AtomicReference<>();
            skillHelper.GetLivingEntitiesInRange(target, 50, false).forEach(entity1 -> {
                if (!AwakenedShadowCapability.isShadow(target) && !AwakenedShadowCapability.isArisen(target)) {
                    if (SkillAPI.getSkillsFrom(entity1).getSkill(skillRegistry.SHADOW_MONARCH.get()).isPresent()) {
                        user.set(entity1);
                    }
                }
            });
            if (user.get() != null) {
                if (target.position().distanceTo(user.get().position()) <= 50) {
                    if (target instanceof Mob mob) {
                        mob.setNoAi(true);
                    }
                    target.setHealth(100);
                    target.setPose(Pose.STANDING);
                    target.setInvisible(true);
                    target.setInvulnerable(true);
                    target.setSpeed(0);
                    SkillHelper.addEffectWithSource(target, target, TensuraMobEffects.PRESENCE_CONCEALMENT.get(), 6000, 255, false, false, false, false);
                    AwakenedShadowCapability.setShadow(target, true);
                    if (entity == user.get()) {
                        double EP = SkillUtils.getEPGain(target, user.get(), false);
                        if (!(EP <= 0.0)) {
                            if (!TensuraEPCapability.isSkippingEPDrop(target)) {
                                entityGetEP(user.get(), target, SkillUtils.getEPGain(target, user.get(), true));
                                EquipmentSlot[] var6 = EquipmentSlot.values();
                                int var7 = var6.length;

                                for (int var8 = 0; var8 < var7; ++var8) {
                                    EquipmentSlot equipmentSlot = var6[var8];
                                    gearGetEP(user.get(), equipmentSlot, EP);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void Handling(LivingEvent.LivingTickEvent event) {
        if (AwakenedShadowCapability.isShadow(event.getEntity()) && !AwakenedShadowCapability.isArisen(event.getEntity())) {
            event.getEntity().setDeltaMovement(0, 0, 0);
            SkillHelper.addEffectWithSource(event.getEntity(), event.getEntity(), MobEffects.MOVEMENT_SLOWDOWN, Integer.MAX_VALUE, 255, false, false, false, false);
            SkillHelper.addEffectWithSource(event.getEntity(), event.getEntity(), MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 255, false, false, false, false);
            if (!event.getEntity().hasEffect(TensuraMobEffects.PRESENCE_CONCEALMENT.get())) {
                event.getEntity().discard();
            }
        }
        if (AwakenedShadowCapability.isShadow(event.getEntity())) {
            TensuraParticleHelper.addServerParticlesAroundSelf(event.getEntity(), ParticleTypes.SMOKE, 1, 5);
            TensuraParticleHelper.addParticlesAroundSelf(event.getEntity(), ParticleTypes.SMOKE);
        }
        if (AwakenedShadowCapability.isArisen(event.getEntity())) {
            TensuraParticleHelper.addServerParticlesAroundSelf(event.getEntity(),
                    event.getEntity().hasEffect(effectRegistry.MONARCHS_DOMAIN.get()) ?
                            particleRegistry.PURPLE_FIRE.get() :
                            ParticleTypes.SOUL_FIRE_FLAME, 0.2, 1);
            if (event.getEntity().getType().toString().contains("orc")) {
                TensuraParticleHelper.addServerParticlesAroundSelf(event.getEntity(), particleRegistry.RED_FIRE.get(), 0.6, 1);
            }
        }
    }

    @SubscribeEvent
    public static void noHeal(LivingHealEvent event) {
        if (AwakenedShadowCapability.isShadow(event.getEntity()) && !AwakenedShadowCapability.isArisen(event.getEntity())) {
            event.getEntity().setHealth(1);
            event.setCanceled(true);
        }
    }

    protected static void entityGetEP(LivingEntity living, LivingEntity target, double totalEP) {
        double maxMP = (double)living.getLevel().getGameRules().getInt(TensuraGameRules.MAX_MP_GAIN);
        double maxAP = (double)living.getLevel().getGameRules().getInt(TensuraGameRules.MAX_AP_GAIN);
        if (living instanceof Player player) {
            TensuraPlayerCapability.getFrom(player).ifPresent((cap) -> {
                if (cap.isDemonLordSeed()) {
                    double souls = SkillUtils.getEPGain(target, living, false) * (Double) TensuraConfig.INSTANCE.awakeningConfig.epToSoulRate.get() / 100.0;
                    souls = Math.min(souls, maxMP + maxAP);
                    cap.setSoulPoints((int)Math.min((double)cap.getSoulPoints() + souls, 2.147483647E9));
                    if (player.getLevel().getGameRules().getBoolean(TensuraGameRules.RIMURU_MODE) && cap.getSoulPoints() >= 20000000 && SkillUtils.learnSkill(player, (ManasSkill) UniqueSkills.MERCILESS.get())) {
                        player.displayClientMessage(Component.translatable("tensura.skill.acquire", new Object[]{((MercilessSkill)UniqueSkills.MERCILESS.get()).getName()}).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
                    }
                }

                Race race = cap.getRace();
                if (race != null) {
                    double MP = Math.min(totalEP * (double)SkillUtils.getMagiculeGain(player, TensuraEPCapability.isMajin(living)), maxMP);
                    double AP = Math.min(totalEP * (double)SkillUtils.getAuraGain(player, TensuraEPCapability.isMajin(living)), maxAP);
                    cap.setBaseMagicule(cap.getBaseMagicule() + MP, player);
                    cap.setBaseAura(cap.getBaseAura() + AP, player);
                    TensuraPlayerCapability.sync(player);
                }
            });
            TensuraEPCapability.updateEP(player);
        } else if (totalEP * (double)TensuraGameRules.getEPGain(living.level) >= 1.0) {
            TensuraEPCapability.setLivingEP(living, (double)Math.round(TensuraEPCapability.getEP(living) + Math.min(totalEP * (double)TensuraGameRules.getEPGain(living.level), maxMP + maxAP)));
            LivingEntity var10 = SkillHelper.getSubordinateOwner(living);
            if (var10 instanceof Player) {
                Player player = (Player)var10;
                TensuraPlayerCapability.getFrom(player).ifPresent((cap) -> {
                    if (cap.isDemonLordSeed()) {
                        double newSoul = (double)cap.getSoulPoints() + Math.min(totalEP * (Double)TensuraConfig.INSTANCE.awakeningConfig.epToSoulRate.get() / 100.0, maxMP + maxAP);
                        cap.setSoulPoints((int)Math.min(newSoul, 2.147483647E9));
                        TensuraPlayerCapability.sync(player);
                    }

                });
            }
        }

    }

    private static void gearGetEP(LivingEntity living, EquipmentSlot slot, double totalEP) {
        ItemStack stack = living.getItemBySlot(slot);
        if (stack.getMaxStackSize() <= 1) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                Iterator var6 = TensuraData.getGearEP().iterator();

                while(var6.hasNext()) {
                    GearEPCount gearEPCount = (GearEPCount)var6.next();
                    if (Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), gearEPCount.getItem())) {
                        if (tag.getDouble("EP") <= (double)gearEPCount.getMinEP()) {
                            tag.putDouble("EP", (double)gearEPCount.getMinEP());
                        }

                        if (tag.getDouble("MaxEP") < (double)gearEPCount.getMaxEP()) {
                            tag.putDouble("MaxEP", (double)gearEPCount.getMaxEP());
                        }

                        double maxEP = tag.getDouble("MaxEP");
                        double gainAmount = (double)Math.round(totalEP * gearEPCount.getGainEP());
                        ItemStack evolvingItem = ((Item)Objects.requireNonNull((Item)ForgeRegistries.ITEMS.getValue(gearEPCount.getEvolvingItem()))).getDefaultInstance();
                        if (tag.getDouble("EP") + gainAmount < maxEP) {
                            tag.putDouble("EP", tag.getDouble("EP") + gainAmount);
                            EngravingEnchantment.randomEngraving(living, stack, tag.getDouble("EP"));
                        } else if (!evolvingItem.is(Items.AIR)) {
                            tag.putDouble("EP", tag.getDouble("EP") + gainAmount);
                            EngravingEnchantment.randomEngraving(living, stack, tag.getDouble("EP"));
                            if (stack.getTag() != null) {
                                evolvingItem.setTag(stack.getTag().copy());
                            }

                            initiateItemEP(evolvingItem);
                            living.setItemSlot(slot, evolvingItem);
                            if (living instanceof HumanoidNPCEntity) {
                                HumanoidNPCEntity npc = (HumanoidNPCEntity)living;
                                int id = HumanoidNPCMenu.getEquipmentSlotId(slot);
                                if (npc.inventory.getItem(id).is(stack.getItem())) {
                                    npc.inventory.setItem(HumanoidNPCMenu.getEquipmentSlotId(slot), evolvingItem);
                                    npc.inventory.setChanged();
                                }
                            }
                        } else if (tag.getDouble("EP") != maxEP) {
                            tag.putDouble("EP", maxEP);
                            EngravingEnchantment.randomEngraving(living, stack, tag.getDouble("EP"));
                        }
                        break;
                    }
                }

            }
        }
    }

    private static void initiateItemEP(ItemStack stack) {
        Iterator var1 = TensuraData.getGearEP().iterator();

        while(var1.hasNext()) {
            GearEPCount gearEPCount = (GearEPCount)var1.next();
            if (Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), gearEPCount.getItem())) {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.getDouble("EP") <= (double)gearEPCount.getMinEP()) {
                    tag.putDouble("EP", (double)gearEPCount.getMinEP());
                }

                if (tag.getDouble("MaxEP") < (double)gearEPCount.getMaxEP()) {
                    tag.putDouble("MaxEP", (double)gearEPCount.getMaxEP());
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        Player user = event.getEntity();
        if (SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).isPresent()) {
            ManasSkillInstance instance = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).get();
            if (instance.getSkill() instanceof ShadowMonarch skill && skill != null) {
                skill.setShadowStorage(instance.getOrCreateTag().getCompound("ShadowStorage"));
                instance.getOrCreateTag().put("ShadowStorage", skill.getShadowStorage());
                skill.setData(instance.getOrCreateTag().getCompound("data"));
                instance.getOrCreateTag().put("data", skill.getData());
                System.out.println(skill.getCommandWord());
                System.out.println(instance.getOrCreateTag());
                if (Objects.equals(skill.getCommandWord(), "")) {
                    if (user instanceof ServerPlayer player) {
                        TRAwakenedNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new openWordScreen(player.getUUID(), instance));
                    }
                }
                if (!skill.getData().getCompound("domain").isEmpty()) {
                    CompoundTag data = skill.getData().getCompound("domain");
                    MonarchsDomain domain = MonarchsDomain.fromNBT(data, event.getEntity());
                    domain.place();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLeaveWorld(PlayerEvent.PlayerLoggedOutEvent event) {
        Player user = event.getEntity();
        if (SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).isPresent()) {
            ManasSkillInstance instance = SkillAPI.getSkillsFrom(user).getSkill(skillRegistry.SHADOW_MONARCH.get()).get();
            if (instance.getSkill() instanceof ShadowMonarch skill && skill != null) {
                skill.setShadowStorage(instance.getOrCreateTag().getCompound("ShadowStorage"));
                instance.getOrCreateTag().put("ShadowStorage", skill.getShadowStorage());
                instance.getOrCreateTag().put("data", skill.getData());
                skill.setData(instance.getOrCreateTag().getCompound("data"));
                System.out.println(skill.getCommandWord());
                System.out.println(instance.getOrCreateTag());
            }
        }
    }

    @SubscribeEvent
    public static void doubleEPGain(UpdateEPEvent event) {
        if (AwakenedShadowCapability.isShadow(event.getEntity())) {
            event.setNewEP(event.getNewEP() * 2);
            Player owner = event.getEntity().level.getPlayerByUUID(AwakenedShadowCapability.getOwnerUUID(event.getEntity()));
            if (owner != null) {
                if (event.getNewEP() >= TensuraEPCapability.getEP(owner)) {
                    event.setNewEP(TensuraEPCapability.getEP(owner));
                }
                if (SkillAPI.getSkillsFrom(owner).getSkill(skillRegistry.SHADOW_MONARCH.get()).isPresent()) {
                    double maxMP = (double)owner.getLevel().getGameRules().getInt(TensuraGameRules.MAX_MP_GAIN);
                    double maxAP = (double)owner.getLevel().getGameRules().getInt(TensuraGameRules.MAX_AP_GAIN);
                    double ep = event.getNewEP() * 0.33;
                    TensuraEPCapability.setLivingEP(owner, (double)Math.round(TensuraEPCapability.getEP(owner) + Math.min(ep * (double)TensuraGameRules.getEPGain(owner.level), maxMP + maxAP)));
                    TensuraEPCapability.updateEP(owner);
                }
            }
        }
    }

    @SubscribeEvent
    public static void alertOwnerWhenHurt(LivingHurtEvent event) {
        if (AwakenedShadowCapability.hasShadow(event.getEntity())) {
            if (AwakenedShadowCapability
                    .getStorage(event.getEntity())
                    .getCompound("EntityData")
                    .getCompound("ForgeCaps")
                    .getCompound("trawakened:shadow")
                    .getString("ownerUUID").length() == 36) {
                float dmg = event.getEntity().getHealth() - event.getAmount();
                if (dmg <= (event.getEntity().getMaxHealth() * 0.10)) {
                    LivingEntity owner = event.getEntity().level.getPlayerByUUID(UUID.fromString(
                            AwakenedShadowCapability
                                    .getStorage(event.getEntity())
                                    .getCompound("EntityData")
                                    .getCompound("ForgeCaps")
                                    .getCompound("trawakened:shadow")
                                    .getString("ownerUUID")
                    ));
                    owner.sendSystemMessage(Component.translatable("message.low_hp", event.getEntity().getDisplayName().getString()));
                }
                if (dmg <= 0.0) {
                    LivingEntity owner = event.getEntity().level.getPlayerByUUID(UUID.fromString(
                            AwakenedShadowCapability
                                    .getStorage(event.getEntity())
                                    .getCompound("EntityData")
                                    .getCompound("ForgeCaps")
                                    .getCompound("trawakened:shadow")
                                    .getString("ownerUUID")
                    ));
                    if (SkillAPI.getSkillsFrom(owner).getSkill(skillRegistry.SHADOW_MONARCH.get()).isPresent()) {
                        if (SkillAPI.getSkillsFrom(owner).getSkill(skillRegistry.SHADOW_MONARCH.get()).get().getSkill() instanceof ShadowMonarch skill) {
                            String id = AwakenedShadowCapability.getStorage(event.getEntity()).getString("UUID");
                            if (!id.isEmpty()) {
                                skill.getShadowStorage().put(id, AwakenedShadowCapability.getStorage(event.getEntity()));
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void magicDMGIncrease(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity living) {
            Item mainHandItem = living.getMainHandItem().getItem();
            Item offHandItem = living.getOffhandItem().getItem();
            if (mainHandItem instanceof amplificationOrb ||
            offHandItem instanceof amplificationOrb) {
                if (DamageSourceHelper.isTensuraMagic(event.getSource()) ||
                        event.getSource().isMagic()) {
                    event.setAmount(event.getAmount() * 2);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo().equals(dimensionRegistry.SHADOW)) {
            ManasSkillInstance instance = SkillUtils.getSkillOrNull(event.getEntity(), skillRegistry.SHADOW_MONARCH.get());
            if (instance != null) {
                if (instance.getSkill() instanceof ShadowMonarch skill) {
                    List<String> validTargets = skill.getShadowStorage().getAllKeys().stream().toList();
                    Player player = event.getEntity();
                    validTargets.forEach((shadow) -> {
                        EntityType<?> type = EntityType.byString(skill.getShadowStorage().getCompound(shadow).getString("entityType")).get();
                        LivingEntity entity = (LivingEntity) type.create(player.level);
                        entity.deserializeNBT(skill.getShadowStorage().getCompound(shadow).getCompound("EntityData"));
                        entity.setPos(player.position());
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING));
                        player.level.addFreshEntity(entity);
                    });
                }
            }
        }
        if (event.getFrom().equals(dimensionRegistry.SHADOW)) {
            if (event.getEntity().level.getServer() != null) {
                MinecraftServer server = event.getEntity().level.getServer();
                if (server.getLevel(event.getFrom()) != null) {
                    ServerLevel level = server.getLevel(event.getFrom());
                    level.getEntities().getAll().forEach((entity -> {
                        if (entity instanceof LivingEntity living) {
                            if (AwakenedShadowCapability.isOwnerShadow(living, event.getEntity())) {
                                living.discard();
                            }
                        }
                    }));
                }
            }
        }
    }
}
