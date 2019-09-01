package com.example.smallbackendapi

import com.example.smallbackendapi.util.GraphQlIT
import com.example.smallbackendapi.util.IntegrationTestWithDB
import org.junit.jupiter.api.Test

@IntegrationTestWithDB
internal class QueryIT : GraphQlIT() {
    @Test
    fun `query person`() {
        app(
            requestPath = "query-person-req.yaml",
            responsePath = "query-person-res.json"
        )
    }
}