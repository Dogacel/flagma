package flagma.server.project

import flagma.server.flag.Flag

/**
 * Represents a project. Projects are physical groupings of feature flags. Projects can be used for organization
 * of a large number of feature flags.
 *
 * @property name name of the project
 * @property flags list of flags in the project
 */
data class Project(val name: String, val flags: List<Flag<*>>)
