package com.rpn.salatetime.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpn.salatetime.data.local.datastore.SettingsRepository
import com.rpn.salatetime.data.repository.MosqueRepository
import com.rpn.salatetime.domain.model.CompatDayOfWeek
import com.rpn.salatetime.domain.model.CompatLocalDateTime
import com.rpn.salatetime.domain.model.CompatLocalTime
import com.rpn.salatetime.domain.model.MosqueCompositeData
import com.rpn.salatetime.domain.model.NotificationTrigger
import com.rpn.salatetime.domain.model.PrayerTime
import com.rpn.salatetime.domain.model.Settings
import com.rpn.salatetime.domain.model.TimeState
import com.rpn.salatetime.domain.model.TriggerType
import com.rpn.salatetime.domain.repository.ImageRepository
import com.rpn.salatetime.domain.repository.TimeRepository
import com.rpn.salatetime.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: MosqueRepository,
    private val timeRepository: TimeRepository,
    private val settingsRepository: SettingsRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {

    private val _effect = Channel<MainScreenEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ── Live clock tick ───────────────────────────────────────────────────────

    val timeState: StateFlow<TimeState> = timeRepository.timeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TimeState(
                currentDateTime = CompatLocalDateTime(0, 0, 0, 0, 0, 0, CompatDayOfWeek.MONDAY),
                currentNotification = null,
                hijriDateFormatted = "",
            ),
        )

    // ── Single source of truth ────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────────────────

    init {
        observeMosqueData()   // reactive: re-fetches on date change or uid change
        observeSettings()     // reactive: display prefs, background image
        observeNotifications()
        startSync()           // one-shot: kick off Supabase Realtime
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mosque data pipeline
    //
    // Combines two reactive triggers:
    //   1. userId from DataStore (never hardcoded)
    //   2. current date from the live clock (auto-reloads at midnight)
    //
    // Both must be non-blank/non-zero before any Room query fires.
    // ─────────────────────────────────────────────────────────────────────────

    private fun observeMosqueData() {
        viewModelScope.launch {
            combine(
                // Stream 1 — userId (waits until a real uid is available)
                settingsRepository.settingsFlow
                    .map { it.userId }
                    .filter { it.isNotBlank() }
                    .distinctUntilChanged(),

                // Stream 2 — current date (fires once per day at midnight)
                timeState
                    .map { it.currentDateTime.toFormattedDateDatabase() }
                    .filter { it.isNotBlank() }          // skip the 0,0,0 initial value
                    .distinctUntilChanged(),

                ) { uid, date -> uid to date }
                .flatMapLatest { (uid, date) ->
                    Timber.d("Fetching mosque data — uid=$uid, date=$date")
                    repository.getMosqueCombineDataByUid(uid, date)
                }
                .catch { e ->
                    Timber.e(e, "Mosque pipeline error")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { incoming ->
                    // Merge: keep existing non-null fields when the new emission has nulls.
                    // This prevents a Room emission that only updates one field from
                    // wiping out data that was already loaded for the other fields.
                    val merged = mergeWithExisting(incoming)

                    Timber.d(
                        "Mosque data merged — " +
                                "\nmosque = ${merged.mosque}, " +
                                "\nprayer = ${merged.todayPrayerTime}, " +
                                "\nslides = ${merged.slides.map { it.localPath }}"
                    )

                    // Persist raw 24h times to DataStore (always 24h — never formatted)

                    settingsRepository.updateFromCompositeData(merged)

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            data = merged,
                            error = if (merged.mosque == null) "No Mosque Found" else null,
                        )
                    }
                }
        }
    }

    /**
     * Merges [incoming] with whatever is already in [_uiState].
     *
     * Rule: a null field in [incoming] does NOT overwrite an existing non-null field.
     * An empty slides list is also treated as "no update" so we don't flash an
     * empty grid while Room is still fetching.
     */
    private fun mergeWithExisting(incoming: MosqueCompositeData?): MosqueCompositeData {
        val existing = _uiState.value.data
        return MosqueCompositeData(
            mosque = incoming?.mosque ?: existing?.mosque,
            todayPrayerTime = incoming?.todayPrayerTime ?: existing?.todayPrayerTime,
            slides = incoming?.slides?.takeIf { it.isNotEmpty() }
                ?: existing?.slides
                ?: emptyList(),
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Settings observer
    //
    // Handles display preferences (12/24h, mosque name, background).
    // When is24HourFormat changes, rebuilds todayPrayerTime from the already-
    // merged Room data so we don't need a second Room query.
    // ─────────────────────────────────────────────────────────────────────────

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                val resolvedBg = resolveBackgroundImage(settings.backgroundImage)

                _uiState.update { current ->
                    current.copy(
                        isAuthReady = true,                      // first emission received
                        userId = settings.userId.takeIf { it.isNotBlank() },
                        is24HourFormat = settings.is24HourFormat,
                        showAmPm = !settings.is24HourFormat,
                        showMosqueName = settings.shouldShowMosqueName,
                        mosqueMessage = settings.message,
                        mosqueName = settings.mosqueName,
                        selectedBackgroundImage = resolvedBg,
                        // Reformat prayer times from existing merged data when format toggles.
                        // Falls back to settings-stored times if Room data isn't loaded yet.
                        todayPrayerTime = buildDisplayPrayerTime(
                            date = current.data?.todayPrayerTime?.date,
                            source = settings,
                            is24Hour = settings.is24HourFormat,
                            fallback = current.todayPrayerTime,
                        ),
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Notification observer
    //
    // Collects notifications from timeState every time the ticker emits.
    // Uses distinctUntilChanged on the notification list so the body only
    // runs when the list actually changes — not once per second.
    //
    // What "changed" means:
    //   - A new before-prayer window just opened   (e.g. 30m before Asr)
    //   - A before-window closed and an after-window opened (prayer passed)
    //   - The list went from non-empty to empty    (window closed)
    // ─────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
// In MainViewModel
// ─────────────────────────────────────────────────────────────────────────────

    private fun observeNotifications() {
        viewModelScope.launch {
            timeState
                .map { it.currentNotification }
                .distinctUntilChanged { old, new ->
                    // Two notifications are "the same" when they represent the same
                    // prayer window — prayer name + trigger direction + offset slot.
                    // We deliberately ignore fields that can differ across ticks even
                    // for the same window (e.g. message strings built with formatted
                    // time, or new object instances created every second in the ticker).
                    old?.compatLocalTime?.name == new?.compatLocalTime?.name &&
                            old?.triggerType == new?.triggerType &&
                            old?.offsetMinutes == new?.offsetMinutes
                    // null == null is handled automatically: if both are null,
                    // the lambda is never called and the values are considered equal.
                }
                .collect { notification ->
                    logNotification(notification)
                    handleNotificationBanner(notification)
                }
        }
    }

    private fun observeNotificationsOld() {
        viewModelScope.launch {
            timeState
                .map { it.currentNotification }
                .distinctUntilChanged()   // only runs when the notification changes
                .collect { notification ->
                    logNotification(notification)
                    handleNotificationBanner(notification)
                }
        }
    }

    /**
     * Logs the active notification trigger.
     * Verbose enough to debug timing issues without flooding release logs.
     */
    private fun logNotification(notification: NotificationTrigger?) {
        if (notification == null) {
            Timber.v("🔕 No active notification window")
            return
        }

        val prayerName = notification.compatLocalTime.name.replaceFirstChar { it.uppercase() }
        val time = notification.compatLocalTime.let {
            "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
        }
        val direction = when (notification.triggerType) {
            TriggerType.BEFORE_PRAYER -> "in ${-notification.offsetMinutes}m"
            TriggerType.AFTER_PRAYER -> "${notification.offsetMinutes}m ago"
        }

        Timber.d("🔔 $prayerName @ $time — $direction | ${notification.message}")
    }

    private fun handleNotificationBanner(notification: NotificationTrigger?) {
        val hasActive = notification != null

        // 1. Resolve slide images/data for the active trigger
        val enriched = if (hasActive && notification != null) {
            updatedNotification(notification)
        } else {
            null
        }

        // ── Log state transition ────────────────────────────────────────────
        if (hasActive && enriched != null) {
            Timber.d(
                "🔔 Notification window OPEN: ${enriched.compatLocalTime.name} " +
                        "(offset=${enriched.offsetMinutes}m, image=${enriched.imageMessage})"
            )
        } else {
            Timber.i("🔕 Notification window CLOSED → showMessageNotification will be set to false")
        }

        _uiState.update { current ->
            // Guard: Prevent redundant recompositions and avoid unnecessary state updates
            val currentActive = current.activeNotifications.firstOrNull()
            if (current.showMessageNotification == hasActive &&
                currentActive == enriched
            ) {
                Timber.v("State unchanged, skipping update")
                return@update current
            }

            // Log the state transition for debugging
            //Timber.d("Banner State Change: hasActive=$hasActive, trigger=${enriched?.compatLocalTime?.name ?: "none"}")

            current.copy(
                showMessageNotification = hasActive,
                activeNotifications = if (enriched != null) listOf(enriched) else emptyList(),
            )
        }

        // 2. Resolve the Effect (Navigation) — but ONLY send if actually needed
        if (hasActive && enriched != null) {
            Timber.d("Navigation Effect: NavigateToMessage for ${enriched.compatLocalTime.name} (Offset: ${enriched.offsetMinutes}m, Image: ${enriched.imageMessage})")
            val result = _effect.trySend(MainScreenEffect.NavigateToMessage(enriched))
            if (!result.isSuccess) {
                Timber.w("Effect delivery failed: Channel might be full or closed")
            }
        } else {
            // Only send CloseMessageScreen if we had notifications before
            if (!hasActive && _uiState.value.activeNotifications.isNotEmpty()) {
                Timber.i("Navigation Effect: CloseMessageScreen triggered (Notification closed)")
                val result = _effect.trySend(MainScreenEffect.CloseMessageScreen)
                if (!result.isSuccess) {
                    Timber.w("CloseMessageScreen delivery failed, but MessageRoute should detect via state")
                }
            } else {
                //No previous notifications, so no CloseMessageScreen needed
                Timber.v("No previous active notifications, CloseMessageScreen not sent")
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────────
    /* Notification image resolver

     Maps a NotificationTrigger to its correct slide image using display order.

     There are up to 10 notification windows total:
       beforePrayerOffsets = [30, 10, 5, 3, 1]  → notification indices 0–4
       afterPrayerOffsets  = [5, 7, 10, 15, 30] → notification indices 5–9

     Slide selection:
       notificationIndex % slides.size

     With 10 notifications and 8 slides:
       index 0 → slide displayOrder 0
       index 1 → slide displayOrder 1
       ...
       index 7 → slide displayOrder 7
       index 8 → slide displayOrder 0  (wraps)
       index 9 → slide displayOrder 1  (wraps)

     If there are no slides at all, imageMessage stays null.*/
// ─────────────────────────────────────────────────────────────────────────────
    private fun updatedNotification(trigger: NotificationTrigger): NotificationTrigger {
        // Slides sorted by displayOrder so index 0 = first slide shown, etc.
        val slides = _uiState.value.data?.slides
            ?.sortedBy { it.displayOrder }
            ?.mapNotNull { slide ->
                // Prefer locally-cached path for offline support; fall back to remote URL
                slide.localPath?.takeIf { it.isNotBlank() }
                    ?: slide.imageUrl.takeIf { it.isNotBlank() }
            }
            ?: emptyList()

        if (slides.isEmpty()) {
            Timber.v("No slides available — notification sent without image")
            return trigger
        }

        // Determine which of the 10 notification slots this trigger occupies.
        // BEFORE windows: slots 0–4  (offset is negative, e.g. -30, -10, -5, -3, -1)
        // AFTER windows:  slots 5–9  (offset is positive, e.g.  +5, +7, +10, +15, +30)
        val notificationIndex: Int = when (trigger.triggerType) {
            TriggerType.BEFORE_PRAYER -> {
                val absOffset = -trigger.offsetMinutes          // e.g. -30 → 30
                Constants.beforePrayerOffsets.indexOf(absOffset.toLong())
            }

            TriggerType.AFTER_PRAYER -> {
                val baseIndex = Constants.beforePrayerOffsets.size  // = 5
                val idx = Constants.afterPrayerOffsets.indexOf(trigger.offsetMinutes.toLong())
                baseIndex + idx
            }
        }

        if (notificationIndex < 0) {
            // Offset not found in either list — should never happen, but safe guard
            Timber.w("Unknown offset ${trigger.offsetMinutes} — no image assigned")
            return trigger
        }

        // Wrap around so we never go out of bounds regardless of slide count
        val slideIndex = notificationIndex % slides.size
        val imagePath = slides[slideIndex]

        Timber.d(
            "🖼 Notification image resolved — " +
                    "prayer=${trigger.compatLocalTime.name}, " +
                    "type=${trigger.triggerType}, " +
                    "offset=${trigger.offsetMinutes}m, " +
                    "notificationSlot=$notificationIndex, " +
                    "slideIndex=$slideIndex/${slides.size}, " +
                    "path=$imagePath"
        )

        return trigger.copy(imageMessage = imagePath)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sync
    // ─────────────────────────────────────────────────────────────────────────

    private fun startSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = settingsRepository.settingsFlow
                .map { it.userId }
                .filter { it.isNotBlank() }
                .first()
            runCatching { repository.initializeSync(uid) }
                .onFailure { e ->
                    Timber.e(e, "Sync init failed")
                    _uiState.update { it.copy(error = "Connection Error") }
                }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Events
    // ─────────────────────────────────────────────────────────────────────────

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ShowSnackBar -> viewModelScope.launch {
                _effect.send(MainScreenEffect.ShowToast(event.message))
            }

            MainScreenEvent.OnMoreClick -> viewModelScope.launch {
                _effect.send(MainScreenEffect.NavigateToSettings)
            }

            MainScreenEvent.OnQrClick ->
                _uiState.update { it.copy(showNotificationBanner = true) }

            MainScreenEvent.DismissNotificationBanner ->
                _uiState.update { it.copy(showNotificationBanner = false) }

            MainScreenEvent.Tick -> Unit  // handled by timeFlow
            is MainScreenEvent.LoadMosqueData -> Unit  // handled by pipeline
            is MainScreenEvent.AddNotification -> Unit  // TODO
            is MainScreenEvent.RemoveNotification -> Unit  // TODO
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.stopSync()
            _uiState.update { MainScreenState() }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Prayer time helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a [PrayerTime] ready for display from [MosqueCompositeData].
     *
     * Always reads from Room data ([source]) when available.
     * Falls back to [fallback] (the last known display prayer time) so the UI
     * never goes blank while Room hasn't emitted yet.
     *
     * @param is24Hour  controls 12/24-hour formatting
     * @param fallback  previous display value to use if [source] has no data
     */

    private fun buildDisplayPrayerTime(
        date: String? = null,
        source: Settings? = null,
        is24Hour: Boolean,
        fallback: PrayerTime? = null,
    ): PrayerTime? {
        Timber.d(
            "buildDisplayPrayerTime() " +
                    "\nDate: $date \nSettings: $source \nis24Hour: $is24Hour"
        )
        source ?: return fallback
        source.apply {
            val prayerTime = PrayerTime(
                date = date ?: "",
                fajr = fajrTime.toCompatTime("fajr").toDisplayString(is24Hour),
                dhuhr = dhuhrTime.toCompatTime("dhuhr").toDisplayString(is24Hour),
                asr = asrTime.toCompatTime("asr").toDisplayString(is24Hour),
                maghrib = maghribTime.toCompatTime("maghrib").toDisplayString(is24Hour),
                isha = ishaTime.toCompatTime("isha").toDisplayString(is24Hour),
                sunrise = sunriseTime.toCompatTime("sunrise").toDisplayString(is24Hour),
                jumah = jumahTime.toCompatTime("jumah").toDisplayString(is24Hour),
            )
            val prayerTime24Hour = PrayerTime(
                date = date ?: "",
                fajr = fajrTime.toCompatTime("fajr").toDisplayString(),
                dhuhr = dhuhrTime.toCompatTime("dhuhr").toDisplayString(),
                asr = asrTime.toCompatTime("asr").toDisplayString(),
                maghrib = maghribTime.toCompatTime("maghrib").toDisplayString(),
                isha = ishaTime.toCompatTime("isha").toDisplayString(),
                sunrise = sunriseTime.toCompatTime("sunrise").toDisplayString(),
                jumah = jumahTime.toCompatTime("jumah").toDisplayString(),
            )
            // Seed the live ticker with today's schedule for updated notification
            timeRepository.initialize(prayerTime24Hour.toCompatLocalTimeList())
            Timber.d("Setting Prayer Time : $prayerTime")
            return prayerTime
        }
    }

    /**
     * Saves raw 24-hour prayer times from Room into DataStore.
     *
     * DataStore always stores the 24h "HH:mm" value regardless of the
     * user's display format. The SettingsScreen reads these for its default
     * time picker values.
     *
     * Only called when [merged] has actual prayer data — no-ops otherwise.
     */
    private suspend fun savePrayerTimesToDataStore(merged: MosqueCompositeData) {
        val prayer = merged.todayPrayerTime ?: return
        val mosque = merged.mosque

        with(settingsRepository) {
            // Strip seconds if present ("04:50:00" → "04:50")
            setPrayerTime("fajr", prayer.fajr.toHHmm())
            setPrayerTime("dhuhr", prayer.dhuhr.toHHmm())
            setPrayerTime("asr", prayer.asr.toHHmm())
            setPrayerTime("maghrib", prayer.maghrib.toHHmm())
            setPrayerTime("isha", prayer.isha.toHHmm())
            setPrayerTime("sunrise", prayer.sunrise.toHHmm())
            mosque?.jumuaTime?.takeIf { it.isNotBlank() }
                ?.let { setPrayerTime("jumah", it.toHHmm()) }

            // Background: prefer locally-cached path, then remote URL
            (mosque?.localPath ?: mosque?.image)
                ?.takeIf { it.isNotBlank() }
                ?.let { setBackgroundImage(it) }
        }
    }

    /**
     * Resolves a background image URL to a local cache path.
     * Returns the path unchanged if it's already local.
     */
    private suspend fun resolveBackgroundImage(url: String): String {
        if (url.isBlank()) return ""
        if (!url.startsWith("http")) return url   // already a local path
        return withContext(Dispatchers.IO) {
            runCatching {
                imageRepository.getCachedFile(url)?.absolutePath
                    ?: imageRepository.cacheImage(url)?.absolutePath
                    ?: url
            }.getOrDefault(url)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // String → CompatLocalTime
    //
    // Handles both "HH:mm" and "HH:mm:ss" formats that come from Room.
    // Returns midnight (00:00) on any parse failure — never throws.
    // ─────────────────────────────────────────────────────────────────────────

    private fun String.toCompatTime(name: String): CompatLocalTime =
        runCatching {
            val parts = split(":")
            CompatLocalTime(name = name, hour = parts[0].toInt(), minute = parts[1].toInt())
        }.getOrDefault(CompatLocalTime(name = name, hour = 0, minute = 0))

    /** "04:50:00" → "04:50"  |  "04:50" → "04:50" */
    private fun String.toHHmm(): String =
        split(":").take(2).joinToString(":").padStart(5, '0')
}
