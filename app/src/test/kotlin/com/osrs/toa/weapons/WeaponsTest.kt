package com.osrs.toa.weapons

import com.osrs.toa.actors.GenericCombatEntity
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
        
        val damage = weapon.spec(target)
        
        // Damage should be between 0 and max damage (110 or 22% of target health)
        assertTrue(damage >= 0)
        assertTrue(damage <= 110)
    }

    @Test
    fun `should calculate Zaryte Crossbow spec damage based on target health`() {
        val weapon = Weapons.ZaryteCrossbow
        val target = createTestTarget()
        
        val damage = weapon.spec(target)
        
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
        
        val damage = weapon.spec(target)
        
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
        
        val damage = weapon.spec(target)
        
        // Damage should be capped at 110
        assertTrue(damage >= 0)
        assertTrue(damage <= 110)
    }

    @Test
    fun `should throw exception for Zaryte Crossbow regular attack`() {
        val weapon = Weapons.ZaryteCrossbow
        val target = createTestTarget()
        
        assertThrows(NotImplementedError::class.java) {
            weapon.attack(target)
        }
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
        
        val damage1 = weapon.spec(target)
        val damage2 = weapon.spec(target)
        
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

    private fun createTestTarget(): GenericCombatEntity {
        return GenericCombatEntity(
            name = "Test Target",
            health = Health(100)
        )
    }
} 