package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.example.model.PhotoItem
import com.example.model.PhotoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PhotoFolderHelper {

    suspend fun queryPhotosFromFolder(context: Context, treeUri: Uri): List<PhotoItem> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<PhotoItem>()
        try {
            // Take persistable permission if not already taken
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
            } catch (_: Exception) {}

            val documentId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )

            context.contentResolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val modifiedIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val childDocId = cursor.getString(idIndex)
                    val mimeType = cursor.getString(mimeIndex)
                    val displayName = cursor.getString(nameIndex) ?: "Photo"
                    val lastModified = cursor.getLong(modifiedIndex)

                    if (mimeType != null && mimeType.startsWith("image/")) {
                        val photoUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId)
                        photos.add(
                            PhotoItem(
                                id = "folder_${childDocId.hashCode()}",
                                uriString = photoUri.toString(),
                                title = displayName.substringBeforeLast('.'),
                                subtitle = "Folder Shot • $displayName",
                                source = PhotoSource.USER_FOLDER,
                                dateAdded = if (lastModified > 0) lastModified else System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        photos
    }

    suspend fun parsePickedPhotos(context: Context, uris: List<Uri>): List<PhotoItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<PhotoItem>()
        for (uri in uris) {
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}

                var displayName = "Imported Shot"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex) ?: displayName
                        }
                    }
                }

                list.add(
                    PhotoItem(
                        id = "user_photo_${uri.hashCode()}_${System.currentTimeMillis()}",
                        uriString = uri.toString(),
                        title = displayName.substringBeforeLast('.'),
                        subtitle = "Imported Photography • $displayName",
                        source = PhotoSource.USER_FILE,
                        dateAdded = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        list
    }
}
