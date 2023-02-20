package com.rpn.mosquetime.repository

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.rpn.mosquetime.alias.toDataClass
import com.rpn.mosquetime.extensions.convertListToString
import com.rpn.mosquetime.extensions.convertToString
import com.rpn.mosquetime.extensions.extractFileNameFromImageUri
import com.rpn.mosquetime.extensions.notifyObserver
import com.rpn.mosquetime.model.MasjidInfo
import com.rpn.mosquetime.model.MosqueTime
import com.rpn.mosquetime.model.PrayerTime
import com.rpn.mosquetime.ui.viewmodel.MainViewModel
import com.rpn.mosquetime.utils.Event
import com.rpn.mosquetime.utils.Resource
import com.rpn.mosquetime.utils.SettingsUtility
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class FireStoreRepository() : KoinComponent {


    val mainViewModel by inject<MainViewModel>()
    val settingsUtility by inject<SettingsUtility>()

    val TAG = "FireStoreRepository"

    val KEY_COLLECTION_TIMINGS = "PRAYER_TIMES"//"MOSQUE_TIMING"
    val KEY_COLLECTION_MASJID_INFO = "MOSQUE_LIST"

//    val KEY_COLLECTION_TIMINGS = "MOSQUE_TIMING"
//    val KEY_COLLECTION_MASJID_INFO = "MOSQUE_INFO"
    var fetchSource: Source = Source.DEFAULT
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val timingsRef = firestoreInstance.collection(KEY_COLLECTION_TIMINGS)
    val masjidInfoRef = firestoreInstance.collection(KEY_COLLECTION_MASJID_INFO)

    suspend fun updateSingleDateTime(
        mosqueId: String,
        date: String,
        MosqueTime: MosqueTime?,
        onComplete: (Event<Resource<String>>) -> Unit
    ) {

        masjidInfoRef
            .document(mosqueId)
            .collection("Time Range")
            .document("Current Running Time")
            .update(date, MosqueTime)
            .addOnSuccessListener {
                onComplete(Event(Resource.success("Updated Times")))
            }.addOnFailureListener {

                if (MosqueTime != null) {
                    masjidInfoRef
                        .document(mosqueId)
                        .collection("Time Range")
                        .document("Current Running Time")
                        .set(mapOf<String, MosqueTime>(date to MosqueTime))
                        .addOnSuccessListener {
                            onComplete(Event(Resource.success("Updated Times")))
                        }.addOnFailureListener {
                            onComplete(Event(Resource.error("${it.message}", null)))
                        }
                }
            }

    }


    suspend fun getMyMosque(
        mosqueId: String,
        onComplete: (Event<Resource<MasjidInfo>>) -> Unit
    ) {
        masjidInfoRef
            .document(mosqueId)
            .get(fetchSource)
            .addOnSuccessListener {
                val masjidInfo = it.toObject(MasjidInfo::class.java)
                onComplete(Event(Resource.success("Fetched My Mosque Info", masjidInfo)))
            }.addOnFailureListener {
                onComplete(Event(Resource.error("${it.message}")))
            }


    }

    suspend fun getMyMosqueTime(
        mosqueId: String,
        onComplete: (Event<Resource<List<MosqueTime>>>) -> Unit
    ) {

        masjidInfoRef
            .document(mosqueId)
            .collection("Time Range")
            .document("Current Running Time")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    onComplete(Event(Resource.error("${e.message}")))
                    return@addSnapshotListener
                }

                val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                    "Local"
                else
                    "Server"

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "$source data: ${snapshot.data}")


                    try {
                        Log.d(TAG, "getMyMosqueTime: ${snapshot.data}")
                        val listOfMosqueTime = arrayListOf<MosqueTime>()
                        val values: Map<String, Any> = snapshot.data as Map<String, Any>;
                        // TODO: Fix and remove extra code
                        Log.d(TAG, "getMyMosqueTime: keys ${values.keys}")
                        values.keys.forEach {
                            val MosqueTimeValue = (values.get(it) as Map<String, Object>)
                            val MosqueTime: MosqueTime = MosqueTimeValue.toDataClass<MosqueTime>()
                            listOfMosqueTime.add(MosqueTime)
                        }
                        Log.d(TAG, "getMyMosqueTime: date MosqueTime ${listOfMosqueTime}")
                        settingsUtility.mosqueTime = Gson().toJson(PrayerTime(listOfMosqueTime)).toString()
                        onComplete(Event(Resource.success("Fetched My Mosque Info", listOfMosqueTime)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete(Event(Resource.error("${e.message}")))
                    }


                } else {
                    Log.d(TAG, "$source data: null")
                }
        }

            /*.get()
            .addOnSuccessListener {
                try {
                    Log.d(TAG, "getMyMosqueTime: ${it.data}")
                    val listOfMosqueTime = arrayListOf<MosqueTime>()
                    val values: Map<String, Any> = it.data as Map<String, Any>;
                    // TODO: Fix and remove extra code
                    Log.d(TAG, "getMyMosqueTime: keys ${values.keys}")
                    values.keys.forEach {
                        val MosqueTimeValue = (values.get(it) as Map<String, Object>)
                        val MosqueTime: MosqueTime = MosqueTimeValue.toDataClass<MosqueTime>()
                        listOfMosqueTime.add(MosqueTime)
                    }
                    Log.d(TAG, "getMyMosqueTime: date MosqueTime ${listOfMosqueTime}")
                    settingsUtility.mosqueTime = Gson().toJson(PrayerTime(listOfMosqueTime)).toString()
                    onComplete(Event(Resource.success("Fetched My Mosque Info", listOfMosqueTime)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    onComplete(Event(Resource.error("${e.message}")))
                }
            }
            .addOnFailureListener {
                onComplete(Event(Resource.error("${it.message}")))
            }*/

    }

    suspend fun getMessage(
        mosqueId: String,
        onComplete: (Event<Resource<MutableList<String?>>>) -> Unit
    ) {
        val TAG = "TAG8"

        masjidInfoRef
            .document(mosqueId)
            .collection("Messages")
            .document("Current Messages")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    onComplete(Event(Resource.error("${e.message}")))
                    return@addSnapshotListener
                }

                val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                    "Local"
                else
                    "Server"

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "$source data setting: ${snapshot.data}")


                    try {
                        val values: Map<String, String> = snapshot.data as Map<String, String>;

                        val messageImages = mutableListOf<String?>()
                        values.keys.sorted().forEach {
                            messageImages.add(values.get(it))
                        }
//                        settingsUtility.messageImages = convertListToString(messageImages)
                        Log.d(TAG, "getMessage: setting: post value ${messageImages.size}")
                        mainViewModel.imgMsg.postValue(messageImages)


                        onComplete(Event(Resource.success("Fetched Message", mainViewModel.imgMsg.value)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete(Event(Resource.error("${e.message}")))
                    }


                } else {
                    Log.d(TAG, "$source data: null")
                }
        }

    }

    suspend fun getMyMosqueDataChanged(
        mosqueId: String,
        onComplete: (Event<Resource<MasjidInfo>>) -> Unit
    ) {
        masjidInfoRef
            .whereEqualTo("documentId", mosqueId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val masjidInfo = dc.document.toObject(MasjidInfo::class.java)
                            onComplete(
                                Event(
                                    Resource.success(
                                        "On Added Mosque Info ${masjidInfo.masjidName}",
                                        masjidInfo
                                    )
                                )
                            )
                            Log.d(TAG, "New city: ${dc.document.data}")
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val masjidInfo = dc.document.toObject(MasjidInfo::class.java)
                            if (mosqueId == masjidInfo.documentId) {
                                val msg =
                                    if (masjidInfo.activated) "Your Mosque is Registered Successfully" else "Your Mosque is not Registered"
                                onComplete(Event(Resource.success(msg, masjidInfo)))
                                Log.d(TAG, "Modified city: ${dc.document.data}")
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            val masjidInfo = dc.document.toObject(MasjidInfo::class.java)
                            if (mosqueId == masjidInfo.documentId) {
                                onComplete(Event(Resource.error("Mosque Removed")))
                                Log.d(TAG, "Removed city: ${dc.document.data}")
                            }
                        }
                    }
                }
            }

    }

    suspend fun getMyMosqueByUserId(
        ownerUid: String,
        onComplete: (Event<Resource<MasjidInfo>>) -> Unit
    ) {
        masjidInfoRef
            .whereEqualTo("ownerUid", ownerUid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "getMyMosqueByUserId: dc Document ${dc.document}")
                            val masjidInfo = dc.document.toObject(MasjidInfo::class.java)
                            onComplete(
                                Event(
                                    Resource.success(
                                        "On Added Mosque Info ${masjidInfo.masjidName}",
                                        masjidInfo
                                    )
                                )
                            )
                            Log.d(TAG, "New city: ${dc.document.data}")
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val masjidInfo = dc.document.toObject(MasjidInfo::class.java)
                            val msg =
                                if (masjidInfo.activated) "Your Mosque is Registered Successfully" else "Your Mosque is not Registered"
                            onComplete(Event(Resource.success(msg, masjidInfo)))
                            Log.d(TAG, "Modified city: ${dc.document.data}")

                        }
                        DocumentChange.Type.REMOVED -> {
                            val masjidInfo = dc.document.toObject(MasjidInfo::class.java)
                            onComplete(Event(Resource.error("Mosque Removed")))
                            Log.d(TAG, "Removed city: ${dc.document.data}")

                        }
                    }
                }
            }

    }







    //todo
    fun downloadImage(url:String){

        // Create a storage reference from our app
        val storageRef = storage.reference

        // Create a reference with an initial file path and name
//        val pathReference = storageRef.child("images/stars.jpg")

        // Create a reference to a file from a Google Cloud Storage URI
//        val gsReference = storage.getReferenceFromUrl("gs://bucket/images/stars.jpg")

        // Create a reference from an HTTPS URL
// Note that in the URL, characters are URL escaped!
//        val httpsReference = storage.getReferenceFromUrl(
//            "https://firebasestorage.googleapis.com/b/bucket/o/images%20stars.jpg")

        var islandRef = storageRef.child(url)

        Log.d(TAG, "downloadImage: $url")
        val ONE_MEGABYTE: Long = 1024 * 1024
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            Log.d(TAG, "downloadImage: success $it")
            // Data for "images/island.jpg" is returned, use this as needed
        }.addOnFailureListener {
            // Handle any errors
            Log.d(TAG, "downloadImage: error ${it.printStackTrace()}")
        }
    }


}