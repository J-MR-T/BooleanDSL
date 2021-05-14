package bool.scopes

import bool.expressions.Expression

class AndScope : Scope() {
    override val separator ="⋀"

    override fun eval(): Boolean {
        return expressions.reduce(Expression::and)()
    }

}