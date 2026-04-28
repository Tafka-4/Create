package com.simibubi.create.compat.recipeViewerCommon;

import java.util.function.Supplier;

public record SequencedAssemblySubCategoryType(Supplier<Supplier<?>> emiFactory,
											   Supplier<Supplier<?>> jeiFactory,
											   Supplier<Supplier<?>> reiFactory) {

	public static final SequencedAssemblySubCategoryType PRESSING = new SequencedAssemblySubCategoryType(
		type("com.simibubi.create.compat.emi.EmiSequencedAssemblySubCategory$AssemblyPressing"),
		type("com.simibubi.create.compat.jei.category.sequencedAssembly.JeiSequencedAssemblySubCategory$AssemblyPressing"),
		type("com.simibubi.create.compat.rei.category.sequencedAssembly.ReiSequencedAssemblySubCategory$AssemblyPressing")
	);
	public static final SequencedAssemblySubCategoryType SPOUTING = new SequencedAssemblySubCategoryType(
		type("com.simibubi.create.compat.emi.EmiSequencedAssemblySubCategory$AssemblySpouting"),
		type("com.simibubi.create.compat.jei.category.sequencedAssembly.JeiSequencedAssemblySubCategory$AssemblySpouting"),
		type("com.simibubi.create.compat.rei.category.sequencedAssembly.ReiSequencedAssemblySubCategory$AssemblySpouting")
	);
	public static final SequencedAssemblySubCategoryType DEPLOYING = new SequencedAssemblySubCategoryType(
		type("com.simibubi.create.compat.emi.EmiSequencedAssemblySubCategory$AssemblyDeploying"),
		type("com.simibubi.create.compat.jei.category.sequencedAssembly.JeiSequencedAssemblySubCategory$AssemblyDeploying"),
		type("com.simibubi.create.compat.rei.category.sequencedAssembly.ReiSequencedAssemblySubCategory$AssemblyDeploying")
	);
	public static final SequencedAssemblySubCategoryType CUTTING = new SequencedAssemblySubCategoryType(
		type("com.simibubi.create.compat.emi.EmiSequencedAssemblySubCategory$AssemblyCutting"),
		type("com.simibubi.create.compat.jei.category.sequencedAssembly.JeiSequencedAssemblySubCategory$AssemblyCutting"),
		type("com.simibubi.create.compat.rei.category.sequencedAssembly.ReiSequencedAssemblySubCategory$AssemblyCutting")
	);

	@SuppressWarnings("unchecked")
	public <T> Supplier<Supplier<T>> emi() {
		return (Supplier<Supplier<T>>) (Supplier<?>) emiFactory;
	}

	@SuppressWarnings("unchecked")
	public <T> Supplier<Supplier<T>> jei() {
		return (Supplier<Supplier<T>>) (Supplier<?>) jeiFactory;
	}

	@SuppressWarnings("unchecked")
	public <T> Supplier<Supplier<T>> rei() {
		return (Supplier<Supplier<T>>) (Supplier<?>) reiFactory;
	}

	private static Supplier<Supplier<?>> type(String className) {
		return () -> () -> {
			try {
				return Class.forName(className)
					.getDeclaredConstructor()
					.newInstance();
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Could not create recipe viewer sub-category " + className, e);
			}
		};
	}
}
