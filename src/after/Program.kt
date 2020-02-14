package after

import after.Program.EOF
import after.Program.NEOF

sealed class Program {
    object EOF : Program()
    data class NEOF(val act: Action, val other: Program) : Program()

    fun toList(): List<Action> = when (this) {
        EOF -> listOf()
        is NEOF -> listOf(act) + other.toList()
    }
}

fun List<Action>.fromList(): Program = if (isNotEmpty()) {
    NEOF(first(), drop(1).fromList())
} else {
    EOF
}