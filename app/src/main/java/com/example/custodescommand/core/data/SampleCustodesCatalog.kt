
package com.example.custodescommand.core.data

import com.example.custodescommand.core.domain.model.*

object SampleCustodesCatalog {

    val units = listOf(
        UnitDefinition(
            id = "allarus",
            name = "Allarus Custodians",
            category = "Terminator Infantry",
            allowedModelCounts = setOf(2, 3),
            characteristics = UnitCharacteristics(
                movementInches = 7,
                toughness = 8,
                armourSave = "2+",
                wounds = 6,
                leadership = "5+",
                objectiveControl = 2,
                invulnerableSave = "4+"
            ),
            weapons = listOf(
                WeaponProfile(
                    id = "ballistus-grenade-launcher",
                    name = "Ballistus Grenade Launcher",
                    type = WeaponType.RANGED,
                    range = "18\"",
                    attacks = "3",
                    skill = "2+",
                    strength = "5",
                    armourPenetration = -1,
                    damage = "1",
                    abilities = listOf("ASSAULT", "BLAST 1")
                ),

                WeaponProfile(
                    id = "castellan-axe",
                    name = "Castellan Axe",
                    type = WeaponType.RANGED,
                    range = "24\"",
                    attacks = "2",
                    skill = "2+",
                    strength = "5",
                    armourPenetration = -1,
                    damage = "2",
                    abilities = listOf("ASSAULT", "RAPID FIRE 2")
                ),

                WeaponProfile(
                    id = "guardian-spear",
                    name = "Guardian Spear",
                    type = WeaponType.RANGED,
                    range = "24\"",
                    attacks = "2",
                    skill = "2+",
                    strength = "5",
                    armourPenetration = -1,
                    damage = "2",
                    abilities = listOf("ASSAULT", "RAPID FIRE 2")
                ),

                WeaponProfile(
                    id = "guardian-spear",
                    name = "Guardian Spear",
                    type = WeaponType.MELEE,
                    range = "Melee",
                    attacks = "6",
                    skill = "2+",
                    strength = "8",
                    armourPenetration = -2,
                    damage = "2"
                ),

                WeaponProfile(
                    id = "castellan-axe",
                    name = "Castellan Axe",
                    type = WeaponType.MELEE,
                    range = "Melee",
                    attacks = "3",
                    skill = "2+",
                    strength = "10",
                    armourPenetration = -2,
                    damage = "4"
                ),
            ),
            keywords = setOf(
                "INFANTRY",
                "IMPERIUM",
                "TERMINATOR"
            ),
            source = SourceReference(
                document = "Custodes PDF",
                page = 20
            )
        ),

        UnitDefinition(
            id = "wardens",
            name = "Custodian Wardens",
            category = "Infantry",
            allowedModelCounts = setOf(2, 3),
            characteristics = UnitCharacteristics(
                movementInches = 8,
                toughness = 7,
                armourSave = "2+",
                wounds = 6,
                leadership = "5+",
                objectiveControl = 2,
                invulnerableSave = "4+"
            ),
            weapons = listOf(
                WeaponProfile(
                    id = "castellan-axe",
                    name = "Castellan Axe",
                    type = WeaponType.RANGED,
                    range = "24\"",
                    attacks = "2",
                    skill = "2+",
                    strength = "5",
                    armourPenetration = -1,
                    damage = "2"
                ),
                WeaponProfile(
                    id = "guardian-spear",
                    name = "Guardian Spear",
                    type = WeaponType.RANGED,
                    range = "24\"",
                    attacks = "2",
                    skill = "2+",
                    strength = "5",
                    armourPenetration = -1,
                    damage = "2"
                ),
                WeaponProfile(
                    id = "castellan-axe",
                    name = "Castellan Axe",
                    type = WeaponType.MELEE,
                    range = "Melee",
                    attacks = "3",
                    skill = "2+",
                    strength = "10",
                    armourPenetration = -2,
                    damage = "4"
                ),
                WeaponProfile(
                    id = "guardian-spear",
                    name = "Guardian Spear",
                    type = WeaponType.MELEE,
                    range = "Melee",
                    attacks = "6",
                    skill = "2+",
                    strength = "8",
                    armourPenetration = -2,
                    damage = "2"
                ),
            ),
            keywords = setOf(
                "INFANTRY",
                "IMPERIUM",
                "TRUSTED SENTINEL"
            ),
            source = SourceReference(
                document = "Custodes PDF",
                page = 19
            )
        ),

        UnitDefinition(
            id = "venatari-lances",
            name = "Venatari with Verutum Lances",
            category = "Jump Pack Infantry",
            allowedModelCounts = setOf(3),
            characteristics = UnitCharacteristics(
                movementInches = 12,
                toughness = 7,
                armourSave = "2+",
                wounds = 5,
                leadership = "5+",
                objectiveControl = 2,
                invulnerableSave = "4+"
            ),
            weapons = listOf(
                WeaponProfile(
                    id = "verutum-lance",
                    name = "Guardian Spear",
                    type = WeaponType.RANGED,
                    range = "Melee",
                    attacks = "6",
                    skill = "2+",
                    strength = "8",
                    armourPenetration = -2,
                    damage = "2"
                ),
            ),
            source = SourceReference(
                document = "Custodes PDF",
                page = 24
            )
        ),

        UnitDefinition(
            id = "caladius-annihilator",
            name = "Caladius Annihilator Grav-Tank",
            category = "Vehicle",
            allowedModelCounts = setOf(1),
            source = SourceReference(
                document = "Custodes PDF",
                page = 27
            )
        )
    )

    val pointsCatalog = PointsCatalog(
        version = "MFM v1.6",
        tiers = listOf(

            // Allarus Custodians
            PointCostTier("allarus", 2, 1, 2, 180),
            PointCostTier("allarus", 3, 1, 2, 270),
            PointCostTier("allarus", 2, 3, null, 210),
            PointCostTier("allarus", 3, 3, null, 300),

            // Custodian Wardens
            PointCostTier("wardens", 2, 1, 1, 200),
            PointCostTier("wardens", 3, 1, 1, 295),
            PointCostTier("wardens", 2, 2, null, 230),
            PointCostTier("wardens", 3, 2, null, 325),

            // Venatari with Verutum Lances
            PointCostTier("venatari-lances", 3, 1, 2, 270),
            PointCostTier("venatari-lances", 3, 3, null, 300),

            // Caladius Annihilator Grav-Tank
            PointCostTier(
                "caladius-annihilator", 1, 1, 2, 250
            ),
            PointCostTier(
                "caladius-annihilator", 1, 3, null, 280
            )
        )
    )
}
