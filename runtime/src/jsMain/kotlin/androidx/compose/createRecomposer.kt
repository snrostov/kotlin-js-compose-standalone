package androidx.compose

import kotlin.browser.window

internal actual fun createRecomposer(): Recomposer = object : Recomposer() {
    var timer: Int = 0

    override fun hasPendingChanges(): Boolean = timer != 0

    override fun scheduleChangesDispatch() {
        if (timer == 0) timer = window.setTimeout({
            dispatchRecomposes()
            timer = 0
        }, 0)
    }
}