package com.personalai.os.core.workflows

/**
 * Deliberately tiny expression evaluator - supports only
 * "key == 'value'" / "key != 'value'" against a flat context map. This is
 * enough for the rule examples in the blueprint ("Marketing mode = ON")
 * without pulling in a full scripting engine; replace if rules grow more
 * complex.
 */
class ConditionEvaluator {
    fun evaluate(condition: Condition, context: Map<String, String>): Boolean {
        val eqMatch = Regex("""(\w+)\s*==\s*'([^']*)'""").find(condition.expression)
        if (eqMatch != null) {
            val (key, value) = eqMatch.destructured
            return context[key] == value
        }
        val neqMatch = Regex("""(\w+)\s*!=\s*'([^']*)'""").find(condition.expression)
        if (neqMatch != null) {
            val (key, value) = neqMatch.destructured
            return context[key] != value
        }
        return false
    }
}
