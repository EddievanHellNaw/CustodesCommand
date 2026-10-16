
package com.example.custodescommand.feature.datasheets

data class UnitPreview(
    val id: String,
    val name: String,
    val category: String
)

val sampleUnits = listOf(
    UnitPreview(
        id = "allarus",
        name = "Allarus Custodians",
        category = "Terminator Infantry"
    ),

    UnitPreview(
        id = "wardens",
        name = "Custodian Wardens",
        category = "Infantry"
    ),

    UnitPreview(
        id = "venatari-lances",
        name = "Venatari with Verutum Lances",
        category = "Jump Pack Infantry"
    ),

    UnitPreview(
        id = "caladius-annihilator",
        name = "Caladius Annihilator Grav-Tank",
        category = "Vehicle"
    )
)
