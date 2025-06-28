package com.osrs.toa

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TickTest {

    @Test
    fun `should create tick with value`() {
        val tick = Tick(5)
        assertEquals(5, tick.value)
    }

    @Test
    fun `should add ticks`() {
        val tick1 = Tick(5)
        val tick2 = Tick(3)
        val result = tick1 + tick2
        assertEquals(8, result.value)
    }

    @Test
    fun `should subtract ticks`() {
        val tick1 = Tick(10)
        val tick2 = Tick(3)
        val result = tick1 - tick2
        assertEquals(7, result.value)
    }

    @Test
    fun `should compare ticks correctly`() {
        val tick1 = Tick(5)
        val tick2 = Tick(10)
        val tick3 = Tick(5)

        assertTrue(tick1 < tick2)
        assertTrue(tick2 > tick1)
        assertTrue(tick1 == tick3)
        assertTrue(tick1 <= tick3)
        assertTrue(tick1 >= tick3)
    }

    @Test
    fun `should handle zero ticks`() {
        val tick1 = Tick(0)
        val tick2 = Tick(5)
        val result = tick1 + tick2
        assertEquals(5, result.value)
    }

    @Test
    fun `should handle negative result from subtraction`() {
        val tick1 = Tick(3)
        val tick2 = Tick(5)
        val result = tick1 - tick2
        assertEquals(-2, result.value)
    }

    @Test
    fun `should handle large tick values`() {
        val tick1 = Tick(1000)
        val tick2 = Tick(500)
        val result = tick1 + tick2
        assertEquals(1500, result.value)
    }
} 