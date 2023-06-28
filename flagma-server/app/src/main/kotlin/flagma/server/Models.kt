package flagma.server

data class BooleanFlag(val name: String, val enabled: Boolean)

data class Project(val name: String, val booleanFlags: List<BooleanFlag>)
