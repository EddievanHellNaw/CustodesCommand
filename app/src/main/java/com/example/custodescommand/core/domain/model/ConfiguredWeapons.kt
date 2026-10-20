package com.example.custodescommand.core.domain.model

/** A weapon that the selected physical model is expected to carry. */
data class EquippedWeapon(
    val modelIndex: Int,
    val name: String,
    val quantity: Int,
    val profile: WeaponProfile?
)

data class ConfiguredWeaponsResult(
    val weapons: List<EquippedWeapon>,
    val warnings: List<String>,
    val supported: Boolean
) {
    val complete: Boolean get() = supported && warnings.isEmpty() && weapons.all { it.profile != null }
}

private data class Grant(val weapon: String, val count: Int = 1)
private data class Replacement(val remove: List<Grant>, val add: List<Grant>)
private data class Definition(
    val defaults: List<Grant>,
    val replacements: Map<String, Replacement>
)

private fun grant(weapon: String, count: Int = 1) = Grant(weapon, count)
private fun swap(old: String, new: String) = Replacement(listOf(grant(old)), listOf(grant(new)))

/**
 * Source-scoped loadout rules, drawn from the supplied Custodes pages.
 * The reference catalogue is intentionally NOT assumed to describe equipped weapons;
 * it also contains alternatives that a model cannot use simultaneously.
 * No rule is inferred for unregistered datasheets.
 */
object ConfiguredWeaponResolver {
    private val definitions: Map<String, Definition> = mapOf(
        "allarus" to Definition(
            listOf(grant("Ballistus Grenade Launcher"), grant("Guardian Spear (ranged)"), grant("Guardian Spear (melee)")),
            mapOf("castellan-axe" to Replacement(
                listOf(grant("Guardian Spear (ranged)"), grant("Guardian Spear (melee)")),
                listOf(grant("Castellan Axe (ranged)"), grant("Castellan Axe (melee)"))
            ))
        ),
        "wardens" to Definition(
            listOf(grant("Guardian Spear (ranged)"), grant("Guardian Spear (melee)")),
            mapOf("castellan-axe" to Replacement(
                listOf(grant("Guardian Spear (ranged)"), grant("Guardian Spear (melee)")),
                listOf(grant("Castellan Axe (ranged)"), grant("Castellan Axe (melee)"))
            ))
        ),
        "guard" to Definition(listOf(grant("Guardian Spear (ranged)"), grant("Guardian Spear (melee)")), emptyMap()),
        "sentinel" to Definition(listOf(grant("Sentinel Blade (ranged)"), grant("Sentinel Blade (melee)")), emptyMap()),
        "aquilon-talons" to Definition(
            listOf(grant("Lastrum Storm Bolter"), grant("Solarite Power Talon")),
            mapOf("infernus-firepike" to swap("Lastrum Storm Bolter", "Infernus Firepike"))
        ),
        "shield-captain-allarus" to Definition(
            listOf(grant("Ballistus Grenade Launcher"), grant("Guardian Spear (ranged)"), grant("Guardian Spear (melee)")),
            mapOf("castellan-axe" to Replacement(
                listOf(grant("Guardian Spear (ranged)"), grant("Guardian Spear (melee)")),
                listOf(grant("Castellan Axe (ranged)"), grant("Castellan Axe (melee)"))
            ))
        ),
        "vertus" to Definition(
            listOf(grant("Salvo Launcher"), grant("Interceptor Lance")),
            mapOf("hurricane-bolter" to swap("Salvo Launcher", "Hurricane Bolter"))
        ),
        "shield-captain-jetbike" to Definition(
            listOf(grant("Salvo Launcher"), grant("Interceptor Lance")),
            mapOf("hurricane-bolter" to swap("Salvo Launcher", "Hurricane Bolter"))
        ),
        "gyrfalcon" to Definition(
            listOf(grant("Lastrum Bolt Cannon"), grant("Solarite Power Lance")),
            mapOf(
                "adrathic-devastator" to swap("Lastrum Bolt Cannon", "Adrathic Devastator"),
                "arachnus-volley-cannon" to swap("Lastrum Bolt Cannon", "Arachnus Volley Cannon"),
                "twin-corvae-las-pulser" to swap("Lastrum Bolt Cannon", "Twin Corvae Las-pulser")
            )
        ),
        "pallas" to Definition(
            listOf(grant("Twin Arachnus Blaze Cannon"), grant("Armoured Hull")),
            mapOf("twin-iliastus" to swap("Twin Arachnus Blaze Cannon", "Twin Iliastus Accelerator Fusil"))
        ),
        "caladius" to Definition(
            listOf(grant("Iliastus Accelerator Cannon"), grant("Twin Lastrum Bolt Cannon"), grant("Armoured Hull")),
            mapOf("twin-neutronium" to swap("Twin Lastrum Bolt Cannon", "Twin Neutronium Cascade Projectors"))
        ),
        "caladius-annihilator" to Definition(
            listOf(grant("Arachnus Blaze Carronade"), grant("Twin Lastrum Bolt Cannon"), grant("Armoured Hull")),
            mapOf("twin-neutronium" to swap("Twin Lastrum Bolt Cannon", "Twin Neutronium Cascade Projectors"))
        ),
        "coronus" to Definition(
            listOf(grant("Twin Arachnus Blaze Cannon"), grant("Twin Lastrum Bolt Cannon"), grant("Armoured Hull")),
            mapOf("twin-neutronium" to swap("Twin Lastrum Bolt Cannon", "Twin Neutronium Cascade Projectors"))
        ),
        "knight-centura" to Definition(
            listOf(grant("Executioner Greatblade")),
            mapOf(
                "master-crafted-boltgun" to Replacement(listOf(grant("Executioner Greatblade")), listOf(grant("Master-crafted Boltgun"), grant("Gun Stock"))),
                "master-crafted-flamer" to Replacement(listOf(grant("Executioner Greatblade")), listOf(grant("Master-crafted Flamer"), grant("Gun Stock")))
            )
        ),
        "achillus" to Definition(
            listOf(grant("Dreadspear", 1), grant("Lastrum Storm Bolter", 2), grant("Dreadspear (melee)")),
            mapOf(
                "double-adrathic" to Replacement(listOf(grant("Lastrum Storm Bolter", 2)), listOf(grant("Adrathic Combi-destructor", 2))),
                "double-infernus" to Replacement(listOf(grant("Lastrum Storm Bolter", 2)), listOf(grant("Twin Infernus Incinerator", 2)))
            )
        ),
        "telemon" to Definition(
            listOf(grant("Spiculus Bolt Launcher"), grant("Twin Neutronium Cascade Projectors", 2), grant("Dual Caestus Fists")),
            mapOf(
                "adrathic-desolator" to Replacement(
                    listOf(grant("Twin Neutronium Cascade Projectors", 2), grant("Dual Caestus Fists")),
                    listOf(grant("Twin Neutronium Cascade Projectors"), grant("Caestus Fists"), grant("Adrathic Desolator"))
                ),
                "arachnus-storm-cannon" to Replacement(
                    listOf(grant("Twin Neutronium Cascade Projectors", 2), grant("Dual Caestus Fists")),
                    listOf(grant("Twin Neutronium Cascade Projectors"), grant("Caestus Fists"), grant("Arachnus Storm Cannon"))
                ),
                "iliastus-culverin" to Replacement(
                    listOf(grant("Twin Neutronium Cascade Projectors", 2), grant("Dual Caestus Fists")),
                    listOf(grant("Twin Neutronium Cascade Projectors"), grant("Caestus Fists"), grant("Iliastus Accelerator Culverin"))
                )
            )
        )
    )

