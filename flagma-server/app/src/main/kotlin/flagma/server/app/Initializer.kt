package flagma.server.app

import com.linecorp.centraldogma.client.CentralDogma
import flagma.server.Config
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

object Initializer : KoinComponent {
    private val logger = LoggerFactory.getLogger(Initializer::class.java)
    private val dogma: CentralDogma by inject()

    /**
     * Initialize the central dogma project and repositories. This method is idempotent.
     */
    fun initializeProject() {
        val projects = dogma.listProjects().join()
        if (Config.CentralDogma.PROJECT_NAME !in projects) {
            logger.info("Creating project ${Config.CentralDogma.PROJECT_NAME}")
            dogma.createProject(Config.CentralDogma.PROJECT_NAME).join()
            logger.info("Created project ${Config.CentralDogma.PROJECT_NAME}.")
        } else {
            logger.info("Found project, ${Config.CentralDogma.PROJECT_NAME}.")
        }

        val repositories = dogma.listRepositories(Config.CentralDogma.PROJECT_NAME).join()


        listOf(
            Config.CentralDogma.PROJECTS_REPOSITORY_NAME
        ).forEach {
            if (it !in repositories) {
                logger.info("Creating repository $it")
                dogma.createRepository(Config.CentralDogma.PROJECT_NAME, it)
                    .join()
                logger.info("Created repository $it.")
            } else {
                logger.info("Found repository, $it.")
            }
        }
    }
}
