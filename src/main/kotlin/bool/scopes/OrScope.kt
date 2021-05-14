package bool.scopes

import bool.expressions.Expression

class OrScope : Scope() {
    override val separator = "⋁"

    override fun eval(): Boolean {
        return expressions.reduce(Expression::or)()
    }
}