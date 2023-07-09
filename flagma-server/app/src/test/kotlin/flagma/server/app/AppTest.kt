package flagma.server.app

import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.testing.junit5.server.ServerExtension
import com.linecorp.centraldogma.client.CentralDogma
import com.linecorp.centraldogma.testing.junit.CentralDogmaExtension
import flagma.server.flag.FlagService
import flagma.server.project.ProjectService
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCaseOrder
import io.kotest.extensions.junit5.JUnitExtensionAdapter
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.mock.declareMock

class AppTest : KoinTest, FunSpec() {
    init {
        val centralDogmaExtension = CentralDogmaExtension()
        val koinTestExtension = KoinExtension(
            modules = Modules.appModule + module { single<CentralDogma> { centralDogmaExtension.client() } },
        )
        val serverExtension = object : ServerExtension(false) {
            override fun configure(sb: ServerBuilder) {
                buildServer(this@AppTest.getKoin(), sb)
            }

            override fun beforeEach(context: ExtensionContext) {
                super.beforeEach(context)
                start()
            }

            override fun afterEach(context: ExtensionContext) {
                super.afterEach(context)
                stop()
            }
        }

        testOrder = TestCaseOrder.Sequential

        extensions(
            koinTestExtension,
            JUnitExtensionAdapter(centralDogmaExtension),
            JUnitExtensionAdapter(serverExtension),
        )

        test("should run") {
            serverExtension.blockingWebClient()
                .get("/healthcheck")
                .status() shouldBe HttpStatus.OK
        }

        test("should have doc service") {
            serverExtension.blockingWebClient()
                .get("/docs/#/")
                .status() shouldBe HttpStatus.OK
        }

        xtest("should have flag and project endpoints") {
            declareMock<ProjectService> { }
            declareMock<FlagService> { }

            serverExtension.blockingWebClient()
                .get("/projects/test_p")
                .status() shouldBe HttpStatus.OK

            serverExtension.blockingWebClient()
                .get("/flags/test_p/test_f")
                .status() shouldBe HttpStatus.OK

            serverExtension.blockingWebClient()
                .get("/stream/flags/test_p/test_f")
                .status() shouldBe HttpStatus.OK
        }
    }
}
