package flagma.server.flag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.client.Watcher
import com.linecorp.centraldogma.common.Query
import flagma.server.Config
import flagma.server.project.ProjectService.Companion.FLAGS_FILE_NAME
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory

/**
 * Flag stream service serves flows of flags. Those flows are updated whenever the underlying flag is updated.
 */
class FlagStreamService : KoinComponent {
    private val logger = LoggerFactory.getLogger(FlagService::class.java)

    private val projectsRepository: CentralDogmaRepository by inject(named(Config.CentralDogma.PROJECTS_REPOSITORY_NAME))
    private val mapper: ObjectMapper by inject()

    /**
     * We have only a single watcher per project or feature flag, thus concurrent writes to the watcher map will
     * result in excessive watchers to be created. This mutex ensures that only a single watcher is created per
     * project or feature flag.
     */
    private val watcherMutex = Mutex()

    /**
     * We have only a single flag flow per feature flag, thus concurrent attempts to set a flow inside the
     * [flagFlows] should be prevented to avoid having multiple flows.
     */
    private val flagFlowMutex = Mutex()

    /**
     * Flag watchers are stored in-memory to optimize performance. Each watcher can be used more than once
     * to serve updates on flags to different flows.
     */
    private val flagWatchers: MutableMap<String, Watcher<Flag<Any>>> = mutableMapOf()

    /**
     * Each feature flag is served by a single flow. This map stores the streams for each feature flag.
     */
    private val flagFlows: MutableMap<String, Flow<Flag<Any>>> = mutableMapOf()

    /**
     * Stream all flags from the project, emitted everytime a flag is updated in the project.
     *
     * @param project project name
     * @return a flow that emits whenever a flag is updated in the project
     */
    suspend fun streamAllFlags(project: String): Flow<List<Flag<Any>>> {
        val watcher = projectsRepository.watcher(
            Query.ofJson("/$project/$FLAGS_FILE_NAME")
        ).map {
            mapper.readValue<Map<String, Flag<Any>>>(it.toString()).values.toList()
        }.start()

        val latestValue = watcher.initialValueFuture().await().value()
            ?: throw IllegalStateException("Value doesn't exist.")
        val mutableFlow: MutableStateFlow<List<Flag<Any>>> = MutableStateFlow(latestValue)
        watcher.watch { flags ->
            mutableFlow.tryEmit(flags)
        }
        return mutableFlow
    }

    /**
     * Stream a flag, emitted everytime the flag is updated.
     *
     * @param project project name
     * @param flagName flag name
     * @return a flow that emits whenever the flag is updated
     */
    suspend fun streamFlag(project: String, flagName: String): Flow<Flag<Any>> {
        val flagID = "${project}_$flagName"

        if (flagID !in flagWatchers) {
            watcherMutex.withLock {
                // Another thread might have created the watcher while we were waiting for the mutex.
                // Don't create a second one by overriding the last if it exists now.
                if (flagID !in flagWatchers) {
                    flagWatchers[flagID] = projectsRepository.watcher(
                        Query.ofJsonPath("/$project/$FLAGS_FILE_NAME", "$.$flagName")
                    ).map {
                        mapper.readValue<Flag<Any>>(it.toString())
                    }.start()
                }
            }
        }

        if (flagID !in flagFlows) {
            flagFlowMutex.withLock {
                // Another thread might have created the flow while we were waiting for the mutex.
                // Don't create a second one by overriding the last if it exists now.
                if (flagID !in flagFlows) {
                    val watcher = flagWatchers[flagID]!!
                    val lastValue = watcher.initialValueFuture().await().value()
                        ?: throw IllegalArgumentException("Flag $flagName not found")
                    val mutableFlow: MutableStateFlow<Flag<Any>> = MutableStateFlow(lastValue)
                    watcher.watch { flags -> mutableFlow.tryEmit(flags) }
                    flagFlows[flagID] = mutableFlow
                }
            }
        }

        return flagFlows[flagID]!!
    }
}
