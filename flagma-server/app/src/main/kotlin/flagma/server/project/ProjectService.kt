package flagma.server.project

import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.common.Change
import com.linecorp.centraldogma.common.PathPattern
import flagma.server.Config
import kotlinx.coroutines.future.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory

class ProjectService : KoinComponent {
    private val logger = LoggerFactory.getLogger(ProjectService::class.java)

    private val projectsRepository: CentralDogmaRepository by inject(named(Config.CentralDogma.PROJECTS_REPOSITORY_NAME))

    companion object {
        const val FLAGS_FILE_NAME = "flags.json"
    }

    suspend fun getAllProjects(): List<Project> {
        val projects = projectsRepository.file(PathPattern.of("/*")).get().await()

        return projects.map {
            Project(
                name = it.key,
                flags = listOf(),
            )
        }
    }

    suspend fun createProject(projectName: String): String {
        val result = projectsRepository.commit(
            "Create $projectName",
            Change.ofJsonUpsert("/$projectName/$FLAGS_FILE_NAME", "{}"),
        ).push().await()

        return "Created project $projectName at ${result.whenAsText()}, Revision: ${result.revision().text()}"
    }

    suspend fun deleteProject(projectName: String): String {
        val result = projectsRepository.commit(
            "Create $projectName",
            Change.ofRemoval("/$projectName"),
        ).push().await()

        return "Deleted project $projectName at ${result.whenAsText()}, Revision: ${result.revision().text()}"
    }
}
