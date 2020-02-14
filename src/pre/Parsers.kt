package pre

import after.Action
import after.Action.*
import after.Program
import after.Program.EOF
import after.fromList
import utils.Either.Err
import utils.Either.Ok
import utils.mapRight

val parseOkS: Parser<String, String> = Parser { Ok(it) }

val parserFreeS: Parser<String, String> = Parser { Ok(Pair(it.first, "")) }
val parseAnyCharC: Parser<String, Char> = Parser {
    if (it.first.isNotEmpty()) {
        Ok(Pair(it.first.drop(1), it.first.first()))
    } else {
        Err("no string input")
    }
}

val parseAnyCharS: Parser<String, String> = Parser {
    if (it.first.isNotEmpty()) {
        Ok(Pair(it.first.drop(1), it.second + it.first.first()))
    } else {
        Err("no string input")
    }
}

val parseExactCharC: ((Char) -> Boolean) -> Parser<String, Char> = { c ->
    Parser {
        if (it.first.isNotEmpty()) {
            val res = it.first.first()
            if (c(res)) {
                Ok(Pair(it.first.drop(1), res))
            } else {
                Err("miss matching characters")
            }
        } else {
            Err("no string input")
        }
    }
}

val parseExactCharS: ((Char) -> Boolean) -> Parser<String, String> = { c ->
    Parser {
        if (it.first.isNotEmpty()) {
            val res = it.first.first()
            if (c(res)) {
                Ok(Pair(it.first.drop(1), it.second + res))
            } else {
                Err("miss matching characters")
            }
        } else {
            Err("no string input")
        }
    }
}

val parseSkipExactCharS: ((Char) -> Boolean) -> Parser<String, String> = { c ->
    Parser {
        if (it.first.isNotEmpty()) {
            val res = it.first.first()
            if (c(res)) {
                Ok(Pair(it.first.drop(1), it.second))
            } else {
                Err("miss matching characters")
            }
        } else {
            Err("no string input")
        }
    }
}

val parseAnyStringLn: Parser<String, String> =
    parseExactCharS { it != '\n' }.comp(parseExactCharS { it != '\n' }.loop()).comp(parseSkipExactCharS { it == '\n' })

val parseWord: Parser<String, String> = parseExactCharS { !it.isWhitespace() }.loop()

val parseWhite: Parser<String, String> = parseExactCharS { it.isWhitespace() }.loop()

val parseWhiteWord: Parser<String, String> = parseWord.comp(parseWhite)

val parseExactString: (String) -> Parser<String, String> = {
    it.map { c -> parseExactCharS { it -> it == c } }.reduce(Parser<String, String>::comp)
}

val parseExpr: Parser<String, Action> = Parser {
    when (val res = parseExactString("Invoke:").comp(parseWhite).comp(parserFreeS).comp(parseAnyStringLn)
        .parse(it.mapRight { "" })) {
        is Ok -> Ok(res.value.mapRight(Action::Expr))
        is Err -> Err(res.value + "\ncan't parse this string!")
    }
}

val parseVar: Parser<String, Action> = Parser {
    when (val res =
        parseWhite
            .comp(parseExactString("The")).comp(parseWhite)
            .comp(parserFreeS)
            .comp(parseWord)
            .parse(it.mapRight { "" })) {
        is Ok -> {
            val name = res.value.second
            when (val ans =
                parseWhite
                    .comp(parseExactString("become")).comp(parseWhite)
                    .comp(parserFreeS)
                    .comp(parseAnyStringLn)
                    .parse(res.value.mapRight { "" })) {
                is Ok -> Ok(Pair(ans.value.first, Var(name, Expr(ans.value.second))))
                is Err -> Err("can't parse expr")
            }
        }
        is Err -> Err("there is no var declaration")
    }
}

