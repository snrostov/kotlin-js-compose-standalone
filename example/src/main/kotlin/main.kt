import androidx.compose.*
import kotlin.browser.document
import kotlin.browser.window

@Composable
fun App() {
    Div {
        Text("Hello, world!")
    }
}

fun main() {
    window.addEventListener("load", {
        val recomposer = object: Recomposer() {
            override fun scheduleChangesDispatch() {
            }
        }
        val composer = HtmlComposer(document, document.body!!, recomposer)

        composer.compose {
            linear {
                text("Hello, ")
                text("world!")
            }
        }

        composer.applyChanges()
    })
}