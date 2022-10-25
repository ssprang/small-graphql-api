package com.example.smallgraphqlapi.graphql

import com.example.smallgraphqlapi.database.PersonEntity
import com.example.smallgraphqlapi.database.PersonEntityRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class PersonController(private val personRepository: PersonEntityRepository) {

    @QueryMapping
    fun findPerson(@Argument id: Int): PersonEntity? {
        return personRepository.findByIdOrNull(id)
    }

    @QueryMapping
    fun findPersons(@Argument page: Int, @Argument size: Int): Page<PersonEntity> {
        return personRepository.findAll(PageRequest.of(page, size))
    }

    @SchemaMapping(typeName = "Person")
    fun lastNameLength(person: PersonEntity): Int {
        return person.lastName.length
    }
}
