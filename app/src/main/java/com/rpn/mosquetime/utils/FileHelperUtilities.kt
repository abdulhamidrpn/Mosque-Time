@file:Suppress("DEPRECATION")

package ncom.rpn.mosquetime.utils

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.rpn.mosquetime.BuildConfig
import com.rpn.mosquetime.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


enum class OutputCode {
    SUCCESS,
    FAILURE
}

class FileHelperUtilities(private val context: Context) {
    companion object {
        fun createInstance(context: Context) = FileHelperUtilities(context)
    }


    fun saveBitmapAsImage(
        bitmapToCompress:Bitmap,
        projectTitle:String,
        compressionOutputQuality: Int,
        compressionFormat: Bitmap.CompressFormat,
        onTaskFinished: (OutputCode, Uri?, String?) -> Unit
    ) {
        // Thanks to https://stackoverflow.com/users/3571603/javatar on StackOverflow - quite a bit of the code is based off of their solution


        var exceptionMessage: String? = null
        var outputCode = OutputCode.SUCCESS
        val pathData = "image/jpeg"
        val outputName = projectTitle
//            if (compressionFormat == Bitmap.CompressFormat.PNG) "$projectTitle.png" else "$projectTitle.jpg"

        val imageOutStream: OutputStream?

        var uri: Uri? = null


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, outputName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + context.getString(R.string.app_name)
                )
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageOutStream = resolver.openOutputStream(uri!!)
            } else {
                val imageFile = File(getDirectory(), outputName)
                imageOutStream = FileOutputStream(imageFile)
                uri = Uri.fromFile(imageFile)
            }

            bitmapToCompress.compress(compressionFormat, compressionOutputQuality, imageOutStream)
            imageOutStream!!.flush()
            imageOutStream.close()

        } catch (exception: Exception) {
            exceptionMessage = exception.message
            outputCode = OutputCode.FAILURE
        } finally {
            onTaskFinished(outputCode, uri, exceptionMessage)
        }
        if (uri != null) {
            galleryAddPic(uri)
        }
    }
    private fun galleryAddPic(uri: Uri) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
    }

    fun getDirectory(): File? {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + File.separator + context.getString(R.string.app_name)
        var storageDir: File? = File(imagesDir)
        if (!storageDir!!.exists()) storageDir.mkdirs()
        return storageDir
    }

    fun deleteFile(file: File) {
        if (file.exists()) {

            Log.d("DELETE_TAG", "DELETE FILE: File: $file Directory: ${file.isDirectory}")
            file.delete()
        }
    }

    fun deleteImage(path: String) {
        try {
            MediaScannerConnection.scanFile(
                context, arrayOf(path),
                null
            ) { path, uri ->
                if (path!=null) {
                    try {
                        context.contentResolver.delete(Uri.parse(path), null, null)
                    }catch (e:Exception){
                        Toast.makeText(context, "There is some error with deleting, Try from Gallery", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("DELETE_TAG", "Not DONE ${e.message}")
        }
    }


    fun openImage(context:Context, path : String) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val photoURI = FileProvider.getUriForFile(
            context, BuildConfig.APPLICATION_ID + ".provider",
            File(path)
        )
        intent.setDataAndType(photoURI, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "View using"))
    }

    fun openFolder(c: Context){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val selectedUri = Uri.parse(FileHelperUtilities(c).getDirectory()?.absolutePath)
        intent.setDataAndType(selectedUri, "image/*")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        c.startActivity(Intent.createChooser(intent, "Open folder"))
    }

    fun isImageIsExist(c:Context,fileName: String, onComplete: (Boolean, String) -> Unit): Boolean {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.RELATIVE_PATH
        )

        val path =
            Environment.DIRECTORY_PICTURES + File.separator + c.getString(R.string.app_name)
        val name = fileName

        val selection =
            MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? and " + MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?"

        val selectionargs = arrayOf("%" + path + "%", "%" + name + "%")
        val cursor = c.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionargs,
            null
        );

        val indexDisplayName = cursor?.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
        // or you can see displayName
        var absolutePathOfImage = ""
        while (cursor?.moveToNext() == true) {
            val displayName = indexDisplayName?.let { cursor.getString(it) }
            absolutePathOfImage =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            val relativePathOfImage =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH));
            val file = File(absolutePathOfImage)
//            Log.d(
//                TAG, "checkImageIsExistOrNot: \n" +
//                        "\nFile Found $displayName" +
//                        "\nabsolutePath $absolutePathOfImage" +
//                        "\nfile $file" +
//                        "\relativePathOfImage $relativePathOfImage"
//            )
        }
        if (cursor!!.count > 0) {
            // file is exist
            onComplete(true, absolutePathOfImage)
            return true
        } else {
            onComplete(false, absolutePathOfImage)
            return false
        }


    }

}



