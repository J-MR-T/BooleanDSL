package mainpack

import bool.algorithms.Algorithms
import bool.expressions.Expression.Companion.expression
import bool.expressions.Expression.Companion.randomExpression
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.math.pow
import kotlin.system.measureNanoTime

fun main() {
    //+ to add a string as a variable/an expression to the scope
    //- to simply convert a string to a variable but **not** add it to the scope

    val expr = randomExpression(2000, 2000, 600)


//    val i1 = Integer.MAX_VALUE / 10000
//    for (i in 0..i1){
//        for (i2 in 0..i1){
//        }
//        if(i % (i1/100) == 0) {
//            println("${i/i1.toDouble()}")
//        }
//    }
    println(expr)

    Path.of("expr2000-2000-600.txt").writeText(expr.toString())


    var works: Boolean
    var     time = measureNanoTime { works = Algorithms.dpll(expr, false) }
    println(time * 10.0.pow(-9))
    println("Erfüllbar: $works")

    time = measureNanoTime { works = Algorithms.dpll(expr, true) }
    println(time * 10.0.pow(-9))
    println("Erfüllbar: $works")

    for(i in 0..1_000_000_000) {
        val t = Thread() {}
        t.start()
    }

    expr.extendMapping()
    println(expr.mapping)
    Path.of("expr2000-2000-600-mapping.txt").writeText(expr.mapping.toString())

//    println(Algorithms.resolution(expr))

//    println(expr)
}

private fun expression1() = expression {
    and {
        +or {
            -"A"
            -"B"
            +"E"
            +"C"
        }
        +or {
            +"C"
            +"A"
            +"D"
            +"E"
        }
        +or {
            +"E"
            -"F"
        }
        +or {
            -"E"
            +"F"
        }
        +or {
            -"E"
            -"F"
        }
        +or {
            +"E"
            +"F"
        }
    }
}
