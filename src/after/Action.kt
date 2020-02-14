package after

sealed class Action {
    data class Expr(val expr: String) : Action()
    data class Var(val name: String, val value: Expr) : Action()
    data class If(val cond: Expr, val left: Program, val right: Program?) : Action()
    data class For(val iterName: String, val obj: Expr, val body: Program) : Action()
    data class Func(val name: String, val params: List<String>, val body: Program) : Action()
    data class Class(val name: String, val statFields: List<Var>, val cons: Func?, val methods: List<Func>) : Action()
}