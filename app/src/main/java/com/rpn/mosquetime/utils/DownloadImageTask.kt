package com.rpn.mosquetime.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.rpn.mosquetime.R
import com.rpn.mosquetime.extensions.extractFileNameFromImageUri
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ncom.rpn.mosquetime.utils.FileHelperUtilities
import ncom.rpn.mosquetime.utils.OutputCode
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class DownloadImageTask {

    fun downloadPicasso(
        context: Context,
        url: String?,
        imageView: ImageView? = null,
        progressBar: ProgressBar? = null,
        tvImagePath: TextView? = null
    ) {
        Picasso.get()
            .load(url)
            .into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    Utility.saveImage(context, bitmap, imageView, tvImagePath)
                    progressBar?.visibility = View.GONE
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    progressBar?.visibility = View.GONE
                    Log.d("TAGIMG", "onBitmapFailed: ${e?.message}")
                    Utility.showToast(context, "Bitmap failed to load")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    imageView?.setImageDrawable(placeHolderDrawable)
                    progressBar?.visibility = View.GONE
                }
            })
    }

    fun volleyDownload(context: Context, imageUrl: String, onComplete: (String) -> Unit) {
        Volley.newRequestQueue(context).add(object : ImageRequest(imageUrl,
            Response.Listener { bitmap ->
                Log.d("TAGIMG", "volleyDownload: $bitmap")

                FileHelperUtilities.createInstance(context).saveBitmapAsImage(
                    bitmap, imageUrl, 90, Bitmap.CompressFormat.JPEG
                ) { outputCode, _file, exceptionMessage_1 ->
                    if (outputCode == OutputCode.SUCCESS) {
                        Log.d("TAGIMG", "onResourceReady: file $_file")
//                            isImageIsExist("0")
                        onComplete(_file.toString())
                    }
                }
            }, 0, 0, null,
            Response.ErrorListener { error ->
                Log.d("TAGIMG", "volleyDownload: error ${error.message}")
                Utility.showToast(context, error.toString())
            }) {})
    }


    suspend fun imageDownload(context: Context, imageUrl: String): String =
        suspendCoroutine { continuation ->
            try {

                val fileName = imageUrl.extractFileNameFromImageUri()
                Volley.newRequestQueue(context).add(object : ImageRequest(imageUrl,
                    Response.Listener { bitmap ->
                        Log.d("TAG29", "volleyDownload: $bitmap.")

                        FileHelperUtilities.createInstance(context).saveBitmapAsImage(
                            bitmap, fileName, 90, Bitmap.CompressFormat.JPEG
                        ) { outputCode, _file, exceptionMessage_1 ->
                            if (outputCode == OutputCode.SUCCESS) {
                                Log.d("TAG29", "onResourceReady: file $_file")
                                continuation.resume(_file.toString())
                            }
                        }
                    }, 0, 0, null,
                    Response.ErrorListener { error ->
                        Log.d("TAG29", "volleyDownload: error ${error.message}")
                        continuation.resume("")
                    }) {})

            } catch (e: Exception) {
                Log.d("TAG29", "volleyDownload: catch error ${e.message}")
                continuation.resumeWithException(e)
            }
        }

    suspend fun isImageIsExist(context: Context, fileName: String): String =
        suspendCoroutine { continuation ->
            CoroutineScope(IO).launch {

                var absolutePathOfImage = ""
                val path =
                    Environment.DIRECTORY_PICTURES + File.separator + context.getString(R.string.app_name)

                if (VERSION.SDK_INT >= 29) {
                    val projection = arrayOf(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.RELATIVE_PATH
                    )

                    val selection =
                        MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? and " + MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?"

                    val selectionargs = arrayOf("%" + path + "%", "%" + fileName + "%")
                    val cursor = context.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionargs,
                        null
                    );

                    val indexDisplayName =
                        cursor?.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)


                    async {
                        while (cursor?.moveToNext() == true) {
                            val displayName = indexDisplayName?.let { cursor.getString(it) }
                            absolutePathOfImage =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                        }
                    }.await()
                    Log.d("TAG8", "isImageIsExist: absolutePath -> $absolutePathOfImage")
                    if (cursor!!.count > 0) {
                        // file is exist
                        continuation.resume(absolutePathOfImage)
                    } else {
                        // file is not existed
                        continuation.resume(absolutePathOfImage)
                    }
                } else {
                    //For below api 29
                    try {

                        val downloadFolder = context.getExternalFilesDir(path)
                        val fileList = downloadFolder?.listFiles()

                        async {
                            if (fileList != null) {
                                for (file in fileList) {
                                    if (file.name == fileName) {
                                        absolutePathOfImage = file.absolutePath
                                    }
                                }
                            }
                        }.await()
                        Log.d("TAG8", "isImageIsExist: fileList -> ${fileList?.map { it.name }}")
                        Log.d("TAG8", "isImageIsExist: absolutePath -> $absolutePathOfImage")

                        continuation.resume(absolutePathOfImage)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }


}