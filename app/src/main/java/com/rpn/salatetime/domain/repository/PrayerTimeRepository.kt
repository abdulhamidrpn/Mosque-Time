package com.rpn.salatetime.domain.repository

/**
 * Domain contract for prayer time data.
 *
 * The repository abstracts:
 *  1. Room as the local single source of truth.
 *  2. Supabase Realtime as the remote change-data-capture trigger.
 *  3. DataStore as the downstream consumer for adjusted settings.
 *
 * Callers (UseCases / ViewModel) never reference Room or Supabase directly.
 */
interface PrayerTimeRepository {


    /**
     * Initialise the Supabase Realtime subscription.
     * Must be called once when the app comes online; safe to call again
     * after a network reconnect (idempotent).
     */
    suspend fun startRealtimeSync()

    /**
     * Clean up the Realtime WebSocket channel.
     * Called when the app moves to the background or the process is stopping.
     */
    suspend fun stopRealtimeSync()

    /**
     * Fetch the next day's record from Room (called by MidnightResetWorker).
     * Returns null if the record hasn't arrived from Supabase yet.
     */
    suspend fun getPrayerTimeForDate(date: String)

    /**
     * Fetch and cache a range of dates from Supabase via REST.
     * Used on first launch or when the local cache is empty.
     */
    suspend fun fetchAndCacheRange(fromDate: String, toDate: String)
}
