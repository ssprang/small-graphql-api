package com.example.smallbackendapi.graphql

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Configuration
class GraphQLConfig {
    @Bean
    fun scalars(): Array<GraphQLScalarType> = arrayOf(instantScalarType)

    val instantScalarType: GraphQLScalarType =
        GraphQLScalarType.newScalar().name("Instant")
            .description("ISO-8859 timestamp with time zone")
            .coercing(
                object : Coercing<Instant, String> {

                    override fun serialize(input: Any): String? = when (input) {
                        is String -> input
                        is Instant -> input.truncatedTo(ChronoUnit.SECONDS).toString()
                        else -> null
                    }

                    override fun parseValue(input: Any): Instant? = parseLiteral(input)

                    override fun parseLiteral(input: Any): Instant? = when (input) {
                        is StringValue -> ZonedDateTime.parse(input.value).toInstant()
                        else -> null
                    }
                }
            ).build()
}