package dev.imb11.icebreak.config;

import com.google.gson.GsonBuilder;
import dev.imb11.icebreak.Icebreak;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.ValueFormatters;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IcebreakConfig {
    public static ConfigClassHandler<IcebreakConfig> CONFIG_CLASS_HANDLER = ConfigClassHandler
            .createBuilder(IcebreakConfig.class)
            .id(ResourceLocation.tryBuild("icebreak", "config"))
            .serializer(config -> GsonConfigSerializerBuilder
                    .create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("icebreak.config.json"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting).build())
            .build();

    @SerialEntry
    public float initialCrackChance = 0.05f;
    @SerialEntry
    public float fallDistanceMultiplier = 0.04f;
    @SerialEntry
    public int holeRadius = 2;
    @SerialEntry
    public int crackExpansionAmount = 2;
    @SerialEntry
    public int blockBreakDelay = 5;

    public static IcebreakConfig get() {
        return CONFIG_CLASS_HANDLER.instance();
    }

    public static void load() {
        CONFIG_CLASS_HANDLER.load();
    }

    public static YetAnotherConfigLib getInstance() {
        return YetAnotherConfigLib.create(CONFIG_CLASS_HANDLER, (IcebreakConfig defaults, IcebreakConfig config, YetAnotherConfigLib.Builder builder) -> {
            var initialCrackChanceOpt = Option.<Float>createBuilder()
                    .name(Component.translatable("config.icebreak.initialCrackChance"))
                    .description(OptionDescription.of(Component.translatable("config.icebreak.initialCrackChance.description")))
                    .binding(defaults.initialCrackChance, () -> config.initialCrackChance, (val) -> config.initialCrackChance = val)
                    .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.05f).formatValue(ValueFormatters.percent(0)))
                    .build();

            var fallDistanceMultiplierOpt = Option.<Float>createBuilder()
                    .name(Component.translatable("config.icebreak.fallDistanceMultiplier"))
                    .description(OptionDescription.of(Component.translatable("config.icebreak.fallDistanceMultiplier.description")))
                    .binding(defaults.fallDistanceMultiplier, () -> config.fallDistanceMultiplier, (val) -> config.fallDistanceMultiplier = val)
                    .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.01f).formatValue(ValueFormatters.percent(0)))
                    .build();

            var holeRadiusOpt = Option.<Integer>createBuilder()
                    .name(Component.translatable("config.icebreak.holeRadius"))
                    .description(OptionDescription.of(Component.translatable("config.icebreak.holeRadius.description")))
                    .binding(defaults.holeRadius, () -> config.holeRadius, (val) -> config.holeRadius = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 10).step(1))
                    .build();

            var crackExpansionAmountOpt = Option.<Integer>createBuilder()
                    .name(Component.translatable("config.icebreak.crackExpansionAmount"))
                    .description(OptionDescription.of(Component.translatable("config.icebreak.crackExpansionAmount.description")))
                    .binding(defaults.crackExpansionAmount, () -> config.crackExpansionAmount, (val) -> config.crackExpansionAmount = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 10).step(1))
                    .build();

            var blockBreakDelayOpt = Option.<Integer>createBuilder()
                    .name(Component.translatable("config.icebreak.blockBreakDelay"))
                    .description(OptionDescription.of(Component.translatable("config.icebreak.blockBreakDelay.description")))
                    .binding(defaults.blockBreakDelay, () -> config.blockBreakDelay, (val) -> config.blockBreakDelay = val)
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 250).step(1).formatValue(integer -> Component.literal(integer + "ms")))
                    .build();

            return builder.title(Component.translatable("config.icebreak.title"))
                    .category(ConfigCategory.createBuilder()
                            .name(Component.translatable("config.icebreak.title"))
                            .option(initialCrackChanceOpt)
                            .option(fallDistanceMultiplierOpt)
                            .option(holeRadiusOpt)
                            .option(crackExpansionAmountOpt)
                            .option(blockBreakDelayOpt)
                            .build());
        });
    }
}
