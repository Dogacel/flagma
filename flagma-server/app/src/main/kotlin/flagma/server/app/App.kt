package flagma.server.app

import com.linecorp.armeria.common.SessionProtocol
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.centraldogma.client.CentralDogma
import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.client.armeria.ArmeriaCentralDogmaBuilder
import flagma.server.*
import flagma.server.flag.FlagController
import flagma.server.flag.FlagService
import flagma.server.project.ProjectController
import flagma.server.project.ProjectService
import kotlinx.coroutines.future.await
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module


suspend fun main() {
    val koin = startKoin {
        modules(
            module {
                single<CentralDogma> {
                    try {
                        ArmeriaCentralDogmaBuilder()
                            .host(Config.CentralDogma.HOST, Config.CentralDogma.PORT)
                            .build()
                    } catch (e: Exception) {
                        throw e
                    }
                }
                single<CentralDogmaRepository>(named(Config.CentralDogma.PROJECTS_REPOSITORY_NAME)) {
                    get<CentralDogma>().forRepo(
                        Config.CentralDogma.PROJECT_NAME,
                        Config.CentralDogma.PROJECTS_REPOSITORY_NAME
                    )
                }
                singleOf<ProjectController>(::ProjectController)
                singleOf<ProjectService>(::ProjectService)
                singleOf<FlagController>(::FlagController)
                singleOf<FlagService>(::FlagService)
            }
        )
    }.koin

    Initializer.initializeProject()

    val server = Server.builder()
        .port(9000, SessionProtocol.HTTP, SessionProtocol.HTTPS)
        .tlsSelfSigned()
        .serviceUnder("/docs", DocService.builder().build())
        .annotatedService("/flags", koin.get<FlagController>())
        .annotatedService("/projects", koin.get<ProjectController>())
        .build()

    server.start().await()
}
