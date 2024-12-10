package com.ashu.photodescriber.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.google.mediapipe.framework.AndroidPacketCreator
import com.google.mediapipe.framework.Graph
import com.google.mediapipe.framework.Packet
import java.io.ByteArrayOutputStream


fun Bitmap.toFaceLandMark(faceDetectionGraph: Graph, packetCreator: AndroidPacketCreator): Long {
    val inputPacket: Packet = packetCreator.createRgbaImageFrame(this)
    // Process the input and retrieve landmarks (this is a simplified example, modify as needed)
    val outputPacket = faceDetectionGraph.addPacketToInputStream("input_stream", inputPacket, System.currentTimeMillis())

    // Return the landmarks
    return inputPacket.timestamp
}

fun Uri.toBitmap(context: Context): Bitmap? {
    return context.contentResolver.openInputStream(this)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    }
}

fun Bitmap.toUri(inContext: Context): Uri {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

    val path =
        MediaStore.Images.Media.insertImage(inContext.contentResolver, this, "tmp", null)
    return Uri.parse(path)
}

fun Bitmap.toUriDownload(context: Context): Uri? {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

    val resolver = context.contentResolver
    val downloadUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "0")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri = resolver.insert(downloadUri, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }

    return uri
}

fun Bitmap.rotatedBy(angle: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(angle) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}