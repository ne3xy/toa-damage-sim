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
    fun `should indicate regenerating when energy is at 0`() {
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

    @Test
    fun `should consume decimal spec energy with precision`() {
        val energy = SpecialAttackEnergy(100)
        
        // Consume 37.5 energy (should use internal precision)
        energy.consume(37.5)
        
        // Internal: 1000 - 375 = 625, displayed as 62
        assertEquals(62, energy.energy)
        
        // Consume another 12.5 energy
        energy.consume(12.5)
        
        // Internal: 625 - 125 = 500, displayed as 50
        assertEquals(50, energy.energy)
    }

    @Test
    fun `should handle multiple small decimal consumptions`() {
        val energy = SpecialAttackEnergy(100)
        
        // Consume 0.1 energy multiple times
        repeat(10) {
            energy.consume(0.1)
        }
        
        // Internal: 1000 - (10 * 1) = 990, displayed as 99
        assertEquals(99, energy.energy)
    }

    @Test
    fun `should check if can use special with decimal cost`() {
        val energy = SpecialAttackEnergy(100)
        
        // Should be able to use 75.5 cost spec (100 >= 75.5)
        assertTrue(energy.canUseSpecial(75.5))
        
        // Should not be able to use 100.5 cost spec (100 < 100.5)
        assertFalse(energy.canUseSpecial(100.5))
        
        // Consume some energy
        energy.consume(25.0)
        
        // Should still be able to use 75.0 cost spec (75 >= 75.0)
        assertTrue(energy.canUseSpecial(75.0))
        
        // Should not be able to use 75.1 cost spec (75 < 75.1)
        assertFalse(energy.canUseSpecial(75.1))
    }

    @Test
    fun `should handle edge case decimal consumptions`() {
        val energy = SpecialAttackEnergy(100)
        
        // Consume exactly 100.0 energy
        energy.consume(100.0)
        assertEquals(0, energy.energy)
        
        // Reset for next test
        val newEnergy = SpecialAttackEnergy(100)
        
        // Consume 99.9 energy
        newEnergy.consume(99.9)
        // Internal: 1000 - 999 = 1, displayed as 0 (integer division)
        assertEquals(0, newEnergy.energy)
    }

    @Test
    fun `should maintain precision across multiple operations`() {
        val energy = SpecialAttackEnergy(100)
        
        // Consume 37.5 energy
        energy.consume(37.5)
        assertEquals(62, energy.energy)
        
        // Regenerate 10 energy
        energy.regenerate(10)
        assertEquals(72, energy.energy)
        
        // Consume 37.5 energy again
        energy.consume(37.5)
        assertEquals(35, energy.energy)
        
        // Consume 34.5 energy (should work)
        energy.consume(34.5)
        assertEquals(0, energy.energy)
    }

    @Test
    fun `should handle negative decimal consumption gracefully`() {
        val energy = SpecialAttackEnergy(100)
        
        // Should throw exception for negative consumption
        assertThrows(IllegalArgumentException::class.java) {
            energy.consume(-0.1)
        }
        
        // Energy should remain unchanged
        assertEquals(100, energy.energy)
    }

    @Test
    fun `should handle excessive decimal consumption gracefully`() {
        val energy = SpecialAttackEnergy(100)
        
        // Should throw exception for consuming more than available
        assertThrows(IllegalArgumentException::class.java) {
            energy.consume(100.1)
        }
        
        // Energy should remain unchanged
        assertEquals(100, energy.energy)
    }

    @Test
    fun `should return self when consuming decimal for method chaining`() {
        val energy = SpecialAttackEnergy(100)
        val result = energy.consume(37.5)
        assertSame(energy, result)
    }

    @Test
    fun `should handle mixed integer and decimal operations`() {
        val energy = SpecialAttackEnergy(100)
        
        // Mix integer and decimal operations
        energy.consume(25)      // Integer consumption
        energy.consume(12.5)    // Decimal consumption
        energy.regenerate(10)   // Integer regeneration
        energy.consume(37.5)    // Decimal consumption
        
        // Internal: 1000 - 250 - 125 + 100 - 375 = 350, displayed as 35
        assertEquals(35, energy.energy)
    }
} 