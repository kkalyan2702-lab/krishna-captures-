package com.example.model

enum class PhotoSource {
    CURATED,
    USER_FILE,
    USER_FOLDER
}

data class PhotoItem(
    val id: String,
    val uriString: String,
    val title: String,
    val subtitle: String = "Krishna Captures Collection",
    val source: PhotoSource = PhotoSource.CURATED,
    val dateAdded: Long = System.currentTimeMillis()
)
