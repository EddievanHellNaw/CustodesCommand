
package com.example.custodescommand.core.domain.service

import com.example.custodescommand.core.data.SampleCustodesCatalog
import com.example.custodescommand.core.domain.model.Roster
import com.example.custodescommand.core.domain.model.RosterUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class PointsCalculatorTest {

    private val calculator = PointsCalculator(
        SampleCustodesCatalog.pointsCatalog
    )

    private fun makeRoster(
        vararg units: RosterUnit
    ) = Roster(
        id = "test-roster",
        name = "Test Army",
        pointsLimit = 2000,
        units = units.toList()
    )

    @Test
    fun threeAllarusSquadsUseCorrectTiers() {

        val roster = makeRoster(
            RosterUnit("a1", "allarus", 2),
            RosterUnit("a2", "allarus", 2),
            RosterUnit("a3", "allarus", 2)
        )

        val result = calculator.calculate(roster)

        assertEquals(570, result.totalPoints)
        assertEquals(1430, result.remainingPoints)
        assertEquals(
            listOf(180, 180, 210),
            result.lines.map { it.points }
        )
    }

    @Test
    fun differentSquadSizesAreCalculated() {

        val roster = makeRoster(
            RosterUnit("a1", "allarus", 2),
            RosterUnit("a2", "allarus", 3),
            RosterUnit("a3", "allarus", 2)
        )

        val result = calculator.calculate(roster)

        assertEquals(660, result.totalPoints)
    }

    @Test
    fun wardensUseSecondUnitPricing() {

        val roster = makeRoster(
            RosterUnit("w1", "wardens", 3),
            RosterUnit("w2", "wardens", 3)
        )

        val result = calculator.calculate(roster)

        assertEquals(620, result.totalPoints)
    }

    @Test
    fun differentDatasheetsHaveSeparateOccurrences() {

        val roster = makeRoster(
            RosterUnit("a1", "allarus", 2),
            RosterUnit("w1", "wardens", 3),
            RosterUnit("a2", "allarus", 2),
            RosterUnit("v1", "venatari-lances", 3)
        )

        val result = calculator.calculate(roster)

        assertEquals(925, result.totalPoints)
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingPricingTierIsRejected() {

        val roster = makeRoster(
            RosterUnit("a1", "allarus", 4)
        )

        calculator.calculate(roster)
    }
}
