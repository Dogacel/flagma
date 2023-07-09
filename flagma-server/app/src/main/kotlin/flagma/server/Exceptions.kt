package flagma.server

class ProjectNotFoundException(projectName: String) : Exception("Project \"${projectName}\" not found")

class FlagNotFoundException(flagId: String) : Exception("Flag \"${flagId}\" not found")
