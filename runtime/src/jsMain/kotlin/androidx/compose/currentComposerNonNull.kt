package androidx.compose



internal actual fun createComposer(
    root: Any,
    context: Context,
    recomposer: Recomposer
): Composer<*> = ViewComposer(root, context, recomposer)

internal actual val currentComposerNonNull
    get() = currentComposer ?: emptyComposition()

private fun emptyComposition(): Nothing =
    error("Composition requires an active composition context")

//val composer get() = ViewComposition(currentComposerNonNull as ViewComposer)

internal actual var currentComposer: Composer<*>? = null
    private set

actual fun <T> Composer<*>.runWithCurrent(block: () -> T): T {
    val prev = currentComposer
    try {
        currentComposer = this
        return block()
    } finally {
        currentComposer = prev
    }
}

class ViewComposer(
    val root: Any,
    val context: Context,
    recomposer: Recomposer,
    val adapters: ViewAdapters? = ViewAdapters()
) : Composer<Any>(
    SlotTable(),
    Applier(root, ViewApplyAdapter(adapters)), recomposer
) {

    init {
        FrameManager.ensureStarted()
    }
}

class ViewAdapters {
    private val adapters = mutableListOf<(parent: Any, child: Any) -> Any?>()

    fun register(adapter: (parent: Any, child: Any) -> Any?) = adapters.add(adapter)
    fun adapt(parent: Any, child: Any): Any? =
        adapters.map { it(parent, child) }.filterNotNull().firstOrNull()
}

interface ViewAdapter {
    val id: Int
    fun willInsert(view: View, parent: ViewGroup)
    fun didInsert(view: View, parent: ViewGroup)
    fun didUpdate(view: View, parent: ViewGroup)
}

class ComposeViewAdapter : ViewAdapter {
    override val id = 0
    val adapters = mutableListOf<ViewAdapter>()

    inline fun <T : ViewAdapter> get(id: Int, factory: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        val existing = adapters.firstOrNull { it.id == id } as? T
        if (existing != null) return existing
        val next = factory()
        adapters.add(next)
        return next
    }

    override fun willInsert(view: View, parent: ViewGroup) {
        for (adapter in adapters) adapter.willInsert(view, parent)
    }

    override fun didInsert(view: View, parent: ViewGroup) {
        for (adapter in adapters) adapter.didInsert(view, parent)
    }

    override fun didUpdate(view: View, parent: ViewGroup) {
        for (adapter in adapters) adapter.didUpdate(view, parent)
    }
}

private fun invalidNode(node: Any): Nothing =
    error("Unsupported node type ${node::class.simpleName}")


internal class ViewApplyAdapter(private val adapters: ViewAdapters? = null) :
    ApplyAdapter<Any> {
    private data class PendingInsert(val index: Int, val instance: Any)

    private val pendingInserts = Stack<PendingInsert>()

    override fun Any.start(instance: Any) {}
    override fun Any.insertAt(index: Int, instance: Any) {
        pendingInserts.push(PendingInsert(index, instance))
    }

    override fun Any.removeAt(index: Int, count: Int) {
        when (this) {
//            is ViewGroup -> removeViews(index, count)
            is Emittable -> emitRemoveAt(index, count)
            else -> invalidNode(this)
        }
    }

    override fun Any.move(from: Int, to: Int, count: Int) {
        when (this) {
//            is ViewGroup -> {
//                if (from > to) {
//                    var currentFrom = from
//                    var currentTo = to
//                    repeat(count) {
//                        val view = getChildAt(currentFrom)
//                        removeViewAt(currentFrom)
//                        addView(view, currentTo)
//                        currentFrom++
//                        currentTo++
//                    }
//                } else {
//                    repeat(count) {
//                        val view = getChildAt(from)
//                        removeViewAt(from)
//                        addView(view, to - 1)
//                    }
//                }
//            }
            is Emittable -> {
                emitMove(from, to, count)
            }
            else -> invalidNode(this)
        }
    }

    override fun Any.end(instance: Any, parent: Any) {
        val adapter = when (instance) {
            is View -> instance.viewAdapter
            else -> null
        }
        if (pendingInserts.isNotEmpty()) {
            val pendingInsert = pendingInserts.peek()
            if (pendingInsert.instance == instance) {
                val index = pendingInsert.index
                pendingInserts.pop()

                when (parent) {
//                    is ViewGroup ->
//                        when (instance) {
//                            is View -> {
//                                adapter?.willInsert(instance, parent)
//                                parent.addView(instance, index)
//                                adapter?.didInsert(instance, parent)
//                            }
//                            is Emittable -> {
//                                val adaptedView = adapters?.adapt(parent, instance) as? View
//                                    ?: error(
//                                        "Could not convert ${
//                                        instance.javaClass.simpleName
//                                        } to a View"
//                                    )
//                                adapter?.willInsert(adaptedView, parent)
//                                parent.addView(adaptedView, index)
//                                adapter?.didInsert(adaptedView, parent)
//                            }
//                            else -> invalidNode(instance)
//                        }
                    is Emittable ->
                        when (instance) {
                            is View -> parent.emitInsertAt(
                                index,
                                adapters?.adapt(parent, instance) as? Emittable
                                    ?: error(
                                        "Could not convert ${instance::class.simpleName} to an Emittable"
                                    )
                            )
                            is Emittable -> parent.emitInsertAt(index, instance)
                            else -> invalidNode(instance)
                        }
                    else -> invalidNode(parent)
                }
                return
            }
        }
//        if (parent is ViewGroup)
//            adapter?.didUpdate(instance as View, parent)
    }
}