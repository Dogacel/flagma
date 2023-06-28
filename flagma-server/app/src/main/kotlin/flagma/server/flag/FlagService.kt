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

    suspend fun createFlag(project: String, flagName: String): String {
        val flag = Flag<Boolean>(
            id = "${project}_$flagName",
            name = flagName,
            description = "",
            tags = listOf(),
            value = false,
        )

        val flagJsonString = mapper.writeValueAsString(flag)

        val result = projectsRepository.commit(
            "Create $flag",
            Change.ofJsonPatch(
                "/$project/$FLAGS_FILE_NAME",
                """[{ "op": "add", "path": "/$flagName", "value": $flagJsonString }]"""
            )
        ).push().await()


        return "Created at " + result.whenAsText() + ", Revision: " + result.revision().text()
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
