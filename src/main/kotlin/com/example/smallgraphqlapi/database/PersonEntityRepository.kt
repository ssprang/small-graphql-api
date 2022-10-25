package com.example.smallgraphqlapi.database

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonEntityRepository : CrudRepository<PersonEntity, Int>, PagingAndSortingRepository<PersonEntity, Int>