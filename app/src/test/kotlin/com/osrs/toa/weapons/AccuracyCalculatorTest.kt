package com.osrs.toa.weapons

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.abs

class AccuracyCalculatorTest {

    @Test
    fun `should calculate hit chance when attack roll is greater than defence roll`() {
        val attackRoll = 1000
        val defenceRoll = 500
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: 1 - (defenceRoll + 2) / (2 * (attackRoll + 1))
        // 1 - (500 + 2) / (2 * (1000 + 1)) = 1 - 502 / 2002 = 1 - 0.2507 = 0.7493
        assertEquals(0.7493, hitChance, 0.0001)
    }

    @Test
    fun `should calculate hit chance when attack roll equals defence roll`() {
        val attackRoll = 1000
        val defenceRoll = 1000
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: attackRoll / (2 * (defenceRoll + 1))
        // 1000 / (2 * (1000 + 1)) = 1000 / 2002 = 0.4995

        assertEquals(0.4995, hitChance, 0.0001)
    }

    @Test
    fun `should calculate hit chance when attack roll is less than defence roll`() {
        val attackRoll = 500
        val defenceRoll = 1000
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: attackRoll / (2 * (defenceRoll + 1))
        // 500 / (2 * (1000 + 1)) = 500 / 2002 = 0.24975
        assertEquals(0.24975, hitChance, 0.0001)
    }

    @Test
    fun `should handle edge case with very low attack roll`() {
        val attackRoll = 1
        val defenceRoll = 1000
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: 1 / (2 * (1000 + 1)) = 1 / 2002 = 0.0005
        assertEquals(0.0005, hitChance, 0.0001)
        assertTrue(hitChance > 0.0) // Should be very low but not zero
    }

    @Test
    fun `should handle edge case with very high attack roll`() {
        val attackRoll = 10000
        val defenceRoll = 1000
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: 1 - (1000 + 2) / (2 * (10000 + 1)) = 1 - 1002 / 20002 = 0.9499
        assertEquals(0.9499, hitChance, 0.0001)
    }

    @Test
    fun `should handle zero defence roll`() {
        val attackRoll = 1000
        val defenceRoll = 0
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: 1 - (0 + 2) / (2 * (1000 + 1)) = 1 - 2 / 2002 = 0.9990
        assertEquals(0.9990, hitChance, 0.0001)
    }

    @Test
    fun `should handle zero attack roll`() {
        val attackRoll = 0
        val defenceRoll = 1000
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: 0 / (2 * (1000 + 1)) = 0 / 2002 = 0.0
        assertEquals(0.0, hitChance, 0.0001)
    }

    @Test
    fun `should handle both zero attack and defence rolls`() {
        val attackRoll = 0
        val defenceRoll = 0
        
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        // Expected: 0 / (2 * (0 + 1)) = 0 / 2 = 0.0
        assertEquals(0.0, hitChance, 0.0001)
    }

    @Test
    fun `doesAttackHit should return true for high hit chance`() {
        // Test with a very high hit chance
        val hitChance = 0.99
        
        // Run multiple times to verify probability
        var hits = 0
        val iterations = 1000
        repeat(iterations) {
            if (AccuracyCalculator.doesAttackHit(hitChance)) {
                hits++
            }
        }
        
        val actualHitRate = hits.toDouble() / iterations
        // Should be close to 0.99, allow some variance
        assertTrue(abs(actualHitRate - hitChance) < 0.05)
    }

    @Test
    fun `doesAttackHit should return false for low hit chance`() {
        // Test with a very low hit chance
        val hitChance = 0.01
        
        // Run multiple times to verify probability
        var hits = 0
        val iterations = 1000
        repeat(iterations) {
            if (AccuracyCalculator.doesAttackHit(hitChance)) {
                hits++
            }
        }
        
        val actualHitRate = hits.toDouble() / iterations
        // Should be close to 0.01, allow some variance
        assertTrue(abs(actualHitRate - hitChance) < 0.05)
    }

    @Test
    fun `doesAttackHit should return true for 100% hit chance`() {
        val hitChance = 1.0
        
        // Should always hit
        repeat(100) {
            assertTrue(AccuracyCalculator.doesAttackHit(hitChance))
        }
    }

    @Test
    fun `doesAttackHit should return false for 0% hit chance`() {
        val hitChance = 0.0
        
        // Should never hit
        repeat(100) {
            assertFalse(AccuracyCalculator.doesAttackHit(hitChance))
        }
    }

    @Test
    fun `doesAttackHit should return approximately 50% hits for 50% hit chance`() {
        val hitChance = 0.5
        
        // Run multiple times to verify probability
        var hits = 0
        val iterations = 1000
        repeat(iterations) {
            if (AccuracyCalculator.doesAttackHit(hitChance)) {
                hits++
            }
        }
        
        val actualHitRate = hits.toDouble() / iterations
        // Should be close to 0.5, allow some variance
        assertTrue(abs(actualHitRate - hitChance) < 0.1)
    }
} 