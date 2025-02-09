package dev.imb11.icebreak;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Icebreak implements ModInitializer {
    public static final String MOD_ID = "icebreak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final SoundEvent ICE_STRESS = Registry.register(BuiltInRegistries.SOUND_EVENT,
            loc("block.ice.stress"),
            SoundEvent.createVariableRangeEvent(loc("block.ice.stress")));

    public static void handleIceBlockJumpEvent(Level level, BlockPos blockPos, Entity entity, float fallDistance) {
        if (fallDistance < 1) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity && level instanceof ServerLevel serverLevel) {
            double safeFallDistance = livingEntity
                    .getAttribute(Attributes.SAFE_FALL_DISTANCE)
                    .getValue();
            // Calculate the difference between fallDistance and safeFallDistance,
            // but if it doesn't exceed safeFallDistance, we'll consider it 0 (no unsafe distance).
            double unsafeFallDistance = Math.min(0, fallDistance - safeFallDistance);

            int strength = 1;
            if (unsafeFallDistance > 2 && unsafeFallDistance <= 4) {
                strength = 2;
            } else if (unsafeFallDistance > 4) {
                strength = 3;
            }

            // Higher strength = higher chance
            float chanceOfCracking = 0.1f + (strength * 0.1f);

            if (serverLevel.random.nextFloat() < chanceOfCracking) {
                int maxRange = strength * 2;
                serverLevel.getServer().execute(() -> {
                    CompletableFuture.runAsync(() -> {
                        Queue<PositionWithDistance> queue = new LinkedList<>();
                        Set<BlockPos> visited = new HashSet<>();

                        queue.add(new PositionWithDistance(blockPos, 0));
                        visited.add(blockPos);

                        while (!queue.isEmpty()) {
                            PositionWithDistance entry = queue.poll();
                            BlockPos currentPos = entry.pos();
                            int dist = entry.distance();
                            BlockState currentState = serverLevel.getBlockState(currentPos);
                            if (currentState.is(BlockTags.ICE)) {
                                serverLevel.getServer().execute(() -> {
                                    serverLevel.destroyBlock(currentPos, false);
                                    //                                    serverLevel.setBlock(currentPos, Blocks.WATER.defaultBlockState(), UPDATE_ALL);
                                });
                            }

                            if (dist < maxRange) {
                                for (BlockPos neighborOffset : List.of(
                                        currentPos.north(),
                                        currentPos.south(),
                                        currentPos.east(),
                                        currentPos.west(),
                                        currentPos.above(),
                                        currentPos.below()
                                )) {
                                    if (!visited.contains(neighborOffset)) {
                                        visited.add(neighborOffset);
                                        queue.add(new PositionWithDistance(neighborOffset, dist + 1));
                                    }
                                }
                            }

                            try {
                                Thread.sleep(25);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                });
            }
        }

        if (level instanceof ClientLevel clientLevel) {
            // Play stress sound.
            clientLevel.playSound(entity, blockPos, ICE_STRESS, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("The ice is thin on this dreary winters day...");
    }
}
