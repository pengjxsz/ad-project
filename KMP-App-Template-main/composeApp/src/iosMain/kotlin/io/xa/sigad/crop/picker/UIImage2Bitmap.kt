package io.xa.sigad.crop.picker

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.*
import platform.CoreGraphics.*

import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage

import platform.UIKit.UIImage
import platform.Foundation.*
import androidx.compose.ui.graphics.toPixelMap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

const val kCGImageAlphaPremultipliedLast = 1U

fun extractPixelData(imageBitmap: ImageBitmap): IntArray {
    val pixelMap = imageBitmap.toPixelMap() // Converts ImageBitmap to PixelMap
    val width = imageBitmap.width
    val height = imageBitmap.height
    val pixels = IntArray(width * height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = pixelMap[x, y] // Get the Color object
            val red = (color.red * 255).toInt() and 0xFF // Red channel (0-255)
            val green = (color.green * 255).toInt() and 0xFF // Green channel (0-255)
            val blue = (color.blue * 255).toInt() and 0xFF // Blue channel (0-255)
            val alpha = (color.alpha * 255).toInt() and 0xFF // Alpha channel (0-255)

            // Combine ARGB channels into a single Int
            pixels[y * width + x] = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }
    }
    return pixels
}

fun ImageBitmap.encodeToPngBytes(): ByteArray? {
    return Image.makeFromBitmap(this.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData? {
    return this.usePinned {
        NSData.create(bytes = it.addressOf(0), length = this.size.toULong())    }
}

fun UIImageFromPng(bitmap: ImageBitmap): UIImage?{
    val pngData = bitmap.encodeToPngBytes();
    val pngImage = pngData?.toNSData()?.let { UIImage(it) };
    return pngImage;
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun IntArray.toNSData(): NSData {
    // Use Kotlin/Native's memory management to create a buffer
    val byteArray = ByteArray(this.size * 4) // Each Int is 4 bytes (32 bits)
    for (i in this.indices) {
        val value = this[i]
        byteArray[i * 4 + 0] = (value shr 24).toByte() // Extract alpha
        byteArray[i * 4 + 1] = (value shr 16).toByte() // Extract red
        byteArray[i * 4 + 2] = (value shr 8).toByte()  // Extract green
        byteArray[i * 4 + 3] = (value).toByte()        // Extract blue
    }

    // Convert ByteArray to NSData
    return byteArray.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = byteArray.size.toULong())
    }
}


@OptIn(ExperimentalForeignApi::class)
fun byteArrayToNSData(byteArray: ByteArray): NSData {
    return byteArray.usePinned {
        NSData.dataWithBytes(it.addressOf(0), byteArray.size.toULong())
    }
}

fun byteArrayToUIImage(byteArray: ByteArray): UIImage? {
    val nsData = byteArrayToNSData(byteArray)
    return UIImage(data = nsData)
}


@OptIn(ExperimentalForeignApi::class)
fun convertToUIImage(pixels: IntArray, width: Int, height: Int): UIImage? {
    // Convert the pixel data to NSData
    val pixelData = pixels.toNSData()

    // Create a color space
    val colorSpace = CGColorSpaceCreateDeviceRGB()

    // Create a CGContext for the pixel data
    val context = CGBitmapContextCreate(
        data = pixelData.bytes, // Pointer to pixel data
        width = width.toULong(),
        height = height.toULong(),
        bitsPerComponent = 8u, // Each color component is 8 bits
        bytesPerRow = (width * 4).toULong(), // 4 bytes per pixel (ARGB)
        space = colorSpace,
        bitmapInfo = kCGImageAlphaPremultipliedLast or kCGBitmapByteOrder32Big // Bitmap info
    )

    // Check if the context is null (error in creation)
    if (context == null) {
        return null
    }

    // Create a CGImage from the context
    val cgImage = CGBitmapContextCreateImage(context) ?: return null

    // Return a UIImage created from the CGImage
    return UIImage.imageWithCGImage(cgImage)
}


/*
@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.toUIImage(): UIImage? {
    val width = this.width
    val height = this.height

    // 1. Create Skia Bitmap from ImageBitmap
    val skiaBitmap = Bitmap()
    val imageInfo = ImageInfo(
        width = width,
        height = height,
        colorType = ColorType.RGBA_8888, // Explicitly use RGBA_8888
        alphaType = ColorAlphaType.PREMUL  //  Match  alpha type
    )

    skiaBitmap.allocPixels(imageInfo)
    this.readPixels(skiaBitmap.readPixels())

    val pixels = skiaBitmap.readPixels(imageInfo) ?: return null

    // 2. Create CGColorSpace
    val colorSpace = CGColorSpaceCreateDeviceRGB() ?: return null

    // 3. Create CGBitmapContext
    val bytesPerPixel = 4 // RGBA: 4 bytes per pixel
    val bytesPerRow = width * bytesPerPixel
    val bitsPerComponent = 8
    val context = CGBitmapContextCreate(
        data = pixels.refTo(0), // Pass the pixel data from Skia Bitmap
        width = width.toULong(),
        height = height.toULong(),
        bitsPerComponent = bitsPerComponent.toULong(),
        bytesPerRow = bytesPerRow.toULong(),
        space = colorSpace,
        bitmapInfo = kCGImageAlphaPremultipliedLast or kCGBitmapByteOrder32Big // Explicit alpha and byte order.
    ) ?: return null

    // 4. Create CGImage
    val cgImage = CGBitmapContextCreateImage(context) ?: return null

    // 5. Create UIImage
    val uiImage = UIImage.imageWithCGImage(cgImage)

    //Cleanup
    CGColorSpaceRelease(colorSpace)
    CGContextRelease(context)
    CGImageRelease(cgImage)

    return uiImage
}
*/

/*
@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.toUIImage(): UIImage? {
    val width = this.width
    val height = this.height
    val buffer = IntArray(width * height)

    this.readPixels(buffer)

    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val context = CGBitmapContextCreate(
        data = buffer.refTo(0),
        width = width.toULong(),
        height = height.toULong(),
        bitsPerComponent = 8u,
        bytesPerRow = (4 * width).toULong(),
        space = colorSpace,
        bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
    )

    val cgImage = CGBitmapContextCreateImage(context)
    return cgImage?.let { UIImage.imageWithCGImage(it) }
}
*/

/*
@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.toUIImage(): UIImage? {

    val width = this.width
    val height = this.height

    // 1. Get ARGB pixel data from ImageBitmap
    val argbPixels = IntArray(width * height)
    this.readPixels(argbPixels)

    // 2. Convert ARGB to RGBA (needed for CGImage)
    val rgbaPixels = ByteArray(width * height * 4)
    for (i in 0 until width * height) {
        val argb = argbPixels[i]
        val a = (argb shr 24) and 0xFF
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF

        rgbaPixels[i * 4] = r.toByte()
        rgbaPixels[i * 4 + 1] = g.toByte()
        rgbaPixels[i * 4 + 2] = b.toByte()
        rgbaPixels[i * 4 + 3] = a.toByte() // Alpha
    }

    // 3. Create a CGImage from the RGBA data
    val colorSpace = CGColorSpaceCreateDeviceRGB() ?: return null
    val bitsPerComponent = 8
    val bytesPerRow = width * 4

    val data = rgbaPixels.usePinned {
        CFDataCreate(
            null,
            it.addressOf(0).reinterpret(),
            rgbaPixels.size.toLong()
        )
    } ?: return null

    val cgImage = CGImageCreate(
        width,
        height,
        bitsPerComponent,
        32, // bitsPerPixel
        bytesPerRow,
        colorSpace,
        kCGImageAlphaLast, // kCGImageAlphaPremultipliedLast,
        null,
        false, // shouldInterpolate
        kCGRenderingIntentDefault
    ) ?: return null

    // 4. Create a UIImage from the CGImage
    val uiImage = UIImage(cgImage = cgImage)

    // Clean up
    CFRelease(data)
    CGImageRelease(cgImage)
    CFRelease(colorSpace)

    return uiImage
}

*/

