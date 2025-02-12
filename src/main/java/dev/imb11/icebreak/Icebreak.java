package dev.imb11.icebreak;

import dev.imb11.icebreak.config.IcebreakConfig;
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
import net.minecraft.world.level.Level;
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
        var config = IcebreakConfig.get();

        if (fallDistance < 1) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity && level instanceof ServerLevel serverLevel) {
            int strength = 1;
            if (fallDistance > 1 && fallDistance <= 2) {
                strength = 2;
            } else if (fallDistance > 2 && fallDistance <= 3) {
                strength = 3;
            } else if (fallDistance > 3) {
                strength = 4;
            }

            // Higher strength = higher chance
            float chanceOfCracking = config.initialCrackChance + (fallDistance * config.fallDistanceMultiplier);
            if (serverLevel.random.nextFloat() < chanceOfCracking) {
                int initialRadius = config.holeRadius;
                int lightningLength = strength + config.crackExpansionAmount;

                serverLevel.getServer().execute(() -> {
                    CompletableFuture.runAsync(() -> {
                        Set<BlockPos> initialCracked = new HashSet<>();
                        Queue<PositionWithDistance> queue = new LinkedList<>();
                        queue.add(new PositionWithDistance(blockPos, 0));
                        initialCracked.add(blockPos);

                        while (!queue.isEmpty()) {
                            PositionWithDistance entry = queue.poll();
                            BlockPos currentPos = entry.pos();
                            int dist = entry.distance();

                            if (serverLevel.getBlockState(currentPos).is(BlockTags.ICE)) {
                                serverLevel.destroyBlock(currentPos, false);
                                try {
                                    Thread.sleep(config.blockBreakDelay); // Slow down the cracking effect
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            if (dist < initialRadius) {
                                for (BlockPos neighbor : List.of(
                                        currentPos.north(), currentPos.south(), currentPos.east(), currentPos.west(), currentPos.above(), currentPos.below())) {
                                    if (!initialCracked.contains(neighbor) && serverLevel.getBlockState(neighbor).is(BlockTags.ICE)) { // Check if it's ice!
                                        initialCracked.add(neighbor);
                                        queue.add(new PositionWithDistance(neighbor, dist + 1));
                                    }
                                }
                            }
                        }

                        Set<BlockPos> allCracked = new HashSet<>(initialCracked);
                        for (BlockPos startPos : initialCracked) {
                            generateLightningBranch(config, serverLevel, startPos, lightningLength, allCracked);
                        }
                    });
                });
            }
        }

        if (entity instanceof LivingEntity livingEntity && level instanceof ClientLevel clientLevel) {
            clientLevel.playSound(livingEntity, blockPos, ICE_STRESS, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    private static void generateLightningBranch(IcebreakConfig config, ServerLevel serverLevel, BlockPos startPos, int length, Set<BlockPos> allCracked) {
        BlockPos currentPos = startPos;
        int currentLength = 0;

        while (currentLength < length) {
            List<BlockPos> possibleDirections = getValidDirections(serverLevel, currentPos, allCracked); // Get valid directions (ice, not already cracked)
            if (possibleDirections.isEmpty()) {
                break; // Dead end
            }

            BlockPos nextPos = possibleDirections.get(serverLevel.random.nextInt(possibleDirections.size())); // Random direction

            if(serverLevel.getBlockState(nextPos).is(BlockTags.ICE)){ // Double check to be safe
                serverLevel.destroyBlock(nextPos, false);
                allCracked.add(nextPos);
                try {
                    Thread.sleep(config.blockBreakDelay); // Slow down the cracking effect
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            currentPos = nextPos;
            currentLength++;
        }
    }


    private static List<BlockPos> getValidDirections(ServerLevel serverLevel, BlockPos currentPos, Set<BlockPos> allCracked) {
        List<BlockPos> validDirections = new ArrayList<>();
        for (BlockPos neighbor : List.of(
                currentPos.north(), currentPos.south(), currentPos.east(), currentPos.west(), currentPos.above(), currentPos.below())) {
            if (serverLevel.getBlockState(neighbor).is(BlockTags.ICE) && !allCracked.contains(neighbor)) {
                validDirections.add(neighbor);
            }
        }
        return validDirections;
    }


    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("The ice is thin on this dreary winters day...");
    }
}
