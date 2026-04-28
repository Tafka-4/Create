import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// versions
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "2024.11.17"
// https://fabricmc.net/develop/
val minecraftVersion = "1.21.1"
val loaderVersion = "0.18.5"
val fapiVersion = "0.116.9+1.21.1"

// in-house dependencies
val flywheelVersion = "1.0.6"
val ponderVersion = "1.0.69"
val registrateVersion = "1.3.77-MC1.21.1"
val milkLibVersion = "1.2.60"
val portingLibVersion = "3.1.0-beta.85+1.21.1"
val portingLibLegacyVersion = "3.1.0-beta.54+1.21.1"
val portingLibLazyRegistrationVersion = "3.1.0-beta.39+1.21.1"

// external dependencies
val configApiVersion = "21.1.6"
val nightConfigVersion =  "3.6.3"
val jsr305Version = "3.0.2"

// compat
// https://modrinth.com/mod/cc-tweaked/versions
val ccVersion = "1.118.0"
// for CC - https://modrinth.com/mod/cloth-config/versions
val clothVersion = "15.0.140+fabric"
// https://modrinth.com/mod/jei/versions
val jeiVersion = "19.27.0.340"
// https://modrinth.com/mod/rei/versions
val reiVersion = "16.0.799"
// https://modrinth.com/mod/emi/versions
val emiVersion = "1.1.20+1.21.1"
// https://modrinth.com/mod/botania
val botaniaVersion = "1.19.2-436-FABRIC"
// https://modrinth.com/mod/modmenu/versions
val modmenuVersion = "11.0.4"
// https://modrinth.com/mod/sandwichable/versions
val sandwichableVersion = "1.3.1+1.20.1"
// https://modrinth.com/mod/sodium
val sodiumVersion = "mc1.21.1-0.6.9-fabric"
// https://github.com/emilyploszaj/trinkets/releases/
val trinketsVersion = "3.10.0"
// for Trinkets - https://modrinth.com/mod/cardinal-components-api/versions
val ccaVersion = "6.1.3"
// https://modrinth.com/mod/journeymap
val jmVersion = "1.21.1-6.0.0-beta.39+fabric"
// check the jm jar, it's JiJ
val jmApiVersion = "1.20-1.9-SNAPSHOT"

// dev stuff
val ccRuntime = false
val recipeViewer = "jei" // jei, rei, or emi

plugins {
    id("fabric-loom") version "1.16.1"
    id("maven-publish")
}

val buildChannel = providers.environmentVariable("CREATE_BUILD_CHANNEL")
    .filter(String::isNotEmpty)
    .orElse("beta")
    .get()
val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .filter(String::isNotEmpty)
    .map { "-$buildChannel.$it" }
    .orElse("-$buildChannel")
    .getOrElse("")

version = "6.0.11+mc$minecraftVersion$buildNum"

group = "com.simibubi.create"
base.archivesName = "create-fabric"

