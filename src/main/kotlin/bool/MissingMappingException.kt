package bool

import bool.expressions.Variable

class MissingMappingException(variable: Variable):Exception("Either no mapping is present or '$variable' is not mapped") {
}