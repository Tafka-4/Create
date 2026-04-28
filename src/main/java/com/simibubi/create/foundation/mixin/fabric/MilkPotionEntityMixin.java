package com.simibubi.create.foundation.mixin.fabric;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.tropheusj.milk.Milk;
import io.github.tropheusj.milk.potion.MilkAreaEffectCloudEntity;
import io.github.tropheusj.milk.potion.bottle.LingeringMilkBottle;
import io.github.tropheusj.milk.potion.bottle.PotionItemEntityExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Mixin(ThrownPotion.class)
public abstract class MilkPotionEntityMixin extends ThrowableItemProjectile implements PotionItemEntityExtensions {
	@Unique
	private boolean create$milk;

	protected MilkPotionEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	private void applyWater() {
		throw new AssertionError();
	}

	@Shadow
	private void dowseFire(BlockPos pos) {
		throw new AssertionError();
	}

	@Inject(method = "onHitBlock", at = @At("TAIL"))
	private void create$onMilkHitBlock(BlockHitResult result, CallbackInfo ci) {
		if (!isMilk())
			return;

		Direction side = result.getDirection();
		BlockPos pos = result.getBlockPos()
			.relative(side);
		dowseFire(pos);
		dowseFire(pos.relative(side.getOpposite()));

		for (Direction direction : Direction.Plane.HORIZONTAL)
			dowseFire(pos.relative(direction));
	}

	@Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
	private void create$onMilkHit(HitResult result, CallbackInfo ci) {
		if (!isMilk())
			return;

		super.onHit(result);
		if (!level().isClientSide) {
			applyWater();

			ItemStack stack = getItem();
			if (stack.getItem() instanceof LingeringMilkBottle) {
				create$makeMilkAreaOfEffectCloud();
			} else {
				create$applySplashMilkEffects();
			}

			level().levelEvent(2002, blockPosition(), 0xFFFFFF);
			discard();
		}

		ci.cancel();
	}

	@Unique
	private void create$applySplashMilkEffects() {
		AABB box = getBoundingBox().inflate(4.0, 2.0, 4.0);
		List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, box);
		for (LivingEntity entity : entities) {
			if (entity.isAffectedByPotions() && distanceToSqr(entity) < 16.0)
				Milk.tryRemoveRandomEffect(entity);
		}
	}

	@Unique
	private void create$makeMilkAreaOfEffectCloud() {
		MilkAreaEffectCloudEntity cloud = new MilkAreaEffectCloudEntity(level(), getX(), getY(), getZ());
		Entity owner = getOwner();
		if (owner instanceof LivingEntity livingOwner)
			cloud.setOwner(livingOwner);

		cloud.setRadius(3.0F);
		cloud.setRadiusOnUse(-0.5F);
		cloud.setWaitTime(10);
		cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());

		level().addFreshEntity(cloud);
	}

	@Override
	public boolean isMilk() {
		return create$milk;
	}

	@Override
	public void setMilk(boolean value) {
		create$milk = value;
	}
}
