package flagma.server.project

import com.linecorp.armeria.common.logging.LogLevel
import com.linecorp.armeria.server.annotation.*
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory


@ProducesJson
@LoggingDecorator(requestLogLevel = LogLevel.INFO, samplingRate = 1.0f)
class ProjectController : KoinComponent {
    private val logger = LoggerFactory.getLogger(ProjectController::class.java)

    private val projectService by inject<ProjectService>()

    @Get("/")
    suspend fun getAllProjects(): List<Project> {
        return projectService.getAllProjects()
    }

    @Post("/{projectName}")
    suspend fun createProject(@Param projectName: String): String {
        return projectService.createProject(projectName)
    }

    @Delete("/{projectName}")
    suspend fun deleteProject(@Param projectName: String): String {
        return projectService.deleteProject(projectName)
    }
}
