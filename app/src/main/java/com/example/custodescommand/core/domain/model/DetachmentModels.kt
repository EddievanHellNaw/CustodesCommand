package com.example.custodescommand.core.domain.model

data class DetachmentSummary(
    val id: String,
    val name: String,
    val dpCost: Int,
    val dispositions: List<String>,
    val uniqueShieldHost: Boolean
)

data class StratagemReference(
    val id: String,
    val name: String,
    val cpCost: Int,
    val phase: String,
    val whenText: String?,
    val targetText: String?,
    val effectText: String?,
    val textStatus: String,
    val sourcePage: Int
)

data class EnhancementReference(
    val id: String,
    val name: String,
    val points: Int,
    val effectText: String?,
    val eligibilityText: String?,
    val textStatus: String
)

data class DetachmentDetail(
    val summary: DetachmentSummary,
    val ruleTitle: String,
    val ruleText: String,
    val ruleTextStatus: String,
    val favouredKatah: String?,
    val favouredKatahEffect: String?,
    val stratagems: List<StratagemReference>,
    val enhancements: List<EnhancementReference>,
    val sourcePages: List<Int>
)
