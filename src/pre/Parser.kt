package pre

import utils.Either
import utils.Either.Err
import utils.Either.Ok

class Parser<I, O>(val parse: (Pair<I, O>) -> Either<Pair<I, O>, String>) {
    fun alt(other: Parser<I, O>): Parser<I, O> =
        Parser {
            when (val res = parse(it)) {
                is Ok -> res
                is Err -> other.parse(it)
            }
        }

    fun comp(other: Parser<I, O>): Parser<I, O> =
        Parser {
            when (val res = parse(it)) {
                is Ok -> res.value.first?.let { inp -> other.parse(Pair(inp, res.value.second)) } ?: Err("combo error")
                is Err -> res
            }
        }

    fun loop(): Parser<I, O> =
        Parser {
            var t = parse(it)
            var r = Ok(it)
            while (t is Ok) {
                r = t
                t = parse(r.value)
            }
            r
        }
}
