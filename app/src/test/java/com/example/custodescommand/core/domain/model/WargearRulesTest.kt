package com.example.custodescommand.core.domain.model

import org.junit.Assert.*
import org.junit.Test

class WargearRulesTest {
    private val axe = WargearOption("allarus", "axe", "Castellan Axe", 0, null, "primary", "SOURCE_TRANSCRIBED", 20, "")
    private val other = WargearOption("allarus", "other", "Alternative melee", 0, null, "primary", "SOURCE_TRANSCRIBED", 20, "")
    private val vexilla = WargearOption("allarus", "vexilla", "Vexilla", 0, 1, null, "SOURCE_TRANSCRIBED", 20, "")
    private val shield = WargearOption("shield-captain", "shield", "Shield", 25, 1, null, "PRICE_ONLY", 16, "")

    @Test fun rejectsSelectionsBeyondSquadSize() {
        assertFalse(WargearRules.checkAdd(axe, "allarus", 2, 3, emptyList(), listOf(axe)).permitted)
    }

    @Test fun rejectsSecondVexilla() {
        val selected = listOf(WargearSelection("i", "allarus", "vexilla", 1, "Vexilla", 0, "SOURCE_TRANSCRIBED"))
        assertFalse(WargearRules.checkAdd(vexilla, "allarus", 3, 2, selected, listOf(vexilla)).permitted)
    }

    @Test fun rejectsConflictingModelChoices() {
        val selected = listOf(WargearSelection("i", "allarus", "axe", 1, "Axe", 0, "SOURCE_TRANSCRIBED"))
        assertFalse(WargearRules.checkAdd(other, "allarus", 3, 1, selected, listOf(axe, other)).permitted)
    }

    @Test fun addsSourceWarningForPaidUnverifiedOption() {
        val result = WargearRules.checkAdd(shield, "shield-captain", 1, 1, emptyList(), listOf(shield))
        assertTrue(result.permitted)
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test fun calculatesAdditionalPoints() {
        assertEquals(50, WargearRules.totalPoints(listOf(
            WargearSelection("a", "shield-captain", "shield", 1, "Shield", 25, "PRICE_ONLY"),
            WargearSelection("b", "shield-captain", "shield", 1, "Shield", 25, "PRICE_ONLY")
        )))
    }
}
