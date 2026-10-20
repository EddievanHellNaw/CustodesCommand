package com.example.custodescommand.core.domain.model

enum class ValidationSeverity { ERROR, WARNING }

data class RosterValidationIssue(
    val severity: ValidationSeverity,
    val message: String
)

/**
 * Local preflight, NOT full edition-11 legality validation. Unknown DP caps,
 * disposition interactions and most enhancement eligibility remain warnings.
 */
object RosterValidation {
    fun check(
        roster: Roster,
        baseUnitPoints: Int?,
        assignedEnhancements: List<AssignedEnhancement>,
        selectedDetachments: List<DetachmentSummary>,
        dpAllowance: Int? = null,
        additionalWargearPoints: Int = 0
    ): List<RosterValidationIssue> {
        val issues = mutableListOf<RosterValidationIssue>()
        val extras = assignedEnhancements.sumOf { it.points } + additionalWargearPoints
        if (baseUnitPoints == null) {
            issues += RosterValidationIssue(ValidationSeverity.ERROR,
                "Cannot verify army points because base unit pricing is unavailable.")
        } else if (baseUnitPoints + extras > roster.pointsLimit) {
            issues += RosterValidationIssue(ValidationSeverity.ERROR,
                "Army is ${baseUnitPoints + extras - roster.pointsLimit} points over its limit.")
        }
        if (selectedDetachments.isEmpty()) {
            issues += RosterValidationIssue(ValidationSeverity.WARNING,
                "No detachment selected.")
        }
        if (dpAllowance == null) {
            issues += RosterValidationIssue(ValidationSeverity.WARNING,
                "DP allowance and combined-detachment restrictions not yet verified.")
        } else {
            val used = selectedDetachments.sumOf { it.dpCost }
            if (used > dpAllowance) {
                issues += RosterValidationIssue(ValidationSeverity.ERROR,
                    "Detachments use $used DP, exceeding allowance of $dpAllowance DP.")
            }
        }
        if (assignedEnhancements.any { it.textStatus != "FULLY_VERIFIED" }) {
            issues += RosterValidationIssue(ValidationSeverity.WARNING,
                "Some assigned enhancement eligibility is not fully transcribed; check source pages.")
        }
        if (assignedEnhancements.groupBy { it.rosterUnitInstanceId }.any { it.value.size > 1 }) {
            issues += RosterValidationIssue(ValidationSeverity.ERROR,
                "More than one enhancement assigned to one roster unit.")
        }
        return issues
    }
}