repositories {
    maven("https://maven.parchmentmc.org") // Parchment
    maven("https://maven.fabricmc.net") // FAPI, Loader
    maven("https://maven.createmod.net") // Ponder, Flywheel
    maven("https://mvn.devos.one/snapshots") // Registrate, Forge Tags, Milk Lib
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven") // Forge Config API Port
    maven("https://maven.shedaniel.me") // REI and deps
    maven("https://api.modrinth.com/maven") { // LazyDFU, Sodium, Sandwichable
        content { includeGroupAndSubgroups("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com") // Mod Menu, Trinkets
    maven("https://maven.squiddev.cc") // CC:T
    maven("https://modmaven.dev") // Botania
    maven("https://maven.jamieswhiteshirt.com/libs-release") { // Reach Entity Attributes
        content { includeGroup("com.jamieswhiteshirt") }
    }
    maven("https://maven.ladysnake.org/releases") // CCA, for Trinkets
    maven("https://maven.ftb.dev/releases") // FTB
    maven("https://maven.architectury.dev") // Architectury API
    maven("https://jm.gserv.me/repository/maven-public/") // Journey map
}

configurations.configureEach {
    resolutionStrategy.force(
        "mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion",
        "mezz.jei:jei-$minecraftVersion-fabric-api:$jeiVersion"
    )
}

val ponder = file("Ponder")

dependencies {
    // setup
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    // dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

    modApi(include("com.tterrag.registrate_fabric:Registrate:$registrateVersion")!!)

    fun portingLib(module: String, version: String = portingLibVersion) {
        modApi(include("io.github.fabricators_of_create.Porting-Lib:$module:$version")!!)
    }

    listOf(
        "base",
        "blocks",
        "brewing",
        "client_events",
        "common",
        "config",
        "core",
        "data",
        "entity",
        "fluids",
        "gametest",
        "gui_utils",
        "item_abilities",
        "items",
        "level_events",
        "loot",
        "mixin_extensions",
        "model_loader",
        "models",
        "obj_loader",
        "registry",
        "render_types",
        "resources",
        "tags",
        "transfer"
    ).forEach(::portingLib)
    portingLib("lazy_registration", portingLibLazyRegistrationVersion)
    portingLib("extensions", portingLibLegacyVersion)

    modApi(include("com.electronwill.night-config:core:$nightConfigVersion")!!)
    modApi(include("com.electronwill.night-config:toml:$nightConfigVersion")!!)
    modApi(include("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$configApiVersion")!!)
    modApi(include("dev.engine-room.flywheel:flywheel-fabric-$minecraftVersion:$flywheelVersion")!!)
    modApi(include("io.github.tropheusj:milk-lib:$milkLibVersion")!!)
    api(include("com.google.code.findbugs:jsr305:$jsr305Version")!!)

    if (ponder.exists()) {
        implementation("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion") { isTransitive = false }
        implementation("net.createmod.ponder:Ponder-Common-$minecraftVersion:$ponderVersion")
    } else {
        modRuntimeOnly(include("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion")!!)
        modCompileOnly("net.createmod.ponder:Ponder-Fabric-$minecraftVersion:$ponderVersion") {
            exclude(group = "io.github.fabricators_of_create.Porting-Lib")
        }
    }

    // compat
    modCompileOnly("cc.tweaked:cc-tweaked-$minecraftVersion-fabric-api:$ccVersion")

    modCompileOnly("vazkii.botania:Botania:$botaniaVersion") { isTransitive = false }
    modCompileOnly("com.terraformersmc:modmenu:$modmenuVersion")
    modCompileOnly("maven.modrinth:sandwichable:$sandwichableVersion")
    modCompileOnly("maven.modrinth:sodium:$sodiumVersion")

    modCompileOnly("dev.emi:trinkets:$trinketsVersion")
    // for Trinkets
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$ccaVersion")
    modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$ccaVersion")

    // FIXME - Use gradle.properties for these versions, make change to concealed for this
    modCompileOnly("dev.architectury:architectury-fabric:13.0.8")
    modCompileOnly("dev.ftb.mods:ftb-chunks-fabric:2101.1.14")
    modCompileOnly("dev.ftb.mods:ftb-teams-fabric:2101.1.10")
    modCompileOnly("dev.ftb.mods:ftb-library-fabric:2101.1.31")

    modCompileOnly("maven.modrinth:journeymap:$jmVersion")
    modCompileOnly("info.journeymap:journeymap-api:$jmApiVersion")

    // EMI
    modCompileOnly("dev.emi:emi-fabric:$emiVersion:api") { isTransitive = false }
    // JEI
    modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion") { isTransitive = false }
    // REI
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:$reiVersion")

    when (recipeViewer) {
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion")
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
        "emi" -> modLocalRuntime("dev.emi:emi-fabric:$emiVersion")
    }

    // dev env
    modLocalRuntime("com.terraformersmc:modmenu:$modmenuVersion")
    modLocalRuntime("dev.emi:trinkets:$trinketsVersion") { isTransitive = false }
    // for Trinkets
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$ccaVersion")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$ccaVersion")
    if (ccRuntime) {
        modLocalRuntime("cc.tweaked:cc-tweaked-$minecraftVersion-fabric:$ccVersion")
        modLocalRuntime("maven.modrinth:cloth-config:$clothVersion")
    }
    // have deprecated modules present at runtime only
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api-deprecated:$fapiVersion")
}

sourceSets.named("main") {
    java {
        if (recipeViewer != "jei") {
            exclude("com/simibubi/create/compat/jei/**")
        }
        if (recipeViewer != "rei") {
            exclude("com/simibubi/create/compat/rei/**")
        }
        if (recipeViewer != "emi") {
            exclude("com/simibubi/create/compat/emi/**")
        }
        exclude("com/simibubi/create/foundation/mixin/compat/xaeros/**")
        exclude("com/simibubi/create/foundation/mixin/accessor/ItemStackHandlerAccessor.java")
        exclude("com/simibubi/create/foundation/mixin/datafixer/ItemStackComponentizationFixMixin.java")
        exclude("com/simibubi/create/foundation/utility/SameSizeCombinedInvWrapper.java")
        exclude("com/simibubi/create/impl/contraption/storage/FallbackMountedStorageType.java")
        exclude("com/simibubi/create/foundation/data/recipe/CompactingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/CrushingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/CuttingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/DeployingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/EmptyingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/FillingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/HauntingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/ItemApplicationRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/MechanicalCraftingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/MillingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/MixingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/PolishingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/PressingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/ProcessingRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/SequencedAssemblyRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/StandardRecipeGen.java")
        exclude("com/simibubi/create/foundation/data/recipe/WashingRecipeGen.java")
    }
    resources {
        srcDir("src/generated/resources")
        exclude(".cache/")
    }
}

fun stripNestedJars(jarFile: File) {
    if (!jarFile.isFile)
        return

    val tempFile = jarFile.resolveSibling("${jarFile.name}.tmp")

    ZipInputStream(FileInputStream(jarFile)).use { input ->
        ZipOutputStream(FileOutputStream(tempFile)).use { output ->
            while (true) {
                val entry = input.nextEntry ?: break
                val name = entry.name

                if (name == "META-INF/jars/" || name.startsWith("META-INF/jars/")) {
                    input.closeEntry()
                    continue
                }

                var bytes = input.readBytes()
                if (name == "fabric.mod.json") {
                    val json = bytes.toString(Charsets.UTF_8)
                        .replace(Regex("""(?s),\s*"jars"\s*:\s*\[[^\]]*]\s*(?=\})"""), "")
                    bytes = json.toByteArray(Charsets.UTF_8)
                }

                val newEntry = ZipEntry(name)
                output.putNextEntry(newEntry)
                output.write(bytes)
                output.closeEntry()
                input.closeEntry()
            }
        }
    }

    tempFile.copyTo(jarFile, overwrite = true)
	tempFile.delete()
}

fun removeIncompatibleMilkMixins(jarFile: File) {
	if (!jarFile.isFile)
		return

	val tempFile = jarFile.resolveSibling("${jarFile.name}.tmp")
	val incompatibleMixins = listOf(
		"PotionEntityMixin",
		"BrewingRecipeRegistryAccessor",
		"BrewingRecipeRegistryMixin"
	)

	ZipInputStream(FileInputStream(jarFile)).use { input ->
		ZipOutputStream(FileOutputStream(tempFile)).use { output ->
			while (true) {
				val entry = input.nextEntry ?: break
				val name = entry.name
				var bytes = input.readBytes()

				if (name == "milk.mixins.json") {
					var json = bytes.toString(Charsets.UTF_8)
					incompatibleMixins.forEach { mixin ->
						json = json
							.replace(Regex("""(?s)\s*"$mixin"\s*,\s*"""), "")
							.replace(Regex("""(?s),\s*"$mixin"\s*"""), "")
					}
					bytes = json.toByteArray(Charsets.UTF_8)
				}

				val newEntry = ZipEntry(name)
				output.putNextEntry(newEntry)
				output.write(bytes)
				output.closeEntry()
				input.closeEntry()
			}
		}
	}

	tempFile.copyTo(jarFile, overwrite = true)
	tempFile.delete()
}

fun removeIncompatiblePortingLibExtensionsMixins(jarFile: File) {
	if (!jarFile.isFile)
		return

	val tempFile = jarFile.resolveSibling("${jarFile.name}.tmp")
	val incompatibleMixins = listOf(
		"common.BlockMixin",
		"common.TrunkPlacerMixin"
	)

	ZipInputStream(FileInputStream(jarFile)).use { input ->
		ZipOutputStream(FileOutputStream(tempFile)).use { output ->
			while (true) {
				val entry = input.nextEntry ?: break
				val name = entry.name
				var bytes = input.readBytes()

				if (name == "porting_lib_extensions.mixins.json") {
					var json = bytes.toString(Charsets.UTF_8)
					incompatibleMixins.forEach { mixin ->
						json = json
							.replace(Regex("""(?s)\s*"$mixin"\s*,\s*"""), "")
							.replace(Regex("""(?s),\s*"$mixin"\s*"""), "")
					}
					bytes = json.toByteArray(Charsets.UTF_8)
				}

				val newEntry = ZipEntry(name)
				output.putNextEntry(newEntry)
				output.write(bytes)
				output.closeEntry()
				input.closeEntry()
			}
		}
	}

	tempFile.copyTo(jarFile, overwrite = true)
	tempFile.delete()
}

fun makeFlywheelAvailableOnServer(jarFile: File) {
	if (!jarFile.isFile)
		return

	val tempFile = jarFile.resolveSibling("${jarFile.name}.tmp")

	ZipInputStream(FileInputStream(jarFile)).use { input ->
		ZipOutputStream(FileOutputStream(tempFile)).use { output ->
			while (true) {
				val entry = input.nextEntry ?: break
				val name = entry.name
				var bytes = input.readBytes()

				if (name == "fabric.mod.json") {
					@Suppress("UNCHECKED_CAST")
					val json = JsonSlurper().parseText(bytes.toString(Charsets.UTF_8)) as MutableMap<String, Any?>
					json["environment"] = "*"
					bytes = (JsonOutput.prettyPrint(JsonOutput.toJson(json)) + "\n").toByteArray(Charsets.UTF_8)
				}

				val newEntry = ZipEntry(name)
				output.putNextEntry(newEntry)
				output.write(bytes)
				output.closeEntry()
				input.closeEntry()
			}
		}
	}

	tempFile.copyTo(jarFile, overwrite = true)
	tempFile.delete()
}

fun replaceJarEntry(jarFile: File, entryName: String, replacement: File) {
	if (!jarFile.isFile || !replacement.isFile)
		return

	val replacementBytes = replacement.readBytes()
	val tempFile = jarFile.resolveSibling("${jarFile.name}.tmp")
	var replaced = false

	ZipInputStream(FileInputStream(jarFile)).use { input ->
		ZipOutputStream(FileOutputStream(tempFile)).use { output ->
			while (true) {
				val entry = input.nextEntry ?: break
				val name = entry.name
				val bytes = if (name == entryName) {
					replaced = true
					replacementBytes
				} else {
					input.readBytes()
				}

				val newEntry = ZipEntry(name)
				output.putNextEntry(newEntry)
				output.write(bytes)
				output.closeEntry()
				input.closeEntry()
			}

			if (!replaced) {
				val newEntry = ZipEntry(entryName)
				output.putNextEntry(newEntry)
				output.write(replacementBytes)
				output.closeEntry()
			}
		}
	}

	tempFile.copyTo(jarFile, overwrite = true)
	tempFile.delete()
}

fun writeJson(file: File, value: Any?) {
	file.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(value)) + "\n")
}

