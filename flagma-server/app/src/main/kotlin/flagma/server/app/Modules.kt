package flagma.server.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.linecorp.centraldogma.client.CentralDogma
import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.client.armeria.ArmeriaCentralDogmaBuilder
import flagma.server.Config
import flagma.server.flag.FlagController
import flagma.server.flag.FlagService
import flagma.server.flag.FlagStreamController
import flagma.server.flag.FlagStreamService
import flagma.server.project.ProjectController
import flagma.server.project.ProjectService
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object Modules {
    val integrationModules = module(createdAtStart = true) {
        single<CentralDogma> {
            try {
                ArmeriaCentralDogmaBuilder()
                    .host(Config.CentralDogma.HOST, Config.CentralDogma.PORT)
                    .build()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    val controllerModules = module(createdAtStart = true) {
        singleOf<ProjectController>(::ProjectController)
        singleOf<FlagController>(::FlagController)
        singleOf<FlagStreamController>(::FlagStreamController)
    }

    val serviceModules = module(createdAtStart = true) {
        singleOf<ProjectService>(::ProjectService)
        singleOf<FlagService>(::FlagService)
        singleOf<FlagStreamService>(::FlagStreamService)
    }

    val utilityModules = module(createdAtStart = true) {
        single<ObjectMapper> { jacksonObjectMapper() }
        single<CentralDogmaRepository>(named(Config.CentralDogma.PROJECTS_REPOSITORY_NAME)) {
            get<CentralDogma>().forRepo(
                Config.CentralDogma.PROJECT_NAME,
                Config.CentralDogma.PROJECTS_REPOSITORY_NAME
            )
        }
    }
}
