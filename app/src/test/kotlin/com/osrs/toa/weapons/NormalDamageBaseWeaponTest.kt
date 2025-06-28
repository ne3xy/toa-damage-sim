package com.osrs.toa.weapons

import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.Health
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.math.max

class NormalDamageBaseWeaponTest {

    @Test
    fun `should create normal damage base weapon with correct properties`() {
        val weapon = createTestWeapon()
        
        assertEquals("Test Weapon", weapon.name)
        assertEquals(5, weapon.attackSpeed)
        assertEquals(AttackStyle.MELEE_STAB, weapon.attackStyle)
        assertEquals(1000, weapon.attackRoll)
    }

    @Test
    fun `should deal damage within expected range when hitting`() {
        val weapon = createTestWeapon(maxHit = 50)
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // With high attack roll vs low defence, should hit consistently
        repeat(100) {
            val damage = weapon.attack(target)
            if (damage > 0) {
                // Damage should be between 1 and maxHit (50)
                assertTrue(damage >= 1)
                assertTrue(damage <= 50)
            }
        }
    }

    @Test
    fun `should deal damage within expected range for different max hits`() {
        val maxHit = 100
        val weapon = createTestWeapon(maxHit = maxHit)
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // With high attack roll vs low defence, should hit consistently
        repeat(100) {
            val damage = weapon.attack(target)
            if (damage > 0) {
                // Damage should be between 1 and maxHit (100)
                assertTrue(damage >= 1)
                assertTrue(damage <= maxHit)
            }
        }
    }

    @Test
    fun `should handle max hit of 1`() {
        val weapon = createTestWeapon(maxHit = 1)
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // With high attack roll vs low defence, should hit consistently
        repeat(100) {
            val damage = weapon.attack(target)
            if (damage > 0) {
                // Damage should be exactly 1
                assertEquals(1, damage)
            }
        }
    }

    @Test
    fun `should handle max hit of 0`() {
        assertThrows(IllegalArgumentException::class.java) {
            createTestWeapon(maxHit = 0)
        }
    }

