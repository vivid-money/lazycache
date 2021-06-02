package vivid.money

data class Option<T : Any>(
    val value: T?
) {

    val isEmpty = value == null
    val hasValue = !isEmpty

    companion object {
        fun <T : Any> none() = Option<T>(null)
    }
}