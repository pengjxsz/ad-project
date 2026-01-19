package io.xa.sigad.data

import kotlinx.serialization.Serializable

@Serializable
data class FileItem(
    val resource: String,
    val thumbnailResource: String,
    val name: String
)

@Serializable
data class FileItemList(
    val items: List<FileItem>
)