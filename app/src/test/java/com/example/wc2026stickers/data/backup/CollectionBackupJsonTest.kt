package com.wc2026stickers.app.data.backup

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionBackupJsonTest {
    @Test
    fun `encode and decode preserve collected stickers in stable order`() {
        val json = CollectionBackupJson.encode(
            CollectionBackupJson.createPayload(
                stickers = listOf(
                    BackupStickerQuantity("ARG-2", 1),
                    BackupStickerQuantity("ARG-1", 3)
                ),
                exportedAt = Instant.parse("2026-01-01T12:00:00Z")
            )
        )

        val payload = CollectionBackupJson.decode(json)

        assertEquals("2026-01-01T12:00:00Z", payload.exportedAt)
        assertEquals(
            listOf(
                BackupStickerQuantity("ARG-1", 3),
                BackupStickerQuantity("ARG-2", 1)
            ),
            payload.stickers
        )
        assertTrue(json.indexOf("ARG-1") < json.indexOf("ARG-2"))
    }

    @Test(expected = CollectionBackupException::class)
    fun `decode rejects duplicate sticker ids`() {
        CollectionBackupJson.decode(
            """
            {
              "type": "wc2026-stickers-collection-backup",
              "formatVersion": 1,
              "exportedAt": "2026-01-01T12:00:00Z",
              "stickers": [
                { "stickerId": "ARG-1", "quantityOwned": 1 },
                { "stickerId": "ARG-1", "quantityOwned": 2 }
              ]
            }
            """.trimIndent()
        )
    }

    @Test(expected = CollectionBackupException::class)
    fun `decode rejects fractional quantities`() {
        CollectionBackupJson.decode(
            """
            {
              "type": "wc2026-stickers-collection-backup",
              "formatVersion": 1,
              "exportedAt": "2026-01-01T12:00:00Z",
              "stickers": [
                { "stickerId": "ARG-1", "quantityOwned": 1.5 }
              ]
            }
            """.trimIndent()
        )
    }
}
