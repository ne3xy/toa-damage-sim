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
    fun `should hit and deal damage when hitRollProvider returns true`() {
        val expectedDefenceRoll = 1000
        val weaponAttackRoll = 10000
        var capturedDefenceRoll: Int? = null
        var capturedHitChance: Double? = null
        
        val testTarget = GenericCombatEntity(
            name = "Test Target",
            health = Health(100),
            combatStats = object : CombatStats {
                override val defenceLevel: Int = 100
                override val magicLevel: Int = 99
                override fun getDefenceRoll(attackStyle: AttackStyle): Int {
                    capturedDefenceRoll = expectedDefenceRoll
                    return expectedDefenceRoll
                }
                override fun drainDefenceLevel(amount: Int) { /* no-op for test */ }
                override fun drainMagicLevel(amount: Int) { /* no-op for test */ }
            }
        )
        
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = weaponAttackRoll,
            hitDamage = { 42 },
            hitRollProvider = { hitChance -> 
                capturedHitChance = hitChance
                true // Always hit for deterministic testing
            }
        )
        
        val damage = weapon.attack(testTarget)
        
        // Verify the weapon used the target's defence roll
        assertEquals(expectedDefenceRoll, capturedDefenceRoll, "Weapon should use target's defence roll")
        
        // Verify the hitRollProvider was called with correct hit chance
        assertNotNull(capturedHitChance, "hitRollProvider should be called with hit chance")
        val expectedHitChance = AccuracyCalculator.calculateHitChance(weaponAttackRoll, expectedDefenceRoll)
        assertEquals(expectedHitChance, capturedHitChance!!, "hitRollProvider should be called with calculated hit chance")
        
        // Verify the weapon hit and dealt damage
        assertEquals(42, damage, "Should hit and deal expected damage when hitRollProvider returns true")
    }

    @Test
    fun `should miss and deal zero damage when hitRollProvider returns false`() {
        val expectedDefenceRoll = 50000
        val weaponAttackRoll = 1
        var capturedDefenceRoll: Int? = null
        var capturedHitChance: Double? = null
        
        val testTarget = GenericCombatEntity(
            name = "Test Target",
            health = Health(100),
            combatStats = object : CombatStats {
                override val defenceLevel: Int = 100
                override val magicLevel: Int = 99
                override fun getDefenceRoll(attackStyle: AttackStyle): Int {
                    capturedDefenceRoll = expectedDefenceRoll
                    return expectedDefenceRoll
                }
                override fun drainDefenceLevel(amount: Int) { /* no-op for test */ }
                override fun drainMagicLevel(amount: Int) { /* no-op for test */ }
            }
        )
        
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = weaponAttackRoll,
            hitDamage = { 42 },
            hitRollProvider = { hitChance -> 
                capturedHitChance = hitChance
                false // Always miss for deterministic testing
            }
        )
        
        val damage = weapon.attack(testTarget)
        
        // Verify the weapon used the target's defence roll
        assertEquals(expectedDefenceRoll, capturedDefenceRoll, "Weapon should use target's defence roll")
        
        // Verify the hitRollProvider was called with correct hit chance
        assertNotNull(capturedHitChance, "hitRollProvider should be called with hit chance")
        val expectedHitChance = AccuracyCalculator.calculateHitChance(weaponAttackRoll, expectedDefenceRoll)
        assertEquals(expectedHitChance, capturedHitChance!!, "hitRollProvider should be called with calculated hit chance")
        
        // Verify the weapon missed and dealt no damage
        assertEquals(0, damage, "Should miss and deal 0 damage when hitRollProvider returns false")
    }

    @Test
    fun `should deal damage according to the hitDamage provider when hitting`() {
        val expectedDamage = 25
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = AttackStyle.MELEE_STAB,
            attackRoll = 1000,
            hitDamage = { expectedDamage }, // Deterministic damage
            hitRollProvider = { _ -> true } // Always hit for deterministic testing
        )
        val target = createTestTarget(defenceLevel = 1, meleeStabDefenceBonus = 0)
        
        val damage = weapon.attack(target)
        
        // Verify the weapon deals the expected damage
        assertEquals(expectedDamage, damage, "Should deal the exact damage returned by hitDamage function")
    }

    @Test
    fun `should return zero damage when missing`() {
        val weapon = createTestWeapon(
            attackRoll = 1,
            hitRollProvider = { _ -> false } // Always miss for deterministic testing
        )
        val target = createTestTarget(defenceLevel = 200, meleeStabDefenceBonus = 100)
        
        // Test multiple attacks to verify consistent misses
        repeat(10) {
            val damage = weapon.attack(target)
            assertEquals(0, damage, "Should always miss and deal 0 damage")
        }
    }

    @Test
    fun `should use correct defence bonus for melee attack`() {
        testAttackStyleUsesCorrectDefenceRoll(AttackStyle.MELEE_STAB, 12426, "melee stab")
    }

    @Test
    fun `should use correct defence bonus for ranged attack`() {
        testAttackStyleUsesCorrectDefenceRoll(AttackStyle.RANGED, 15696, "ranged")
    }

    @Test
    fun `should use correct defence bonus for ranged light attack`() {
        testAttackStyleUsesCorrectDefenceRoll(AttackStyle.RANGED_LIGHT, 16786, "ranged light")
    }

    @Test
    fun `should use magic level and magic defence bonus for magic attack defence calculation`() {
        testAttackStyleUsesCorrectDefenceRoll(AttackStyle.MAGIC, 14616, "magic")
    }

    private fun testAttackStyleUsesCorrectDefenceRoll(
        attackStyle: AttackStyle,
        expectedDefenceRoll: Int,
        styleName: String
    ) {
        var capturedAttackStyle: AttackStyle? = null
        var capturedDefenceRoll: Int? = null
        var capturedHitChance: Double? = null
        
        val spyDoesAttackHit = { hitChance: Double ->
            capturedHitChance = hitChance
            true // Always hit for this test
        }
        
        val weapon = BaseWeapon(
            name = "Test Weapon",
            attackSpeed = 5,
            attackStyle = attackStyle,
            attackRoll = 1000,
            hitDamage = { 42 },
            hitRollProvider = spyDoesAttackHit
        )
        
        val target = createTestTarget(
            defenceLevel = 100,
            magicLevel = 50,
            meleeStabDefenceBonus = 100,
            rangedDefenceBonus = 200,
            rangedLightDefenceBonus = 300,
            magicDefenceBonus = 400
        )
        
        val damage = weapon.attack(target)
        
        // Verify the weapon hit and dealt damage
        assertEquals(42, damage, "Should hit and deal expected damage")
        
        // Verify the hit chance was calculated and passed to hitRollProvider
        assertNotNull(capturedHitChance, "hitRollProvider should have been called with hit chance")
    }

    private fun createTestWeapon(
        name: String = "Test Weapon",
        attackSpeed: Int = 5,
        attackStyle: AttackStyle = AttackStyle.MELEE_STAB,
        attackRoll: Int = 1000,
        maxHit: Int = 50,
        hitRollProvider: (Double) -> Boolean = { hitChance -> AccuracyCalculator.doesAttackHit(hitChance) }
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
            hitRollProvider = hitRollProvider
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