# Fabric 1.21.1 Migration Work Log

Date: 2026-04-27

## Baseline

- Local branch: `mc1.21.1/fabric/dev`
- Initial local HEAD: `7f1f67b6ec` (`porty mcport`)
- Upstream source: `https://github.com/Creators-of-Create/Create`
- Upstream branch fetched: `mc1.21.1/dev`
- Upstream ref used: `ad6b389fb6` (`Remove unnecessary variable in KineticBlockEntity#addToGoggleTooltip`)
- Latest upstream release tag observed during fetch: `mc1.21.1-6.0.10` at `ac0c444d98`
- Upstream development version in `gradle.properties`: `6.0.11`
- Merge base between this branch and upstream: `655f640757`
- Divergence at start: Fabric side `1824` commits, upstream side `613` commits.

## Merge / Update Work

- Fetched `Creators-of-Create/Create:mc1.21.1/dev` into `refs/remotes/upstream/mc1.21.1/dev`.
- Started a no-commit merge from upstream into `mc1.21.1/fabric/dev`.
- Resolved Git-level conflicts.
- Preserved Fabric build structure:
  - Kept `build.gradle.kts` and `settings.gradle.kts`.
  - Removed upstream NeoForge `build.gradle` and `settings.gradle` from the merge result.
- Accepted upstream generated data/assets where conflicts were purely generated resource conflicts.
- Kept current Fabric-side Java conflict files where taking upstream would replace Fabric adaptations with NeoForge APIs.
- Removed stale generated conflict-marker files whose upstream equivalents were renamed.
- Cleaned all conflict markers from tracked files.
- Combined `create.mixins.json`:
  - Kept Fabric mixins.
  - Added upstream Xaero map mixins.
  - Removed stale JourneyMap mixin entries whose Java files no longer exist.

## Dependency / Build Script Updates

- Updated project version in `build.gradle.kts` to `6.0.11+mc1.21.1...`.
- Updated Gradle wrapper target to `9.4.0` because Fabric Loom `1.16.1` requires Gradle plugin API `9.4.0`.
- Updated Fabric Loom to `1.16.1`.
- Updated Fabric Loader to `0.19.2`.
- Updated Fabric API to `0.116.11+1.21.1`.
- Updated Flywheel Fabric to `1.0.6`.
- Updated Ponder Fabric/Common to `1.0.69` because Fabric `1.21.1` artifacts do not have the upstream NeoForge `1.0.82` version.
- Updated Forge Config API Port to `21.1.6`.
- Updated CC: Tweaked Fabric to `1.118.0`.
- Updated Mod Menu to `11.0.4`.
- Updated Cardinal Components API to `6.1.3`.
- Updated Architectury Fabric to `13.0.8`.
- Updated FTB Maven from `maven.saps.dev` to `maven.ftb.dev/releases`.
- Updated FTB Fabric compile-only versions:
  - `ftb-chunks-fabric:2101.1.14`
  - `ftb-teams-fabric:2101.1.10`
  - `ftb-library-fabric:2101.1.31`
- Added available Porting Lib 1.21.1 beta modules to the compile classpath:
  - `3.1.0-beta.85+1.21.1`: `base`, `blocks`, `brewing`, `client_events`, `common`, `core`, `data`, `entity`, `fluids`, `gametest`, `items`, `level_events`, `loot`, `mixin_extensions`, `models`, `obj_loader`, `tags`, `transfer`
  - `3.1.0-beta.54+1.21.1`: `extensions`
- CI Java setup changed from Java 17 to Java 21.
- Local compile uses Java `--release 21` instead of requiring an installed Java 21 toolchain, because this machine is running Java 24 and has no Java 21 toolchain configured.
- Removed stale unresolved `fabric.mod.json` placeholders for Porting Lib and Reach Entity Attributes so resource expansion is not blocked by undefined variables. Porting Lib API usage remains an open compile blocker because current source imports old package paths that the 1.21.1 beta modules do not provide.

## Verification

- `git grep` conflict-marker scan: clean after resolution.
- `./gradlew.bat compileJava`: failed after dependency and Gradle setup progressed to Java compilation.

The current failure is no longer a Gradle/wrapper setup failure. It is Java porting work remaining in the Fabric branch.

## Current Compile Blockers

- `net.neoforged.*` imports remain in Fabric code. Current count after the first Fabric conversion pass: 72 Java files.
  - Examples: `AllAttachmentTypes`, NeoForge capabilities, NeoForge events, data maps, registry helpers, `FMLPaths`, `ServerLifecycleHooks`.
