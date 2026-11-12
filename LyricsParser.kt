package com.example.universalmusicplayer

data class LyricLine(val timeMs: Long, val text: String)

object LyricsParser {
    private val timestamp = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?]")

    fun parse(lrc: String): List<LyricLine> {
        return lrc.lineSequence().mapNotNull { line ->
            val match = timestamp.find(line) ?: return@mapNotNull null
            val minutes = match.groupValues[1].toLong()
            val seconds = match.groupValues[2].toLong()
            val fraction = match.groupValues[3].padEnd(3, '0').ifEmpty { "000" }.toLong()
            val text = line.substring(match.range.last + 1).trim()
            LyricLine((minutes * 60 + seconds) * 1000 + fraction, text)
        }.sortedBy { it.timeMs }.toList()
    }
}
