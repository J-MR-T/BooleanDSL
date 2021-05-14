package bool.scopes

import bool.expressions.Expression
import bool.expressions.Variable

abstract class Scope : Expression() {
    internal open val separator: String? = null

    override operator fun plus(expression: Expression): Expression {
        this@Scope.expressions.add(expression)
        return expression
    }

    override operator fun String.unaryPlus(): Expression {
        val v = Variable(this)
        this@Scope.expressions.add(v)
        return v
    }

    operator fun Variable.unaryMinus(): Expression {
        val v = !this
        this@Scope.expressions.add(v)
        return v
    }

    operator fun String.invoke(): Expression {
        return Variable(this)
    }

    operator fun String.unaryMinus(): Expression {
        return (!Variable(this)).unaryPlus()
    }

    operator fun Expression.unaryPlus(): Expression {
        val e = this
        this@Scope.expressions.add(e)
        return e
    }

    operator fun Expression.unaryMinus(): Expression {
        return (not(this)).unaryPlus()
    }


    override fun toString(): String {
        return expressions.joinToString(" $separator ", "(", ")")
    }
}