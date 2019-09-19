package androidx.compose

import androidx.compose.Compose.TAG_ROOT_COMPONENT
import kotlin.browser.window

actual class BitSet actual constructor() {
    private var words: IntArray = IntArray(1)

    fun ensureCapacity(index: Int) {
        if (index > words.size * SLOT_SIZE) {
            var n = words.size
            while (index > n * SLOT_SIZE) n *= 2
            words = words.copyOf(n)
        }
    }

    actual fun set(bitIndex: Int) {
        ensureCapacity(bitIndex)
        val slot = bitIndex / SLOT_SIZE
        words[slot] = words[slot].or(1.shl(bitIndex))
    }

    actual fun or(set: BitSet) {
        words = set.words
    }

    actual fun clear(bitIndex: Int) {
        ensureCapacity(bitIndex)
        val slot = bitIndex / SLOT_SIZE
        words[slot] = words[slot].and(1.shl(bitIndex).inv())
    }

    actual operator fun get(bitIndex: Int): Boolean {
        ensureCapacity(bitIndex)
        val slot = bitIndex / SLOT_SIZE
        return words[slot].and(1.shl(bitIndex)) != 0
    }

    companion object {
        const val SLOT_SIZE = 32
    }
}

actual open class ThreadLocal<T> actual constructor() {
    private var value: T? = initialValue()

    actual fun get(): T? = value

    actual fun set(value: T?) {
        this.value = value
    }

    protected actual open fun initialValue(): T? = null
}

actual fun identityHashCode(instance: Any?): Int =
    instance?.hashCode() ?: 0

actual interface ViewParent

actual open class View {
    var viewAdapter: ViewAdapter? = null
    var _parent: ViewParent? = null
    var _context: Context? = null
    var rootComponent: Any? = null
    actual fun getTag(key: Int): Any {
        check(key == TAG_ROOT_COMPONENT)
        return rootComponent!!
    }

    actual fun setTag(key: Int, tag: Any?) {
        check(key == TAG_ROOT_COMPONENT)
        rootComponent = tag
    }
}

actual val View.parent: ViewParent
    get() = this._parent!!

actual val View.context: Context
    get() = _context!!

actual abstract class ViewGroup : View() {
    actual fun removeAllViews() {
    }
}

actual abstract class Context

actual class FrameLayout actual constructor(context: Context)

actual inline fun <R> synchronized(lock: Any, block: () -> R): R = block()

actual class WeakReference<T> actual constructor(val instance: T) {
    actual fun get(): T? = instance
}

actual class Looper

val looper = Looper()

actual fun isMainThread(): Boolean = false

actual object LooperWrapper {
    actual fun getMainLooper(): Looper = looper
}

actual class Handler actual constructor(looper: Looper) {
    actual fun postAtFrontOfQueue(block: () -> Unit): Boolean {
        window.setTimeout({block()}, 0)
        return true
    }
}

actual interface ChoreographerFrameCallback {
    actual fun doFrame(frameTimeNanos: Long)
}

actual object Choreographer {
    actual fun postFrameCallback(callback: ChoreographerFrameCallback) {
    }

    actual fun postFrameCallbackDelayed(
        delayMillis: Long,
        callback: ChoreographerFrameCallback
    ) {
    }

    actual fun removeFrameCallback(callback: ChoreographerFrameCallback) {
    }
}

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR
)
actual annotation class MainThread actual constructor()

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR
)
actual annotation class TestOnly actual constructor()

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
actual annotation class CheckResult actual constructor(actual val suggest: String)