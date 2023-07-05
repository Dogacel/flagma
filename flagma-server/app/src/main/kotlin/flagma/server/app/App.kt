package flagma.server.app

import com.linecorp.armeria.common.SessionProtocol
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.healthcheck.HealthCheckService
import flagma.server.flag.FlagController
import flagma.server.flag.FlagStreamController
import flagma.server.project.ProjectController
import kotlinx.coroutines.future.await
import org.koin.core.Koin
import org.koin.core.context.startKoin

fun startKoin(): Koin {
    return startKoin { Modules.appModule }.koin
}

fun buildServer(koin: Koin, builder: ServerBuilder = Server.builder()): ServerBuilder {
    return builder
        .port(9000, SessionProtocol.HTTP, SessionProtocol.HTTPS)
        .tlsSelfSigned()
        .serviceUnder("/docs", DocService.builder().build())
        .annotatedService("/flags", koin.get<FlagController>())
        .annotatedService("/projects", koin.get<ProjectController>())
        .annotatedService("/stream/flags", koin.get<FlagStreamController>())
        .service("/healthcheck", HealthCheckService.of())
}

suspend fun main() {
    val koin = startKoin()
    Initializer.initializeProject()
    val server = buildServer(koin).build()

    server.start().await()
}
