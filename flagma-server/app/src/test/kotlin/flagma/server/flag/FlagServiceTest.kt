package flagma.server.flag

import com.linecorp.centraldogma.client.CentralDogma
import com.linecorp.centraldogma.testing.junit.CentralDogmaExtension
import flagma.server.FlagNotFoundException
import flagma.server.ProjectNotFoundException
import flagma.server.app.Initializer
import flagma.server.app.Modules
import flagma.server.project.ProjectService
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.junit5.JUnitExtensionAdapter
import io.kotest.koin.KoinExtension
import io.kotest.koin.KoinLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.*
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module
import org.koin.test.KoinTest

class FlagServiceTest : KoinTest, FunSpec({
    val randomSource = RandomSource.seeded(2023_07_09)
    val centralDogmaExtension = CentralDogmaExtension()

    val koinTestExtension = KoinExtension(
        modules = listOf(
            module { single<CentralDogma> { centralDogmaExtension.client() } },
            Modules.repositoryModules,
            Modules.utilityModules,
        ),
        mode = KoinLifecycleMode.Test,
    )

    beforeEach {
        Initializer.initializeProject()
    }

    extensions(koinTestExtension, JUnitExtensionAdapter(centralDogmaExtension))

    val service = FlagService()
    val projectService = ProjectService()

    context("getAllFlags") {
        test("should throw exception for a non-existing project") {
            assertThrows<ProjectNotFoundException> {
                service.getAllFlags("no_project")
            }
        }

        test("should return empty list for a new project") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)
            service.getAllFlags(projectName) shouldBe emptyList()
        }

        test("should return all flags after created") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)
            val flagName = Arb.stringPattern("\\w+").single(randomSource)
            val flagValue = Arb.int()

            (1..5).forEach {
                service.createFlag(
                    projectName,
                    CreateFlagBody(flagName + it, type = FlagType.NUMBER, value = flagValue.next(randomSource))
                )
            }

            val allFlags = service.getAllFlags(projectName)
            allFlags.size shouldBe 5
            allFlags.forEach {
                it.name.startsWith(flagName) shouldBe true
                it.type shouldBe FlagType.NUMBER
            }
        }
    }

    context("getFlag and createFlag") {
        test("should fail if project does not exist") {
            assertThrows<ProjectNotFoundException> {
                service.getFlag<Nothing>("no_project", "no_flag")
            }
        }

        test("should fail if flag does not exist") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)
            assertThrows<FlagNotFoundException> {
                service.getFlag<Nothing>(projectName, "no_flag")
            }
        }

        test("should return the existing flag") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)

            runBlocking {
                val flagName = Arb.stringPattern("\\w+").single(randomSource)
                val flagValue = Arb.string().single(randomSource)

                service.createFlag(
                    projectName,
                    CreateFlagBody(flagName, type = FlagType.STRING, value = flagValue)
                )
                val flag = service.getFlag<String>(projectName, flagName)
                flag?.name shouldBe flagName
                flag?.type shouldBe FlagType.STRING
                flag?.value shouldBe flagValue
            }

            runBlocking {
                val flagName = Arb.stringPattern("\\w+").single(randomSource)
                val flagValue = Arb.int().single(randomSource)

                service.createFlag(
                    projectName,
                    CreateFlagBody(flagName, type = FlagType.NUMBER, value = flagValue)
                )
                val flag = service.getFlag<Int>(projectName, flagName)
                flag?.name shouldBe flagName
                flag?.type shouldBe FlagType.NUMBER
                flag?.value shouldBe flagValue

            }

            runBlocking {
                val flagName = Arb.stringPattern("\\w+").single(randomSource)
                val flagValue = Arb.double().single(randomSource)

                service.createFlag(
                    projectName,
                    CreateFlagBody(flagName, type = FlagType.NUMBER, value = flagValue)
                )
                val flag = service.getFlag<Double>(projectName, flagName)
                flag?.name shouldBe flagName
                flag?.type shouldBe FlagType.NUMBER
                flag?.value shouldBe flagValue
            }

            runBlocking {
                val flagName = Arb.stringPattern("\\w+").single(randomSource)
                val flagValue = Arb.boolean().single(randomSource)

                service.createFlag(
                    projectName,
                    CreateFlagBody(flagName, type = FlagType.BOOLEAN, value = flagValue)
                )
                val flag = service.getFlag<Boolean>(projectName, flagName)
                flag?.name shouldBe flagName
                flag?.type shouldBe FlagType.BOOLEAN
                flag?.value shouldBe flagValue
            }
        }

    }

    context("updateFlagValue") { }

    context("deleteFlag") { }
})
