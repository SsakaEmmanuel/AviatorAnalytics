package com.example.data.datasource

import com.example.domain.model.GameRound

object CsvDataHandler {

    fun exportToCsv(rounds: List<GameRound>): String {
        val sb = StringBuilder()
        sb.append("RoundID,Multiplier,Timestamp,Date,Source\n")
        rounds.forEach { r ->
            sb.append("${r.roundId},${String.format("%.2f", r.multiplier)},${r.timestamp},${r.formattedDate},${r.source}\n")
        }
        return sb.toString()
    }

    fun parseCsv(csvContent: String): List<GameRound> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val parsedRounds = mutableListOf<GameRound>()
        val dataLines = if (lines.first().contains("RoundID", ignoreCase = true) || lines.first().contains("Multiplier", ignoreCase = true)) {
            lines.drop(1)
        } else {
            lines
        }

        dataLines.forEachIndexed { index, line ->
            val tokens = line.split(",").map { it.trim() }
            if (tokens.isNotEmpty()) {
                val roundId = if (tokens.size > 1 && tokens[0].isNotBlank()) tokens[0] else "IMP-${System.currentTimeMillis()}-$index"
                val multStr = if (tokens.size > 1) tokens[1] else tokens[0]
                val multiplier = multStr.replace("x", "", ignoreCase = true).toDoubleOrNull()
                val timestamp = if (tokens.size > 2) tokens[2].toLongOrNull() ?: System.currentTimeMillis() else System.currentTimeMillis()
                val source = if (tokens.size > 4) tokens[4] else "CSV_IMPORT"

                if (multiplier != null && multiplier >= 1.0) {
                    parsedRounds.add(
                        GameRound(
                            roundId = roundId,
                            multiplier = multiplier,
                            timestamp = timestamp,
                            source = source
                        )
                    )
                }
            }
        }
        return parsedRounds
    }
}
