package flagma.server

object Config {
    object CentralDogma {
        val HOST: String = System.getenv("DOGMA_HOST") ?: "127.0.0.1"
        val PORT: Int = System.getenv("DOGMA_PORT")?.toInt() ?: 36462

        const val PROJECT_NAME = "flagma"

        const val PROJECTS_REPOSITORY_NAME = "projects"
    }
}
