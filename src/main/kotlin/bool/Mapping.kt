package bool

import bool.expressions.Expression
import bool.expressions.Variable

class Mapping {
    companion object {
        val standard: Mapping = Mapping()
    }

    private var mapping: MutableMap<in Variable, Boolean> = mutableMapOf()

    operator fun get(v: Variable): Boolean? {
        return mapping[v]
    }

    operator fun plus(pair: Pair<Variable, Boolean>): Mapping {
        mapping[pair.first] = pair.second
        return this
    }

    operator fun minus(v: Variable): Mapping {
        mapping.remove(v)
        return this
    }

    operator fun not(): Mapping {
        mapping.replaceAll { _, u -> !u }
        return this
    }

    operator fun set(v: Variable, boolean: Boolean) {
        mapping[v] = boolean
    }

    fun set(mapping: Map<Variable, Boolean>): Mapping {
        this.mapping = mapping.toMutableMap()
        return this
    }

    fun asSequence() = mapping.asSequence()

    fun contains(expression: Expression): Boolean {
        //TODO make sure this works with single variables (should)
        return this.mapping.keys.flatMap { key -> (key as Expression).variables() }.containsAll(expression.variables())
    }

    override fun toString(): String {
        return this.asSequence()
            .joinToString(System.lineSeparator(), "Mapping: ${System.lineSeparator()}", System.lineSeparator()) {
                "${it.component1()} is mapped to ${it.component2()}"
            }
    }

    fun remove(variable: Variable) {
        mapping.remove(variable)
    }

    fun containsKey(v: Variable) = mapping.containsKey(v)
}
