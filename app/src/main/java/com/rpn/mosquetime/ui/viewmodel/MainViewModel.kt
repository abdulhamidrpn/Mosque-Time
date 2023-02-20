package com.rpn.mosquetime.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rpn.mosquetime.extensions.convertLongToTime
import com.rpn.mosquetime.extensions.convertToList
import com.rpn.mosquetime.extensions.getMinuteDefferance
import com.rpn.mosquetime.extensions.notifyObserver
import com.rpn.mosquetime.model.*
import com.rpn.mosquetime.repository.FireStoreRepository
import com.rpn.mosquetime.utils.*
import com.rpn.mosquetime.utils.GeneralUtils.sdfDate
import com.rpn.mosquetime.utils.GeneralUtils.sdfTimeSecond24
import com.rpn.mosquetime.utils.GeneralUtils.sdfTimeSecondAm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*


class MainViewModel : CoroutineViewModel(Dispatchers.Main),
    KoinComponent {
    val TAG = "MainViewModel"

    val fireStoreRepository by inject<FireStoreRepository>()
    val settingsUtility by inject<SettingsUtility>()

    var masjidInfo = MutableLiveData<MasjidInfo?>()
    var imgMsg = MutableLiveData<MutableList<String?>>()
    var currentMessage = MutableLiveData<String?>()
    var imgMsgDownloaded: MutableList<String?> = mutableListOf()
    var isMasjidActivated = MutableLiveData(false)
    var allMosqueTime = MutableLiveData<PrayerTime?>()
    var todayMosqueTime = MutableLiveData<MosqueTime?>()
    val todayFormat = MutableLiveData<String>()
    val weekDayFormat = MutableLiveData<String>()
    val currentWakt = MutableLiveData<Wakt>(Wakt.FAJR)
    var getHour = true
    var getMinute = true
    var currentTimeDayCheck = ""
    var currentTimeHourCheck = ""
    var currentTimeMinuteCheck = ""

    val delay1Min = -1

    val delay9MinAfterSalah = 9
    val delay7MinAfterSalah = 7
    val delay5MinAfterSalah = 5

    val delay7MinBeforeSalah = -7
    val delay5MinBeforeSalah = -5
    val delay3MinBeforeSalah = -3

    fun getCurrentDay(): String {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_WEEK]
        return when (day) {
            Calendar.SUNDAY -> Constants.weekdays[0]
            Calendar.MONDAY -> Constants.weekdays[1]
            Calendar.TUESDAY -> Constants.weekdays[2]
            Calendar.WEDNESDAY -> Constants.weekdays[3]
            Calendar.THURSDAY -> Constants.weekdays[4]
            Calendar.FRIDAY -> Constants.weekdays[5]
            Calendar.SATURDAY -> Constants.weekdays[6]
            else -> Constants.weekdays[0]
        }
    }

    var timerAnalogClock: Timer? = null

    data class MainUiState(
        val title: String,
        val body: String,
        val bookmarked: Boolean = false,
        val publicationDate: String,
        val onBookmark: () -> Unit
    )

    //ui handle
    data class LatestNewsUiState(
        val news: List<TimingDetails> = emptyList(),
        val waktTime: String = "",
        val isLoading: Boolean = false,
        val userMessageImage: String? = null,
        val userMessage: String? = null
    )

    private val _uiState = MutableStateFlow(LatestNewsUiState(isLoading = true))
    val uiState: StateFlow<LatestNewsUiState> = _uiState

    fun userMessageShown() {
        _uiState.update { currentUiState ->
            currentUiState.copy(userMessage = null)
        }
    }

    // TODO: update function and make other changes. take care of message app send data to message fragment 
    fun userMessageShow(messageNo: String) {

        currentMessage.postValue(imgMsg.value?.get(messageNo.toInt() - 1))
        _uiState.update { currentUiState ->
            currentUiState.copy(userMessage = messageNo)
        }
    }

    fun startTimer() {
        currentTimeSecond.postValue(currentSecond())


        if (currentTimeSecond.value == "00" || getMinute) {
            getMinute = false
            currentTimeMinuteCheck = currentMunite()

            checkTime()
        }
        if (currentTimeMinuteCheck != currentMunite()) {
            getMinute = true
        }

        if (currentTimeSecond.value == "00" && currentTimeMinute.value == "00" || getHour) {
            getHour = false
            currentTimeHourCheck = currentHour()

            val today = sdfDate.format(Calendar.getInstance().time)
            if (today != currentTimeDayCheck) {
                todayFormat.postValue(today)
                weekDayFormat.postValue(getCurrentDay())
                val todayTime = allMosqueTime.value?.date?.find {
                    it.readable == today
                }
                Log.d(TAG, "Check time hour and post new date time: $todayTime ")
                todayMosqueTime.postValue(todayTime)


            }
            currentTimeHour.postValue(currentHour())
        }
        if (currentTimeHourCheck != currentHour()) {
            getHour = true
        }


    }

    fun checkTime() {

        val waktDate = todayMosqueTime.value?.timestamp ?: currentDate().time.toString()

        val sunrise = todayMosqueTime.value?.sunrise
        val sunset = todayMosqueTime.value?.sunset
        val imsak = todayMosqueTime.value?.timingDetails?.imsak

        val wakt1 = todayMosqueTime.value?.timingDetails?.fajr
        val wakt2 = todayMosqueTime.value?.timingDetails?.dhuhr
        val wakt3 = todayMosqueTime.value?.timingDetails?.asr
        val wakt4 = todayMosqueTime.value?.timingDetails?.maghrib
        val wakt5 = todayMosqueTime.value?.timingDetails?.isha
        val jumua = masjidInfo.value?.jumua


        var dayOfTheWeek: String = GeneralUtils.sdfDayFull.format(waktDate.convertLongToTime())

        Log.d(TAG, "startTimer: dayOfTheWeek $dayOfTheWeek")

        //Converting String to Date of today
        val sunriseDate = waktDate.convertLongToTime(
            sunrise?.split(":")?.firstOrNull(),
            sunrise?.split(":")?.lastOrNull()
        )
        val sunsetDate = waktDate.convertLongToTime(
            sunset?.split(":")?.firstOrNull(),
            sunset?.split(":")?.lastOrNull()
        )
        val imsakDate = waktDate.convertLongToTime(
            imsak?.split(":")?.firstOrNull(),
            imsak?.split(":")?.lastOrNull()
        )
        val jumuaDate = waktDate.convertLongToTime(
            jumua?.split(":")?.firstOrNull(),
            jumua?.split(":")?.lastOrNull()
        )

        val wakt1Date = waktDate.convertLongToTime(
            wakt1?.split(":")?.firstOrNull(),
            wakt1?.split(":")?.lastOrNull()
        )
        val wakt2Date =
            if (dayOfTheWeek.equals("Friday")) jumuaDate else waktDate.convertLongToTime(
                wakt2?.split(":")?.firstOrNull(), wakt2?.split(":")?.lastOrNull()
            )
        val wakt3Date = waktDate.convertLongToTime(
            wakt3?.split(":")?.firstOrNull(),
            wakt3?.split(":")?.lastOrNull()
        )
        val wakt4Date = waktDate.convertLongToTime(
            wakt4?.split(":")?.firstOrNull(),
            wakt4?.split(":")?.lastOrNull()
        )
        val wakt5Date = waktDate.convertLongToTime(
            wakt5?.split(":")?.firstOrNull(),
            wakt5?.split(":")?.lastOrNull()
        )


/*
            Log.d(TAG, "startTimer: current Date ${currentDate()}")
            Log.d(TAG, "startTimer: wakt1 Date $wakt1Date")
            Log.d(TAG, "startTimer: wakt2 Date $wakt2Date")
            Log.d(TAG, "startTimer: wakt3 Date $wakt3Date")
            Log.d(TAG, "startTimer: wakt4 Date $wakt4Date")
            Log.d(TAG, "startTimer: wakt5 Date $wakt5Date")
            Log.d(TAG, "startTimer: wakt1 Difference ${currentDate().getMinuteDefferance(wakt1Date)}")
            Log.d(TAG, "startTimer: wakt2 Difference ${currentDate().getMinuteDefferance(wakt2Date)}")
            Log.d(TAG, "startTimer: wakt3 Difference ${currentDate().getMinuteDefferance(wakt3Date)}")
            Log.d(TAG, "startTimer: wakt4 Difference ${currentDate().getMinuteDefferance(wakt4Date)}")
            Log.d(TAG, "startTimer: wakt5 Difference ${currentDate().getMinuteDefferance(wakt5Date)}")*/

        viewModelScope.launch {

            val currentDate = currentDate()
            conditionShowMessage(wakt1Date, delay7MinBeforeSalah, currentDate)
            conditionShowMessage(wakt2Date, delay7MinBeforeSalah, currentDate)
            conditionShowMessage(wakt3Date, delay7MinBeforeSalah, currentDate)
            conditionShowMessage(wakt4Date, delay7MinBeforeSalah, currentDate)
            conditionShowMessage(wakt5Date, delay7MinBeforeSalah, currentDate)

            conditionShowMessage(wakt1Date, delay5MinBeforeSalah, currentDate)
            conditionShowMessage(wakt2Date, delay5MinBeforeSalah, currentDate)
            conditionShowMessage(wakt3Date, delay5MinBeforeSalah, currentDate)
            conditionShowMessage(wakt4Date, delay5MinBeforeSalah, currentDate)
            conditionShowMessage(wakt5Date, delay5MinBeforeSalah, currentDate)

            conditionShowMessage(wakt1Date, delay3MinBeforeSalah, currentDate)
            conditionShowMessage(wakt2Date, delay3MinBeforeSalah, currentDate)
            conditionShowMessage(wakt3Date, delay3MinBeforeSalah, currentDate)
            conditionShowMessage(wakt4Date, delay3MinBeforeSalah, currentDate)
            conditionShowMessage(wakt5Date, delay3MinBeforeSalah, currentDate)

            //Countdown timer
            conditionShowMessage(wakt1Date, delay1Min, currentDate)
            conditionShowMessage(wakt2Date, delay1Min, currentDate)
            conditionShowMessage(wakt3Date, delay1Min, currentDate)
            conditionShowMessage(wakt4Date, delay1Min, currentDate)
            conditionShowMessage(wakt5Date, delay1Min, currentDate)

            //After Salah
            conditionShowMessage(wakt1Date, delay9MinAfterSalah, currentDate)
            conditionShowMessage(wakt2Date, delay9MinAfterSalah, currentDate)
            conditionShowMessage(wakt3Date, delay9MinAfterSalah, currentDate)
            conditionShowMessage(wakt4Date, delay9MinAfterSalah, currentDate)
            conditionShowMessage(wakt5Date, delay9MinAfterSalah, currentDate)

            conditionShowMessage(wakt1Date, delay7MinAfterSalah, currentDate)
            conditionShowMessage(wakt2Date, delay7MinAfterSalah, currentDate)
            conditionShowMessage(wakt3Date, delay7MinAfterSalah, currentDate)
            conditionShowMessage(wakt4Date, delay7MinAfterSalah, currentDate)
            conditionShowMessage(wakt5Date, delay7MinAfterSalah, currentDate)

            conditionShowMessage(wakt1Date, delay5MinAfterSalah, currentDate)
            conditionShowMessage(wakt2Date, delay5MinAfterSalah, currentDate)
            conditionShowMessage(wakt3Date, delay5MinAfterSalah, currentDate)
            conditionShowMessage(wakt4Date, delay5MinAfterSalah, currentDate)
            conditionShowMessage(wakt5Date, delay5MinAfterSalah, currentDate)
            currentTimeMinute.postValue(currentMunite())
        }

    }

    fun conditionShowMessage(
        waktTime: Date? = null,
        delay: Int,
        currentDate: Date? = null
    ) {


        val calendar = Calendar.getInstance()
        currentDate?.time?.let { calendar.setTimeInMillis(it) }
        calendar.set(Calendar.SECOND, 1)
        val currentDate = calendar.time


//        currentDate.time.seconds = 04

        if (waktTime != null && currentDate?.getMinuteDefferance(waktTime) == delay) {

            Log.d(
                TAG, "condition ok" +
                        "\n//Now Date -> ${currentDate} " +
                        "\n//Current WaktTime -> $waktTime " +
                        "\n//Delay -> $delay " +
                        "\n//Condition 1 After Salah +delay" +
                        "\n//Condition 2 Before salah -delay" +
                        "\n//Minute Difference ${currentDate.getMinuteDefferance(waktTime)}"
            )
//            Now Date -> Sat Dec 03 13:11:01 GMT 2022 // Current WaktTime -> Sat Dec 03 13:10:01 GMT 2022 // delay -> 1 Condition 1 true Condition 2 false Minute Difference1


            if (delay == delay7MinBeforeSalah) {
                userMessageShow("1")
            } else if (delay == delay5MinBeforeSalah) {
                userMessageShow("2")
            } else if (delay == delay3MinBeforeSalah) {
                userMessageShow("3")
            } else if (delay == delay5MinAfterSalah) {
                userMessageShow("4")
            } else if (delay == delay7MinAfterSalah) {
                userMessageShow("5")
            } else if (delay == delay9MinAfterSalah) {
                userMessageShow("6")
            } else if (delay == delay1Min) {
                //Show CountDown
                userMessageShow("7")
            }
//            return true
        } else {
            Log.d(TAG, "Wakt Time 3 $waktTime delay $delay")
            Log.d(
                TAG, "else" +
                        "\ndelay Wakt > ${currentDate?.getMinuteDefferance(waktTime)}" +
                        "\ndelay $delay" +
                        "\nwakttime $waktTime " +
                        "\nDate -> ${currentDate} "
            )
//            return false
        }
    }


    val currentTimeHour = MutableLiveData(currentHour())
    val currentTimeMinute = MutableLiveData(currentMunite())
    val currentTimeSecond = MutableLiveData(currentSecond())


    val currentTime = liveData {
        while (true) {
            val time =
                if (settingsUtility.is24HourFormat) sdfTimeSecond24.format(Calendar.getInstance().time) else sdfTimeSecondAm.format(
                    Calendar.getInstance().time
                )
            emit(time)
            kotlinx.coroutines.delay(1000)
        }
    }


    init {
        Log.d(TAG, "Init MainviewModel: Check Mosque Updated()")
        viewModelScope.launch {
            while (true) {
                startTimer()
                delay(1000)
            }
        }
//        startTimer()
        checkMosqueUpdated()
        if (!settingsUtility.messageImages.convertToList().isNullOrEmpty() && settingsUtility.messageImages.convertToList()?.size==7){
            Log.d(
                "TAG8",
                "post setting image : inside imgMsg ${settingsUtility.messageImages.convertToList()?.size}"
            )
            imgMsg.postValue(settingsUtility.messageImages.convertToList())
        }

        getMessage()

    }

    fun checkMosqueUpdated() {

        if (settingsUtility.userId != "" || settingsUtility.userId.isNotEmpty()) {
            Log.d(TAG, "checkMosqueUpdated: There Is a user id ${settingsUtility.userId}")
            getMyMosquebyOwnerId {
                setTodayMosqueTime()
            }
        }
    }

    fun setTodayMosqueTime() {
        Log.d(TAG, "setTodayMosqueTime: Mosque Document Id ${settingsUtility.mosqueDocumentId}")
        Log.d(TAG, "setTodayMosqueTime: Mosque Info ${settingsUtility.mosqueInfo}")
        Log.d(TAG, "setTodayMosqueTime: Mosque Time ${settingsUtility.mosqueTime}")
        if (settingsUtility.mosqueTime != "" && settingsUtility.mosqueTime.isNotEmpty()) {
            val prayerTimeDate = Gson().fromJson(settingsUtility.mosqueTime, PrayerTime::class.java)
            val todayDateFormat = sdfDate.format(Calendar.getInstance().time)
            val todayTime = prayerTimeDate.date?.find {
                it.readable == todayDateFormat
            }
            todayFormat.postValue(todayDateFormat)
            weekDayFormat.postValue(getCurrentDay())
            allMosqueTime.postValue(prayerTimeDate)
            todayMosqueTime.postValue(todayTime)

            Log.d(
                TAG, "setTodayMosqueTime: " +
                        "\n Today Mosque Time ${todayMosqueTime.value}" +
                        "\n Today Time $todayTime" +
                        "\n Today $todayDateFormat" +
                        "\n Prayer Time $prayerTimeDate" +
                        ""
            )
        }
        if (todayMosqueTime.value == null) {
            getMyMosqueTime {
                Log.d(TAG, "setTodayMosqueTime: " + it.peekContent().message)
            }
        }

    }

    private fun getTiming() {
        launch {
            FireStoreUtil.getTiming {
                Log.d("FireStoreUtil", "getTiming viewmodel:\n $it")
            }

        }
    }

    fun changeTimeFormat(is24Hour: Boolean = settingsUtility.is24HourFormat) {

        if (is24Hour) {
            todayMosqueTime.value?.timingDetails?.to24()
        } else {
            todayMosqueTime.value?.timingDetails?.toAM()
        }
        masjidInfo.notifyObserver()
        todayMosqueTime.notifyObserver()
    }


    fun currentMunite(): String {
        return if (Calendar.getInstance().get(Calendar.MINUTE).toString().length == 1)
            "0" + Calendar.getInstance().get(Calendar.MINUTE).toString()
        else Calendar.getInstance().get(Calendar.MINUTE).toString()
    }

    fun currentSecond(): String {
        return if (Calendar.getInstance().get(Calendar.SECOND).toString().length == 1)
            "0" + Calendar.getInstance().get(Calendar.SECOND).toString()
        else Calendar.getInstance().get(Calendar.SECOND).toString()

    }

    fun currentHour(): String {
        val sdfTimeHour24 = SimpleDateFormat("HH")
        val sdfTimeHourAmPm = SimpleDateFormat("hh")
        if (settingsUtility.is24HourFormat) {
            return sdfTimeHour24.format(Calendar.getInstance().time.time)
        } else {
            return sdfTimeHourAmPm.format(Calendar.getInstance().time.time)
        }
        /*if (settingsUtility.is24HourFormat) {
                return if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString().length == 1)
                    "0" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
                else Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
            } else {
                return if (Calendar.getInstance().get(Calendar.HOUR).toString().length == 1)
                    "0" + Calendar.getInstance().get(Calendar.HOUR).toString()
                else Calendar.getInstance().get(Calendar.HOUR).toString()
            }*/
    }

    fun currentTime(): String {
        return GeneralUtils.sdfTime24.format(Calendar.getInstance().time)
    }

    fun currentDate(): Date {
        return Calendar.getInstance().time
    }


    fun getMyMosqueTime(
        onComplete: (Event<Resource<String>>) -> Unit
    ) {
        viewModelScope.launch {
            if (settingsUtility.mosqueDocumentId != "" && settingsUtility.mosqueDocumentId.isNotEmpty()) {
                fireStoreRepository.getMyMosqueTime(settingsUtility.mosqueDocumentId) {

                    if (it.peekContent().status == Status.SUCCESS) {
                        val todayFormatx = sdfDate.format(Calendar.getInstance().time)
                        todayFormat.postValue(todayFormatx)
                        weekDayFormat.postValue(getCurrentDay())
                        val todayTime = it.peekContent().data?.find {
                            it.readable == todayFormatx
                        }
                        Log.d(
                            TAG,
                            "getMyMosqueTime: Mosque document id ${settingsUtility.mosqueDocumentId}"
                        )
                        Log.d(TAG, "getMyMosqueTime: Today Time $todayFormatx")
                        Log.d(TAG, "getMyMosqueTime: Today Time ${todayTime?.readable.toString()}")
                        Log.d(TAG, "getMyMosqueTime: Today Time ${todayTime?.toString()}")
                        Log.d(TAG, "getMyMosqueTime: Today Time data ${it.peekContent().data}")

                        allMosqueTime.postValue(PrayerTime(it.peekContent().data))
                        currentTimeDayCheck = todayTime?.readable ?: ""
                        todayMosqueTime.postValue(todayTime)


                        onComplete(Event(Resource.success("Your mosque time fetched")))
                    } else {
                        onComplete(Event(Resource.error(it.peekContent().message)))
                    }
                }
            } else {
                onComplete(Event(Resource.error("Select a mosque")))
            }
        }

    }


    fun getMessage() {
        viewModelScope.launch {
            if (settingsUtility.mosqueDocumentId != "" && settingsUtility.mosqueDocumentId.isNotEmpty()) {
                fireStoreRepository.getMessage(settingsUtility.mosqueDocumentId) {

                    if (it.peekContent().status == Status.SUCCESS) {
                        Log.d("TAG8", "getMessage: Viewmodel success")
                    } else {
                        Log.d("TAG8", "getMessage: Viewmodel failed")
                        getMessage()
                    }
                }
            } else {
                Log.d("TAG27", "getMessage: Viewmodel no doc id")
            }
        }

    }


    fun getMyMosquebyOwnerId(
        onComplete: (msg: String?) -> Unit
    ) {
        viewModelScope.launch {
            fireStoreRepository.getMyMosqueByUserId(settingsUtility.userId) {
                Log.d(TAG, "getMyMosquebyOwnerId: ${it.peekContent().status}")
                if (it.peekContent().status == Status.SUCCESS) {
                    val masjidInfoData = it.peekContent().data
                    masjidInfo.postValue(masjidInfoData)
                    settingsUtility.mosqueInfo = masjidInfoData.toString()
                    settingsUtility.mosqueImage = masjidInfoData?.image.toString()
                    settingsUtility.userPhone = masjidInfoData?.phoneNumber.toString()
                    settingsUtility.mosqueDocumentId = masjidInfoData?.documentId.toString()
                    settingsUtility.mosqueActivated = masjidInfoData?.activated ?: false
                    isMasjidActivated.postValue(masjidInfoData?.activated ?: false)
                    Log.d(TAG, "getMyMosquebyOwnerId: mosque fetched successfully")
                } else {
                    settingsUtility.mosqueInfo = ""
                    settingsUtility.mosqueDocumentId = ""
                    settingsUtility.mosqueActivated = false
                    isMasjidActivated.postValue(false)
                    Log.d(
                        TAG,
                        "getMyMosquebyOwnerId: mosque fetched error ${it.peekContent().message}"
                    )
                }
                onComplete(it.peekContent().message)
            }
        }
    }

}


