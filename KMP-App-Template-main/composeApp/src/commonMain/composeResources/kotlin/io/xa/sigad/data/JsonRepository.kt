package io.xa.sigad.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem // The actual FileSystem implementation
import kotlinx.io.readString

class DataRepository {
    private val fileName = "file.json"
    private val json = Json { ignoreUnknownKeys = true }
    private val storedObjects = MutableStateFlow(emptyList<FileItem>())

    private val scope = CoroutineScope(SupervisorJob())

    fun initialize() {
        scope.launch {
            storedObjects.value = loadData()
        }
    }


    // Using SystemFileSystem directly from commonMain (it's 'expect' behind the scenes)
    suspend fun loadData(): List<FileItem> {
        val path = Path(fileName) // Create a Path object for your file

        return if (SystemFileSystem.exists(path)) {
            // Read the content of the file
            SystemFileSystem.source(path).buffered().use { source ->
                val jsonString = source.readString() // kotlinx-io provides readString()
                json.decodeFromString(FileItemList.serializer(), jsonString).items
            }
        } else {
            println("File not found: $fileName")
            emptyList()
        }
    }

    fun getObjects(): Flow<List<FileItem>> = storedObjects
}



val resourcePictures = listOf(
    FileItem(
        resource = "img_1.jpg",
        thumbnailResource = "img_1_t.jpg",
        name = "Mountain K2",

    ),
    FileItem(
        resource = "img_2.jpg",
        thumbnailResource = "img_2_t.jpg",
        name = "Kina The Calico",

    ),
    FileItem(
        resource = "img_3.jpg",
        thumbnailResource = "img_3_t.jpg",
        name = "Blue City",

    ),
    FileItem(
        resource = "img_4.jpg",
        thumbnailResource = "img_4_t.jpg",
        name = "Tokyo Skytree",

    ),
    FileItem(
        resource = "img_5.jpg",
        thumbnailResource = "img_5_t.jpg",
        name = "Taranaki",

    ),
    FileItem(
        resource = "img_6.jpg",
        thumbnailResource = "img_6_t.jpg",
        name = "Auckland SkyCity",

    ),
    FileItem(
        resource = "img_7.jpg",
        thumbnailResource = "img_7_t.jpg",
        name = "Berliner Fernsehturm",

    ),
    FileItem(
        resource = "img_8.jpg",
        thumbnailResource = "img_8_t.jpg",
        name = "Hoggar Mountains",

    ),
    FileItem(
        resource = "img_9.jpg",
        thumbnailResource = "img_9_t.jpg",
        name = "Nakhal Fort",

    ),
    FileItem(
        resource = "img_10.jpg",
        thumbnailResource = "img_10_t.jpg",
        name = "Mountain Ararat",

    ),
    FileItem(
        resource = "img_11.jpg",
        thumbnailResource = "img_11_t.jpg",
        name = "Cabo da Roca",

    ),
    FileItem(
        resource = "img_12.jpg",
        thumbnailResource = "img_12_t.jpg",
        name = "Surprised Whiskers 🐱",

    ),
    FileItem(
        resource = "img_13.jpg",
        thumbnailResource = "img_13_t.jpg",
        name = "Software Engineering Donut",

    ),
    FileItem(
        resource = "img_14.jpg",
        thumbnailResource = "img_14_t.jpg",
        name = "Seligman Police Car.",

    ),
    FileItem(
        resource = "img_15.jpg",
        thumbnailResource = "img_15_t.jpg",
        name = "Good Luck Charms",

    ),
    FileItem(
        resource = "img_16.jpg",
        thumbnailResource = "img_16_t.jpg",
        name = "Pong Restaurant",

    ),
    FileItem(
        resource = "img_17.jpg",
        thumbnailResource = "img_17_t.jpg",
        name = "Loki",

    ),
)
