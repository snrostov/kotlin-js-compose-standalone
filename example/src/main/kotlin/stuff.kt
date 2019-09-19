@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class Composable

@Composable
fun Div(body: @Composable() () -> Unit) {

}

@Composable
fun Text(text: String) {

}