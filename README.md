<p align="center"><img src="./.idea/icon.png" alt="Logo" width="200"></p>
<h1 align="center">Create  <br>
	<a href="https://r.createmod.net/p"><img src="https://img.shields.io/endpoint.svg?url=https%3A%2F%2Fshieldsio-patreon.vercel.app%2Fapi%3Fusername%3Dsimibubi%26type%3Dpatrons&style=flat&label=Supporters&color=FF5733" alt="Patreon"></a>
	<a href="https://r.createmod.net/d"><img src="https://img.shields.io/discord/620934202875183104?color=5865F2&label=Discord&style=flat" alt="Discord"></a>
	<a href="https://www.curseforge.com/minecraft/mc-mods/create-fabric"><img src="https://img.shields.io/curseforge/dt/624165?logo=curseforge&label=&suffix=%20&style=flat&color=242629&labelColor=F16436&logoColor=1C1C1C" alt="CurseForge"></a>
    <a href="https://modrinth.com/mod/create-fabric"><img src="https://img.shields.io/modrinth/dt/create-fabric?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5CA424&logoColor=1C1C1C" alt="Modrinth"></a>
    <br><br>
    <a href="https://fabricmc.net/"><img
        src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_64h.png"
        alt="Available on Fabric"
        width="200"
    ></a>
    <a href="https://quiltmc.org/"><img
        src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/quilt_64h.png"
        alt="Available on Quilt"
        width="200"
    ></a>
</h1>

<p>Welcome to Create, a mod offering a variety of tools and blocks for Building, Decoration, and Aesthetic Automation.</p>
<p>The added elements of tech are designed to leave as many design choices to the player as possible. With Create, the game isn't played inside a bunch of UIs, it challenges you to set up contraptions of animated components working together in many possible arrangements.</p>
<p>Create's visual in-game documentation called 'Ponder' will guide you through all added mechanics and gadgets.</p>
<p>&nbsp;</p>
<p align="center"><a href="https://github.com/Fabricators-of-Create/Create/issues"><img src="https://i.imgur.com/qPmjSXy.png" width="160" /></a> <a href="https://www.youtube.com/channel/UCrKV2QTuyGcv4E3eSJpBiYA/playlists"><img src="https://i.imgur.com/L1bU9mr.png" width="160" /></a><a href="https://discord.gg/hmaD7Se"><img src="https://i.imgur.com/uf6V9ZX.png" width="160" /></a> <a href="https://github.com/Creators-of-Create/Create/wiki/Supporting-the-Project"><img src="https://i.imgur.com/fHQ45KR.png" width="227" /></a></p>

<h4 align="center">Find out more about Create on our <a href="https://www.curseforge.com/minecraft/mc-mods/create-fabric">CurseForge</a> or <a href="https://modrinth.com/mod/create-fabric">Modrinth</a> page</h4>
<h4 align="center">Looking for the Forge version? <a href="https://github.com/Creators-of-Create/Create">Find it here</a></h4>

<h3 align="center">Development</h3>

To depend on Create Fabric for an addon or compatibility, you'll need to set up a few things.

Five Maven repos are required:
- `https://mvn.devos.one/snapshots/`
  - Create (`com.simibubi.create:create-fabric`)
  - Registrate (`com.tterrag.registrate_fabric:Registrate`)
  - Milk Lib (`io.github.tropheusj:milk-lib`)
  - Dripstone Fluid Lib (`io.github.tropheusj:dripstone-fluid-lib`)
- `https://mvn.devos.one/releases/`
  - Porting Lib (`io.github.fabricators_of_create.Porting-Lib:<module>`)
- `https://maven.createmod.net/`
  - Flywheel (`dev.engine-room.flywheel:flywheel-fabric-<mc-version>`)
  - Flywheel API (`dev.engine-room.flywheel:flywheel-fabric-api-<mc-version>`)
  - Ponder Fabric (`net.createmod.ponder:Ponder-Fabric-<mc-version>`)
  - Ponder Common (`net.createmod.ponder:Ponder-Common-<mc-version>`)
- `https://raw.githubusercontent.com/Fuzss/modresources/main/maven`
  - Forge Config API Port (`fuzs.forgeconfigapiport:forgeconfigapiport-fabric`)
- `https://maven.jamieswhiteshirt.com/libs-release`
  - Reach Entity Attributes (`com.jamieswhiteshirt:reach-entity-attributes`)

All you need dependency-wise is a dependency on `com.simibubi.create:create-fabric:<version>`. Everything else will be pulled in transitively.
Check [Modrinth](https://modrinth.com/mod/create-fabric/versions) to find the right version number.
---
Note: I made this for Create 1.21.1 on fabric. Check 1.21.1/fabric/beta branch or release file.
