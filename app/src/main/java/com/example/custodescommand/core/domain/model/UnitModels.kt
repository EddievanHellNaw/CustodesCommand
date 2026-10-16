
package com.example.custodescommand.core.domain.model

enum class WeaponType {
    RANGED,
    MELEE
}

data class SourceReference(
    val document: String,
    val page: Int? = null
)

data class UnitCharacteristics(
    val movementInches: Int,
    val toughness: Int,
    val armourSave: String,
    val wounds: Int,
    val leadership: String,
    val objectiveControl: Int,
    val invulnerableSave: String? = null
)

data class WeaponProfile(
    val id: String,
    val name: String,
    val type: WeaponType,
    val range: String,
    val attacks: String,
    val skill: String,
    val strength: String,
    val armourPenetration: Int,
    val damage: String,
    val abilities: List<String> = emptyList()
)

data class UnitDefinition(
    val id: String,
    val name: String,
    val category: String,
    val allowedModelCounts: Set<Int>,
    val characteristics: UnitCharacteristics? = null,
    val weapons: List<WeaponProfile> = emptyList(),
    val keywords: Set<String> = emptySet(),
    val abilityIds: List<String> = emptyList(),
    val source: SourceReference? = null
)
