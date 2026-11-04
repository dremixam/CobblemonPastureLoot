package com.dremixam.pastureloot.minecraft.mixin;

import com.cobblemon.mod.common.block.PastureBlock;
import com.dremixam.pastureloot.minecraft.PastureTicker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Wraps the ticker of the Cobblemon pasture block so that PastureTicker runs after it. */
@Mixin(PastureBlock.class)
public abstract class PastureBlockMixin {

    @Inject(method = "getTicker", at = @At("RETURN"), cancellable = true)
    private <T extends BlockEntity> void pastureloot$wrapTicker(
            Level level, BlockState state, BlockEntityType<T> type,
            CallbackInfoReturnable<BlockEntityTicker<T>> callback) {
        BlockEntityTicker<T> original = callback.getReturnValue();
        if (original == null || level.isClientSide()) {
            return;
        }
        callback.setReturnValue((tickLevel, pos, tickState, blockEntity) -> {
            original.tick(tickLevel, pos, tickState, blockEntity);
            PastureTicker.tick(tickLevel, pos, blockEntity);
        });
    }
}
