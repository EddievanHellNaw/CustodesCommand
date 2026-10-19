package com.example.custodescommand.core.domain.model

import org.junit.Assert.*
import org.junit.Test

class EnhancementEligibilityTest {
    @Test fun lionsEnhancementRequiresAllarusCaptain() {
        assertTrue(EnhancementEligibility.check("lions-e0", "allarus").hardErrors.isNotEmpty())
        assertTrue(EnhancementEligibility.check("lions-e0", "shield-captain-allarus").hardErrors.isEmpty())
    }

    @Test fun dreadnoughtUpgradeRejectsInfantry() {
        assertTrue(EnhancementEligibility.check("moritoi-e0", "wardens").hardErrors.isNotEmpty())
        assertTrue(EnhancementEligibility.check("moritoi-e0", "telemon").hardErrors.isEmpty())
    }

    @Test fun unknownEligibilityProducesAWarningRatherThanFalseValidity() {
        val result = EnhancementEligibility.check("aquilan-shield-e1", "allarus")
        assertTrue(result.hardErrors.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test fun pointsComposeWithoutModifyingBaseCalculation() {
        val summary = ArmyPointBreakdown(baseUnitPoints = 180, enhancementPoints = 20)
        assertEquals(200, summary.total)
        assertEquals(800, summary.remaining(1000))
    }
}
