package com.example.smallbackendapi.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.example.smallbackendapi.database.PersonEntity
import com.example.smallbackendapi.database.PersonEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class Query(private val personRepository: PersonEntityRepository) : GraphQLQueryResolver {

    fun findPerson(id: Long): PersonEntity? {
        return personRepository.findByIdOrNull(id)
    }
}