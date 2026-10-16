
package com.example.custodescommand.navigation

enum class MainTab(
    val key: String,
    val label: String,
    val symbol: String
) {
    Rosters(
        key = "rosters",
        label = "Rosters",
        symbol = "▤"
    ),

    Datasheets(
        key = "datasheets",
        label = "Datasheets",
        symbol = "◈"
    ),

    Detachments(
        key = "detachments",
        label = "Detachments",
        symbol = "⚔"
    ),

    Rules(
        key = "rules",
        label = "Rules",
        symbol = "§"
    )
}
