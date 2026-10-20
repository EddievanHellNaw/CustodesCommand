package com.example.custodescommand.core.domain.model

import org.junit.Assert.*
import org.junit.Test

class ConfiguredWeaponsTest {
    private fun profile(name: String, type: WeaponType) = WeaponProfile(
        id = name, name = name, type = type, range = if (type == WeaponType.MELEE) "Melee" else "24\"",
        attacks = "2", skill = "2+", strength = "5", armourPenetration = -1, damage = "2"
    )
    private val allarus = DetailedUnit(
        id = "allarus", name = "Allarus Custodians", category = "Terminator Infantry",
        playStatus = "CURRENT", profileStatus = "PARTIAL_TRANSCRIPTION",
        allowedModelCounts = setOf(2, 3), characteristics = null, keywords = emptyList(),
        weapons = listOf(
            profile("Ballistus Grenade Launcher", WeaponType.RANGED),
            profile("Guardian Spear (ranged)", WeaponType.RANGED),
            profile("Guardian Spear (melee)", WeaponType.MELEE),
            profile("Castellan Axe (ranged)", WeaponType.RANGED),
            profile("Castellan Axe (melee)", WeaponType.MELEE)
        ), abilities = emptyList(), wargearOptions = emptyList(),
        eligibleBodyguardNames = emptyList(), pointTiers = emptyList(), extraWargearCosts = emptyList(),
        sourcePage = 20
    )
    private fun select(option: String, model: Int) = WargearSelection(
        "squad-1", "allarus", option, model,
        if (option == "vexilla") "Vexilla" else "Castellan Axe", 0, "SOURCE_TRANSCRIBED"
    )

    @Test fun oneVexillaDoesNotReplaceAnyWeapon() {
        val result = ConfiguredWeaponResolver.resolve(allarus, 3, listOf(select("vexilla", 2)))
        assertEquals(9, result.weapons.size) // 3 models x (launcher + spear ranged + spear melee)
        assertEquals(3, result.weapons.count { it.name == "Guardian Spear (melee)" })
        assertTrue(result.complete)
    }

    @Test fun selectedAxeReplacesOnlyTheBearersSpear() {
        val result = ConfiguredWeaponResolver.resolve(allarus, 3,
            listOf(select("castellan-axe", 2), select("vexilla", 1)))
        val model2 = result.weapons.filter { it.modelIndex == 2 }.map { it.name }
        assertTrue(model2.contains("Castellan Axe (melee)"))
        assertTrue(model2.contains("Castellan Axe (ranged)"))
        assertFalse(model2.contains("Guardian Spear (melee)"))
        assertTrue(result.weapons.any { it.modelIndex == 1 && it.name == "Guardian Spear (melee)" })
    }

    @Test fun secondVexillaOnOtherModelIsRejectedAtDomainLevel() {
        val vexilla = WargearOption("allarus", "vexilla", "Vexilla", 0, 1, null,
            "SOURCE_TRANSCRIBED", 20, "One model per unit")
        val result = WargearRules.checkAdd(
            vexilla, "allarus", 3, 3, listOf(select("vexilla", 1)), listOf(vexilla)
        )
        assertFalse(result.permitted)
        assertTrue(result.errors.any { it.contains("per unit", ignoreCase = true) })
    }

    @Test fun eachSeparateSquadCanHaveItsOwnVexilla() {
        val option = WargearOption("allarus", "vexilla", "Vexilla", 0, 1, null,
            "SOURCE_TRANSCRIBED", 20, "One model per unit")
        val anotherSquadSelections = emptyList<WargearSelection>()
        assertTrue(WargearRules.checkAdd(option, "allarus", 3, 2,
            anotherSquadSelections, listOf(option)).permitted)
    }
}
