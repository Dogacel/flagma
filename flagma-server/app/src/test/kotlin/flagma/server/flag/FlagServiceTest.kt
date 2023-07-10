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
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.extensions.junit5.JUnitExtensionAdapter
import io.kotest.koin.KoinExtension
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
        ),
    )

    beforeEach {
        Initializer.initializeProject(centralDogmaExtension.client())
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

    context("getFlag") {
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

            forAll<Arb<Any>, FlagType>(
                row(Arb.string(), FlagType.STRING),
                row(Arb.int(), FlagType.NUMBER),
                row(Arb.double(), FlagType.NUMBER),
                row(Arb.boolean(), FlagType.BOOLEAN),
                row(
                    Arb.constant(
                        mapOf<String, Any?>(
                            "a" to 1,
                            "b" to "2",
                            "c" to true,
                            "d" to mapOf<String, Any?>(
                                "e" to null,
                                "f" to listOf<Any?>(
                                    1, 2, "3", false, null, mapOf<String, Any?>("g" to 4), listOf<Any?>()
                                )
                            )
                        )
                    ),
                    FlagType.JSON
                ),
            ) { generator, type ->
                val flagName = Arb.stringPattern("\\w+").single(randomSource)
                val flagValue = generator.single(randomSource)

                runBlocking {
                    service.createFlag(
                        projectName,
                        CreateFlagBody(flagName, type = type, value = flagValue)
                    )
                    val flag = service.getFlag<Any>(projectName, flagName)
                    flag.name shouldBe flagName
                    flag.type shouldBe type
                    flag.value shouldBe flagValue
                }
            }
        }
    }

    context("createFlag") {
        test("should fail if project does not exist") {
            assertThrows<ProjectNotFoundException> {
                service.createFlag(
                    "no_project",
                    CreateFlagBody("no_flag", type = FlagType.STRING, value = "no_value")
                )
            }
        }
    }

    context("hasFlag") {
        test("should fail if project does not exist") {
            assertThrows<ProjectNotFoundException> {
                service.getFlag<Nothing>("no_project", "no_flag")
            }
        }

        test("should return false if does exist") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)

            service.hasFlag(projectName, "no_flag") shouldBe false
        }

        test("should return true if exists") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)

            val flagName = Arb.stringPattern("\\w+").single(randomSource)
            service.createFlag(projectName, CreateFlagBody(flagName, type = FlagType.STRING, value = ""))

            service.hasFlag(projectName, flagName) shouldBe true
        }
    }

    context("updateFlagValue") {
        test("should fail if project does not exist") {
            assertThrows<ProjectNotFoundException> {
                service.updateFlagValue("no_project", "flag", UpdateFlagValue(""))
            }
        }

        test("should update value of the flag") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)

            forAll<Arb<Any>, FlagType, Arb<Any>>(
                row(Arb.string(), FlagType.STRING, Arb.string()),
                row(Arb.int(), FlagType.NUMBER, Arb.double()),
                row(Arb.double(), FlagType.NUMBER, Arb.int()),
                row(Arb.boolean(), FlagType.BOOLEAN, Arb.boolean()),
                row(
                    Arb.constant(
                        mapOf<String, Any?>(
                            "a" to 1,
                            "b" to "2",
                            "c" to true,
                            "d" to mapOf<String, Any?>(
                                "e" to null,
                                "f" to listOf<Any?>(
                                    1, 2, "3", false, null, mapOf<String, Any?>("g" to 4), listOf<Any?>()
                                )
                            )
                        )
                    ),
                    FlagType.JSON,
                    Arb.constant(mapOf<String, Any?>("a" to 2))
                ),
            ) { generator, type, newGenerator ->
                val flagName = Arb.stringPattern("\\w+").single(randomSource)
                val flagValue = generator.single(randomSource)
                val newValue = newGenerator.single(randomSource)
                runBlocking {
                    service.createFlag(
                        projectName,
                        CreateFlagBody(flagName, type = type, value = flagValue)
                    )
                    service.updateFlagValue(
                        projectName,
                        flagName,
                        UpdateFlagValue(newValue)
                    )
                    val flag = service.getFlag<Any>(projectName, flagName)
                    flag.name shouldBe flagName
                    flag.type shouldBe type
                    flag.value shouldBe newValue
                }
            }
        }
    }

    context("deleteFlag") {
        test("should fail if project does not exist") {
            assertThrows<ProjectNotFoundException> {
                service.deleteFlag("no_project", "flag")
            }
        }

        test("should delete existing flag") {
            val projectName = Arb.stringPattern("\\w+").single(randomSource)
            projectService.createProject(projectName)

            val flagName = Arb.stringPattern("\\w+").single(randomSource)


            service.createFlag(projectName, CreateFlagBody(flagName, type = FlagType.NUMBER, value = 1))
            service.hasFlag(projectName, flagName) shouldBe true

            service.deleteFlag(projectName, flagName)
            service.hasFlag(projectName, flagName) shouldBe false
        }
    }
})
