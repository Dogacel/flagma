package flagma.server.flag

import com.linecorp.armeria.common.logging.LogLevel
import com.linecorp.armeria.server.annotation.*
import com.linecorp.armeria.server.annotation.decorator.LoggingDecorator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory


@ProducesJson
@LoggingDecorator(requestLogLevel = LogLevel.INFO, samplingRate = 1.0f)
class FlagController : KoinComponent {
    private val logger = LoggerFactory.getLogger(FlagController::class.java)

    private val flagService by inject<FlagService>()

    @Get("/{project}")
    suspend fun getAllFlags(@Param project: String): List<Flag<Any>> {
        return flagService.getAllFlags(project)
    }

    @Get("/{project}/{flag}")
    suspend fun getFlag(@Param project: String, @Param flag: String): Flag<Any>? {
        return flagService.getFlag(project, flag)
    }

    @Post("/{project}")
    @ProducesText
    suspend fun createFlag(@Param project: String, createFlagBody: CreateFlagBody): String {
        return flagService.createFlag(project, createFlagBody)
    }

    @Put("/{project}/{flag}")
    suspend fun updateFlagValue(
        @Param project: String,
        @Param flag: String,
        updateFlagValue: UpdateFlagValue<Any>
    ): Flag<Any>? {
        return flagService.updateFlagValue(project, flag, updateFlagValue)
    }

    @Delete("/{project}/{flag}")
    @ProducesText
    suspend fun deleteFlag(@Param project: String, @Param flag: String): String {
        return flagService.deleteFlag(project, flag)
    }
}
