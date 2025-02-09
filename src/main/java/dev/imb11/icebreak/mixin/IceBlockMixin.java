package dev.imb11.icebreak.mixin;

import dev.imb11.icebreak.Icebreak;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IceBlock.class)
public abstract class IceBlockMixin extends HalfTransparentBlock {
    public IceBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float fallDistance) {
        super.fallOn(level, blockState, blockPos, entity, fallDistance);

        Icebreak.handleIceBlockJumpEvent(level, blockPos, entity, fallDistance);
    }

}
