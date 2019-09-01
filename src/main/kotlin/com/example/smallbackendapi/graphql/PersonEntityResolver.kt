package com.example.smallbackendapi.graphql

import com.coxautodev.graphql.tools.GraphQLResolver
import com.example.smallbackendapi.database.PersonEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PersonEntityResolver: GraphQLResolver<PersonEntity> {

    fun birthDay(value: PersonEntity) = Instant.parse("1983-08-24T00:00:00.00Z")
}