package flagma.server.flag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.linecorp.armeria.common.logging.LogLevel
import com.linecorp.armeria.common.sse.ServerSentEvent
import com.linecorp.armeria.server.annotation.*
import com.linecorp.armeria.server.annotation.decorator.CorsDecorator
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator
import com.linecorp.armeria.server.annotation.decorator.RequestTimeout
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

/**
 * Flag stream controller serves Server-Sent Events to stream feature flags. Using Server-Sent Events ensures
 * minimum latency with minimum overhead. Clients can subscribe to those events and keep the flag values
 * in-memory for fast access without needing to make a network call.
 */
@LoggingDecorator(requestLogLevel = LogLevel.INFO, samplingRate = 1.0f)
@CorsDecorator(origins = ["*"])
class FlagStreamController : KoinComponent {
    private val logger = LoggerFactory.getLogger(FlagStreamController::class.java)
    private val mapper: ObjectMapper = jacksonObjectMapper()

    private val flagStreamService by inject<FlagStreamService>()

    /**
     * Subscribe to a project's flags.
     *
     * Tested via
     * > curl -N localhost:9000/stream/flags/test
     */
    @Get("/{project}")
    @ProducesEventStream
    @RequestTimeout(0)
    suspend fun getAllFlags(@Param project: String): Flow<ServerSentEvent> {
        return flagStreamService.streamProjectFlags(project).map {
            ServerSentEvent.ofData(mapper.writeValueAsString(it))
        }
    }

    /**
     * Subscribe to a single flag.
     *
     * Tested via
     * > curl -N localhost:9000/stream/flags/test/foo_bool
     *
     * For load testing,
     * > for i in {1..100}; do curl -N localhost:9000/stream/flags/test/foo_bool &; done
     *
     * to cleanup,
     * > kill $(jobs -p)
     */
    @Get("/{project}/{flag}")
    @ProducesEventStream
    @RequestTimeout(0)
    suspend fun getFlag(@Param project: String, @Param flag: String): Flow<ServerSentEvent> {
        return flagStreamService.streamFlag(project, flag).map {
            ServerSentEvent.ofData(mapper.writeValueAsString(it))
        }
    }

    /**
     * Subscribe to multiple flags at once.
     *
     * Tested via
     * curl -N -X POST -H "content-type: application/json" localhost:9000/stream/flags/subscribe -d '{"test": ["foo_bool", "foo_integer"]}'
     */
    @Post("/subscribe")
    @ProducesEventStream
    @ConsumesJson
    @RequestTimeout(0)
    suspend fun subscribe(subscribedFlags: Map<String, List<String>>): Flow<ServerSentEvent> {
        val projectFlagPairs = subscribedFlags.flatMap { (project, flags) ->
            flags.map { flag -> project to flag }
        }

        // TODO: Handle invalid flags or individual failures
        val flows = projectFlagPairs.map { (project, flag) -> flagStreamService.streamFlag(project, flag) }

        // TODO: This can be optimized, no need to maintain a separate flow for each flag
        return flows.merge().map { flag ->
            ServerSentEvent
                .builder()
                .event("flagUpdate")
                .data(mapper.writeValueAsString(flag))
                .build()
        }
    }
}