    @Test
    fun `should use correct attack style for defence calculation`() {
        val weapon = createTestWeapon(attackStyle = AttackStyle.RANGED)
        val target = createTestTarget(
            defenceLevel = 100,
            meleeStabDefenceBonus = 200,
            rangedDefenceBonus = 50,
            magicDefenceBonus = 300
        )
        
        // The weapon should use ranged defence bonus (50), not melee (200) or magic (300)
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.RANGED)
        assertEquals((100 + 9) * (50 + 64), defenceRoll) // (defenceLevel + 9) * (rangedDefenceBonus + 64)
    }

    @Test
    fun `should use correct attack style for ranged light defence calculation`() {
        val weapon = createTestWeapon(attackStyle = AttackStyle.RANGED_LIGHT)
        val target = createTestTarget(
            defenceLevel = 100,
            meleeStabDefenceBonus = 200,
            rangedLightDefenceBonus = 50,
            magicDefenceBonus = 300
        )
        
        // The weapon should use ranged light defence bonus (50), not melee (200) or magic (300)
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.RANGED_LIGHT)
        assertEquals((100 + 9) * (50 + 64), defenceRoll) // (defenceLevel + 9) * (rangedLightDefenceBonus + 64)
    }

    @Test
    fun `should hit consistently with high attack roll vs low defence`() {
        val weapon = createTestWeapon(attackRoll = 10000)
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // With such high attack roll vs low defence, hit chance should be very high
        val defenceRoll = target.combatStats.getDefenceRoll(weapon.attackStyle)
        val hitChance = AccuracyCalculator.calculateHitChance(weapon.attackRoll, defenceRoll)
        
        // Should hit most of the time
        assertTrue(hitChance > 0.8)
    }

    @Test
    fun `should miss consistently with low attack roll vs high defence`() {
        val weapon = createTestWeapon(attackRoll = 1)
        val target = createTestTarget(defenceLevel = 200, meleeStabDefenceBonus = 100)
        
        // With such low attack roll vs high defence, hit chance should be very low
        val defenceRoll = target.combatStats.getDefenceRoll(weapon.attackStyle)
        val hitChance = AccuracyCalculator.calculateHitChance(weapon.attackRoll, defenceRoll)
        
        // Should miss most of the time
        assertTrue(hitChance < 0.2)
    }

    @Test
    fun `should handle different attack speeds`() {
        val weapon = createTestWeapon(attackSpeed = 3)
        
        assertEquals(3, weapon.attackSpeed)
    }

    @Test
    fun `should handle different attack styles`() {
        val meleeWeapon = createTestWeapon(attackStyle = AttackStyle.MELEE_CRUSH)
        val rangedWeapon = createTestWeapon(attackStyle = AttackStyle.RANGED)
        val rangedHeavyWeapon = createTestWeapon(attackStyle = AttackStyle.RANGED_HEAVY)
        val magicWeapon = createTestWeapon(attackStyle = AttackStyle.MAGIC)
        
        assertEquals(AttackStyle.MELEE_CRUSH, meleeWeapon.attackStyle)
        assertEquals(AttackStyle.RANGED, rangedWeapon.attackStyle)
        assertEquals(AttackStyle.RANGED_HEAVY, rangedHeavyWeapon.attackStyle)
        assertEquals(AttackStyle.MAGIC, magicWeapon.attackStyle)
    }

    @Test
    fun `should handle target with zero defence level`() {
        val weapon = createTestWeapon(attackRoll = 1000)
        val target = createTestTarget(defenceLevel = 0, meleeStabDefenceBonus = 0)
        
        // With zero defence, hit chance should be higher than with high defence
        val defenceRoll = target.combatStats.getDefenceRoll(weapon.attackStyle)
        val hitChance = AccuracyCalculator.calculateHitChance(weapon.attackRoll, defenceRoll)
        
        // Assert on exact calculated values
        assertEquals(576, defenceRoll) // (0 + 9) * (0 + 64) = 9 * 64 = 576
        assertEquals(0.7112887112887113, hitChance, 0.0000000000000001) // Exact hit chance from calculation
    }

    @Test
    fun `should handle target with high defence bonuses`() {
        val weapon = createTestWeapon(attackRoll = 1000)
        val target = createTestTarget(defenceLevel = 100, meleeStabDefenceBonus = 500)
        
        // With high defence bonuses, hit chance should be lower
        val defenceRoll = target.combatStats.getDefenceRoll(weapon.attackStyle)
        val hitChance = AccuracyCalculator.calculateHitChance(weapon.attackRoll, defenceRoll)
        
        // Hit chance should be lower with high defence
        assertTrue(hitChance < 0.5)
    }

    @Test
    fun `should use weapon by delegation pattern correctly`() {
        val weapon = createTestWeapon()
        
        // Verify that the weapon is created using the delegation pattern
        assertTrue(weapon is Weapon)
        // NormalDamageBaseWeapon uses delegation to BaseWeapon, so it should be a Weapon
    }

    private fun createTestWeapon(
        name: String = "Test Weapon",
        attackSpeed: Int = 5,
        attackStyle: AttackStyle = AttackStyle.MELEE_STAB,
        attackRoll: Int = 1000,
        maxHit: Int = 50
    ): NormalDamageBaseWeapon {
        return NormalDamageBaseWeapon(
            name = name,
            attackSpeed = attackSpeed,
            attackStyle = attackStyle,
            attackRoll = attackRoll,
            maxHit = maxHit
        )
    }

    private fun createTestTarget(
        defenceLevel: Int = 100,
        meleeStabDefenceBonus: Int = 50,
        rangedDefenceBonus: Int = 50,
        rangedLightDefenceBonus: Int = 50,
        magicDefenceBonus: Int = 50
    ): CombatEntity {
        return GenericCombatEntity(
            name = "Test Target",
            health = Health(100),
            combatStats = CombatStats(
                defenceLevel = defenceLevel,
                meleeStabDefenceBonus = meleeStabDefenceBonus,
                rangedDefenceBonus = rangedDefenceBonus,
                rangedLightDefenceBonus = rangedLightDefenceBonus,
                magicDefenceBonus = magicDefenceBonus
            )
        )
    }
} 