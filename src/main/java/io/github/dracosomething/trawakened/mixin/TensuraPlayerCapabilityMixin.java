package io.github.dracosomething.trawakened.mixin;

import com.github.manasmods.tensura.capability.race.TensuraPlayerCapability;
import com.github.manasmods.tensura.config.TensuraConfig;
import io.github.dracosomething.trawakened.config.StarterRaceConfig;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(TensuraPlayerCapability.class)
public abstract class TensuraPlayerCapabilityMixin {
    @Shadow
    public static List<ResourceLocation> loadRaces() {
        return null;
    }

    private TensuraPlayerCapabilityMixin(){}

    @Inject(
            method = "loadRaces",
            at = @At("HEAD"),
            cancellable = true,
            remap=false
    )
    private static void loadRaces(CallbackInfoReturnable<List<ResourceLocation>> cir) {
        System.out.println("ewrqqwrwet");

        List<ResourceLocation> races = new ArrayList(((List<String>)TensuraConfig.INSTANCE.racesConfig.startingRaces.get()).stream().map(ResourceLocation::new).toList());

        List<String> randomRaces = (List)TensuraConfig.INSTANCE.racesConfig.randomRaces.get();

        for (String location : StarterRaceConfig.INSTANCE.startingRaces.get()) {
            if (!races.contains(new ResourceLocation(location))) {
                races.add(new ResourceLocation(location));
            }
            if (!randomRaces.contains(location)) {
                randomRaces.add(location);
            }
        }

        System.out.println(races);
        System.out.println(randomRaces);

        if (randomRaces.isEmpty()) {
            cir.setReturnValue(races);
        } else {
            Random random = new Random();
            String race = (String)randomRaces.get(random.nextInt(randomRaces.size()));
            if (!race.isEmpty() && !race.isBlank()) {
                races.add(new ResourceLocation(race));
            }

            cir.setReturnValue(races);
            System.out.println(races);
        }


//        cir.setReturnValue(races);
    }
}