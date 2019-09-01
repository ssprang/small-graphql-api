package com.example.smallbackendapi.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.skyscreamer.jsonassert.JSONCompareMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.json.JsonContent
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.yaml.snakeyaml.Yaml

abstract class GraphQlIT {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val yaml = Yaml()
    private val json = BasicJsonTester(javaClass)

    fun app(
        requestPath: String,
        responsePath: String
    ) = requestAndAssert(APP_GRAPHQL_URL, requestPath, responsePath)

    private fun requestAndAssert(
        url: String,
        requestPath: String,
        responsePath: String,
        variables: Map<String, Any> = mapOf()
    ) {
        val requestDefinition = requestDefinition(requestPath)
        requestAndAssert(url, requestDefinition, responsePath, variables)
        requestDefinition.retryAfter?.toLong()?.also { delay ->
            retry(delay) {
                requestAndAssert(url, requestDefinition, responsePath)
            }
        }
    }

    private fun requestAndAssert(
        url: String,
        requestDefinition: RequestDefinition,
        responsePath: String,
        variables: Map<String, Any> = mapOf()
    ) {
        // Perform request
        val actual = performRequest(url, requestDefinition)

        val expected = interpolateTemplate(
            template = javaClass.getResource(responsePath).readText(),
            variables = variables
        )

        // Assert response
        try {
            assertThat(actual).isEqualToJson(
                expected,
                JSONCompareMode.STRICT
            )
        } catch (e: AssertionError) {
            val actualJsonObject = objectMapper.readTree(actual.json)
            logger.error(
                "Response differed from expected:\nexpected: {}\nwas: {}",
                expected,
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualJsonObject)
            )
            throw e
        }
    }

    private fun requestDefinition(
        requestPath: String,
        variables: Map<String, Any> = mapOf()
    ): RequestDefinition {
        // Read request
        val fileContent = interpolateTemplate(
            template = javaClass.getResource(requestPath).readText(),
            variables = variables
        )

        return yaml.loadAs(
            fileContent,
            RequestDefinition::class.java
        )
    }

    private fun performRequest(
        url: String,
        requestDefinition: RequestDefinition
    ): JsonContent<Any> {
        val headers = HttpHeaders()
        requestDefinition.headers?.forEach { (name, value) ->
            headers.add(name, value)
        }
        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            HttpEntity(GraphQLRequest(requestDefinition), headers),
            String::class.java
        )
        val expectedResponseCode = requestDefinition.expectedResponseCode ?: 200
        assertThat(response.statusCode.value())
            .describedAs("Response code: ${response.statusCode} Body:${response.body}")
            .isEqualTo(expectedResponseCode)

        return this.json.from(response.body)
    }

    private fun retry(delay: Long, runnable: () -> Unit) {
        try {
            logger.info("Retrying request in {} ms", delay)
            Thread.sleep(delay)
            runnable()
        } catch (e: InterruptedException) {
            logger.warn("Retry of request was interrupted", e)
        }
    }

    private fun interpolateTemplate(template: String, variables: Map<String, Any>): String =
        variables.entries.fold(template) { accumulatedTemplate, (variableName, variableValue) ->
            accumulatedTemplate.replace("\${$variableName}", variableValue.toString())
        }

    companion object {
        private const val APP_GRAPHQL_URL = "/graphql"
    }

    private data class GraphQLRequest(
        val query: String,
        val operationName: String,
        val variables: Map<String, Any>? = null
    ) {

        constructor(requestDefinition: RequestDefinition) :
            this(
                requestDefinition.query,
                requestDefinition.operationName,
                requestDefinition.variables
            )
    }

    data class RequestDefinition(
        var query: String,
        var operationName: String,
        var time: String? = null,
        var uuid: List<String>? = null,
        var variables: Map<String, Any>? = null,
        var headers: Map<String, String>? = null,
        var retryAfter: String? = null,
        var expectedResponseCode: Int? = null
    ) {
        constructor() : this("", "")
    }
}
