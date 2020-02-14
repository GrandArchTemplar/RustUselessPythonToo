import after.Program.EOF
import after.progProcess
import pre.parseProgram
import utils.Either.Err
import utils.Either.Ok
import java.io.File

fun main() {
    val prog = File("example.rupt").readLines().joinToString(separator = "\n")
    when (val pars = parseProgram.parse(Pair(prog, EOF))) {
        is Ok -> println(progProcess(pars.value.second, 2, 0))
        is Err -> Err("fail")
    }
}