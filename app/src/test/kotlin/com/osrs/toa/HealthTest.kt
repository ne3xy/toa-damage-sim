package com.osrs.toa

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

class HealthTest {

    @Test
    fun `should create health with valid value`() {
        val health = Health(99)
        assertEquals(99, health.value)
    }

    @Test
    fun `should create health with zero value`() {
        val health = Health(0)
        assertEquals(0, health.value)
    }

    @Test
    fun `should throw exception when creating health with negative value`() {
        assertThrows(IllegalArgumentException::class.java) {
            Health(-1)
        }
    }

    @Test
    fun `should take damage and reduce health`() {
        val health = Health(99)
        health.takeDamage(20)
        assertEquals(79, health.value)
    }

    @Test
    fun `should take damage and not go below zero`() {
        val health = Health(10)
        health.takeDamage(20)
        assertEquals(0, health.value)
    }

    @Test
    fun `should take zero damage without changing health`() {
        val health = Health(99)
        health.takeDamage(0)
        assertEquals(99, health.value)
    }

    @Test
    fun `should throw exception when taking negative damage`() {
        val health = Health(99)
        assertThrows(IllegalArgumentException::class.java) {
            health.takeDamage(-5)
        }
    }

    @Test
    fun `should return self when taking damage for method chaining`() {
        val health = Health(99)
        val result = health.takeDamage(10)
        assertSame(health, result)
    }

    @Test
    fun `should handle multiple damage instances`() {
        val health = Health(100)
        health.takeDamage(30)
        health.takeDamage(20)
        health.takeDamage(10)
        assertEquals(40, health.value)
    }
} 