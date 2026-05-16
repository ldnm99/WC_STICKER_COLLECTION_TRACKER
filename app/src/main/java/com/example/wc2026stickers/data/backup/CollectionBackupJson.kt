package com.wc2026stickers.app.data.backup

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class BackupStickerQuantity(
    val stickerId: String,
    val quantityOwned: Int
)

data class CollectionBackupPayload(
    val exportedAt: String,
    val stickers: List<BackupStickerQuantity>,
    val formatVersion: Int = CollectionBackupJson.formatVersion,
    val type: String = CollectionBackupJson.fileType
)

data class CollectionRestorePreview(
    val backup: CollectionBackupPayload,
    val validEntries: List<BackupStickerQuantity>,
    val skippedStickerIds: List<String>
)

enum class CollectionRestoreMode {
    MERGE,
    REPLACE
}

data class CollectionRestoreResult(
    val mode: CollectionRestoreMode,
    val importedStickerCount: Int,
    val unchangedStickerCount: Int,
    val skippedStickerCount: Int
)

class CollectionBackupException(message: String) : IllegalArgumentException(message)

object CollectionBackupJson {
    const val fileType = "wc2026-stickers-collection-backup"
    const val formatVersion = 1

    private val exportedAtFormatter = DateTimeFormatter.ISO_INSTANT
    private val fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

    fun createPayload(
        stickers: List<BackupStickerQuantity>,
        exportedAt: Instant = Instant.now()
    ): CollectionBackupPayload = CollectionBackupPayload(
        exportedAt = exportedAtFormatter.format(exportedAt),
        stickers = stickers
            .filter { it.quantityOwned > 0 }
            .sortedBy { it.stickerId }
    )

    fun encode(payload: CollectionBackupPayload): String {
        val stickers = JsonArray()
        payload.stickers
            .filter { it.quantityOwned > 0 }
            .sortedBy { it.stickerId }
            .forEach { sticker ->
                stickers.add(
                    JsonObject().apply {
                        add("stickerId", JsonPrimitive(sticker.stickerId))
                        add("quantityOwned", JsonPrimitive(sticker.quantityOwned))
                    }
                )
            }

        return JsonObject().apply {
            add("type", JsonPrimitive(payload.type))
            add("formatVersion", JsonPrimitive(payload.formatVersion))
            add("exportedAt", JsonPrimitive(payload.exportedAt))
            add("stickers", stickers)
        }.toString()
    }

    fun decode(json: String): CollectionBackupPayload {
        val root = try {
            JsonParser.parseString(json).asJsonObject
        } catch (_: IllegalStateException) {
            throw CollectionBackupException("Backup file is not valid JSON.")
        } catch (_: JsonSyntaxException) {
            throw CollectionBackupException("Backup file is not valid JSON.")
        }

        val type = root.requiredString("type")
        if (type != fileType) {
            throw CollectionBackupException("Backup file type is not supported.")
        }

        val version = parseWholeNumber(root.get("formatVersion"), "formatVersion")
        if (version != formatVersion) {
            throw CollectionBackupException("Backup format version $version is not supported.")
        }

        val exportedAt = root.requiredString("exportedAt")
        try {
            Instant.parse(exportedAt)
        } catch (_: Exception) {
            throw CollectionBackupException("Backup exportedAt timestamp is invalid.")
        }

        val stickersArray = root.getAsJsonArray("stickers")
            ?: throw CollectionBackupException("Backup file is missing stickers.")
        val seenStickerIds = mutableSetOf<String>()
        val stickers = buildList {
            stickersArray.forEachIndexed { index, stickerElement ->
                val stickerObject = stickerElement.asJsonObjectOrNull()
                    ?: throw CollectionBackupException("Sticker entry #${index + 1} is invalid.")
                val stickerId = stickerObject.requiredString("stickerId")
                if (!seenStickerIds.add(stickerId)) {
                    throw CollectionBackupException("Sticker $stickerId appears more than once in the backup.")
                }

                val quantityOwned = parseWholeNumber(
                    stickerObject.get("quantityOwned"),
                    "quantityOwned for $stickerId"
                )
                if (quantityOwned < 0) {
                    throw CollectionBackupException("Sticker $stickerId has a negative quantity.")
                }

                add(BackupStickerQuantity(stickerId = stickerId, quantityOwned = quantityOwned))
            }
        }

        return CollectionBackupPayload(
            exportedAt = exportedAt,
            stickers = stickers,
            formatVersion = version,
            type = type
        )
    }

    fun suggestedFileName(now: ZonedDateTime = ZonedDateTime.now()): String =
        "wc2026-stickers-backup-${fileNameFormatter.format(now)}.json"

    private fun parseWholeNumber(value: JsonElement?, fieldName: String): Int {
        val primitive = value as? JsonPrimitive
            ?: throw CollectionBackupException("Backup $fieldName must be a number.")
        if (!primitive.isNumber) {
            throw CollectionBackupException("Backup $fieldName must be a number.")
        }
        val doubleValue = primitive.asDouble
        if (!doubleValue.isFinite() || doubleValue != doubleValue.toInt().toDouble()) {
            throw CollectionBackupException("Backup $fieldName must be a whole number.")
        }
        return doubleValue.toInt()
    }
}

private fun JsonObject.requiredString(fieldName: String): String {
    val value = get(fieldName) as? JsonPrimitive
        ?: throw CollectionBackupException("Backup file is missing $fieldName.")
    if (!value.isString) {
        throw CollectionBackupException("Backup $fieldName must be text.")
    }
    return value.asString.trim().ifBlank {
        throw CollectionBackupException("Backup file is missing $fieldName.")
    }
}

private fun JsonElement.asJsonObjectOrNull(): JsonObject? = try {
    asJsonObject
} catch (_: IllegalStateException) {
    null
}
