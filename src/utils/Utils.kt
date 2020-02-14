package utils

sealed class Either<out A, out B> {
    data class Ok<A>(val value: A) : Either<A, Nothing>()
    data class Err<B>(val value: B) : Either<Nothing, B>()
}

fun <A, B, C> Pair<A, B>.mapRight(f: (B) -> C): Pair<A, C> = Pair(first, f(second))
fun <A, B, C> Pair<A, B>.mapLeft(f: (A) -> C): Pair<C, B> = Pair(f(first), second)