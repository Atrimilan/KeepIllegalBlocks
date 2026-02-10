# Keep Illegal Blocks

KIB is a Paper plugin that prevents illegal blocks from breaking when a player interacts with an adjacent block.

<table>
  <tr>
    <td><img alt="with_kib" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/with_kib_ezgif.gif" style="width: 100%; height: auto;" /></td>
    <td><img alt="without_kib" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/without_kib_ezgif.gif" style="width: 100%; height: auto;" /></td>
  </tr>
</table>

<details>
<summary><b>🖼️ Click to see more examples</b></summary>
<br/>

There you go 😎:

<table>
  <tr>
    <td><img alt="chairs_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/chairs_ezgif.gif" style="width: 100%; height: auto;" /></td>
    <td><img alt="rail_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/rail_ezgif.gif" style="width: 100%; height: auto;" /></td>
    <td><img alt="copper_slabs_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/copper_slabs_ezgif.gif" style="width: 100%; height: auto;" /></td>
  </tr>
</table>
<table>
  <tr>
    <td><img alt="drawbridge_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/drawbridge_ezgif.gif" style="width: 100%; height: auto;" /></td>
    <td><img alt="lantern_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/lantern_ezgif.gif" style="width: 100%; height: auto;" /></td>
    <td><img alt="end_portal_frame_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/end_portal_frame_ezgif.gif" style="width: 100%; height: auto;" /></td>
  </tr>
</table>
<table>
  <tr>
    <td><img alt="grave_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/grave_ezgif.gif" style="width: 100%; height: auto;" /></td>
    <td><img alt="cauldron_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/cauldron_ezgif.gif" style="width: 100%; height: auto;" /></td>
    <td><img alt="gate_ezgif" src="https://raw.githubusercontent.com/Atrimilan/KeepIllegalBlocks/master/assets/gate_ezgif.gif" style="width: 100%; height: auto;" /></td>
  </tr>
</table>

</details>

<br/>

## So what's the point?

"**Fragile blocks**" are illegal blocks that cannot normally be placed and are automatically broken by Minecraft
physics. These **fragile blocks** can be placed using a Debug Stick or plugins such as WorldEdit or Axiom.

"**Interactable blocks**" are blocks that can be directly interacted with _(using right-click)_, and can cause **fragile
blocks** to break.

Interacting with an **interactable block** triggers a physical update that propagates through all chained **fragile
blocks**, causing them to break.

**--> This plugin prevents this behavior to protect your illegal builds, by restoring broken blocks.**

<br/>

<details>
<summary>🔵 List of fragile block categories</summary>

List of fragile block categories that are automatically restored by the plugin when broken:

* `amethyst-clusters`
* `bamboos`
* `banners`
* `beds`
* `bells`
* `cactus`
* `cakes`
* `carpets`
* `cave-vines`
* `chorus-plants`
* `cocoa`
* `comparators`
* `corals` _(does not apply to waterlogged corals)_
* `crops`
* `dead-bushes`
* `doors`
* `dripleaves`
* `ferns`
* `flowers`
* `frogspawn`
* `fungus`
* `glow-lichens`
* `grass`
* `hanging-roots`
* `hanging-signs`
* `ladders`
* `lanterns`
* `lily-pads`
* `mangrove-propagules`
* `mushrooms`
* `nether-roots`
* `nether-sprouts`
* `nether-warts`
* `pointed-dripstones`
* `pressure-plates`
* `rails`
* `redstone-wires`
* `repeaters`
* `saplings`
* `scaffolding`
* `sculk-veins`
* `sea-pickles`
* `signs`
* `snow`
* `sugar-canes`
* `sweet-berry-bushes`
* `switches` _(levers and buttons)_
* `torches`
* `tripwire-hooks`
* `twisting-vines`
* `vines`
* `weeping-vines`

</details>

<details>
<summary>🔴 List of interactable block categories</summary>

List of interactable blocks categories recorded by the plugin:

* `campfires`
* `candles`
* `cauldrons`
* `comparators`
* `composters`
* `copper-blocks` _(non-plain copper blocks that can be waxed or scraped)_
* `daylight-detectors`
* `doors`
* `end-portal-frames`
* `gates`
* `lecterns` _(putting a book on them)_
* `repeaters`
* `switches` _(levers and buttons)_
* `trap-doors`

</details>

<br/>

> **NOTE -** KIB does **not** support underwater restorations. This is why `seagrass` and `kelp` are not included in the
> fragile block list.

> **WARNING -** Even after being restored by KIB, some fragile block will still update naturally, such as cactus
> breaking as they grow, or coral dying when not waterlogged. This is the default behavior of the game, KIB will NOT
> prevent this, even if they were initially placed using plugins like WorldEdit or Axiom (which rely on advanced chunk
> management systems).

<br/>

## Why would I need it?

This may seem niche, but it can be very useful for specific builds made using plugins like WorldEdit or Axiom,
especially when using custom resource packs that significantly change block textures and models.

This may also interest you if other players come onto your map and are likely to interact with your builds and break
everything accidentally... 😅

<br/>

## How do I configure?

In your server directory, you can edit `./plugins/KeepIllegalBlocks/config.yml` to:

* Blacklist some fragile or interactable blocks _(everything is enabled by default)_
* Change the maximum number of fragile blocks to restore _(default: 500)_
* Only allow KIB in creative mode _(default: true)_
* Use [PacketEvents](https://modrinth.com/plugin/packetevents) if it is detected _(default: true)_

To reload your configuration, use the `/kib reload` command (it requires the `kib.reload` permission for a non-op
player).

<br/>

## Can the restoration be any smoother?

**Yes, [PacketEvents](https://modrinth.com/plugin/packetevents) is highly recommended to improve client-side rendering
and performance.**

It is supported by KIB to tweak network packets and make fragile block restoration completely transparent by hiding
their break particles, break animation, break sound and item drops.
