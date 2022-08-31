package com.example.smallgraphqlapi.graphql

import graphql.ExecutionResult
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.SimpleInstrumentationContext
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.time.Duration

@Component
class LoggingInstrumentation : SimpleInstrumentation() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun beginExecution(
        parameters: InstrumentationExecutionParameters
    ): InstrumentationContext<ExecutionResult> {
        MDC.put("context", "GraphQL")
        val stopWatch = StopWatch().apply { start() }
        return SimpleInstrumentationContext.whenCompleted { result: ExecutionResult?, throwable: Throwable? ->
            try {
                val duration = Duration.ofMillis(stopWatch.apply { stop() }.lastTaskTimeMillis)
                logExecutionResult(parameters, result, throwable, duration)
            } finally {
                MDC.remove("context")
                MDC.remove("operationType")
                MDC.remove("operationName")
            }
        }
    }

    override fun beginExecuteOperation(
        parameters: InstrumentationExecuteOperationParameters
    ): InstrumentationContext<ExecutionResult> {
        val operationDefinition = parameters.executionContext.operationDefinition
        MDC.put("operationType", operationDefinition.operation.name.lowercase())
        MDC.put("operationName", operationDefinition.name)
        return SimpleInstrumentationContext()
    }

    private fun getLoggedVariables(
        parameters: InstrumentationExecutionParameters,
        result: ExecutionResult?,
        duration: Duration
    ): Array<StructuredArgument> = arrayOf(
        kv("durationMs", duration.toMillis()),
        kv("input.query", parameters.query.replace("\n", " ")),
        kv("input.variables", parameters.variables),
        kv("output.data", result?.getData())
    )

    private fun logExecutionResult(
        parameters: InstrumentationExecutionParameters,
        result: ExecutionResult?,
        throwable: Throwable?,
        duration: Duration
    ) {
        val loggedVariables = getLoggedVariables(parameters, result, duration)

        if (throwable != null) {
            return logExecutionErrors(
                throwable = throwable,
                result = result,
                loggedVariables = loggedVariables
            )
        }

        if (result?.errors?.isNotEmpty() == true) {
            return logGraphQlErrors(
                result = result,
                loggedVariables = loggedVariables
            )
        }

        logger.info(
            "GraphQL execution {}",
            *loggedVariables
        )
    }

    private fun logExecutionErrors(
        throwable: Throwable,
        result: ExecutionResult?,
        loggedVariables: Array<StructuredArgument>
    ) = logger.error(
        "GraphQL execution threw exception {}",
        kv("exceptionMessage", throwable.message),
        kv("exception", throwable),
        kv("errors", result?.errors),
        *loggedVariables
    )

    private fun logGraphQlErrors(
        result: ExecutionResult,
        loggedVariables: Array<StructuredArgument>
    ) = logger.error(
        "GraphQL execution with errors {}",
        kv("errors", result.errors),
        *loggedVariables,
    )
}