fun normalizeFabricModJson(file: File) {
	if (!file.isFile)
		return

	@Suppress("UNCHECKED_CAST")
	val json = JsonSlurper().parse(file) as MutableMap<String, Any?>
	@Suppress("UNCHECKED_CAST")
	val entrypoints = json["entrypoints"] as? MutableMap<String, Any?> ?: return

	if (recipeViewer != "jei") {
		entrypoints.remove("jei_mod_plugin")
	}
	if (recipeViewer != "rei") {
		entrypoints.remove("rei_client")
	}
	if (recipeViewer != "emi") {
		entrypoints.remove("emi")
	}

	writeJson(file, json)
}

fun convertNeoForgeCondition(value: Any?): Any? {
	val condition = value as? Map<*, *> ?: return null
	return when (condition["type"]?.toString()) {
		"neoforge:mod_loaded" -> condition["modid"]?.toString()?.let {
			linkedMapOf(
				"condition" to "fabric:all_mods_loaded",
				"values" to listOf(it)
			)
		}
		"neoforge:not" -> convertNeoForgeCondition(condition["value"])?.let {
			linkedMapOf(
				"condition" to "fabric:not",
				"value" to it
			)
		}
		"neoforge:tag_empty" -> condition["tag"]?.toString()?.let {
			linkedMapOf(
				"condition" to "fabric:not",
				"value" to linkedMapOf(
					"condition" to "fabric:tags_populated",
					"values" to listOf(it)
				)
			)
		}
		else -> null
	}
}

