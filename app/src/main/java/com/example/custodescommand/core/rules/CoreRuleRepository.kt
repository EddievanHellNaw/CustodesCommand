package com.example.custodescommand.core.rules

import android.content.Context
import org.json.JSONObject
import java.text.Normalizer

/**
 * Read-only bundled reference. Nothing here writes to Room or to the user's roster.
 * Each explanation is a short, original summary; official text remains at sourceUrl.
 */
data class CoreRule(
    val id: String,
    val number: String,
    val title: String,
    val category: String,
    val summary: String,
    val context: String,
    val aliases: List<String>,
    val tags: List<String>,
    val sourceUrl: String
)

class CoreRuleRepository(context: Context) {
    private val appContext = context.applicationContext
    private val assetName = "rules/core_rules_11ed_quickref_v1.json"
    val officialPdfUrl: String =
        "https://assets.warhammer-community.com/eng_01-06_warhammer40k_new40k_core_rules-was6fbu1ix-hfewhmxyiy.pdf"

    /** Thread-safe lazy loading, performed at most once per repository instance. */
    val entries: List<CoreRule> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val content = appContext.assets.open(assetName).bufferedReader().use { it.readText() }
        val root = JSONObject(content)
        require(root.getInt("edition") == 11) { "Wrong Core Rules edition" }
        val input = root.getJSONArray("rules")
        List(input.length()) { i ->
            val item = input.getJSONObject(i)
            fun stringList(key: String): List<String> {
                val arr = item.optJSONArray(key) ?: return emptyList()
                return List(arr.length()) { j -> arr.getString(j) }
            }
            CoreRule(
                id = item.getString("id"),
                number = item.getString("number"),
                title = item.getString("title"),
                category = item.getString("category"),
                summary = item.getString("summary"),
                context = item.optString("context"),
                aliases = stringList("aliases"),
                tags = stringList("tags"),
                sourceUrl = item.optString("sourceUrl", officialPdfUrl)
            )
        }
    }

    fun find(id: String): CoreRule? = entries.firstOrNull { it.id == id }

    fun search(query: String, category: String? = null): List<CoreRule> {
        val q = normalize(query)
        return entries.filter { rule ->
            (category == null || category == "All" || rule.category == category) &&
                (q.isBlank() || listOf(rule.title, rule.number, rule.summary, rule.context)
                    .plus(rule.aliases).plus(rule.tags)
                    .any { normalize(it).contains(q) })
        }
    }

    /** E.g. [ASSAULT], BLAST 1, ANTI-VEHICLE 4+, Feel No Pain 4+. */
    fun forAbility(label: String): CoreRule? {
        val normalized = normalize(label)
        if (normalized.isBlank()) return null
        // Prioritize exact names/aliases to avoid mistakenly matching a similar rule.
        entries.firstOrNull { rule ->
            (rule.aliases + rule.title).any { normalize(it) == normalized }
        }?.let { return it }

        // Parameters change without changing which ability defines the rule.
        val key = when {
            normalized.startsWith("ANTI ") || normalized.startsWith("ANTI-") -> "ANTI"
            normalized.startsWith("BLAST ") -> "BLAST"
            normalized.startsWith("CLEAVE ") -> "CLEAVE"
            normalized.startsWith("SUSTAINED HITS ") -> "SUSTAINED HITS"
            normalized.startsWith("RAPID FIRE ") -> "RAPID FIRE"
            normalized.startsWith("MELTA ") -> "MELTA"
            normalized.startsWith("DEADLY DEMISE ") -> "DEADLY DEMISE"
            normalized.startsWith("FEEL NO PAIN ") -> "FEEL NO PAIN"
            normalized.startsWith("SCOUTS ") -> "SCOUTS"
            normalized.startsWith("FIRING DECK ") -> "FIRING DECK"
            normalized.startsWith("LONE OPERATIVE ") -> "LONE OPERATIVE"
            normalized.startsWith("DAMAGED ") -> "DAMAGED"
            else -> normalized.substringBefore(":").trim()
        }
        return entries.firstOrNull { rule ->
            (rule.aliases + rule.title).any { normalize(it) == key }
        }
    }

    companion object {
        @Volatile private var shared: CoreRuleRepository? = null

        /** Share one parsed reference across screens and weapon rows. */
        fun from(context: Context): CoreRuleRepository =
            shared ?: synchronized(this) {
                shared ?: CoreRuleRepository(context.applicationContext).also { shared = it }
            }

        fun normalize(value: String): String {
            val plain = Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replace(Regex("\\p{M}+"), "")
                .uppercase()
                .replace('‑', '-')
                .replace('–', '-')
                .replace('—', '-')
                .replace(Regex("[\\[\\]{}]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
            return plain
        }
    }
}