    fun resolve(unit: DetailedUnit, modelCount: Int, selected: List<WargearSelection>): ConfiguredWeaponsResult {
        val definition = definitions[unit.id] ?: return ConfiguredWeaponsResult(
            emptyList(), listOf("A verified model-by-model default loadout is not registered for this datasheet. Use the source page."), false
        )
        val warnings = mutableListOf<String>()
        val profiles = unit.weapons.associateBy { it.name.trim().lowercase() }
        val allWeapons = mutableListOf<EquippedWeapon>()
        for (index in 1..modelCount) {
            val equipped = definition.defaults.map { it.copy() }.toMutableList()
            selected.filter { it.modelIndex == index }.forEach { selection ->
                // Vexilla changes abilities/OC, not the primary weapons.
                if (selection.optionId == "vexilla") return@forEach
                val replacement = definition.replacements[selection.optionId]
                if (replacement == null) {
                    warnings += "${selection.name} requires further loadout verification; its weapon effect is not modeled."
                    return@forEach
                }
                replacement.remove.forEach { old ->
                    val oldIndex = equipped.indexOfFirst { it.weapon.equals(old.weapon, ignoreCase = true) && it.count >= old.count }
                    if (oldIndex < 0) {
                        warnings += "Cannot apply ${selection.name}: expected ${old.weapon} was not equipped."
                    } else {
                        val current = equipped[oldIndex]
                        equipped.removeAt(oldIndex)
                        if (current.count > old.count) equipped.add(current.copy(count = current.count - old.count))
                    }
                }
                equipped.addAll(replacement.add)
            }
            equipped.forEach { weapon ->
                val profile = profiles[weapon.weapon.lowercase()]
                if (profile == null) warnings += "Profile not transcribed: ${weapon.weapon} (model $index)."
                allWeapons += EquippedWeapon(index, weapon.weapon, weapon.count, profile)
            }
        }
        return ConfiguredWeaponsResult(allWeapons, warnings.distinct(), true)
    }
}