fun normalizeResourceJsonValue(value: Any?, parentKey: String? = null, recipeResource: Boolean = false): Any? {
	return when (value) {
		is Map<*, *> -> {
			val normalized = linkedMapOf<String, Any?>()
			value.forEach { (key, child) ->
				if (key != null) {
					normalized[key.toString()] = normalizeResourceJsonValue(child, key.toString(), recipeResource)
				}
			}

			val neoForgeConditions = normalized.remove("neoforge:conditions")
			if (neoForgeConditions is List<*>) {
				val convertedConditions = neoForgeConditions.mapNotNull(::convertNeoForgeCondition)
				if (convertedConditions.isNotEmpty()) {
					@Suppress("UNCHECKED_CAST")
					val existingConditions = normalized["fabric:load_conditions"] as? List<Any?>
					normalized["fabric:load_conditions"] = (existingConditions ?: emptyList()) + convertedConditions
				}
			}

			if (recipeResource) {
				if (normalized.containsKey("heatRequirement") && !normalized.containsKey("heat_requirement")) {
					normalized["heat_requirement"] = normalized.remove("heatRequirement")
				}
				if (normalized.containsKey("processingTime") && !normalized.containsKey("processing_time")) {
					normalized["processing_time"] = normalized.remove("processingTime")
				}
				if (normalized.containsKey("acceptMirrored") && !normalized.containsKey("accept_mirrored")) {
					normalized["accept_mirrored"] = normalized.remove("acceptMirrored")
				}

				when (normalized["type"]) {
					"neoforge:compound" -> {
						@Suppress("UNCHECKED_CAST")
						val ingredients = normalized["ingredients"] as? List<Any?>
						if (ingredients != null && ingredients.size == 1) {
							return ingredients.single()
						}
						normalized.remove("type")
						normalized["fabric:type"] = "fabric:all"
					}
					"neoforge:single" -> normalized["type"] = "fluid_stack"
					"neoforge:tag" -> {
						normalized["type"] = "fluid_tag"
						if (normalized.containsKey("tag")) {
							normalized["fluid_tag"] = normalized.remove("tag")
						}
					}
					"neoforge:components" -> {
						normalized["type"] = "fluid_stack"
						if (normalized.containsKey("fluids")) {
							normalized["fluid"] = normalized.remove("fluids")
						}
					}
					"neoforge:block_tag" -> {
						normalized.remove("type")
						normalized["fabric:type"] = "create:block_tag_ingredient"
					}
				}

				if ((parentKey == "result" || parentKey == "results") && normalized.containsKey("item") && !normalized.containsKey("id")) {
					normalized["id"] = normalized.remove("item")
				}
				if (parentKey == "results" && normalized["id"] is String && normalized.containsKey("amount") && !normalized.containsKey("count")) {
					normalized["fluid"] = linkedMapOf("fluid" to normalized.remove("id"))
				}
				if (parentKey == "results" && normalized["fluid"] is String && normalized.containsKey("amount")) {
					normalized["fluid"] = linkedMapOf("fluid" to normalized["fluid"])
				}
			}

			normalized
		}
		is List<*> -> value.map { normalizeResourceJsonValue(it, parentKey, recipeResource) }
		is String -> if (recipeResource && parentKey == "result") linkedMapOf("id" to value) else value
		else -> value
	}
}

