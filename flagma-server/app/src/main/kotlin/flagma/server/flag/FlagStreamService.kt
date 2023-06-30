package flagma.server.flag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.client.Watcher
import com.linecorp.centraldogma.common.Query
import flagma.server.Config
import flagma.server.project.ProjectService.Companion.FLAGS_FILE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
     * We have only a single watcher per project or feature flag, thus concurrent writes to the watchers will
     * result in excessive watchers to be created. This mutex ensures that only a single watcher is created per
     * project or feature flag.
     */
    private val watcherMutex = Mutex()

    /**
     * Project watchers are stored in-memory to optimize performance. Each watcher can be used more than once
     * to serve updates on projects to different flows.
     */
    private val projectWatchers: MutableMap<String, Watcher<List<Flag<Any>>>> = mutableMapOf()

    /**
     * Flag watchers are stored in-memory to optimize performance. Each watcher can be used more than once
     * to serve updates on flags to different flows.
     */
    private val flagWatchers: MutableMap<String, Watcher<Flag<Any>>> = mutableMapOf()

    /**
     * Stream all flags from the project, emitted everytime a flag is updated in the project.
     *
     * @param project project name
     * @return a flow that emits whenever a flag is updated in the project
     */
    suspend fun streamAllFlags(project: String): Flow<List<Flag<Any>>> {
        if (project !in project) {
            // Prevent concurrent requests from creating multiple watcher simultaneously.
            watcherMutex.withLock {
                // Another thread might have created the watcher while we were waiting for the mutex.
                // Don't create a second one by overriding the last if it exists now.
                if (project !in project) {
                    projectWatchers[project] = projectsRepository.watcher(
                        Query.ofJson("/$project/$FLAGS_FILE_NAME")
                    ).map {
                        mapper.readValue<Map<String, Flag<Any>>>(it.toString()).values.toList()
                    }.start()
                    // Wait for initial value to be available. This makes sure `latestValue` won't fail for
                    // existing flags.
                    projectWatchers[project]!!.initialValueFuture().await()
                }
            }
        }

        val watcher = projectWatchers[project]!!
        val latestValue = watcher.latestValue() ?: throw IllegalStateException("Watcher has not started yet")
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
                    // Wait for initial value to be available. This makes sure `latestValue` won't fail for
                    // existing flags.
                    flagWatchers[flagID]!!.initialValueFuture().await()
                }
            }
        }

        val watcher = flagWatchers[flagID]!!
        val lastValue = watcher.latestValue() ?: throw IllegalArgumentException("Flag $flagName not found")
        val mutableFlow: MutableStateFlow<Flag<Any>> = MutableStateFlow(lastValue)
        watcher.watch { flags ->
            logger.info("Hello! $flags")
            mutableFlow.tryEmit(flags)
        }
        return mutableFlow
    }
}
