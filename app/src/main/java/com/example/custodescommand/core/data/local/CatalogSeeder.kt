
package com.example.custodescommand.core.data.local

import androidx.room.withTransaction
import com.example.custodescommand.core.data.SampleCustodesCatalog
import com.example.custodescommand.core.data.local.entity.*

class CatalogSeeder(
    private val database: CustodesDatabase
) {

    suspend fun seedIfEmpty() {
        val dao = database.referenceDao()

        database.withTransaction {

            if (dao.unitCount() > 0) {
                return@withTransaction
            }

            val sample = SampleCustodesCatalog

            dao.insertMetadata(
                CatalogMetadataEntity(
                    id = "custodes",
                    pointsVersion = sample.pointsCatalog.version
                )
            )

            dao.insertUnits(
                sample.units.map { unit ->
                    UnitEntity(
                        id = unit.id,
                        name = unit.name,
                        category = unit.category,
                        allowedModelCounts = unit.allowedModelCounts,
                        sourceDocument = unit.source?.document,
                        sourcePage = unit.source?.page
                    )
                }
            )

            dao.insertPointTiers(
                sample.pointsCatalog.tiers.map { tier ->
                    PointTierEntity(
                        unitId = tier.unitId,
                        modelCount = tier.modelCount,
                        fromOccurrence = tier.fromOccurrence,
                        throughOccurrence = tier.throughOccurrence,
                        points = tier.points
                    )
                }
            )
        }
    }
}
