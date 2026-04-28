package com.simibubi.create.foundation.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;
import com.simibubi.create.infrastructure.fabric.HelmetOverlay;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import dev.engine_room.flywheel.api.visual.EntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

	public static void registerHelmetOverlay(Item item, String overlayClassName) {
		try {
			Constructor<?> constructor = Class.forName(overlayClassName)
				.getDeclaredConstructor();
			constructor.setAccessible(true);
			HelmetOverlay overlay = (HelmetOverlay) constructor.newInstance();
			HelmetOverlay.REGISTRY.register(item, overlay);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to register helmet overlay " + overlayClassName, e);
		}
	}

	public static Predicate<Item> makeClient3dItemPredicate() {
		return item -> {
			ItemRenderer itemRenderer = Minecraft.getInstance()
				.getItemRenderer();
			BakedModel model = itemRenderer.getModel(new ItemStack(item), null, null, 0);
			return model.isGui3d();
		};
	}

	public static <T extends Entity> NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>
	entityRenderer(String rendererClassName) {
		try {
			Constructor<?> constructor = Class.forName(rendererClassName)
				.getDeclaredConstructor(EntityRendererProvider.Context.class);
			constructor.setAccessible(true);
			return context -> invokeEntityRendererConstructor(constructor, context);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to create entity renderer factory for " + rendererClassName, e);
		}
	}

	public static <T extends Entity> SimpleEntityVisualizer.Factory<T> entityVisual(String visualClassName) {
		try {
			Constructor<?> constructor = findEntityVisualConstructor(Class.forName(visualClassName));
			constructor.setAccessible(true);
			return (context, entity, partialTick) ->
				invokeEntityVisualConstructor(constructor, context, entity, partialTick);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to create entity visual factory for " + visualClassName, e);
		}
	}

	private static Constructor<?> findEntityVisualConstructor(Class<?> visualClass) throws NoSuchMethodException {
		for (Constructor<?> constructor : visualClass.getDeclaredConstructors()) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length != 3)
				continue;
			if (!VisualizationContext.class.isAssignableFrom(parameterTypes[0]))
				continue;
			if (!Entity.class.isAssignableFrom(parameterTypes[1]))
				continue;
			if (parameterTypes[2] != float.class)
				continue;
			return constructor;
		}
		throw new NoSuchMethodException(visualClass.getName()
			+ "(VisualizationContext, Entity, float)");
	}

	@SuppressWarnings("unchecked")
	private static <T extends Entity> EntityRenderer<? super T> invokeEntityRendererConstructor(
		Constructor<?> constructor, EntityRendererProvider.Context context) {
		try {
			return (EntityRenderer<? super T>) constructor.newInstance(context);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to invoke entity renderer constructor " + constructor, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Entity> EntityVisual<? super T> invokeEntityVisualConstructor(Constructor<?> constructor,
		VisualizationContext context, T entity, float partialTick) {
		try {
			return (EntityVisual<? super T>) constructor.newInstance(context, entity, partialTick);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to invoke entity visual constructor " + constructor, e);
		}
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
