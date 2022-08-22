package com.example.smallgraphqlapi

import com.example.smallgraphqlapi.util.GraphQlIT
import com.example.smallgraphqlapi.util.IntegrationTestWithDB
import org.junit.jupiter.api.Test

@IntegrationTestWithDB
internal class PersonControllerIT : GraphQlIT() {
    @Test
    fun `query person`() {
        app(
            requestPath = "query-person-req.yaml",
            responsePath = "query-person-res.json"
        )
    }
}