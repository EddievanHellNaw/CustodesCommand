package com.example.custodescommand.core.data.repository

import com.example.custodescommand.core.data.local.dao.ReferenceDao
import com.example.custodescommand.core.domain.model.*
import org.json.JSONArray

/** Maps persisted, partially-transcribed references into a screen-readable model. */
class UnitDetailRepository(private val dao: ReferenceDao) {
    suspend fun findUnit(unitId: String): DetailedUnit? {
        val unit = dao.findUnit(unitId) ?: return null
        val profile = dao.findProfile(unitId) ?: return null
        val c = if (profile.movementInches != null &&
            profile.toughness != null &&
            profile.armourSave != null &&
            profile.wounds != null &&
            profile.leadership != null &&
            profile.objectiveControl != null
        ) UnitCharacteristics(
            movementInches = profile.movementInches,
            toughness = profile.toughness,
            armourSave = profile.armourSave,
            wounds = profile.wounds,
            leadership = profile.leadership,
            objectiveControl = profile.objectiveControl,
            invulnerableSave = profile.invulnerableSave
        ) else null

        val leaders = profile.leaderBodyguardIdsJson.readStringList()
        val bodyguards = leaders.map { dao.findUnit(it)?.name ?: it }

        return DetailedUnit(
            id = unit.id,
            name = unit.name,
            category = unit.category,
            playStatus = profile.playStatus,
            profileStatus = profile.profileStatus,
            allowedModelCounts = unit.allowedModelCounts,
            characteristics = c,
            keywords = profile.keywordsJson.readStringList(),
            weapons = dao.findWeapons(unitId).map { w ->
                WeaponProfile(
                    id = "${w.unitId}-${w.sortOrder}",
                    name = w.name,
                    type = WeaponType.valueOf(w.weaponType),
                    range = w.rangeText,
                    attacks = w.attacks,
                    skill = w.skill,
                    strength = w.strength,
                    armourPenetration = w.armourPenetration,
                    damage = w.damage,
                    abilities = w.abilitiesJson.readStringList()
                )
            },
            abilities = dao.findAbilities(unitId).map { RuleSnippet(it.name, it.ruleText) },
            wargearOptions = dao.findWargearOptions(unitId).map { it.description },
            eligibleBodyguardNames = bodyguards,
            pointTiers = dao.findPointTiers(unitId).map {
                PointCostTier(it.unitId, it.modelCount, it.fromOccurrence, it.throughOccurrence, it.points)
            },
            extraWargearCosts = dao.findOptionalWargearCosts(unitId).map {
                OptionalWargearCost(it.optionName, it.pointsPerItem)
            },
            sourcePage = profile.sourcePage,
            codexSections = dao.findCodexSections(unitId).map { section ->
                CodexSection(
                    kind = section.kind,
                    text = section.text,
                    sourcePage = section.sourcePage,
                    status = section.status
                )
            }
        )
    }
}

private fun String.readStringList(): List<String> = JSONArray(this).let { arr ->
    (0 until arr.length()).map { arr.getString(it) }
}
