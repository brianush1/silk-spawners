package com.brianush1.silkspawners.mixin;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Mixin(FallingBlockEntity.class)
public class MixinFallingBlockEntity {
    @Inject(at = @At("HEAD"), cancellable = true, method = "handleFallDamage")
    private void handleFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Boolean> info) {
        FallingBlockEntity self = (FallingBlockEntity)(Object)this;
        BlockState fallingBlock = self.getBlockState();

        int i = MathHelper.ceil(fallDistance - 1.0F);
        if (i > 0 && fallingBlock.matches(BlockTags.ANVIL) && !self.world.isClient) {
            List<Entity> list = Lists.newArrayList(self.world.getEntities(self, self.getBoundingBox()));
            Entity[] entities = new Entity[list.size()];

            int index = 0;
            for (Entity e : list) {
                if (!(e instanceof PlayerEntity)) {
                    entities[index++] = e;
                }
            }

            entities = Arrays.copyOf(entities, index);

            if (entities.length == 0) return;
            Entity entity = entities[(int)(Math.random() * entities.length)];

            Identifier id = EntityType.getId(entity.getType());

            BlockPos blockPosUnder = self.getBlockPos().add(0, -1, 0);
            BlockState blockUnder = self.world.getBlockState(blockPosUnder);
            if (blockUnder.getBlock() == Blocks.SPAWNER) {
                entity.remove();
                MobSpawnerBlockEntity spawnerEntity = (MobSpawnerBlockEntity)self.world.getBlockEntity(blockPosUnder);
                CompoundTag tag = new CompoundTag();
                spawnerEntity.toTag(tag);

                CompoundTag spawnData = new CompoundTag();
                spawnData.putString("id", id.toString());
                tag.put("SpawnData", spawnData);

                ListTag spawnPotentials = new ListTag();
                CompoundTag spawn = new CompoundTag();
                CompoundTag entityTag = new CompoundTag();
                entityTag.putString("id", id.toString());
                spawn.put("Entity", entityTag);
                spawn.putInt("Weight", 1);
                spawnPotentials.add(spawn);
                tag.put("SpawnPotentials", spawnPotentials);

                spawnerEntity.fromTag(tag);
                info.setReturnValue(false);
                info.cancel();
            }
        }
    }
}
