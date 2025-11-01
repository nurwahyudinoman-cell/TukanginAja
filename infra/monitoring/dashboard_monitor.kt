package com.tukangin.monitoring

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DashboardMonitor {

    private val reportsDir = File("reports")
    private val logsDir = File("logs")

    fun generateSummaryJson(): String {
        val reports = reportsDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.sortedBy { it.lastModified() }
            ?.map { it.name }
            ?: emptyList()

        val errorLogs = logsDir.listFiles()
            ?.filter { it.isFile && it.name.contains("error", ignoreCase = true) }
            ?.sortedBy { it.lastModified() }
            ?.map { it.name }
            ?: emptyList()

        val timestamp = System.currentTimeMillis()
        val formatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .format(Date(timestamp))

        val builder = StringBuilder()
        builder.append("{\n")
        builder.append("  \"date\": \"").append(formatted).append("\",\n")
        builder.append("  \"reports_collected\": ${reports.size},\n")
        builder.append("  \"error_logs_detected\": ${errorLogs.size},\n")
        builder.append("  \"latest_reports\": [")
        builder.append(reports.takeLast(5).joinToString(separator = ", ") { "\"$it\"" })
        builder.append("],\n")
        builder.append("  \"latest_logs\": [")
        builder.append(errorLogs.takeLast(5).joinToString(separator = ", ") { "\"$it\"" })
        builder.append("]\n")
        builder.append("}")
        return builder.toString()
    }

    fun writeSummary() {
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val summaryJson = generateSummaryJson()
        val file = File(reportsDir, "release-monitoring-${System.currentTimeMillis()}.json")
        file.writeText(summaryJson)
        println("📊 Monitoring summary updated: ${file.name}")
    }
}

fun main() {
    DashboardMonitor.writeSummary()
}

