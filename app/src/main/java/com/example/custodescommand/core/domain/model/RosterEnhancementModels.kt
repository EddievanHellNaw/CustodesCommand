package com.example.custodescommand.core.domain.model

/** Defines reference options presented in the editor. */
data class AvailableEnhancement(
    val detachmentId: String,
    val enhancementId: String,
    val name: String,
    val points: Int,
    val textStatus: String,
    val eligibilityText: String?
)

data class AssignedEnhancement(
    val detachmentId: String,
    val enhancementId: String,
    val rosterUnitInstanceId: String,
    val unitId: String,
    val name: String,
    val points: Int,
    val textStatus: String,
    val eligibilityText: String?,
    val sourcePage: Int
)

data class ArmyPointBreakdown(
    val baseUnitPoints: Int,
    val enhancementPoints: Int,
    val additionalWargearPoints: Int = 0
) {
    val total: Int get() = baseUnitPoints + enhancementPoints + additionalWargearPoints
    fun remaining(limit: Int): Int = limit - total
}

data class EnhancementValidation(
    val hardErrors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Only restrictions unambiguously readable from the user's page scans are
 * hard enforced. All other eligibility texts in the Lesson 8 pack are INDEX_ONLY.
 * This is NOT a comprehensive tournament army validator.
 */
object EnhancementEligibility {
    fun check(enhancementId: String, unitId: String): EnhancementValidation {
        val errors = when (enhancementId) {
            // Lions of the Emperor, supplied PDF page 14.
            "lions-e0", "lions-e1" ->
                if (unitId != "shield-captain-allarus")
                    listOf("This Lions enhancement requires a Shield-Captain in Allarus Terminator Armour.")
                else emptyList()
            // Might of the Moritoi, supplied PDF page 15.
            "moritoi-e0", "moritoi-e1" ->
                if (unitId !in setOf("achillus", "galatus", "telemon", "venerable-contemptor"))
                    listOf("This Moritoi upgrade requires an Adeptus Custodes Dreadnought.")
                else emptyList()
            else -> emptyList()
        }
        return EnhancementValidation(
            hardErrors = errors,
            warnings = listOf("Check the enhancement's complete eligibility, target-model and once-per-army restrictions against the source page before play.")
        )
    }
}
