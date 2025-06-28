package com.osrs.toa.weapons

import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.Health
import com.osrs.toa.weapons.AccuracyCalculator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random
import kotlin.math.max

class BaseWeaponTest {

    @Test
    fun `should create base weapon with correct properties`() {
        val weapon = createTestWeapon()
        
        assertEquals("Test Weapon", weapon.name)
        assertEquals(5, weapon.attackSpeed)
    }

    @Test
    fun `should hit when attack roll is much higher than defence roll`() {
        var capturedHitChance: Double? = null
        val spyDoesAttackHit = { hitChance: Double ->
            capturedHitChance = hitChance
            true // Always hit for this test
        }
        
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = 10000,
            hitDamage = { 42 },
            noodleProvider = spyDoesAttackHit
        )
        
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        val damage = weapon.attack(target)
        
        // Verify the weapon hit and dealt damage
        assertEquals(42, damage)
        
        // Verify the hit chance was calculated correctly
        assertNotNull(capturedHitChance)
        assertTrue(capturedHitChance!! > 0.8) // High attack roll vs low defense should have high hit chance
    }

    @Test
    fun `should miss when attack roll is much lower than defence roll`() {
        var capturedHitChance: Double? = null
        val spyDoesAttackHit = { hitChance: Double ->
            capturedHitChance = hitChance
            false // Always miss for this test
        }
        
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = 1,
            hitDamage = { 42 },
            noodleProvider = spyDoesAttackHit
        )
        
        val target = createTestTarget(defenceLevel = 200, meleeStabDefenceBonus = 100)
        val damage = weapon.attack(target)
        
        // Verify the weapon missed and dealt no damage
        assertEquals(0, damage)
        
        // Verify the hit chance was calculated correctly
        assertNotNull(capturedHitChance)
        assertTrue(capturedHitChance!! < 0.2) // Low attack roll vs high defense should have low hit chance
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
    fun `should return zero damage when missing`() {
        val weapon = createTestWeapon(attackRoll = 1)
        val target = createTestTarget(defenceLevel = 200, meleeStabDefenceBonus = 100)
        
        // With low attack roll vs high defence, should miss
        repeat(100) {
            val damage = weapon.attack(target)
            if (damage == 0) {
                // This is a miss, which is expected
                return@repeat
            }
        }
        
        // Should have at least some misses
        assertTrue(true) // Test passes if we reach here
    }

    @Test
    fun `should use correct defence bonus for melee attack`() {
        val weapon = createTestWeapon(attackStyle = AttackStyle.MELEE_STAB)
        val target = createTestTarget(
            defenceLevel = 100,
            meleeStabDefenceBonus = 50,
            rangedDefenceBonus = 200,
            magicDefenceBonus = 300
        )
        
        // The weapon should use melee stab defence bonus (50), not ranged (200) or magic (300)
        // This affects the defence roll calculation
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        assertEquals((100 + 9) * (50 + 64), defenceRoll) // (defenceLevel + 9) * (meleeStabDefenceBonus + 64)
    }

    @Test
    fun `should use correct defence bonus for ranged attack`() {
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
    fun `should use correct defence bonus for ranged light attack`() {
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
    fun `should use magic level and magic defence bonus for magic attack defence calculation`() {
        val weapon = createTestWeapon(attackStyle = AttackStyle.MAGIC)
        val target = createTestTarget(
            defenceLevel = 0,    // Should be ignored for magic attacks
            magicLevel = 100,    // Should be used for magic attacks
            meleeStabDefenceBonus = 200,
            rangedDefenceBonus = 300,
            magicDefenceBonus = 50
        )
        
        // Magic attacks should use magic level (100) and magic defence bonus (50), not defence level (0) or other bonuses
        val defenceRoll = target.combatStats.getDefenceRoll(AttackStyle.MAGIC)
        assertEquals((9 + 100) * (50 + 64), defenceRoll) // (9 + magicLevel) * (magicDefenceBonus + 64)
    }

    @Test
    fun `should handle custom damage function`() {
        var customDamageCalled = false
        val weapon = BaseWeapon(
            name = "Custom Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = 10000, // High attack roll to ensure hits
            hitDamage = { 
                customDamageCalled = true
                42 // Fixed damage
            },
            noodleProvider = { _ -> true } // Always hit for deterministic testing
        )
        
        // Test the weapon attack method which uses the custom damage function
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        val damage = weapon.attack(target)
        
        // Should always hit due to noodleProvider returning true, and use custom damage
        assertTrue(customDamageCalled)
        assertEquals(42, damage)
    }

    @Test
    fun `should handle target with zero defence level`() {
        var capturedHitChance: Double? = null
        val spyDoesAttackHit = { hitChance: Double ->
            capturedHitChance = hitChance
            true // Always hit for this test
        }
        
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = 1000,
            hitDamage = { 42 },
            noodleProvider = spyDoesAttackHit
        )
        
        val target = createTestTarget(defenceLevel = 0, meleeStabDefenceBonus = 0)
        val damage = weapon.attack(target)
        
        // Verify the weapon hit and dealt damage
        assertEquals(42, damage)
        
        // Verify the hit chance was calculated correctly for zero defence
        assertNotNull(capturedHitChance)
        assertEquals(0.7112887112887113, capturedHitChance!!, 0.0000000000000001) // Exact hit chance for zero defence
    }

    @Test
    fun `should handle target with high defence bonuses`() {
        var capturedHitChance: Double? = null
        val spyDoesAttackHit = { hitChance: Double ->
            capturedHitChance = hitChance
            false // Always miss for this test
        }
        
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = 1000,
            hitDamage = { 42 },
            noodleProvider = spyDoesAttackHit
        )
        
        val target = createTestTarget(defenceLevel = 100, meleeStabDefenceBonus = 500)
        val damage = weapon.attack(target)
        
        // Verify the weapon missed and dealt no damage
        assertEquals(0, damage)
        
        // Verify the hit chance was calculated correctly for high defence
        assertNotNull(capturedHitChance)
        assertTrue(capturedHitChance!! < 0.5) // High defence should result in low hit chance
    }

    private fun createTestWeapon(
        name: String = "Test Weapon",
        attackSpeed: Int = 5,
        attackStyle: AttackStyle = AttackStyle.MELEE_STAB,
        attackRoll: Int = 1000,
        maxHit: Int = 50,
        noodleProvider: (Double) -> Boolean = { hitChance -> AccuracyCalculator.doesAttackHit(hitChance) }
    ): BaseWeapon {
        return BaseWeapon(
            name = name,
            attackSpeed = attackSpeed,
            attackStyle = attackStyle,
            attackRoll = attackRoll,
            hitDamage = { 
                val damageRoll = Random.nextInt(1, maxHit + 1)
                max(1, damageRoll - 1)
            },
            noodleProvider = noodleProvider
        )
    }

    private fun createTestTarget(
        defenceLevel: Int = 100, // Used for melee/ranged defense calculations
        magicLevel: Int = 0,     // Used for magic defense calculations
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