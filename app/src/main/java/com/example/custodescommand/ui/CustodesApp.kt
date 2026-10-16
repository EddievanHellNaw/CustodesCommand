
package com.example.custodescommand.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private data class AppSection(
    val title: String,
    val description: String
)

private val sections = listOf(
    AppSection(
        title = "My Rosters",
        description = "Create and manage your Custodes armies."
    ),
    AppSection(
        title = "Datasheets",
        description = "Consult unit characteristics and abilities."
    ),
    AppSection(
        title = "Detachments",
        description = "Browse rules, stratagems and enhancements."
    ),
    AppSection(
        title = "Core Rules",
        description = "Search the Warhammer 40,000 rulebook."
    )
)

@Composable
fun CustodesApp() {
    Scaffold { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Column(
                    verticalArrangement =
                    Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Custodes Command",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = "Your offline Adeptus Custodes companion",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            items(sections, key = { it.title }) { section ->
                SectionPreview(section)
            }

            item {
                Text(
                    text = "Prototype 0.1 - Project foundation",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun SectionPreview(section: AppSection) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = section.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustodesAppPreview() {
    MaterialTheme {
        CustodesApp()
    }
}
