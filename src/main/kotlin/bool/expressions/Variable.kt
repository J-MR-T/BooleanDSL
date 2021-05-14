package bool.expressions

import bool.MissingMappingException

class Variable(private val name: String) : Expression() {

    override fun eval(): Boolean {
        return mapping[this] ?: throw MissingMappingException(this)
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Variable)?.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun copy(): Expression = this
}
