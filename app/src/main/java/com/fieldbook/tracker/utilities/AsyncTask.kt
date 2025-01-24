package com.fieldbook.tracker.utilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class AsyncJob {

    companion object {

        fun async(backgroundBlock: suspend () -> Unit, uiBlock: suspend () -> Unit, onCanceled: suspend () -> Unit) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    backgroundBlock()
                    withContext(Dispatchers.Main) {
                        uiBlock()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onCanceled()
                } finally {
                    scope.cancel()
                }
            }
        }
    }
}