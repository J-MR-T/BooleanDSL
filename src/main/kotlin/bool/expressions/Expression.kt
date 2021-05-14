package bool.expressions

import bool.Mapping
import bool.MissingMappingException
import bool.scopes.ExpressionScope
import java.util.*
import kotlin.math.exp

abstract class Expression(vararg expressions: Expression, var mapping: Mapping = Mapping.standard) {
    val expressions: MutableSet<Expression> = mutableSetOf()

    init {
        this.expressions += expressions
    }

    abstract fun eval(): Boolean

    operator fun invoke(): Boolean {
        return eval()
    }

    operator fun not(): Expression {
        return object : Expression(this@Expression) {
            override fun eval(): Boolean {
                return !this@Expression()
            }

            override fun toString(): String {
                return "!${this@Expression}"
            }
        }
    }

    fun variables(): List<Variable> {
        return if (this is Variable) {
            listOf(this)
        } else {
            expressions.map(Expression::variables).flatMap(List<Variable>::asSequence)
        }
    }

    fun addMapping(mapping: Mapping) {
        this.mapping = mapping
    }

    fun addMapping(mapping: Map<Variable, Boolean>) {
        this.mapping.set(mapping)
    }

    open operator fun plus(expression: Expression): Expression {
        return object : Expression(this@Expression, expression) {
            override fun eval(): Boolean {
                return this@Expression() || expression()
            }

            override fun toString(): String {
                return "(${this@Expression} ⋁ $expression)"
            }
        }
    }

    operator fun times(expression: Expression): Expression {
        return object : Expression(this@Expression, expression) {
            override fun eval(): Boolean {
                return this@Expression() && expression()
            }

            override fun toString(): String {
                return "(${this@Expression} ⋀ $expression)"
            }
        }
    }

    infix fun implies(expression: Expression): Expression {
        return object : Expression(this@Expression, expression) {
            override fun eval(): Boolean {
                //TODO make sure the precendence here is right ('()' before '!'), but I think it is
                return !this@Expression() || expression()
            }

            override fun toString(): String {
                return "(${this@Expression} -> $expression)"
            }
        }
    }

    fun register(vararg e: Expression) {
        expressions += e
    }

    companion object {
        val TRUE = object : Expression() {
            override fun eval(): Boolean {
                return true
            }

            override fun toString(): String {
                return "TRUE"
            }
        }

        val FALSE = object : Expression() {
            override fun eval(): Boolean {
                return false
            }

            override fun toString(): String {
                return "FALSE"
            }
        }

        fun expression(function: ExpressionScope.() -> Expression): Expression {
            return function(ExpressionScope())
        }

        fun and(expression1: Expression, expression2: Expression): Expression {
            return expression1 * expression2
        }

        fun or(expression1: Expression, expression2: Expression): Expression {
            return expression1 + expression2
        }

        fun not(expression: Expression): Expression {
            return !expression
        }

        fun randomExpression(numberOfVariables: Int, numberOfClauses: Int, numberOfLiteralsInClause: Int): Expression {
            val variables = List(numberOfVariables) {
                Variable("${'A' + it}")
            }.toSet()

            return expression {
                and {
                    repeat(numberOfClauses) {
                        +or {
                            repeat(numberOfLiteralsInClause) {
                                val random = variables.random()
                                if (Math.random() > 0.5) {
                                    +random
                                } else {
                                    -random
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    open operator fun String.unaryPlus(): Expression {
        return Variable(this)
    }

    fun toPrettyString(): String {
        val result: String =
            try {
                this().toString()
            } catch (e: MissingMappingException) {
                "at least one Variable is not mapped: $e ${System.lineSeparator()}If this is because DPLL has generated the mapping, this is fixable by calling the `extendMapping()` method."
            }
        return "$this: $result ${System.lineSeparator()}${toBooleanString()}"
    }

    fun toBooleanString(): String {
        val trueVariables = mapping.asSequence().filter { entry ->
            entry.value
        }.map { entry -> entry.key }.joinToString(separator = "|")

        val falseVariables = mapping.asSequence().filter { entry ->
            !entry.value
        }.map { entry -> entry.key }.joinToString(separator = "|")
        var str = toString()
        if (trueVariables.isNotBlank()) {
            str = str.replace(Regex(trueVariables), "True")
        }
        if (falseVariables.isNotBlank()) {
            str = str.replace(Regex(falseVariables), "False")
        }
        return str
    }


    fun setAllToSameMapping(newMapping: Mapping = Mapping()) {
        addMapping(newMapping)
        if (expressions.isNotEmpty()) {
            expressions.forEach { expression ->
                expression.setAllToSameMapping(newMapping)
            }
        }
    }

    open fun copy(): Expression {
        return object : Expression(
            expressions = this@Expression.expressions.map { e -> e.copy() }.toTypedArray(),
            mapping = mapping
        ) {
            //this deliberately uses the other expressions function and does not directly set it via =
            override fun eval(): Boolean {
                return this@Expression.eval()
            }

            override fun toString(): String = this@Expression.toString()
        }
    }

    /**
     * This can be used in conjunction with dpll to set irrelevant variables, because dpll might not set them
     */
    fun extendMapping() {
        this.variables().filter { variable -> !this.mapping.contains(variable) }.forEach { variable ->
            mapping + (variable to false)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Expression) return false
        return this.expressions == other.expressions
    }

    override fun hashCode(): Int {
        return this.expressions.hashCode()
    }
}