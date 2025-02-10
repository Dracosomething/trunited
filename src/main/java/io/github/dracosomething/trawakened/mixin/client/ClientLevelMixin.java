package io.github.dracosomething.trawakened.mixin.client;

import io.github.dracosomething.trawakened.registry.effectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(value = ClientLevel.class, priority = 1000000)
public abstract class ClientLevelMixin {
    public ClientLevelMixin() {
    }

    @Shadow
    @Final
    EntityTickList tickingEntities;

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    public void stopTime(BooleanSupplier p_104727_, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null) {
            if (Minecraft.getInstance().player.hasEffect(effectRegistry.TIMESTOP_CORE.get())) {
                ci.cancel();
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tickEntities() {
        ClientLevel level = ((ClientLevel) (Object) this);
        ProfilerFiller profilerfiller = level.getProfiler();
        profilerfiller.push("entities");
        this.tickingEntities.forEach((entity) -> {
            if (Minecraft.getInstance().player != null) {
                if (Minecraft.getInstance().player.hasEffect(effectRegistry.TIMESTOP_CORE.get())) {
                    if (entity != Minecraft.getInstance().player) {
//                        ci.cancel();
                        return;
                    }
                }
            }
            if (!entity.isRemoved() && !entity.isPassenger()) {
                level.guardEntityTick(level::tickNonPassenger, entity);
            }
        });
        profilerfiller.pop();
        level.tickBlockEntities();
    }
}