- `io.github.fabricators_of_create.porting_lib.*` imports remain unresolved or partially unresolved. Current count: 235 Java files.
  - Porting Lib 1.21.1 beta artifacts are now on the classpath where available, but some packages used by this branch map to APIs that moved or no longer exist in those artifacts.
  - Examples from compile output: `porting_lib.util.MinecartAndRailUtil`, `porting_lib.util.EnvExecutor`, `porting_lib.block.CustomRenderBoundingBoxBlockEntity`, `porting_lib.models.generators.item.ItemModelBuilder`.
- Several upstream 1.21.1/NeoForge code paths still need Fabric equivalents:
  - Attachments/capabilities for minecart controller data.
  - Block/entity/item capability registration.
  - NeoForge event bus usage.
  - Datamap and custom registry setup.
  - Forge/NeoForge utility paths and server lifecycle hooks.
- Minecraft 1.21.1 API differences still need cleanup in Fabric-side code:
  - `ResourceLocation` constructor changes.
  - `StructureProcessorType` now requiring `MapCodec`.
  - ItemStack/enchantment helper method changes.
  - Custom data APIs on entities/block entities.
  - Processing recipe generic type changes.

## Next Recommended Steps

1. Decide whether to keep using Porting Lib 1.21.1 beta modules or replace the remaining Porting Lib usage with local Fabric helpers.
2. Port `AllAttachmentTypes` and minecart controller storage to Fabric's available attachment/component pattern.
3. Replace NeoForge capability registration with Fabric API lookup/transfer registrations.
4. Replace NeoForge event bus usage with Fabric events or local hooks.
5. Re-run `./gradlew.bat compileJava` after each subsystem is ported.

## Fabric API Conversion Pass 1

- Replaced the NeoForge minecart attachment path with the existing Fabric `AbstractMinecartMixin` controller storage:
  - `MinecartController` no longer depends on NeoForge `IAttachmentSerializer`.
  - Minecart controller NBT save/load now passes the 1.21.1 registry lookup provider.
  - Minecart controller ticking is driven from the Fabric minecart mixin instead of NeoForge `EntityTickEvent`.
  - Client minecart controller sync now updates the mixin-held controller instead of `AllAttachmentTypes`.
- Converted `AllAttachmentTypes` to a Fabric no-op because minecart controller data is no longer backed by NeoForge attachments.
- Converted simple deferred registries to direct Fabric/vanilla registry calls:
  - `AllArmorMaterials`
  - `AllDataComponents`
  - `AllMapDecorationTypes`
  - `AllPackagePortTargetTypes`
- Converted `Create` initialization away from `modEventBus` and removed the upstream NeoForge register-event conditional for advancements/triggers.
- Updated Porting Lib package moves found in the 1.21.1 beta artifacts:
  - `porting_lib.block.*` -> `porting_lib.blocks.extensions.*`
  - `porting_lib.util.MinecartAndRailUtil` -> `porting_lib.blocks.util.MinecartAndRailUtil`
  - `porting_lib.util.EnvExecutor` -> `porting_lib.common.util.EnvExecutor`
- Adjusted rail direction calls for the new `MinecartAndRailUtil#getDirectionOfRail(..., AbstractMinecart)` signature.
- Reintroduced Fabric utility/support files required by the current code:
  - `ListeningStorageView`
  - `ReachUtil`
  - `AbstractRegistrateAccessor`
  - deleted unpacking API files and `PackageDefragmenter`
- Added a temporary `DefaultNbtSerializable` compatibility shim for stale Porting Lib bytecode references in the remapped classpath.
- Temporarily disabled the NeoForge custom map decoration renderer event hook in `CommonEvents`; the renderer class now compiles without the NeoForge renderer interface, but a proper Fabric client rendering hook is still needed.

## Verification After Pass 1

- `./gradlew.bat compileJava` still fails, but the first errors moved past `AllAttachmentTypes`, `Create`, `CreateRegistrate`, and the Porting Lib block package move.
- Current first blocker group:
  - CC:Tweaked NeoForge `PeripheralCapability` registration left in block entities; Fabric already has `PeripheralLookup`, but the per-block registrations still need to be removed or converted.
  - NeoForge capability registration and capability lookup remain in item/fluid/block entity code.
  - Several upstream files were renamed from `api.unpacking` to `api.packager.unpacking`; current Fabric files still import the old package in places.
  - Missing/changed Porting Lib and Fabric resource-condition/datagen APIs remain.
  - 1.21.1 recipe/input generic changes still produce many downstream type errors.
