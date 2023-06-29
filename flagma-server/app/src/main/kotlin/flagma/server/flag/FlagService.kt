package flagma.server.flag

import com.fasterxml.jackson.databind.JsonNode
import com.linecorp.centraldogma.client.CentralDogmaRepository
import com.linecorp.centraldogma.common.*
import flagma.server.Config
import kotlinx.coroutines.future.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import flagma.server.project.ProjectService.Companion.FLAGS_FILE_NAME

import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

class FlagService : KoinComponent {
    private val logger = LoggerFactory.getLogger(FlagService::class.java)

    private val projectsRepository: CentralDogmaRepository by inject(named(Config.CentralDogma.PROJECTS_REPOSITORY_NAME))
    val mapper = jacksonObjectMapper()


    suspend fun getAllFlags(project: String): List<Flag<Any>> {
        val flagEntry: Entry<JsonNode> = projectsRepository.file(
            Query.ofJson("/$project/$FLAGS_FILE_NAME")
        ).get().await()

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

    suspend fun <T> getFlag(project: String, flagName: String): Flag<T>? {
        val flag = try {
            val flagEntry: Entry<JsonNode> = projectsRepository.file(
                Query.ofJsonPath("/$project/$FLAGS_FILE_NAME", "$.$flagName")
            ).get().await()

            if (flagEntry.hasContent()) {
                logger.info(flagEntry.content().toPrettyString())
                mapper.readValue<Flag<T>>(flagEntry.content().toString())
            } else {
                null
            }
        } catch (e: QueryExecutionException) {
            logger.warn("Can't get flag $flagName.", e)
            return null
        }

        return flag
    }

    suspend fun createFlag(project: String, createFlagBody: CreateFlagBody): String {
        if (getFlag<Any>(project, createFlagBody.name) != null) {
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

        val flagJsonString = mapper.writeValueAsString(flag)

        val result = projectsRepository.commit(
            "Create $flag",
            Change.ofJsonPatch(
                "/$project/$FLAGS_FILE_NAME",
                """[{ "op": "add", "path": "/${flag.name}", "value": $flagJsonString }]"""
            )
        ).push().await()


        return "Created at " + result.whenAsText() + ", Revision: " + result.revision().text()
    }

    suspend fun updateFlagValue(
        project: String,
        flagName: String,
        updateFlagValue: UpdateFlagValue<Any>
    ): Flag<Any> {
        val flag = getFlag<Any>(project, flagName) ?: throw IllegalArgumentException("Flag $flagName not found")

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
