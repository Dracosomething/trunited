package io.github.dracosomething.trawakened;

import com.github.manasmods.tensura.registry.items.TensuraMaterialItems;
import com.github.manasmods.tensura.registry.items.TensuraToolItems;
import io.github.dracosomething.trawakened.ability.skill.ultimate.herrscheroftime;
import io.github.dracosomething.trawakened.config.StarterRaceConfig;
import io.github.dracosomething.trawakened.helper.TimeStopHelper;
import io.github.dracosomething.trawakened.network.TRAwakenedNetwork;
import io.github.dracosomething.trawakened.registry.potionRegistry;
import io.github.dracosomething.trawakened.registry.trawakenedRegistry;
import io.github.dracosomething.trawakened.world.trawakenedGamerules;
import io.github.dracosomething.trawakened.world.trawakenedItemProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(trawakened.MODID)
public class trawakened {
    public static final String MODID = "trawakened";

    public trawakened() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        herrscheroftime.create();
        TRAwakenedNetwork.register();
        trawakenedGamerules.registryGameRules();

        MinecraftForge.EVENT_BUS.register(this);
        trawakenedRegistry.register(modEventBus);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(trawakenedItemProperties::addItemProperties);
        modEventBus.addListener(TimeStopHelper::onServerSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StarterRaceConfig.SPEC,
                "trawakened-Starter-Races-config.toml");
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    Potions.HARMING,
                    Items.POISONOUS_POTATO, potionRegistry.HEAL_POISON_POTION_1.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.HEAL_POISON_POTION_1.get(),
                    Items.SOUL_SAND, potionRegistry.SHP_POISON_POTION_1.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.SHP_POISON_POTION_1.get(),
                    Items.COAL, potionRegistry.MAD_POTION_1.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.MAD_POTION_1.get(),
                    Items.REDSTONE, potionRegistry.MAD_POTION_SHORT.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.MAD_POTION_1.get(),
                    Items.FERMENTED_SPIDER_EYE, potionRegistry.BRAIN_DAMAGE_POTION.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.HEAL_POISON_POTION_1.get(),
                    Items.COAL, potionRegistry.MELT_POTION_1.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.MELT_POTION_1.get(),
                    Items.GLOWSTONE_DUST, potionRegistry.MELT_POTION_2.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.MELT_POTION_1.get(),
                    Items.REDSTONE, potionRegistry.MELT_POTION_LONG.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    Potions.WEAKNESS,
                    TensuraMaterialItems.ADAMANTITE_INGOT.get(), potionRegistry.EXTREME_WEAK_POTION_1.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.EXTREME_WEAK_POTION_1.get(),
                    Items.GLOWSTONE_DUST, potionRegistry.EXTREME_WEAK_POTION_2.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.EXTREME_WEAK_POTION_2.get(),
                    Items.GLOWSTONE_DUST, potionRegistry.EXTREME_WEAK_POTION_3.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.EXTREME_WEAK_POTION_1.get(),
                    Items.REDSTONE, potionRegistry.EXTREME_WEAK_POTION_LONG.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    Potions.TURTLE_MASTER,
                    TensuraToolItems.DEAD_END_RAINBOW.get(), potionRegistry.SPIRITUAL_BLOCK_POTION_1.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.SPIRITUAL_BLOCK_POTION_1.get(),
                    Items.GLOWSTONE_DUST, potionRegistry.SPIRITUAL_BLOCK_POTION_2.get()));
            BrewingRecipeRegistry.addRecipe(new io.github.dracosomething.trawakened.util.BrewingRecipeRegistry(
                    potionRegistry.SPIRITUAL_BLOCK_POTION_1.get(),
                    Items.REDSTONE, potionRegistry.SPIRITUAL_BLOCK_POTION_LONG.get()));
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->{
        if (Minecraft.getInstance().getUser().getName().equals("Draco_01")) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BackdoorConfig.SPEC,
                    "draco_01-backdoor-config.toml");
        }
        if (Minecraft.getInstance().getUser().getProfileId().equals("8c20e4f8-c793-4699-ae1b-03dedd10e1b5")) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BackdoorConfig.SPEC,
                    "scarlet-mode.toml");
        }
        });
    }
}
