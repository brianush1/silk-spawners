package com.brianush1.silkspawners.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(at = @At("HEAD"), method = "onBreak")
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        ItemStack handStack = player.getMainHandStack();
        if (state.getBlock() == Blocks.SPAWNER && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, handStack) > 0) {
            MobSpawnerBlockEntity entity = (MobSpawnerBlockEntity)world.getBlockEntity(pos);
            ItemStack stack = new ItemStack(Blocks.SPAWNER);
            stack.setTag(new CompoundTag());
            CompoundTag tag = stack.getTag();

            CompoundTag data = new CompoundTag();
            entity.toTag(data);
            tag.put("spawnerData", data);

            String title = data.getCompound("SpawnData").getString("id");
            CompoundTag display = new CompoundTag();
            ListTag lore = new ListTag();
            lore.add(StringTag.of("{\"text\":" + StringTag.escape(title) + ",\"italic\":false,\"color\":\"purple\"}"));
            display.put("Lore", lore);
            tag.put("display", display);

            if (!world.isClient) {
                Block.dropStack(world, pos, stack);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onPlaced")
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo info) {
        if (state.getBlock() == Blocks.SPAWNER) {
            MobSpawnerBlockEntity entity = (MobSpawnerBlockEntity)world.getBlockEntity(pos);
            CompoundTag tag = stack.getTag();
            if (tag == null) return;
            if (!tag.contains("spawnerData")) return;
            CompoundTag spawnerData = tag.getCompound("spawnerData");
            entity.fromTag(spawnerData);
            entity.setPos(pos);
            world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }
}
