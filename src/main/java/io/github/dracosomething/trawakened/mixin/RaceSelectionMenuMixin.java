package io.github.dracosomething.trawakened.mixin;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.config.TensuraConfig;
import com.github.manasmods.tensura.menu.RaceSelectionMenu;
import com.github.manasmods.tensura.world.TensuraGameRules;
import com.github.manasmods.tensura.world.savedata.UniqueSkillSaveData;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.dracosomething.trawakened.config.StarterRaceConfig;
import io.github.dracosomething.trawakened.registry.skillRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RaceSelectionMenu.class)
public abstract class RaceSelectionMenuMixin {
    @Shadow
    public static void randomUniqueSkill(Player player, boolean coverEP) {}

    @Inject(
            method = "getReincarnationSkills",
            at = @At(value = "HEAD"),
            remap = false,
            cancellable = true)
    private static void NewUniques(ServerLevel level, boolean ignoreGameRule, Player roller, CallbackInfoReturnable<List<ManasSkill>> cir) {
        List<ManasSkill> skills = new ArrayList<ManasSkill>(((List)TensuraConfig.INSTANCE.skillsConfig.reincarnationSkills.get()).stream().filter((skill) -> {
            if (ignoreGameRule) {
                return true;
            } else if (!level.getGameRules().getBoolean(TensuraGameRules.TRULY_UNIQUE)) {
                return true;
            } else {
                return !UniqueSkillSaveData.get(level.getServer().overworld()).hasSkill(new ResourceLocation(skill.toString()));
            }
        }).map((skill) -> {
            return (ManasSkill)SkillAPI.getSkillRegistry().getValue(new ResourceLocation(skill.toString()));
        }).filter((skill) -> {
            if (skill == null) {
                return false;
            } else {
                return roller == null || !SkillUtils.hasSkill(roller, (ManasSkill) skill);
            }
        }).toList());
        for (String location : StarterRaceConfig.INSTANCE.startingSkills.get()) {
            skills.add(SkillAPI.getSkillRegistry().getValue(new ResourceLocation(location)));
        }
        cir.setReturnValue(skills);
    }

//    @ModifyVariable(method = "randomUniqueSkill", at = @At("STORE"))
//    private static ManasSkill removeSystemIfSkill(ManasSkill original) {
//        if (original instanceof SystemSkill) {
//            List<?> list = StarterRaceConfig.STARTER_SKILLS.get();
//            if (list.contains("trawakened:system")) {
//                list.remove("trawakened:system");
//            }
//        }
//        return original;
//    }
}

