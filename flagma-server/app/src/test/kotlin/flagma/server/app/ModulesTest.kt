package flagma.server.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.testing.junit.CentralDogmaExtension
import flagma.server.flag.FlagController
import flagma.server.flag.FlagStreamController
import flagma.server.project.ProjectController
import io.kotest.core.spec.style.FunSpec
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class ModulesTest : KoinTest, FunSpec({
    context(Modules::integrationModules.name) {
        test("should contain dogma") {
            Modules.integrationModules.verify(
                extraTypes = listOf(
                    CentralDogmaExtension::class
                )
            )
        }
    }

    context(Modules::controllerModules.name) {
        test("should have all controllers") {
            Modules.controllerModules.verify(
                extraTypes = listOf(
                    FlagController::class,
                    FlagStreamController::class,
                    ProjectController::class
                )
            )
        }
    }

    context(Modules::serviceModules.name) {
        test("should have all services") {
            Modules.serviceModules.verify(
                extraTypes = listOf(
                    flagma.server.flag.FlagService::class,
                    flagma.server.flag.FlagStreamService::class,
                    flagma.server.project.ProjectService::class
                )
            )
        }
    }

    context(Modules::repositoryModules.name) {
        test("should have central dogma repository") {
            Modules.repositoryModules.verify(
                extraTypes = listOf(
                    CentralDogmaRepository::class
                ),
            )
        }
    }

    xcontext(Modules::utilityModules.name) {
        test("should have object mapper") {
            Modules.utilityModules.verify(
                extraTypes = listOf(
                    ObjectMapper::class,
                )
            )
        }
    }
})