fun normalizeResourceJson(file: File) {
	if (!file.isFile)
		return

	val json = JsonSlurper().parse(file)
	val path = file.path.replace(File.separatorChar, '/')
	val recipeResource = "/data/" in path && ("/recipe/" in path || "/recipes/" in path)
	writeJson(file, normalizeResourceJsonValue(json, recipeResource = recipeResource))
}

fun copyLegacyTagDirectories(outputDir: File) {
	val dataDir = outputDir.resolve("data")
	val legacyDirectories = mapOf(
		"blocks" to "block",
		"items" to "item",
		"fluids" to "fluid",
		"entity_types" to "entity_type",
		"game_events" to "game_event"
	)

	dataDir.listFiles { file -> file.isDirectory }?.forEach { namespaceDir ->
		legacyDirectories.forEach { (oldName, newName) ->
			val oldDir = namespaceDir.resolve("tags/$oldName")
			if (!oldDir.isDirectory)
				return@forEach

			oldDir.walkTopDown()
				.filter { it.isFile }
				.forEach { oldFile ->
					val newFile = namespaceDir.resolve("tags/$newName/${oldFile.relativeTo(oldDir).invariantSeparatorsPath}")
					if (!newFile.exists()) {
						newFile.parentFile.mkdirs()
						oldFile.copyTo(newFile)
					}
				}
		}
	}

	val wrenchesTag = dataDir.resolve("c/tags/item/wrenches.json")
	val toolWrenchTag = dataDir.resolve("c/tags/item/tools/wrench.json")
	if (wrenchesTag.isFile && !toolWrenchTag.exists()) {
		toolWrenchTag.parentFile.mkdirs()
		wrenchesTag.copyTo(toolWrenchTag)
	}
}

