package com.example.custodescommand.core.domain.model

/** Model indices are 1-based. Every equipment selection targets a particular miniature. */
data class WargearOption(
    val unitId: String,
    val optionId: String,
    val name: String,
    val pointsPerSelection: Int,
    val maxPerUnit: Int?,
    val exclusivityGroup: String?,
    val sourceStatus: String,
    val sourcePage: Int?,
    val description: String
)

data class WargearSelection(
    val rosterUnitInstanceId: String,
    val unitId: String,
    val optionId: String,
    val modelIndex: Int,
    val name: String,
    val points: Int,
    val sourceStatus: String
)

data class WargearCheck(val errors: List<String> = emptyList(), val warnings: List<String> = emptyList()) {
    val permitted: Boolean get() = errors.isEmpty()
}

/** Pure rules-level safety checks; the source pack is not a complete legality authority. */
object WargearRules {
    fun checkAdd(
        option: WargearOption,
        unitId: String,
        modelCount: Int,
        modelIndex: Int,
        alreadySelected: List<WargearSelection>,
        definitions: List<WargearOption>
    ): WargearCheck {
        val errors = mutableListOf<String>()
        if (option.unitId != unitId) errors += "This option belongs to a different datasheet."
        if (modelIndex !in 1..modelCount) errors += "Model slot must be between 1 and $modelCount."
        if (alreadySelected.any { it.optionId == option.optionId && it.modelIndex == modelIndex })
            errors += "This model already has the selected option."
        if (option.maxPerUnit != null && alreadySelected.count { it.optionId == option.optionId } >= option.maxPerUnit)
            errors += "This option is limited to ${option.maxPerUnit} model(s) per unit."
        if (option.exclusivityGroup != null && alreadySelected.any { existing ->
                existing.modelIndex == modelIndex &&
                    definitions.firstOrNull { it.optionId == existing.optionId }?.exclusivityGroup == option.exclusivityGroup
            }) errors += "This model already has a mutually exclusive equipment choice."
        val warnings = if (option.sourceStatus == "PRICE_ONLY")
            listOf("The cost is sourced, but the option's complete eligibility must be verified on the datasheet.")
        else emptyList()
        return WargearCheck(errors, warnings)
    }

    fun validateResize(modelCount: Int, selections: List<WargearSelection>): WargearCheck {
        val errors = mutableListOf<String>()
        if (modelCount < 1) errors += "The unit must have at least one model."
        if (selections.any { it.modelIndex > modelCount })
            errors += "Remove equipment assigned to a model slot above $modelCount before reducing the squad size."
        return WargearCheck(errors)
    }

    fun totalPoints(selections: List<WargearSelection>): Int = selections.sumOf { it.points }
}
