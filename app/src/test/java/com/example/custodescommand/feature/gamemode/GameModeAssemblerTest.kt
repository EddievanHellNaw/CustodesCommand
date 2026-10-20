package com.example.custodescommand.feature.gamemode

import com.example.custodescommand.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class GameModeAssemblerTest {
    private val roster = Roster(
        id = "roster-1", name = "Golden Talon", pointsLimit = 1000,
        units = listOf(RosterUnit("unit-1", "allarus", 2))
    )
    private val units = listOf(UnitSummary("allarus", "Allarus Custodians",
        "Terminator Infantry", setOf(2, 3), SourceReference("Custodes PDF", 20)))
    private val priced = PointSummary("MFM v1.6", listOf(
        PointLine("unit-1", "allarus", 2, 1, 180)
    ), 180, 820)
    private val detachment = DetachmentDetail(
        summary = DetachmentSummary("lions", "Lions", 1, listOf("DISRUPTION"), false),
        ruleTitle = "On Gilded Wings", ruleText = "Reference summary",
        ruleTextStatus = "SUMMARY_CHECK_SCAN", favouredKatah = null,
        favouredKatahEffect = null,
        stratagems = listOf(StratagemReference("swift", "Swift Strike", 1,
            "Your Fight phase", null, null, null, "INDEX_ONLY", 14)),
        enhancements = emptyList(), sourcePages = listOf(14)
    )

    @Test fun combinesBaseEnhancementsAndWargearWithoutMutatingRoster() {
        val assigned = listOf(AssignedEnhancement("lions", "lion-e", "unit-1",
            "allarus", "Test enhancement", 20, "INDEX_ONLY", null, 14))
        val gear = listOf(WargearSelection("unit-1", "allarus", "vexilla", 1,
            "Vexilla", 0, "TRANSCRIBED"))
        val game = GameModeAssembler.assemble(roster, units, priced, null,
            setOf("lions"), listOf(detachment), assigned, gear, emptyMap())
        assertEquals(200, game.points.totalPoints)
        assertEquals(800, game.points.remainingPoints)
        assertEquals(1, game.dpUsed)
        assertEquals(1, game.stratagems.size)
        assertEquals("Allarus Custodians", game.enhancements.first().targetUnitName)
        assertEquals(1, roster.units.size)
    }

    @Test fun missingBasePricesRemainUnknownNotZero() {
        val game = GameModeAssembler.assemble(roster, units, null, "missing tier",
            emptySet(), emptyList(), emptyList(), emptyList(), emptyMap())
        assertNull(game.points.totalPoints)
        assertTrue(game.warnings.any { it.contains("missing tier") })
        assertEquals(0, game.dpUsed)
    }

    @Test fun missingDetachmentMakesDpUnknown() {
        val game = GameModeAssembler.assemble(roster, units, priced, null,
            setOf("lions", "unavailable"), listOf(detachment), emptyList(), emptyList(), emptyMap())
        assertNull(game.dpUsed)
        assertTrue(game.warnings.any { it.contains("definitions could not be loaded") })
    }

    @Test fun phaseFilteringIncludesAnyPhaseAndMixedPhases() {
        assertTrue(GamePhaseFilter.matches("Any phase", GamePhase.CHARGE))
        assertTrue(GamePhaseFilter.matches("End of any phase", GamePhase.COMMAND))
        assertTrue(GamePhaseFilter.matches("Your Shooting phase / Fight phase", GamePhase.FIGHT))
        assertTrue(GamePhaseFilter.matches("Opponent’s Shooting phase", GamePhase.SHOOTING))
        assertFalse(GamePhaseFilter.matches("Your Shooting phase", GamePhase.MOVEMENT))
    }
}
