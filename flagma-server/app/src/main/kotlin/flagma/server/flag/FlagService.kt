package flagma.server.flag

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.common.*
import flagma.server.Config
import kotlinx.coroutines.future.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import flagma.server.FlagNotFoundException
import flagma.server.ProjectNotFoundException
import flagma.server.project.ProjectService.Companion.FLAGS_FILE_NAME

import org.slf4j.LoggerFactory

/**
 * Flag service is responsible for CRUD operations on Flags and persistence of Flags.
 */
class FlagService : KoinComponent {
    private val logger = LoggerFactory.getLogger(FlagService::class.java)

    private val projectsRepository: CentralDogmaRepository by inject(named(Config.CentralDogma.PROJECTS_REPOSITORY_NAME))
    private val mapper: ObjectMapper = jacksonObjectMapper()

    /**
     * Get all flags inside a project.
     *
     * @param project project name
     * @return list of flags
     */
    suspend fun getAllFlags(project: String): List<Flag<Any>> {
        val flagEntry: Entry<JsonNode> = try {
            projectsRepository.file(
                Query.ofJson("/$project/$FLAGS_FILE_NAME")
            ).get().await()
        } catch (e: EntryNotFoundException) {
            throw ProjectNotFoundException(project)
        }

        if (!flagEntry.hasContent()) return listOf()

        val flagNames = flagEntry.content().fieldNames().asSequence().toList()

        return flagNames.mapNotNull {
            try {
                val flag = getFlag<Any>(project, it)
                flag
            } catch (e: Exception) {
                logger.warn("Corrupted flag file $it.", e)
                null
            }
        }
    }

    /**
     * Get a flag.
     *
     * @param T type of flag, if unknown, use Any
     * @param project name of the project
     * @param flagName name of the flag
     * @return the flag if exists else null
     */
    suspend fun <T> getFlag(project: String, flagName: String): Flag<T> {
        val flag = try {
            val flagEntry: Entry<JsonNode> = projectsRepository.file(
                Query.ofJsonPath("/$project/$FLAGS_FILE_NAME", "$.$flagName")
            ).get().await()

            if (flagEntry.hasContent()) {
                mapper.readValue<Flag<T>>(flagEntry.content().toString())
            } else {
                throw FlagNotFoundException("${project}_$flagName")
            }
        } catch (e: QueryExecutionException) {
            throw FlagNotFoundException("${project}_$flagName")
        } catch (e: EntryNotFoundException) {
            throw ProjectNotFoundException(project)
        }

        return flag
    }

    suspend fun hasFlag(project: String, flagName: String): Boolean {
        val projectEntry: Entry<JsonNode> = projectsRepository.file(
            Query.ofJson("/$project/$FLAGS_FILE_NAME")
        ).get().await()

        return projectEntry.hasContent() && projectEntry.contentAsJson().has(flagName)
    }

    /**
     * Create flag inside a project.
     *
     * @param project name of the project
     * @param createFlagBody parameters to create the flag
     * @return a message indicating the result of the operation
     */
    suspend fun createFlag(project: String, createFlagBody: CreateFlagBody<Any>): String {
        if (hasFlag(project, createFlagBody.name)) {
            return "Flag ${createFlagBody.name} already exists"
        }

        val flag = Flag(
            id = "${project}_${createFlagBody.name}",
            name = createFlagBody.name,
            description = createFlagBody.description,
            tags = createFlagBody.tags,
            type = createFlagBody.type,
            value = createFlagBody.value,
        )

        val flagJson = mapper.writeValueAsString(flag)

        val result = projectsRepository.commit(
            "Create $flag",
            Change.ofJsonPatch(
                "/$project/$FLAGS_FILE_NAME",
                """[{ "op": "add", "path": "/${flag.name}", "value": $flagJson }]"""
            )
        ).push().await()


        return "Created at " + result.whenAsText() + ", Revision: " + result.revision().text()
    }

    /**
     * Update value of a flag.
     *
     * @param project name of the project
     * @param flagName name of the flag
     * @param updateFlagValue parameter to update the value of the flag
     * @return a message indicating the result of the operation
     */
    suspend fun updateFlagValue(
        project: String,
        flagName: String,
        updateFlagValue: UpdateFlagValue<Any>
    ): Flag<Any> {
        val flag = getFlag<Any>(project, flagName)

        // Test changes by re-initializing the flag
        val newFlag = flag.copy(value = updateFlagValue.value)

        val valueJsonString = mapper.writeValueAsString(updateFlagValue.value)

        try {
            projectsRepository.commit(
                "Update $flagName value",
                Change.ofJsonPatch(
                    "/$project/$FLAGS_FILE_NAME",
                    """[{ "op": "add", "path": "/${flagName}/value", "value": $valueJsonString }]"""
                )
            ).push().await()
        } catch (_: RedundantChangeException) {
            logger.info("Redundant change for $flagName")
        }

        return newFlag
    }

    /**
     * Delete a flag.
     *
     * @param project project name
     * @param flagName flag name
     * @return a message indicating the result of the operation
     */
    suspend fun deleteFlag(project: String, flagName: String): String {
        val result =
            projectsRepository.commit(
                "Delete $flagName",
                Change.ofJsonPatch(
                    "/$project/$FLAGS_FILE_NAME", """
                [{ "op": "remove", "path": "/$flagName" }]
            """.trimIndent()
                )
            ).push().await()


        return "Deleted at " + result.whenAsText() + ", Revision: " + result.revision().text()
    }
}
