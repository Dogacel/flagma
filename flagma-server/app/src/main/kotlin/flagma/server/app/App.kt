package flagma.server.app

import com.linecorp.armeria.common.SessionProtocol
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.docs.DocService
import flagma.server.flag.FlagController
import flagma.server.flag.FlagStreamController
import flagma.server.project.ProjectController
import kotlinx.coroutines.future.await
import org.koin.core.Koin
import org.koin.core.context.startKoin

fun startKoin(): Koin {
    return startKoin {
        modules(
            Modules.integrationModules,
            Modules.controllerModules,
            Modules.serviceModules,
            Modules.utilityModules,
        )
    }.koin
}

fun buildServer(koin: Koin): Server {
    return Server.builder()
        .port(9000, SessionProtocol.HTTP, SessionProtocol.HTTPS)
        .tlsSelfSigned()
        .serviceUnder("/docs", DocService.builder().build())
        .annotatedService("/flags", koin.get<FlagController>())
        .annotatedService("/projects", koin.get<ProjectController>())
        .annotatedService("/stream/flags", koin.get<FlagStreamController>())
        .build()
}

suspend fun main() {
    val koin = startKoin()
    Initializer.initializeProject()
    val server = buildServer(koin)

    server.start().await()
}
