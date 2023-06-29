package flagma.server.flag

import com.linecorp.armeria.common.logging.LogLevel
import com.linecorp.armeria.common.sse.ServerSentEvent
import com.linecorp.armeria.server.annotation.*
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory


@LoggingDecorator(requestLogLevel = LogLevel.INFO, samplingRate = 1.0f)
class FlagStreamController : KoinComponent {
    private val logger = LoggerFactory.getLogger(FlagStreamController::class.java)

    private val flagService by inject<FlagService>()

    @Get("/{project}")
    @ProducesEventStream
    suspend fun getAllFlags(@Param project: String): Flow<ServerSentEvent> {
        return flow {
            (1..3).forEach {
                logger.info("Hey!")
                emit(ServerSentEvent.ofData("Hello $it"))
                delay(1000)
            }
        }
    }
}