fun copyLegacyRecipeDirectories(outputDir: File) {
	val dataDir = outputDir.resolve("data")
	dataDir.listFiles { file -> file.isDirectory }?.forEach { namespaceDir ->
		val oldDir = namespaceDir.resolve("recipes")
		if (!oldDir.isDirectory)
			return@forEach

		oldDir.walkTopDown()
			.filter { it.isFile }
			.forEach { oldFile ->
				val newFile = namespaceDir.resolve("recipe/${oldFile.relativeTo(oldDir).invariantSeparatorsPath}")
				if (!newFile.exists()) {
					newFile.parentFile.mkdirs()
					oldFile.copyTo(newFile)
				}
			}
	}
}

fun mergeTagFile(file: File, values: List<String>) {
	val json = if (file.isFile) JsonSlurper().parse(file) as? Map<*, *> else null
	val existingValues = (json?.get("values") as? List<*>) ?: emptyList<Any?>()
	val mergedValues = (existingValues + values)
		.distinctBy { it.toString() }
	val replace = json?.get("replace") as? Boolean ?: false

	file.parentFile.mkdirs()
	writeJson(file, linkedMapOf(
		"replace" to replace,
		"values" to mergedValues
	))
}

fun addCommonTagAliases(outputDir: File) {
	val tagsDir = outputDir.resolve("data/c/tags")

	val itemAliases = linkedMapOf<String, List<String>>(
		"cobblestone" to listOf("#c:cobblestones"),
		"colorless_sand" to listOf("#c:sands/colorless"),
		"copper_blocks" to listOf("#c:storage_blocks/copper"),
		"copper_ingots" to listOf("#c:ingots/copper"),
		"copper_raw_materials" to listOf("#c:raw_materials/copper"),
		"doughs" to listOf("create:dough"),
		"doughs/wheat" to listOf("create:dough"),
		"flours" to listOf("create:wheat_flour"),
		"flours/wheat" to listOf("create:wheat_flour"),
		"gold_ingots" to listOf("#c:ingots/gold"),
		"gold_raw_materials" to listOf("#c:raw_materials/gold"),
		"gunpowder" to listOf("#c:gunpowders"),
		"ingots/brass" to listOf("create:brass_ingot"),
		"ingots/zinc" to listOf("create:zinc_ingot"),
		"iron_blocks" to listOf("#c:storage_blocks/iron"),
		"iron_ingots" to listOf("#c:ingots/iron"),
		"iron_nuggets" to listOf("#c:nuggets/iron"),
		"iron_raw_materials" to listOf("#c:raw_materials/iron"),
		"lapis" to listOf("#c:gems/lapis"),
		"leather" to listOf("#c:leathers"),
		"netherite_ingots" to listOf("#c:ingots/netherite"),
		"netherrack" to listOf("#c:netherracks"),
		"nuggets/zinc" to listOf("create:zinc_nugget"),
		"obsidian" to listOf("#c:obsidians"),
		"plates/brass" to listOf("create:brass_sheet"),
		"plates/copper" to listOf("create:copper_sheet"),
		"plates/gold" to listOf("create:golden_sheet"),
		"plates/iron" to listOf("create:iron_sheet"),
		"plates/obsidian" to listOf("create:sturdy_sheet"),
		"quartz" to listOf("#c:gems/quartz"),
		"raw_copper_blocks" to listOf("#c:storage_blocks/raw_copper"),
		"raw_gold_blocks" to listOf("#c:storage_blocks/raw_gold"),
		"raw_iron_blocks" to listOf("#c:storage_blocks/raw_iron"),
		"raw_materials/zinc" to listOf("create:raw_zinc"),
		"raw_zinc_blocks" to listOf("create:raw_zinc_block"),
		"red_sand" to listOf("#c:sands/red"),
		"slimeballs" to listOf("#c:slime_balls"),
		"stone" to listOf("#c:stones"),
		"storage_blocks/brass" to listOf("create:brass_block"),
		"storage_blocks/raw_zinc" to listOf("create:raw_zinc_block"),
		"storage_blocks/zinc" to listOf("create:zinc_block"),
		"string" to listOf("#c:strings"),
		"wooden_barrels" to listOf("#c:barrels/wooden"),
		"wooden_chests" to listOf("#c:chests/wooden"),
		"wooden_rods" to listOf("#c:rods/wooden")
	).apply {
		listOf(
			"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray",
			"lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow"
		).forEach { color ->
			put("${color}_dyes", listOf("#c:dyes/$color"))
		}
	}

	val blockAliases = linkedMapOf<String, List<String>>(
		"brass_blocks" to listOf("create:brass_block"),
		"cobblestone" to listOf("#c:cobblestones"),
		"copper_blocks" to listOf("#c:storage_blocks/copper"),
		"iron_blocks" to listOf("#c:storage_blocks/iron"),
		"obsidian" to listOf("#c:obsidians"),
		"raw_copper_blocks" to listOf("#c:storage_blocks/raw_copper"),
		"raw_gold_blocks" to listOf("#c:storage_blocks/raw_gold"),
		"raw_iron_blocks" to listOf("#c:storage_blocks/raw_iron"),
		"raw_zinc_blocks" to listOf("create:raw_zinc_block"),
		"stone" to listOf("#c:stones"),
		"storage_blocks/brass" to listOf("create:brass_block"),
		"storage_blocks/raw_zinc" to listOf("create:raw_zinc_block"),
		"storage_blocks/zinc" to listOf("create:zinc_block"),
		"zinc_blocks" to listOf("create:zinc_block")
	)

	itemAliases.forEach { (path, values) ->
		listOf("item", "items").forEach { type ->
			mergeTagFile(tagsDir.resolve("$type/$path.json"), values)
		}
	}
	blockAliases.forEach { (path, values) ->
		listOf("block", "blocks").forEach { type ->
			mergeTagFile(tagsDir.resolve("$type/$path.json"), values)
		}
	}
}

