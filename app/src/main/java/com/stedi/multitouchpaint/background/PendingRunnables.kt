package com.stedi.multitouchpaint.background

import android.os.Handler
import android.os.Looper
import java.util.*

object PendingRunnables {
    private val pendingRunnables = LinkedList<Runnable>()
    private val handler = Handler(Looper.getMainLooper())

    private var isResumed = false

    fun post(runnable: Runnable) {
        handler.post {
            if (isResumed) {
                runnable.run()
            } else {
                pendingRunnables.add(runnable)
            }
        }
    }

    fun release() {
        isResumed = true
        releaseRunnables()
    }

    fun hold() {
        isResumed = false
    }

    private fun releaseRunnables() {
        if (!pendingRunnables.isEmpty()) {
            val release = LinkedList<Runnable>(pendingRunnables)
            pendingRunnables.clear()
            while (!release.isEmpty()) {
                post(release.pollFirst())
            }
        }
    }
}