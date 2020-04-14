package com.brianush1.silkspawners.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnerBlock.class)
public class MixinSpawnerBlock {
	@Inject(at = @At("HEAD"), cancellable = true, method = "onStacksDropped")
	private void onStacksDropped(BlockState state, World world, BlockPos pos, ItemStack handStack, CallbackInfo info) {
		if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, handStack) > 0) {
			info.cancel();
		}
	}
}
