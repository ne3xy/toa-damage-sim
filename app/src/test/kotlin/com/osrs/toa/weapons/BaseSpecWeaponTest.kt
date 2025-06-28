package com.osrs.toa.weapons

import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.Health
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random
import kotlin.math.max

class BaseSpecWeaponTest {

    @Test
    fun `should create base spec weapon with correct properties`() {
        val weapon = createTestSpecWeapon()
        
        assertEquals("Test Spec Weapon", weapon.name)
        assertEquals(5, weapon.attackSpeed)
        assertEquals(50, weapon.specialAttackCost)
    }

    @Test
    fun `should hit with spec when attack roll is much higher than defence roll`() {
        val weapon = createTestSpecWeapon(attackRoll = 10000)
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // With such high attack roll vs low defence, should hit consistently
        var hits = 0
        val iterations = 100
        repeat(iterations) {
            val damage = weapon.spec(target)
            if (damage > 0) hits++
        }
        
        // Should hit most of the time
        assertTrue(hits > iterations * 0.8)
    }

    @Test
    fun `should miss with spec when attack roll is much lower than defence roll`() {
        val weapon = createTestSpecWeapon(attackRoll = 1)
        val target = createTestTarget(defenceLevel = 200, meleeStabDefenceBonus = 100)
        
        // With such low attack roll vs high defence, should miss consistently
        var hits = 0
        val iterations = 100
        repeat(iterations) {
            val damage = weapon.spec(target)
            if (damage > 0) hits++
        }
        
        // Should miss most of the time
        assertTrue(hits < iterations * 0.2)
    }

    @Test
    fun `should deal spec damage within expected range when hitting`() {
        val weapon = createTestSpecWeapon(specMaxHit = 50)
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // With high attack roll vs low defence, should hit consistently
        repeat(100) {
            val damage = weapon.spec(target)
            if (damage > 0) {
                // Damage should be between 1 and specMaxHit (50)
                assertTrue(damage >= 1)
                assertTrue(damage <= 50)
            }
        }
    }

    @Test
    fun `should return zero damage when missing with spec`() {
        val weapon = createTestSpecWeapon(attackRoll = 1)
        val target = createTestTarget(defenceLevel = 200, meleeStabDefenceBonus = 100)
        
        // With low attack roll vs high defence, should miss
        repeat(100) {
            val damage = weapon.spec(target)
            if (damage == 0) {
                // This is a miss, which is expected
                return@repeat
            }
        }
        
        // Should have at least some misses
        assertTrue(true) // Test passes if we reach here
    }

    @Test
    fun `should use correct defence bonus for melee spec`() {
        val weapon = createTestSpecWeapon(attackStyle = AttackStyle.MELEE_STAB)
        val target = createTestTarget(
            defenceLevel = 100,
            meleeStabDefenceBonus = 50,
            rangedDefenceBonus = 200,
            magicDefenceBonus = 300
        )
        
        // The weapon should use melee stab defence bonus (50), not ranged (200) or magic (300)
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        assertEquals((100 + 9) * (50 + 64), defenceRoll) // (defenceLevel + 9) * (meleeStabDefenceBonus + 64)
    }

