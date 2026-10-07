package com.example.cartonplan.data

import com.example.cartonplan.model.SavedCalculation

object CalculationJsonCodec {

    fun listToJson(items: List<SavedCalculation>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        items.forEachIndexed { index, item ->
            sb.append("  ").append(toJson(item))
            if (index < items.size - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun toJson(item: SavedCalculation): String {
        return buildString {
            append("{")
            append("\"id\":").append(escapeString(item.id)).append(",")
            append("\"timestamp\":").append(item.timestamp).append(",")
            append("\"projectName\":").append(escapeString(item.projectName)).append(",")
            append("\"totalItems\":").append(item.totalItems).append(",")
            append("\"itemsPerCarton\":").append(item.itemsPerCarton).append(",")
            append("\"includePlanningAllowance\":").append(item.includePlanningAllowance).append(",")
            append("\"planningAllowancePercent\":").append(escapeString(item.planningAllowancePercent)).append(",")
            append("\"planningQuantity\":").append(item.planningQuantity).append(",")
            append("\"extraCartons\":").append(item.extraCartons).append(",")
            append("\"includeExtraCartons\":").append(item.includeExtraCartons).append(",")
            append("\"requiredCartons\":").append(item.requiredCartons).append(",")
            append("\"plannedCartons\":").append(item.plannedCartons).append(",")
            append("\"fullCartons\":").append(item.fullCartons).append(",")
            append("\"partialCartons\":").append(item.partialCartons).append(",")
            append("\"finalCartonItems\":").append(item.finalCartonItems).append(",")
            append("\"itemsPacked\":").append(item.itemsPacked).append(",")
            append("\"unusedCapacity\":").append(item.unusedCapacity).append(",")
            append("\"utilizationPercent\":").append(escapeString(item.utilizationPercent)).append(",")
            append("\"isFavorite\":").append(item.isFavorite)
            append("}")
        }
    }

    fun listFromJson(json: String): List<SavedCalculation> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]" || !trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return emptyList()
        }

        val result = mutableListOf<SavedCalculation>()
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        // Split objects by matching balanced braces
        var braceCount = 0
        var inQuotes = false
        var isEscaped = false
        val currentObj = StringBuilder()

        for (char in content) {
            if (isEscaped) {
                currentObj.append(char)
                isEscaped = false
                continue
            }
            if (char == '\\') {
                currentObj.append(char)
                isEscaped = true
                continue
            }
            if (char == '"') {
                inQuotes = !inQuotes
                currentObj.append(char)
                continue
            }

            if (!inQuotes) {
                if (char == '{') {
                    braceCount++
                    currentObj.append(char)
                } else if (char == '}') {
                    braceCount--
                    currentObj.append(char)
                    if (braceCount == 0) {
                        val objStr = currentObj.toString().trim()
                        if (objStr.isNotEmpty()) {
                            fromJson(objStr)?.let { result.add(it) }
                        }
                        currentObj.clear()
                    }
                } else if (char == ',' && braceCount == 0) {
                    // separator between objects, ignore
                } else {
                    currentObj.append(char)
                }
            } else {
                currentObj.append(char)
            }
        }

        return result
    }

    fun fromJson(json: String): SavedCalculation? {
        val trimmed = json.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null

        val map = parseSimpleJsonObject(trimmed)
        return try {
            SavedCalculation(
                id = map["id"] ?: "",
                timestamp = map["timestamp"]?.toLongOrNull() ?: 0L,
                projectName = map["projectName"] ?: "",
                totalItems = map["totalItems"]?.toIntOrNull() ?: 0,
                itemsPerCarton = map["itemsPerCarton"]?.toIntOrNull() ?: 1,
                includePlanningAllowance = map["includePlanningAllowance"]?.toBooleanStrictOrNull() ?: false,
                planningAllowancePercent = map["planningAllowancePercent"] ?: "0",
                planningQuantity = map["planningQuantity"]?.toIntOrNull() ?: 0,
                extraCartons = map["extraCartons"]?.toIntOrNull() ?: 0,
                includeExtraCartons = map["includeExtraCartons"]?.toBooleanStrictOrNull() ?: false,
                requiredCartons = map["requiredCartons"]?.toIntOrNull() ?: 0,
                plannedCartons = map["plannedCartons"]?.toIntOrNull() ?: 0,
                fullCartons = map["fullCartons"]?.toIntOrNull() ?: 0,
                partialCartons = map["partialCartons"]?.toIntOrNull() ?: 0,
                finalCartonItems = map["finalCartonItems"]?.toIntOrNull() ?: 0,
                itemsPacked = map["itemsPacked"]?.toLongOrNull() ?: 0L,
                unusedCapacity = map["unusedCapacity"]?.toLongOrNull() ?: 0L,
                utilizationPercent = map["utilizationPercent"] ?: "0",
                isFavorite = map["isFavorite"]?.toBooleanStrictOrNull() ?: false
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSimpleJsonObject(objStr: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val inner = objStr.trim().removeSurrounding("{", "}").trim()
        var inQuotes = false
        var isEscaped = false
        val currentToken = StringBuilder()
        var currentKey = ""
        var isReadingValue = false

        for (char in inner) {
            if (isEscaped) {
                currentToken.append(char)
                isEscaped = false
                continue
            }
            if (char == '\\') {
                isEscaped = true
                continue
            }
            if (char == '"') {
                inQuotes = !inQuotes
                continue
            }

            if (!inQuotes) {
                if (char == ':' && !isReadingValue) {
                    currentKey = currentToken.toString().trim()
                    currentToken.clear()
                    isReadingValue = true
                } else if (char == ',') {
                    if (isReadingValue && currentKey.isNotEmpty()) {
                        result[currentKey] = currentToken.toString().trim()
                        currentToken.clear()
                        currentKey = ""
                        isReadingValue = false
                    }
                } else {
                    currentToken.append(char)
                }
            } else {
                currentToken.append(char)
            }
        }

        if (isReadingValue && currentKey.isNotEmpty()) {
            result[currentKey] = currentToken.toString().trim()
        }

        return result
    }

    private fun escapeString(str: String): String {
        val escaped = str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}