tasks.named("processIncludeJars") {
	dependsOn(tasks.named("compileJava"))
	doLast {
		listOf(
			"Registrate-$registrateVersion.jar",
			"lazy_registration-$portingLibLazyRegistrationVersion.jar"
		).forEach { jarName ->
			stripNestedJars(layout.buildDirectory.file("processIncludeJars/$jarName").get().asFile)
		}
		replaceJarEntry(
			layout.buildDirectory.file("processIncludeJars/Registrate-$registrateVersion.jar").get().asFile,
			"com/tterrag/registrate/builders/MenuBuilder.class",
			layout.buildDirectory.file("classes/java/main/com/tterrag/registrate/builders/MenuBuilder.class").get().asFile
		)
		makeFlywheelAvailableOnServer(layout.buildDirectory.file("processIncludeJars/flywheel-fabric-$minecraftVersion-$flywheelVersion.jar").get().asFile)
		removeIncompatibleMilkMixins(layout.buildDirectory.file("processIncludeJars/milk-lib-$milkLibVersion.jar").get().asFile)
		removeIncompatiblePortingLibExtensionsMixins(layout.buildDirectory.file("processIncludeJars/extensions-$portingLibLegacyVersion.jar").get().asFile)
	}
}

loom {
    accessWidenerPath = file("src/main/resources/create.accesswidener")

    runs {
        register("datagen") {
            client()
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=create")
        }

        register("gametestServer") {
            server()
            name("Headlesss GameTests")
            ideConfigGenerated(false) // this run is for CI
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${layout.buildDirectory}/junit.xml")
            runDir("run/gametest")
        }

        named("server") {
            runDir("run/server")
        }

        configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            property("mixin.debug.export", "true")
        }
    }
}

