package de.phash.manuel.asw.integration.cmc.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CmCApiServiceImplTest {

    @Test
    fun calculate() {
        val service = CmCApiServiceImpl()

        service.calculate(null)
    }
}