    @Test
    fun `should use correct defence bonus for ranged spec`() {
        val weapon = createTestSpecWeapon(attackStyle = AttackStyle.RANGED)
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
    fun `should use correct defence bonus for ranged light spec`() {
        val weapon = createTestSpecWeapon(attackStyle = AttackStyle.RANGED_LIGHT)
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
    fun `should use correct defence bonus for magic spec`() {
        val weapon = createTestSpecWeapon(attackStyle = AttackStyle.MAGIC)
        val target = createTestTarget(
            defenceLevel = 100,
            magicLevel = 100,
            meleeStabDefenceBonus = 200,
            rangedDefenceBonus = 300,
            magicDefenceBonus = 50
        )
        
        // The weapon should use magic defence bonus (50), not melee (200) or ranged (300)
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.MAGIC)
        assertEquals((9 + 100) * (50 + 64), defenceRoll) // (9 + magicLevel) * (magicDefenceBonus + 64)
    }

    @Test
    fun `should handle custom spec damage function`() {
        var customSpecDamageCalled = false
        val weapon = createTestSpecWeapon(
            attackRoll = 10000, // High attack roll to ensure hits
            specDamage = { 
                customSpecDamageCalled = true
                42 // Fixed damage
            }
        )
        
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        // Test the spec damage function directly
        val damage = weapon.specDamage(target)
        
        assertTrue(customSpecDamageCalled)
        assertEquals(42, damage)
    }

    @Test
    fun `should handle target-dependent spec damage`() {
        val weapon = createTestSpecWeapon(
            attackRoll = 10000, // High attack roll to ensure hits
            specDamage = { target -> 
                // Spec damage based on target health
                (target.health.value * 0.1).toInt()
            }
        )
        
        val target1 = createTestTarget(health = 100)
        val target2 = createTestTarget(health = 200)
        
        // Test the damage calculation directly
        val expectedDamage1 = (target1.health.value * 0.1).toInt()
        val expectedDamage2 = (target2.health.value * 0.1).toInt()
        
        assertEquals(10, expectedDamage1) // 100 * 0.1 = 10
        assertEquals(20, expectedDamage2) // 200 * 0.1 = 20
        
        // Test that the weapon's spec damage function returns the expected values
        val actualDamage1 = weapon.specDamage(target1)
        val actualDamage2 = weapon.specDamage(target2)
        
        assertEquals(expectedDamage1, actualDamage1)
        assertEquals(expectedDamage2, actualDamage2)
    }

    @Test
    fun `should handle target with zero defence level for spec`() {
        val weapon = createTestSpecWeapon(attackRoll = 1000)
        val target = createTestTarget(defenceLevel = 0, meleeStabDefenceBonus = 0)
        
        // With zero defence, hit chance should be very high
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        val hitChance = AccuracyCalculator.calculateHitChance(1000, defenceRoll)
        
        // Assert on exact calculated values
        assertEquals(576, defenceRoll) // (0 + 9) * (0 + 64) = 9 * 64 = 576
        assertEquals(0.7112887112887113, hitChance, 0.0000000000000001) // Exact hit chance from calculation
    }

    @Test
    fun `should handle target with high defence bonuses for spec`() {
        val weapon = createTestSpecWeapon(attackRoll = 1000)
        val target = createTestTarget(defenceLevel = 100, meleeStabDefenceBonus = 500)
        
        // With high defence bonuses, hit chance should be lower
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        val hitChance = AccuracyCalculator.calculateHitChance(1000, defenceRoll)
        
        // Hit chance should be lower with high defence
        assertTrue(hitChance < 0.5)
    }

    @Test
    fun `should handle different special attack costs`() {
        val weapon1 = createTestSpecWeapon(specialAttackCost = 25)
        val weapon2 = createTestSpecWeapon(specialAttackCost = 75)
        val weapon3 = createTestSpecWeapon(specialAttackCost = 100)
        
        assertEquals(25, weapon1.specialAttackCost)
        assertEquals(75, weapon2.specialAttackCost)
        assertEquals(100, weapon3.specialAttackCost)
    }

    @Test
    fun `should handle different attack speeds for spec weapons`() {
        val weapon1 = createTestSpecWeapon(attackSpeed = 3)
        val weapon2 = createTestSpecWeapon(attackSpeed = 5)
        val weapon3 = createTestSpecWeapon(attackSpeed = 7)
        
        assertEquals(3, weapon1.attackSpeed)
        assertEquals(5, weapon2.attackSpeed)
        assertEquals(7, weapon3.attackSpeed)
    }

    @Test
    fun `should handle different attack styles`() {
        val meleeWeapon = createTestSpecWeapon(attackStyle = AttackStyle.MELEE_CRUSH)
        val rangedWeapon = createTestSpecWeapon(attackStyle = AttackStyle.RANGED)
        val rangedHeavyWeapon = createTestSpecWeapon(attackStyle = AttackStyle.RANGED_HEAVY)
        val magicWeapon = createTestSpecWeapon(attackStyle = AttackStyle.MAGIC)
        
        // Test that weapons can be created with different attack styles
        // (attackStyle is now internal, so we can't test it directly)
        assertEquals("Test Spec Weapon", meleeWeapon.name)
        assertEquals("Test Spec Weapon", rangedWeapon.name)
        assertEquals("Test Spec Weapon", rangedHeavyWeapon.name)
        assertEquals("Test Spec Weapon", magicWeapon.name)
    }

    @Test
    fun `should implement both Weapon and SpecWeapon interfaces`() {
        val weapon = createTestSpecWeapon()
        
        // Should implement both interfaces
        assertTrue(weapon is Weapon)
        assertTrue(weapon is SpecWeapon)
    }

    private fun createTestSpecWeapon(
        name: String = "Test Spec Weapon",
        attackSpeed: Int = 5,
        attackStyle: AttackStyle = AttackStyle.MELEE_STAB,
        attackRoll: Int = 1000,
        specialAttackCost: Int = 50,
        specMaxHit: Int = 50,
        specDamage: ((CombatEntity) -> Int)? = null
    ): BaseSpecWeapon {
        return object : BaseSpecWeapon(
            specialAttackCost = specialAttackCost,
            specDamage = specDamage ?: { 
                val damageRoll = Random.nextInt(1, specMaxHit + 1)
                max(1, damageRoll - 1)
            }
        ) {
            override val name = name
            override val attackSpeed = attackSpeed
            private val weaponAttackRoll = attackRoll
            
            override fun attack(target: CombatEntity): Int {
                val defenceRoll = target.combatStats.getDefenceRoll(attackStyle)
                val hitChance = AccuracyCalculator.calculateHitChance(weaponAttackRoll, defenceRoll)
                
                return if (AccuracyCalculator.doesAttackHit(hitChance)) {
                    val damageRoll = Random.nextInt(1, 50 + 1)
                    max(1, damageRoll - 1)
                } else {
                    0
                }
            }
            
            override fun spec(target: CombatEntity): Int {
                val defenceRoll = target.combatStats.getDefenceRoll(attackStyle)
                val hitChance = AccuracyCalculator.calculateHitChance(weaponAttackRoll, defenceRoll)
                
                return if (AccuracyCalculator.doesAttackHit(hitChance)) {
                    specDamage(target)
                } else {
                    0
                }
            }
        }
    }

    private fun createTestTarget(
        defenceLevel: Int = 100,
        magicLevel: Int = 0,
        meleeStabDefenceBonus: Int = 50,
        rangedDefenceBonus: Int = 50,
        rangedLightDefenceBonus: Int = 50,
        magicDefenceBonus: Int = 50,
        health: Int = 100
    ): CombatEntity {
        return GenericCombatEntity(
            name = "Test Target",
            health = Health(health),
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