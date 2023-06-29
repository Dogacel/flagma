package flagma.server.flag

typealias BooleanFlag = Flag<Boolean>
typealias NumberFlag = Flag<Number>
typealias StringFlag = Flag<String>
typealias JsonFlag = Flag<Map<*, *>>

data class Flag<T>(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<String>,
    val type: FlagType,
    val value: T,
) {
    init {
        when (type) {
            FlagType.BOOLEAN -> {
                if (value !is Boolean) {
                    throw IllegalArgumentException("Flag value must be Boolean.")
                }
            }

            FlagType.NUMBER -> {
                if (value !is Number) {
                    throw IllegalArgumentException("Flag value must be Number.")
                }
            }

            FlagType.STRING -> {
                if (value !is String) {
                    throw IllegalArgumentException("Flag value must be String.")
                }
            }

            FlagType.JSON -> {
                if (value !is Map<*, *>) {
                    throw IllegalArgumentException("Flag value must be Map<*, *>.")
                }
            }
        }
    }
}

enum class FlagType {
    BOOLEAN, NUMBER, STRING, JSON
}

data class CreateFlagBody(
    val name: String,
    val description: String = "",
    val tags: List<String> = listOf(),
    val type: FlagType,
    val value: Any,
)

data class UpdateFlagValue<T>(
    val value: T
)

data class FlagEvaluationContext(
    val flag: String,
    val project: String,
    val userAttributes: Map<String, Any>,
)

abstract class Evaluation<T>() {
    // Evaluation.static(123)
    // Evaluation.random(0.0, 1.0)
    // Evaluation.random(1, 100)
    // Evaluation.if(Condition.equals("userId", "123...")).then(true).else(false)
    // Evaluation.if(Condition.equals("userId", "123...")).then(123).elseIf( ... ).then(456).else(789)
    // Evaluation.if(Condition.equals("userId", "123...")).then(true).else(Evaluation.if(Condition...).then(0).else(123))
    abstract fun evaluate(flagEvaluationContext: FlagEvaluationContext): T

    companion object {
        fun <T> ofStatic(t: T): Evaluation<T> = object : Evaluation<T>() {
            override fun evaluate(flagEvaluationContext: FlagEvaluationContext): T {
                return t;
            }
        }
    }
}


abstract class Condition() {
    // random %25
    // userId == "123..."
    // userId in ["123...", "456..."]
    // not [ Condition ]
    // and [ Condition ] or [ Condition ]
    // num < <= > >= 123
    // userId exists
}
