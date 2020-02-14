package after

import after.Action.*

fun indent(width: Int, level: Int): String = List(width * level) { " " }.joinToString(separator = "")

fun actProcess(act: Action, width: Int, level: Int): String = when (act) {
    is Var -> "${indent(width, level)}${act.name} = ${act.value.expr}\n"
    is If -> "${indent(width, level)}if ${act.cond.expr}:\n" +
            progProcess(act.left, width, level + 1) +
            act.right?.let { prog ->
                "${indent(width, level)}else:\n" +
                        "${progProcess(prog, width, level + 1)}\n"
            }.orEmpty()
    is For -> "for ${act.iterName} in ${act.obj.expr}:\n" +
            "${progProcess(act.body, width, level + 1)}\n"
    is Func -> "def ${act.name}(${act.params.joinToString()}):\n" +
            "${progProcess(act.body, width, level + 1)}\n"
    is Class -> "class ${act.name}:\n" +
            act.statFields
                .joinToString(separator = "") { actProcess(it, width, level + 1) } +
            act.cons
                ?.let { cons ->
                    actProcess(Func("__init__", cons.params, cons.body), width, level + 1)
                }.orEmpty() +
            act.methods
                .joinToString(separator = "") { actProcess(it, width, level + 1) }
    is Expr -> "${indent(width, level)}${act.expr}\n"
}

fun progProcess(prog: Program, width: Int, level: Int): String = when (prog) {
    Program.EOF -> if (level == 0) "//Created by GrandArchTemplar" else ""
    is Program.NEOF -> "${actProcess(
        prog.act,
        width,
        level
    )}${progProcess(prog.other, width, level)}"
}