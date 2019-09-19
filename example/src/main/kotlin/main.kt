import kotlin.browser.document

@Composable
fun App() {
    Div {
        Text("Hello, world!")
    }
}

fun main() {
    document.write("Hello, world!")
}