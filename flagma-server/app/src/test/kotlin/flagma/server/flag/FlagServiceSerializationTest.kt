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

class FlagServiceSerializationTest : KoinTest, FunSpec({
    val randomSource = RandomSource.seeded(2023_07_10)
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

    context("createFlag") {
        test("should serialize boolean flags correctly") {

        }
    }
})
