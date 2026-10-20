package com.example.custodescommand.core.domain.model

/** SOURCE_READ records have been transcribed; SUMMARY_CHECK_SOURCE is not full rules wording. */
data class CodexSection(
    val kind: String,
    val text: String,
    val sourcePage: Int?,
    val status: String
)
