package com.rpn.mosquetime.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/*val source = if (it.metadata.isFromCache)
    "local cache"
else
    "server"
Timber.d("trackingX source $source")*/

object FireStoreUtil {

    val TAG = "FireStoreUtil"
    const val KEY_COLLECTION_TIMINGS = "timings"
    var fetchSource: Source = Source.DEFAULT
    val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }


    val timingsRef = firestoreInstance.collection(KEY_COLLECTION_TIMINGS)

    fun getTiming(
        onComplete: (list: List<Any?>?) -> Unit
    ) {
        val existsPlaylist = mutableListOf<Any>()
        timingsRef
            .get(fetchSource)
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (i in it.documents) {
                        try {

//                            val timingItem = Gson().fromJson(
//                                i.data.toString(),
//                                Timing::class.java
//                            )
//                            existsPlaylist.add(timingItem)
                            Log.d(TAG, "getTiming: i\n ${i.data.toString()}")
                        } catch (e: Exception) {
                            Log.d(TAG, "getTiming: exception ${e.message}")
                        }
                    }
                    onComplete(existsPlaylist)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "getTiming: exception $it")
            }
    }


}