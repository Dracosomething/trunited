package io.github.dracosomething.trawakened.mixin;

import io.github.dracosomething.trawakened.helper.TimeStopHelper;
import io.github.dracosomething.trawakened.registry.effectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(value = ServerLevel.class, priority = 1000000)
public class ServerLevelMixin {
    @Inject(
            method = "tickChunk",
            at = @At("HEAD"),
            cancellable = true
    )
    public void stopTickChunk(LevelChunk p_8715_, int p_8716_, CallbackInfo ci){
        if (((ServerLevel) (Object) this).getServer().getPlayerList().getPlayers() != null) {
            ((ServerLevel) (Object) this).getServer().getPlayerList().getPlayers().forEach((player) -> {
                if (player.hasEffect(effectRegistry.TIMESTOP_CORE.get())) {
                    ci.cancel();
                }
            });
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    public void stopTick(BooleanSupplier p_129871_, CallbackInfo ci){
        if (((ServerLevel) (Object) this).getServer().getPlayerList().getPlayers() != null) {
            ((ServerLevel) (Object) this).getServer().getPlayerList().getPlayers().forEach((player) -> {
                if (player.hasEffect(effectRegistry.TIMESTOP_CORE.get())) {
                    ci.cancel();
                }
            });
        }
    }
}