val parseIf: Parser<String, Action> = Parser {
    when (val cond =
        parseWhite
            .comp(parseExactString("Your")).comp(parseWhite)
            .comp(parseExactString("wisdom")).comp(parseWhite)
            .comp(parseExactString("is")).comp(parseWhite)
            .comp(parserFreeS)
            .comp(parseAnyStringLn)
            .parse(it.mapRight { "" })) {
        is Ok -> when (val leftPred =
            parseWhite
                .comp(parseExactString("Choose")).comp(parseWhite)
                .comp(parseExactString("wisely")).comp(parseWhite)
                .comp(parseExactString("between")).comp(parseWhite)
                .comp(parserFreeS).parse(cond.value)) {
            is Ok -> when (val left =
                parseProgram.parse(leftPred.value.mapRight { EOF })) {
                is Ok -> when (val isElse =
                    parseWhite
                        .comp(parseExactString("and")).comp(parseWhite)
                        .comp(parserFreeS).parse(left.value.mapRight { "" })
                    ) {
                    is Ok -> when (val right =
                        parseProgram.parse(isElse.value.mapRight { EOF })) {
                        is Ok -> when (val end = parseWhite.comp(parseExactString("Slow")).comp(parseWhite)
                            .parse(right.value.mapRight { "" })) {
                            is Ok -> Ok(end.value.mapRight {
                                If(
                                    Expr(cond.value.second),
                                    left.value.second,
                                    right.value.second
                                )
                            })
                            is Err -> Err("no end of if")
                        }
                        is Err -> Err("no else part")
                    }
                    is Err -> Ok(left.value.mapRight { then -> If(Expr(cond.value.second), then, null) })
                }
                is Err -> Err("can't parse if")
            }
            is Err -> Err("shit")
        }
        is Err -> Err("can't parse condition")
    }
}

val parseFn: Parser<String, Action> = Parser {
    when (val name =
        parseWhite
            .comp(parseExactString("Ascension")).comp(parseWhite)
            .comp(parseExactString("to")).comp(parseWhite)
            .comp(parseExactString("the")).comp(parseWhite)
            .comp(parserFreeS)
            .comp(parseWord).parse(it.mapRight { "" })
        ) {
        is Ok -> when (val preArgs =
            parseWhite
                .comp(parseExactString("requires")).comp(parseWhite)
                .comp(parserFreeS)
                .comp(parseAnyStringLn).parse(name.value)
            ) {
            is Ok -> {
                val args = preArgs.value.second.filter { !it.isWhitespace() }.split(',')
                when (val preBody =
                    parseWhite
                        .comp(parseExactString("so"))
                        .comp(parseWhite).parse(preArgs.value)
                    ) {
                    is Ok -> when (val body =
                        parseProgram.parse(preBody.value.mapRight { EOF })) {
                        is Ok -> when (val end =
                            parseWhite.comp(parseExactString("Descent")).comp(parseWhite)
                                .parse(body.value.mapRight { "" })) {
                            is Ok -> Ok(end.value.mapRight { Func(name.value.second, args, body.value.second) })
                            is Err -> Err("no end of fn")
                        }
                        //Ok(body.value.mapRight { t -> Func(name.value.second, args, t) })
                        is Err -> Err("no body")
                    }
                    is Err -> Err("no prebody")
                }
            }
            is Err -> Err("no args")
        }
        is Err -> Err("no func")
    }
}

val parseAct: Parser<String, Action> = parseFn.alt(parseIf).alt(parseVar).alt(parseExpr)

val parseActL: Parser<String, List<Action>> = Parser {
    when (val res = parseAct.parse(it.mapRight { Expr("") })) {
        is Ok -> Ok(res.value.mapRight { t -> it.second + listOf(t) })

        is Err -> Err("error on parse")
    }
}

val parseProgram: Parser<String, Program> = Parser {
    when (val res = parseActL.loop().parse(it.mapRight { listOf<Action>() })) {
        is Ok -> Ok(res.value.mapRight(List<Action>::fromList))
        is Err -> Err("shit")
    }
}