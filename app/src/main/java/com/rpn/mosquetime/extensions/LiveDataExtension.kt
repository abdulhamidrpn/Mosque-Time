package com.rpn.mosquetime.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun <T> LiveData<T>.observeOnce(onEmission: (T) -> Unit) {
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            onEmission(value)
            removeObserver(this)
        }
    }
    observeForever(observer)
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}


fun <R> CoroutineScope.executeAsyncTask(
    onPreExecute: () -> Unit,
    doInBackground: () -> R,
    onPostExecute: (R) -> Unit
) = launch {
    onPreExecute() // runs in Main Thread
    val result = withContext(Dispatchers.IO) {
        doInBackground() // runs in background thread without blocking the Main Thread
    }
    onPostExecute(result) // runs in Main Thread
}
