package com.osrs.toa

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

class SpecialAttackEnergyTest {

    @Test
    fun `should create with default energy`() {
        val energy = SpecialAttackEnergy()
        assertEquals(100, energy.energy)
    }

    @Test
    fun `should create with custom energy`() {
        val energy = SpecialAttackEnergy(50)
        assertEquals(50, energy.energy)
    }

    @Test
    fun `should create with zero energy`() {
        val energy = SpecialAttackEnergy(0)
        assertEquals(0, energy.energy)
    }

    @Test
    fun `should create with max energy`() {
        val energy = SpecialAttackEnergy(100)
        assertEquals(100, energy.energy)
    }

    @Test
    fun `should throw exception when creating with negative energy`() {
        assertThrows(IllegalArgumentException::class.java) {
            SpecialAttackEnergy(-1)
        }
    }

    @Test
    fun `should throw exception when creating with energy above 100`() {
        assertThrows(IllegalArgumentException::class.java) {
            SpecialAttackEnergy(101)
        }
    }

    @Test
    fun `should consume energy`() {
        val energy = SpecialAttackEnergy(100)
        energy.consume(30)
        assertEquals(70, energy.energy)
    }

    @Test
    fun `should consume zero energy`() {
        val energy = SpecialAttackEnergy(100)
        energy.consume(0)
        assertEquals(100, energy.energy)
    }

    @Test
    fun `should throw exception when consuming negative amount`() {
        val energy = SpecialAttackEnergy(100)
        assertThrows(IllegalArgumentException::class.java) {
            energy.consume(-5)
        }
    }

    @Test
    fun `should throw exception when consuming more than available`() {
        val energy = SpecialAttackEnergy(50)
        assertThrows(IllegalArgumentException::class.java) {
            energy.consume(60)
        }
    }

    @Test
    fun `should return self when consuming for method chaining`() {
        val energy = SpecialAttackEnergy(100)
        val result = energy.consume(20)
        assertSame(energy, result)
    }

    @Test
    fun `should regenerate energy`() {
        val energy = SpecialAttackEnergy(50)
        energy.regenerate(20)
        assertEquals(70, energy.energy)
    }

    @Test
    fun `should regenerate with default amount`() {
        val energy = SpecialAttackEnergy(50)
        energy.regenerate()
        assertEquals(60, energy.energy)
    }

    @Test
    fun `should not exceed max energy when regenerating`() {
        val energy = SpecialAttackEnergy(95)
        energy.regenerate(20)
        assertEquals(100, energy.energy)
    }

    @Test
    fun `should regenerate zero energy`() {
        val energy = SpecialAttackEnergy(50)
        energy.regenerate(0)
        assertEquals(50, energy.energy)
    }

    @Test
    fun `should throw exception when regenerating negative amount`() {
        val energy = SpecialAttackEnergy(50)
        assertThrows(IllegalArgumentException::class.java) {
            energy.regenerate(-5)
        }
    }

    @Test
    fun `should return self when regenerating for method chaining`() {
        val energy = SpecialAttackEnergy(50)
        val result = energy.regenerate(20)
        assertSame(energy, result)
    }

    @Test
    fun `should indicate regenerating when energy is below 100`() {
        val energy = SpecialAttackEnergy(50)
        assertTrue(energy.isRegenerating())
    }

    @Test
    fun `should not indicate regenerating when energy is at 100`() {
        val energy = SpecialAttackEnergy(100)
        assertFalse(energy.isRegenerating())
    }

    @Test
    fun `should not indicate regenerating when energy is at 0`() {
        val energy = SpecialAttackEnergy(0)
        assertTrue(energy.isRegenerating())
    }

    @Test
    fun `should allow special attack when energy is sufficient`() {
        val energy = SpecialAttackEnergy(100)
        assertTrue(energy.canUseSpecial(75))
    }

    @Test
    fun `should not allow special attack when energy is insufficient`() {
        val energy = SpecialAttackEnergy(50)
        assertFalse(energy.canUseSpecial(75))
    }

    @Test
    fun `should allow special attack when energy exactly matches cost`() {
        val energy = SpecialAttackEnergy(75)
        assertTrue(energy.canUseSpecial(75))
    }

    @Test
    fun `should handle multiple operations`() {
        val energy = SpecialAttackEnergy(100)
        energy.consume(30)
        energy.regenerate(10)
        energy.consume(20)
        assertEquals(60, energy.energy)
    }
} 