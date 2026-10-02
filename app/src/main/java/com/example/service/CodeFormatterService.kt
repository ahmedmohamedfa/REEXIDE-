package com.example.service

class CodeFormatterService {

    fun format(source: String, indentSpaces: Int = 2): String {
        val lines = source.lines()
        val result = StringBuilder()
        var currentIndent = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                result.append("\n")
                continue
            }

            // Adjust indent down if line starts with closing bracket
            val leadingCloses = trimmed.takeWhile { it in "})]" }.length
            val effectiveIndent = (currentIndent - leadingCloses).coerceAtLeast(0)

            val indentPrefix = " ".repeat(effectiveIndent * indentSpaces)
            result.append(indentPrefix).append(trimmed).append("\n")

            // Count openers and closers in the line
            var inQuote = false
            var quoteChar = ' '
            var openers = 0
            var closers = 0

            for (i in trimmed.indices) {
                val c = trimmed[i]
                if (c == '\'' || c == '"') {
                    if (!inQuote) {
                        inQuote = true
                        quoteChar = c
                    } else if (c == quoteChar) {
                        inQuote = false
                    }
                } else if (!inQuote) {
                    if (c in "{([") openers++
                    if (c in "})]") closers++
                }
            }

            currentIndent += (openers - closers)
            if (currentIndent < 0) currentIndent = 0
        }

        return result.toString().trimEnd() + "\n"
    }
}
