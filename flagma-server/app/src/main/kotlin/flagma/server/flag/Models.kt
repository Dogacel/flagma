package flagma.server.flag

// Typealias for flag types.
typealias BooleanFlag = Flag<Boolean>
typealias NumberFlag = Flag<Number>
typealias StringFlag = Flag<String>
typealias JsonFlag = Flag<Map<*, *>>

/**
 * Flag represents a feature flag. Feature flags are shared variables that can be changed ad-hoc. They can be
 * used to control
 *
 * Current implementation only supports a single value for the feature flag.
 *
 * @param T type of value of the feature flag, depends on property type
 * @property id unique identifier of the feature flag, should be unique among all projects
 * @property name name of the feature flag
 * @property description description of the feature flag
 * @property tags list of tags
 * @property type oneof [FlagType]
 * @property value value of the feature flag
 */
data class Flag<T>(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<String>,
    val type: FlagType,
    val value: T,
) {
    init {
        // Allow flag type to match the actual value type.
        // Defining this check inside `init` helps us validate the data classes we are initializing.
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

/**
 * Type of flag based on the value it holds.
 */
enum class FlagType {
    BOOLEAN, NUMBER, STRING, JSON
}

/**
 * An entity storing information about a flag that will be created.
 *
 * @property name name of the flag
 * @property description optional description, default empty string
 * @property tags list of tags, default empty list
 * @property type type of the flag
 * @property value value of the flag
 */
data class CreateFlagBody<T>(
    val name: String,
    val description: String = "",
    val tags: List<String> = listOf(),
    val type: FlagType,
    val value: T,
)

/**
 * An entity storing information about a flag that will be updated.
 *
 * @param T type of the flag value
 * @property value new value of the flag
 */
data class UpdateFlagValue<T>(
    val value: T
)

/**
 * A context that the flag will be evaluated against.
 *
 * For example a context can contain a userId and a feature flag can return true for a specific userId.
 *
 * @property flag name of the flag to be evaluated
 * @property project name of the project that the flag belongs to
 * @property contextualInformation an JSON object of contextual information that the flag will be evaluated against,
 * such as userId, country, IP address etc.
 */
data class FlagEvaluationContext(
    val flag: String,
    val project: String,
    val contextualInformation: Map<String, Any>,
)

/**
 * Represent an evaluation rule for a flag.
 *
 * Example:
 *
 * Evaluation.static(123)
 * Evaluation.random(0.0, 1.0)
 * Evaluation.random(1, 100)
 * Evaluation.if(Condition.equals("userId", "123...")).then(true).else(false)
 * Evaluation.if(Condition.equals("userId", "123...")).then(123).elseIf( ... ).then(456).else(789)
 * Evaluation.if(Condition.equals("userId", "123...")).then(true).else(Evaluation.if(Condition...).then(0).else(123))
 *
 * @param T type of evaluated value.
 */
abstract class Evaluation<T>() {

    /**
     * Evaluate the flag for the given context.
     *
     * @param flagEvaluationContext context that the flag will be evaluated against
     * @return evaluated value
     */
    abstract fun evaluate(flagEvaluationContext: FlagEvaluationContext): T

    companion object {
        /**
         * An evaluation of a static value, i.e. a constant flag.
         *
         * @param T type of the evaluated value
         * @param t static value to be returned
         * @return an evaluation that always returns the given value
         */
        fun <T> ofStatic(t: T): Evaluation<T> = object : Evaluation<T>() {
            override fun evaluate(flagEvaluationContext: FlagEvaluationContext): T {
                return t;
            }
        }
    }
}

/**
 * Represent a condition that can be used in flag evaluation.
 *
 * Conditions are helpers that will be used to serialize evaluation rules to a JSON format. Different evaluation
 * strategies can be used to evaluate the flag based on different conditions.
 *
 * Example:
 *
 * random %25
 * userId == "123..."
 * userId in ["123...", "456..."]
 * not [ Condition ]
 * and [ Condition ] or [ Condition ]
 * num < <= > >= 123
 * userId exists
 *
 */
abstract class Condition() {

}
