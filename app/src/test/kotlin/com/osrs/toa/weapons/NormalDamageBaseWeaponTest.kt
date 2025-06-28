package com.osrs.toa.weapons

import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.Health
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class NormalDamageBaseWeaponTest {

    @Test
    fun `should create normal damage base weapon with correct properties`() {
        val weapon = createTestWeapon()
        
        assertEquals("Test Weapon", weapon.name)
        assertEquals(5, weapon.attackSpeed)
    }

    @Test
    fun `should validate maxHit in constructor`() {
        // Test that maxHit >= 1 validation works
        assertThrows(IllegalArgumentException::class.java) {
            createTestWeapon(maxHit = 0)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            createTestWeapon(maxHit = -1)
        }
        
        // Valid maxHit values should not throw
        assertDoesNotThrow {
            createTestWeapon(maxHit = 1)
            createTestWeapon(maxHit = 50)
            createTestWeapon(maxHit = 100)
        }
    }

    @Test
    fun `should delegate attack to BaseWeapon correctly`() {
        val weapon = createTestWeapon(
            attackRoll = 10000, // High attack roll
            hitRollProvider = { _ -> true } // Always hit for deterministic testing
        )
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // Test that the weapon can attack and returns a valid damage value
        val damage = weapon.attack(target)
        
        // Since we're always hitting, damage should be >= 1
        assertTrue(damage >= 1, "Damage should be at least 1 when hitting")
        assertTrue(damage <= 50, "Damage should not exceed maxHit when hitting")
    }

    @Test
    fun `should handle miss correctly through delegation`() {
        val weapon = createTestWeapon(
            hitRollProvider = { _ -> false } // Always miss
        )
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // Test that the weapon properly delegates miss handling
        val damage = weapon.attack(target)
        assertEquals(0, damage, "Should deal 0 damage when missing")
    }

    @Test
    fun `should handle different maxHit values`() {
        // Test that different maxHit values work correctly
        val weapon1 = createTestWeapon(maxHit = 1)
        val weapon50 = createTestWeapon(maxHit = 50)
        val weapon100 = createTestWeapon(maxHit = 100)
        
        // All should be valid weapons
        assertTrue(weapon1 is Weapon)
        assertTrue(weapon50 is Weapon)
        assertTrue(weapon100 is Weapon)
        
        assertEquals("Test Weapon", weapon1.name)
        assertEquals("Test Weapon", weapon50.name)
        assertEquals("Test Weapon", weapon100.name)
    }

    private fun createTestWeapon(
        name: String = "Test Weapon",
        attackSpeed: Int = 5,
        attackStyle: AttackStyle = AttackStyle.MELEE_STAB,
        attackRoll: Int = 1000,
        maxHit: Int = 50,
        hitRollProvider: (Double) -> Boolean = { _ -> true }
    ): NormalDamageBaseWeapon {
        return NormalDamageBaseWeapon(
            name = name,
            attackSpeed = attackSpeed,
            attackStyle = attackStyle,
            attackRoll = attackRoll,
            maxHit = maxHit,
            hitRollProvider = hitRollProvider
        )
    }

    private fun createTestTarget(
        defenceLevel: Int = 100,
        magicLevel: Int = 0,
        meleeStabDefenceBonus: Int = 50,
        rangedDefenceBonus: Int = 50,
        rangedLightDefenceBonus: Int = 50,
        magicDefenceBonus: Int = 50
    ): CombatEntity {
        return GenericCombatEntity(
            name = "Test Target",
            health = Health(100),
            combatStats = DefaultCombatStats(
                defenceLevel = defenceLevel,
                magicLevel = magicLevel,
                meleeStabDefenceBonus = meleeStabDefenceBonus,
                rangedDefenceBonus = rangedDefenceBonus,
                rangedLightDefenceBonus = rangedLightDefenceBonus,
                magicDefenceBonus = magicDefenceBonus
            )
        )
    }
} 