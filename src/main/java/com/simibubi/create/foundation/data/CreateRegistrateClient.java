package com.simibubi.create.foundation.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class CreateRegistrateClient {

	public static <T extends Block> void registerCasingConnectivity(T entry,
																	BiConsumer<T, CasingConnectivity> consumer) {
		consumer.accept(entry, CreateClient.CASING_CONNECTIVITY);
	}

	public static void registerBlockModel(Block entry, String modelClassName, @Nullable String factoryMethod) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), modelFactory(modelClassName, factoryMethod));
	}

	public static void registerItemModel(Item entry, String modelClassName, @Nullable String factoryMethod) {
		CreateClient.MODEL_SWAPPER.getCustomItemModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), modelFactory(modelClassName, factoryMethod));
	}

	public static void registerCTBehaviour(Block entry, Supplier<?> behaviorSupplier) {
		ConnectedTextureBehaviour behavior = (ConnectedTextureBehaviour) behaviorSupplier.get();
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), model -> new CTModel(model, behavior));
	}

	public static <T extends Item, P> void customRenderedItem(ItemBuilder<T, P> b, String rendererClassName) {
		b.onRegister(new CustomRendererRegistrationHelper(rendererClassName));
	}

	private static NonNullFunction<BakedModel, ? extends BakedModel> modelFactory(String modelClassName,
																				 @Nullable String factoryMethod) {
		try {
			Class<?> modelClass = Class.forName(modelClassName);
			if (factoryMethod != null) {
				Method method = modelClass.getDeclaredMethod(factoryMethod, BakedModel.class);
				method.setAccessible(true);
				return model -> invokeFactory(method, model);
			}

			Constructor<?> constructor = modelClass.getDeclaredConstructor(BakedModel.class);
			constructor.setAccessible(true);
			return model -> invokeConstructor(constructor, model);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to create model factory for " + modelClassName, e);
		}
	}

	private static BakedModel invokeFactory(Method method, BakedModel model) {
		try {
			return (BakedModel) method.invoke(null, model);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to invoke model factory " + method, e);
		}
	}

	private static BakedModel invokeConstructor(Constructor<?> constructor, BakedModel model) {
		try {
			return (BakedModel) constructor.newInstance(model);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to invoke model constructor " + constructor, e);
		}
	}

	private record CustomRendererRegistrationHelper(String rendererClassName) implements NonNullConsumer<Item> {
		@Override
		public void accept(Item entry) {
			try {
				Constructor<?> constructor = Class.forName(rendererClassName)
					.getDeclaredConstructor();
				constructor.setAccessible(true);
				CustomRenderedItemModelRenderer renderer = (CustomRenderedItemModelRenderer) constructor.newInstance();
				BuiltinItemRendererRegistry.INSTANCE.register(entry, renderer);
				CustomRenderedItems.register(entry);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Unable to register custom item renderer " + rendererClassName, e);
			}
		}
	}
}
