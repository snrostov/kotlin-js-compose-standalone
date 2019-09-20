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

        var counter = 0
        window.setInterval({
            composer.compose {
                span {
                    text("Hello, ")
                    text("world ${counter++}!")
                }
            }
            composer.applyChanges()
        }, 1000)
    })
}