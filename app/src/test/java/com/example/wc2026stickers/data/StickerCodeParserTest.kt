package com.wc2026stickers.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StickerCodeParserTest {
    @Test
    fun `parse normalizes whitespace and casing`() {
        val result = StickerCodeParser.parse("  arg12 ")

        assertEquals(StickerCode("ARG", 12), result)
    }

    @Test
    fun `parse accepts hyphenated ids`() {
        val result = StickerCodeParser.parse("arg-12")

        assertEquals(StickerCode("ARG", 12), result)
    }

    @Test
    fun `parse supports special section codes`() {
        val result = StickerCodeParser.parse("fwc9")

        assertEquals(StickerCode("FWC", 9), result)
    }

    @Test
    fun `parse rejects malformed codes`() {
        assertNull(StickerCodeParser.parse("A1"))
        assertNull(StickerCodeParser.parse("ARG100"))
        assertNull(StickerCodeParser.parse("12ARG"))
    }
}
