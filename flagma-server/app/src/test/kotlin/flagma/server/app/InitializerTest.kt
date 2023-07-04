package flagma.server.app

import com.linecorp.centraldogma.client.CentralDogma
import com.linecorp.centraldogma.testing.junit.CentralDogmaExtension
import flagma.server.Config
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCaseOrder
import io.kotest.extensions.junit5.JUnitExtensionAdapter
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldExist
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class InitializerTest : AnnotationSpec(), KoinTest {
    val extension: CentralDogmaExtension = CentralDogmaExtension()
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single<CentralDogma> { extension.client() }
            },
            Modules.controllerModules,
            Modules.serviceModules,
            Modules.utilityModules,
        )
    }

    override fun listeners(): List<TestListener> {
        return listOf(
            JUnitExtensionAdapter(extension),
            JUnitExtensionAdapter(koinTestExtension),
        )
    }

    override fun testCaseOrder(): TestCaseOrder = TestCaseOrder.Sequential

    @Test
    fun appShouldInitialize() {
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

    @Test
    fun appShouldInitializeAgain() {
        shouldNotThrowAny {
            Initializer.initializeProject()
        }
    }
}
