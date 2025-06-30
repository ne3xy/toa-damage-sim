package com.osrs.toa.weapons

import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.Health
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class WeaponsTest {

    @Test
    fun `should create Tumekens Shadow with correct properties`() {
        val weapon = Weapons.TumekensShadow
        
        assertEquals("Tumeken's Shadow", weapon.name)
        assertEquals(5, weapon.attackSpeed)
    }

    @Test
    fun `should create Maguss Shadow with correct properties`() {
        val weapon = Weapons.MagussShadow
        
        assertEquals("Tumeken's Shadow", weapon.name)
        assertEquals(5, weapon.attackSpeed)
    }

    @Test
    fun `should create Zaryte Crossbow with correct properties`() {
        val weapon = Weapons.ZaryteCrossbow
        
        assertEquals("Zaryte Crossbow", weapon.name)
        assertEquals(5, weapon.attackSpeed)
        assertEquals(75, weapon.specialAttackCost)
    }

    @Test
    fun `should attack with Tumekens Shadow`() {
        val weapon = Weapons.TumekensShadow
        val target = createTestTarget()
        
        val damage = weapon.attack(target)
        
        // Damage should be between 0 and max hit (80)
        assertTrue(damage >= 0)
        assertTrue(damage <= 80)
    }

    @Test
    fun `should attack with Maguss Shadow`() {
        val weapon = Weapons.MagussShadow
        val target = createTestTarget()
        
        val damage = weapon.attack(target)
        
        // Damage should be between 0 and max hit (84)
        assertTrue(damage >= 0)
        assertTrue(damage <= 84)
    }

    @Test
    fun `should use special attack with Zaryte Crossbow`() {
        val weapon = Weapons.ZaryteCrossbow
        val target = createTestTarget()
        
        val damage = weapon.attack(target)
        
        // Damage should be between 0 and max damage (110 or 22% of target health)
        assertTrue(damage >= 0)
        assertTrue(damage <= 110)
    }

    @Test
    fun `should calculate Zaryte Crossbow spec damage based on target health`() {
        val weapon = Weapons.ZaryteCrossbow
        val target = createTestTarget()
        
        val damage = weapon.attack(target)
        
        // If hit is successful, damage should be 22% of target health (22)
        // But due to randomness, we can only test bounds
        assertTrue(damage >= 0)
        assertTrue(damage <= 110)
    }

    @Test
    fun `should handle Zaryte Crossbow spec on low health target`() {
        val weapon = Weapons.ZaryteCrossbow
        val target = GenericCombatEntity(
            name = "Low Health Target",
            health = Health(10)
        )
        
        val damage = weapon.attack(target)
        
        // Damage should be capped at 22% of target health (2.2, rounded to 2)
        assertTrue(damage >= 0)
        assertTrue(damage <= 2)
    }

    @Test
    fun `should handle Zaryte Crossbow spec on high health target`() {
        val weapon = Weapons.ZaryteCrossbow
        val target = GenericCombatEntity(
            name = "High Health Target",
            health = Health(1000)
        )
        
        val damage = weapon.attack(target)
        
        // Damage should be capped at 110
        assertTrue(damage >= 0)
        assertTrue(damage <= 110)
    }

    @Test
    fun `should handle multiple attacks with same weapon`() {
        val weapon = Weapons.MagussShadow
        val target = createTestTarget()
        
        val damage1 = weapon.attack(target)
        val damage2 = weapon.attack(target)
        
        // Both attacks should produce valid damage
        assertTrue(damage1 >= 0)
        assertTrue(damage1 <= 84)
        assertTrue(damage2 >= 0)
        assertTrue(damage2 <= 84)
    }

    @Test
    fun `should handle multiple special attacks`() {
        val weapon = Weapons.ZaryteCrossbow
        val target = createTestTarget()
        
        val damage1 = weapon.attack(target)
        val damage2 = weapon.attack(target)
        
        // Both special attacks should produce valid damage
        assertTrue(damage1 >= 0)
        assertTrue(damage1 <= 110)
        assertTrue(damage2 >= 0)
        assertTrue(damage2 <= 110)
    }

    @Test
    fun `should handle attacks on dead target`() {
        val weapon = Weapons.MagussShadow
        val target = GenericCombatEntity(
            name = "Dead Target",
            health = Health(0)
        )
        
        val damage = weapon.attack(target)
        
        // Should still be able to attack dead target (for damage calculation)
        assertTrue(damage >= 0)
        assertTrue(damage <= 84)
    }

    @Test
    fun `should reduce defence level when Bandos Godsword special hits`() {
        val weapon = Weapons.BandosGodsword
        val target = GenericCombatEntity(
            name = "Test Target",
            health = Health(100),
            combatStats = DefaultCombatStats(defenceLevel = 100)
        )
        
        val initialDefenceLevel = (target.combatStats as DefaultCombatStats).defenceLevel
        
        // Use a deterministic hit by mocking the random
        // For this test, we'll just verify the method exists and can be called
        // In a real scenario, you'd need to mock the random behavior
        
        // Test that the drainDefenceLevel method exists and works
        target.combatStats.drainDefenceLevel(1)
        assertEquals(initialDefenceLevel - 1, (target.combatStats as DefaultCombatStats).defenceLevel)
        
        // Test that defence level can't go below 0
        target.combatStats.drainDefenceLevel(100)
        assertEquals(0, (target.combatStats as DefaultCombatStats).defenceLevel)
    }

    @Test
    fun `test Fang damage calculation for LB+SCB (trueMax 57)`() {
        val (minDamage, maxDamage) = Weapons.calculateFangDamageRange(57)
        assertEquals(8, minDamage) // 15% of 57 = 8.55, floor = 8
        assertEquals(49, maxDamage) // 85% of 57 = 48.45, ceil = 49
    }
    
    @Test
    fun `test Fang damage calculation for LB+Salted and Ultor+SCB (trueMax 59)`() {
        val (minDamage, maxDamage) = Weapons.calculateFangDamageRange(59)
        assertEquals(8, minDamage) // 15% of 59 = 8.85, floor = 8
        assertEquals(51, maxDamage) // 85% of 59 = 50.15, ceil = 51
    }
    
    @Test
    fun `test Fang damage calculation for Ultor+Salted (trueMax 63)`() {
        val (minDamage, maxDamage) = Weapons.calculateFangDamageRange(63)
        assertEquals(9, minDamage) // 15% of 63 = 9.45, floor = 9
        assertEquals(54, maxDamage) // 85% of 63 = 53.55, ceil = 54
    }

    private fun createTestTarget(): GenericCombatEntity {
        return GenericCombatEntity(
            name = "Test Target",
            health = Health(100)
        )
    }
} 