configurations {
    // this avoids remapping ponder when it's local
    named("runtimeClasspath") {
        attributes {
            attribute(Attribute.of("create.marker", String::class.java), "h")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    exclude("**/*.bbmodel", "**/*.lnk", "data/neoforge/data_maps/**", "data/create/data_maps/**")

    val properties: MutableMap<String, Any> = mutableMapOf(
        "version" to version,
        "minecraft_version" to minecraftVersion,
        "loader_version" to loaderVersion,
        "fabric_version" to fapiVersion,
        "forge_config_version" to configApiVersion,
        "milk_lib_version" to milkLibVersion
    )

    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }

    doLast {
        normalizeFabricModJson(destinationDir.resolve("fabric.mod.json"))

        val dataDir = destinationDir.resolve("data")
        if (dataDir.isDirectory) {
            dataDir.walkTopDown()
                .filter { it.isFile && it.extension == "json" }
                .forEach(::normalizeResourceJson)
        }

		copyLegacyRecipeDirectories(destinationDir)
		copyLegacyTagDirectories(destinationDir)
		addCommonTagAliases(destinationDir)
	}
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(21)
    options.compilerArgs.add("-Xmaxerrs")
    options.compilerArgs.add("10000")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "create-fabric-$minecraftVersion"
            from(components["java"])
        }
    }

    repositories {
        maven("https://mvn.devos.one/releases") {
            name = "devOsReleases"
            credentials(PasswordCredentials::class)
        }

        maven("https://mvn.devos.one/snapshots") {
            name = "devOsSnapshots"
            credentials(PasswordCredentials::class)
        }
    }
}
