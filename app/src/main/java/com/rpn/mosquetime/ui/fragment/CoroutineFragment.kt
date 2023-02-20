package com.rpn.mosquetime.ui.fragment

import androidx.fragment.app.Fragment
import com.rpn.mosquetime.ui.viewmodel.LoginRegisterViewModel
import com.rpn.mosquetime.ui.viewmodel.MainViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

open class CoroutineFragment : Fragment(), KoinComponent {

    private val mainDispatcher: CoroutineDispatcher get() = Main
    protected val mainViewModel by inject<MainViewModel>()
    protected val loginRegisterViewModel by inject<LoginRegisterViewModel>()
    private val job = Job()
    private val scope = CoroutineScope(job + mainDispatcher)

    protected fun launch(
        context: CoroutineContext = mainDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(context, start, block)

    override fun onDestroyView() {
        job.cancel()
        super.onDestroyView()
    }

    open suspend fun doWork(): Any {
        TODO("Not yet implemented")
    }
}