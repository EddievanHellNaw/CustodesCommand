package com.example.custodescommand.core.domain.model

/** A reference lookup, not a configured roster unit. */
data class DetailedUnit(
    val id: String,
    val name: String,
    val category: String,
    val playStatus: String,
    val profileStatus: String,
    val allowedModelCounts: Set<Int>,
    val characteristics: UnitCharacteristics?,
    val keywords: List<String>,
    val weapons: List<WeaponProfile>,
    val abilities: List<RuleSnippet>,
    val wargearOptions: List<String>,
    val eligibleBodyguardNames: List<String>,
    val pointTiers: List<PointCostTier>,
    val extraWargearCosts: List<OptionalWargearCost>,
    val sourcePage: Int?,
    val codexSections: List<CodexSection> = emptyList()
)

data class RuleSnippet(val name: String, val text: String)
data class OptionalWargearCost(val name: String, val pointsPerItem: Int)
