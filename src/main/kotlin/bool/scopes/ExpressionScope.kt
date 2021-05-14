package bool.scopes

import bool.expressions.Expression

class ExpressionScope {
    fun and(function: AndScope.() -> Unit): Expression {
        val expression = AndScope()
        expression.function()
        return expression
    }

    fun or(function: OrScope.() -> Unit): Expression {
        val expression = OrScope()
        expression.function()
        return expression
    }
}