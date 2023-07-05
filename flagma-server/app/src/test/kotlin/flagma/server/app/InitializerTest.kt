package flagma.server.app

import com.linecorp.centraldogma.client.CentralDogma
import com.linecorp.centraldogma.testing.junit.CentralDogmaExtension
import flagma.server.Config
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCaseOrder
import io.kotest.extensions.junit5.JUnitExtensionAdapter
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldExist
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class InitializerTest : KoinTest, FunSpec({
    val extension = CentralDogmaExtension()
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module { single<CentralDogma> { extension.client() } },
            Modules.repositoryModules,
        )
    }

    testOrder = TestCaseOrder.Sequential
    listeners(
        JUnitExtensionAdapter(extension),
        JUnitExtensionAdapter(koinTestExtension),
    )

    test("app should initialize") {
        // Dogma should be clean
        extension.client()
            .listProjects().join()
            .shouldBeEmpty()

        Initializer.initializeProject()

        extension.client()
            .listProjects().join()
            .shouldExist { it == Config.CentralDogma.PROJECT_NAME }
        extension.client()
            .listRepositories(Config.CentralDogma.PROJECT_NAME).join()
            .keys.shouldContain(Config.CentralDogma.PROJECTS_REPOSITORY_NAME)
    }

    test("app should initialize if already initialized") {
        shouldNotThrowAny {
            Initializer.initializeProject()
        }
    }
})
