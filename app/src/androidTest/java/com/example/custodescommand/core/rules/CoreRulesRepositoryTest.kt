package com.example.custodescommand.core.rules

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreRulesRepositoryTest {
    private val repo by lazy {
        CoreRuleRepository(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test fun bundledRulesLoadAndHaveStableIdentifiers() {
        val entries = repo.entries
        assertTrue(entries.size >= 75)
        assertEquals(entries.size, entries.map { it.id }.distinct().size)
        assertTrue(entries.all { it.summary.isNotBlank() && it.sourceUrl.startsWith("https://") })
    }

    @Test fun qualifiedWeaponAbilitiesResolveToBaseRules() {
        assertEquals("Assault", repo.forAbility("[ASSAULT]")?.title)
        assertEquals("Blast", repo.forAbility("BLAST 2")?.title)
        assertEquals("Anti", repo.forAbility("ANTI-VEHICLE 4+")?.title)
        assertEquals("Sustained Hits", repo.forAbility("SUSTAINED HITS 3: INFANTRY")?.title)
        assertEquals("Rapid Fire", repo.forAbility("RAPID FIRE 4")?.title)
        assertEquals("Feel No Pain", repo.forAbility("Feel No Pain 5+")?.title)
        assertEquals("Pistol", repo.forAbility("PISTOL")?.title)
        assertEquals("Twin-linked", repo.forAbility("TWIN–LINKED")?.title)
    }

    @Test fun newEditionRulesNotMistakenForTenthEdition() {
        val deepStrike = repo.forAbility("Deep Strike")!!
        assertTrue(deepStrike.summary.contains("8″"))
        val lethal = repo.forAbility("Lethal Hits")!!
        assertTrue(lethal.summary.contains("may choose"))
        assertTrue(repo.forAbility("Close-quarters") != null)
    }

    @Test fun searchFindsNamedRulesAndSections() {
        assertTrue(repo.search("wound", "Weapon Abilities").isNotEmpty())
        assertTrue(repo.search("24.23").any { it.title == "Lethal Hits" })
        assertTrue(repo.search("roster", "Core Abilities").isEmpty())
    }
}
