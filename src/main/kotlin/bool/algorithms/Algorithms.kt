package bool.algorithms

import bool.Mapping
import bool.expressions.Expression
import bool.expressions.Variable

class Algorithms {

    companion object {

        /**
         * For now, this function just requires @param root to be in KNF/CNF
         */
        fun dpll(root: Expression, raw: Boolean = false): Boolean {
            val clauses = root.expressions
            if (clauses.isEmpty()) {
                return true
            } else if (clauses.containsEmpty()) {
                return false
            }
            root.setAllToSameMapping()
            return dpll(clauses,raw)
        }


        private fun dpll(clauses: MutableSet<Expression>, raw: Boolean = false): Boolean {
            if (clauses.isEmpty()) {
                return true
            } else if (clauses.containsEmpty()) {
                return false
            }
            val mapping = clauses.first().expressions.first().mapping
            if(!raw) olr(clauses, mapping,raw)?.let { return it }
//            if(!raw) plr(clauses, mapping,raw)?.let { return it }
            return dpllBranch(clauses, mapping,raw)
        }

        private fun olr(clauses: MutableSet<Expression>, mapping: Mapping, raw: Boolean): Boolean? {
            clauses.firstOrNull { clause -> clause.expressions.size == 1 }
                ?.let { expression ->
                    val variable = expression.variables().first() //always exists
                    return ruleMandate(mapping, variable, clauses, expression, raw)
                }
            return null
        }

        private fun plr(clauses: MutableSet<Expression>, mapping: Mapping, raw: Boolean): Boolean? {
            clauses.allVariables().forEach { variable ->
                val mappingExisted = mapping.containsKey(variable)
                mapping[variable] = true
                val listThatHasToBeUnified =
                    clauses.filter { clause -> clause.expressions.allVariables().contains(variable) }
                        .flatMap { clause -> clause.expressions }
                        .filter { literal -> literal.variables().contains(variable) }
                listThatHasToBeUnified.firstOrNull()?.let { mandate ->
                    if (listThatHasToBeUnified.all { expression ->
                            expression() == mandate()
                        }) {
                        //PLR is satisfied and can be mandated
                        return ruleMandate(mapping, variable, clauses, mandate, raw)
                    }
                }
                if (!mappingExisted) mapping.remove(variable)
            }
            return null
        }

        private fun ruleMandate(
            mapping: Mapping,
            variable: Variable,
            clauses: MutableSet<Expression>,
            expression: Expression,
            raw: Boolean,
        ): Boolean {
            mapping[variable] = true
            val clausesCopy = clauses.map { clause -> clause.copy() }.toMutableSet()
            return if (expression()) {
                !clausesCopy.cleanUpClauses() && dpll(clausesCopy, raw)
            } else {
                mapping[variable] = false
                !clausesCopy.cleanUpClauses() && dpll(clausesCopy, raw)
            }
        }

        private fun dpllBranch(
            clauses: MutableSet<Expression>,
            mapping: Mapping,
            raw: Boolean,
        ): Boolean {
            val variable = clauses.allVariables().first()
            //true branch
            mapping[variable] = true
            var clausesCopy = clauses.map { clause -> clause.copy() }.toMutableSet()
            var alreadyImpossible = clausesCopy.cleanUpClauses()
            return when {
                !alreadyImpossible && dpll(clausesCopy, raw) -> {
                    true
                }
                else -> {
                    //false branch
                    mapping[variable] = false
                    clausesCopy = clauses.map { clause -> clause.copy() }.toMutableSet()
                    alreadyImpossible = clausesCopy.cleanUpClauses()
                    !alreadyImpossible && dpll(clausesCopy, raw)
                }
            }
        }

        /**
         * @return's true if the expression is already impossible to satisfy after cleaning up
         */
        private fun MutableSet<Expression>.cleanUpClauses(): Boolean {
            //presumption is that this is in KNF/CNF -> only or's in individual expressions
            val mapping = this.first().mapping
            this.removeIf { clause ->
                clause.expressions.any { literal -> mapping.contains(literal) && literal() }
            }
            forEach { clause ->
                clause.expressions.removeIf { literal -> mapping.contains(literal) && !literal() }
            }
            //if any of the clauses are empty its not possible to fulfill the criteria anymore
            return this.any { clause -> clause.expressions.isEmpty() }
        }

        private fun MutableSet<Expression>.allVariables(): List<Variable> {
            return this.flatMap(Expression::variables)
        }


        private fun MutableSet<Expression>.containsEmpty(): Boolean {
            return any { clause -> clause !is Variable && clause.expressions.isEmpty() }
        }

        fun resolution(root: Expression): Boolean {
            root.extendMapping()
            return resolution(root.expressions.map(Expression::copy).toSet())
        }

        private fun resolution(originalClauses: Set<Expression>): Boolean {
            var clauses = originalClauses.toMutableSet()
            while (true) {
                val nextClauses = clauses.toMutableSet()
                for (clause1 in clauses) {
                    for (clause2 in clauses) {
                        if (clause1 == clause2) {
                            continue
                        }
                        val expressions1 =
                            if (clause1 is Variable || (clause1.expressions.size == 1 && clause1.expressions.first() is Variable)) setOf(
                                clause1
                            ) else clause1.expressions
                        val expressions2 =
                            if (clause2 is Variable || (clause2.expressions.size == 1 && clause2.expressions.first() is Variable)) setOf(
                                clause2
                            ) else clause2.expressions
                        for (literalOf1 in expressions1) {
                            for (literalOf2 in expressions2) {
                                if (literalOf1.variables() == literalOf2.variables() &&
                                    literalOf1.eval() != literalOf2.eval()
                                ) {
                                    val newClause = mutableSetOf<Expression>()
                                    val clause1WithoutLiteral1 = expressions1
                                        .filter { expression -> expression !== literalOf1 }.toMutableSet()
                                    val clause2WithoutLiteral2 = expressions2
                                        .filter { expression -> expression !== literalOf2 }.toMutableSet()
                                    newClause.addAll(clause1WithoutLiteral1)
                                    newClause.addAll(clause2WithoutLiteral2)
                                    if (newClause.isEmpty()) {
                                        return false
                                    }
                                    nextClauses.add(newClause.reduce(Expression::or))
                                }
                            }
                        }
                    }
                }
                if (clauses == nextClauses) return true
                clauses = nextClauses
                if (clauses.containsEmpty()) {
                    return false
                }
            }
        }
    }
}
