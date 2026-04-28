package com.simibubi.create.infrastructure.data;

import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.compat.archEx.ArchExCompat;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.data.DamageTypeTagGen;
import com.simibubi.create.foundation.data.recipe.CreateCompactingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateCrushingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateCuttingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateDeployingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateEmptyingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateFillingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateHauntingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateItemApplicationRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateMechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateMillingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateMixingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreatePolishingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreatePressingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateSequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateStandardRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateWashingRecipeGen;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;

import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;

public class CreateDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		ExistingFileHelper helper = ExistingFileHelper.withResourcesFromArg();
		FabricDataGenerator.Pack pack = generator.createPack();
		Create.registrate().setupDatagen(pack, helper);
		gatherData(pack, helper);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder builder) {
		GeneratedEntriesProvider.addBootstraps(builder);
	}

	public static void gatherData(FabricDataGenerator.Pack pack, ExistingFileHelper existingFileHelper) {
		addExtraRegistrateData();

		ArchExCompat.init(pack);

		pack.addProvider(AllSoundEvents::provider);
		pack.addProvider(GeneratedEntriesProvider::new);
		pack.addProvider(CreateRecipeSerializerTagsProvider::new);
		pack.addProvider(CreateContraptionTypeTagsProvider::new);
		pack.addProvider(CreateMountedItemStorageTypeTagsProvider::new);
		pack.addProvider(DamageTypeTagGen::new);
		pack.addProvider(AllAdvancements::new);
		pack.addProvider(CreateStandardRecipeGen::new);
		pack.addProvider(CreateMechanicalCraftingRecipeGen::new);
		pack.addProvider(CreateSequencedAssemblyRecipeGen::new);
		pack.addProvider(VanillaHatOffsetGenerator::new);
		pack.addProvider((FabricDataGenerator.Pack.Factory<CreateWikiBlockInfoProvider>) CreateWikiBlockInfoProvider::new);
		pack.addProvider(CreateCrushingRecipeGen::new);
		pack.addProvider(CreateMillingRecipeGen::new);
		pack.addProvider(CreateCuttingRecipeGen::new);
		pack.addProvider(CreateWashingRecipeGen::new);
		pack.addProvider(CreatePolishingRecipeGen::new);
		pack.addProvider(CreateDeployingRecipeGen::new);
		pack.addProvider(CreateMixingRecipeGen::new);
		pack.addProvider(CreateCompactingRecipeGen::new);
		pack.addProvider(CreatePressingRecipeGen::new);
		pack.addProvider(CreateFillingRecipeGen::new);
		pack.addProvider(CreateEmptyingRecipeGen::new);
		pack.addProvider(CreateHauntingRecipeGen::new);
		pack.addProvider(CreateItemApplicationRecipeGen::new);
	}

	private static void addExtraRegistrateData() {
		CreateRegistrateTags.addGenerators();

		Create.registrate().addDataGenerator(ProviderType.LANG, provider -> {
			BiConsumer<String, String> langConsumer = provider::add;

			provideDefaultLang("interface", langConsumer);
			provideDefaultLang("tooltips", langConsumer);
			AllAdvancements.provideLang(langConsumer);
			AllSoundEvents.provideLang(langConsumer);
			AllKeys.provideLang(langConsumer);
			providePonderLang(langConsumer);
			new TagLangGenerator(langConsumer).generate();
		});
	}

	private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
		String path = "assets/create/lang/default/" + fileName + ".json";
		JsonElement jsonElement = FilesHelper.loadJsonResource(path);
		if (jsonElement == null) {
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			consumer.accept(key, value);
		}
	}

	private static void providePonderLang(BiConsumer<String, String> consumer) {
		PonderIndex.addPlugin(new CreatePonderPlugin());
		PonderIndex.getLangAccess().provideLang(Create.ID, consumer);
	}
}
