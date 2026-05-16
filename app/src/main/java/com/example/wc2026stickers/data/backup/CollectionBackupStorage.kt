package com.wc2026stickers.app.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface CollectionBackupStorage {
    @Throws(IOException::class)
    fun readText(uri: Uri): String

    @Throws(IOException::class)
    fun writeText(uri: Uri, value: String)
}

@Singleton
class AndroidCollectionBackupStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : CollectionBackupStorage {
    override fun readText(uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
            reader.readText()
        } ?: throw IOException("Couldn't open the selected backup file.")

    override fun writeText(uri: Uri, value: String) {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write(value)
        } ?: throw IOException("Couldn't write the backup file.")
    }
}
