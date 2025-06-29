package com.osrs.toa.actors

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CombatStatsTest {

    @Test
    fun `should calculate melee stab defence roll correctly`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 100,
            meleeStabDefenceBonus = 50
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        
        // Expected: (defenceLevel + 9) * (meleeStabDefenceBonus + 64)
        // (100 + 9) * (50 + 64) = 109 * 114 = 12,426
        assertEquals(12426, defenceRoll)
    }

    @Test
    fun `should calculate melee slash defence roll correctly`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 80,
            meleeSlashDefenceBonus = 30
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_SLASH)
        
        // Expected: (defenceLevel + 9) * (meleeSlashDefenceBonus + 64)
        // (80 + 9) * (30 + 64) = 89 * 94 = 8,366
        assertEquals(8366, defenceRoll)
    }

    @Test
    fun `should calculate melee crush defence roll correctly`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 120,
            meleeCrushDefenceBonus = 75
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_CRUSH)
        
        // Expected: (defenceLevel + 9) * (meleeCrushDefenceBonus + 64)
        // (120 + 9) * (75 + 64) = 129 * 139 = 17,931
        assertEquals(17931, defenceRoll)
    }

    @Test
    fun `should calculate ranged defence roll correctly`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 90,
            rangedDefenceBonus = 40
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.RANGED)
        
        // Expected: (defenceLevel + 9) * (rangedDefenceBonus + 64)
        // (90 + 9) * (40 + 64) = 99 * 104 = 10,296
        assertEquals(10296, defenceRoll)
    }

    @Test
    fun `should calculate ranged light defence roll correctly`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 70,
            rangedLightDefenceBonus = 25
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.RANGED_LIGHT)
        
        // Expected: (defenceLevel + 9) * (rangedLightDefenceBonus + 64)
        // (70 + 9) * (25 + 64) = 79 * 89 = 7,031
        assertEquals(7031, defenceRoll)
    }

    @Test
    fun `should calculate ranged heavy defence roll correctly`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 110,
            rangedHeavyDefenceBonus = 60
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.RANGED_HEAVY)
        
        // Expected: (defenceLevel + 9) * (rangedHeavyDefenceBonus + 64)
        // (110 + 9) * (60 + 64) = 119 * 124 = 14,756
        assertEquals(14756, defenceRoll)
    }

    @Test
    fun `should calculate magic defence roll using magic level correctly`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 100, // Should be ignored for magic
            magicLevel = 80,
            magicDefenceBonus = 45
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.MAGIC)
        
        // Expected: (9 + magicLevel) * (magicDefenceBonus + 64)
        // (9 + 80) * (45 + 64) = 89 * 109 = 9,701
        assertEquals(9701, defenceRoll)
    }

    @Test
    fun `should handle zero defence level for melee attacks`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 0,
            meleeStabDefenceBonus = 20
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        
        // Expected: (0 + 9) * (20 + 64) = 9 * 84 = 756
        assertEquals(756, defenceRoll)
    }

    @Test
    fun `should handle zero magic level for magic attacks`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 0, // Required parameter
            magicLevel = 0,
            magicDefenceBonus = 30
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.MAGIC)
        
        // Expected: (9 + 0) * (30 + 64) = 9 * 94 = 846
        assertEquals(846, defenceRoll)
    }

    @Test
    fun `should handle zero defence bonuses`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 50,
            magicLevel = 40
        )
        
        // Test melee attack with zero defence bonus
        val meleeDefenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        // Expected: (50 + 9) * (0 + 64) = 59 * 64 = 3,776
        assertEquals(3776, meleeDefenceRoll)
        
        // Test magic attack with zero defence bonus
        val magicDefenceRoll = combatStats.getDefenceRoll(AttackStyle.MAGIC)
        // Expected: (9 + 40) * (0 + 64) = 49 * 64 = 3,136
        assertEquals(3136, magicDefenceRoll)
    }

    @Test
    fun `should handle negative defence bonuses`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 60,
            meleeStabDefenceBonus = -10
        )
        
        val defenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        
        // Expected: (60 + 9) * (-10 + 64) = 69 * 54 = 3,726
        assertEquals(3726, defenceRoll)
    }

    @Test
    fun `should handle very high defence levels and bonuses`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 200,
            magicLevel = 150,
            meleeStabDefenceBonus = 100,
            magicDefenceBonus = 80
        )
        
        // Test melee attack
        val meleeDefenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        // Expected: (200 + 9) * (100 + 64) = 209 * 164 = 34,276
        assertEquals(34276, meleeDefenceRoll)
        
        // Test magic attack
        val magicDefenceRoll = combatStats.getDefenceRoll(AttackStyle.MAGIC)
        // Expected: (9 + 150) * (80 + 64) = 159 * 144 = 22,896
        assertEquals(22896, magicDefenceRoll)
    }

    @Test
    fun `should use correct defence bonus for each attack style`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 100,
            magicLevel = 80,
            meleeStabDefenceBonus = 10,
            meleeSlashDefenceBonus = 20,
            meleeCrushDefenceBonus = 30,
            rangedDefenceBonus = 40,
            rangedLightDefenceBonus = 50,
            rangedHeavyDefenceBonus = 60,
            magicDefenceBonus = 70
        )
        
        // Test that each attack style uses its specific defence bonus
        assertEquals((100 + 9) * (10 + 64), combatStats.getDefenceRoll(AttackStyle.MELEE_STAB))
        assertEquals((100 + 9) * (20 + 64), combatStats.getDefenceRoll(AttackStyle.MELEE_SLASH))
        assertEquals((100 + 9) * (30 + 64), combatStats.getDefenceRoll(AttackStyle.MELEE_CRUSH))
        assertEquals((100 + 9) * (40 + 64), combatStats.getDefenceRoll(AttackStyle.RANGED))
        assertEquals((100 + 9) * (50 + 64), combatStats.getDefenceRoll(AttackStyle.RANGED_LIGHT))
        assertEquals((100 + 9) * (60 + 64), combatStats.getDefenceRoll(AttackStyle.RANGED_HEAVY))
        assertEquals((9 + 80) * (70 + 64), combatStats.getDefenceRoll(AttackStyle.MAGIC))
    }

    @Test
    fun `should handle edge case with minimum values`() {
        val combatStats = DefaultCombatStats(
            defenceLevel = 1,
            magicLevel = 1,
            meleeStabDefenceBonus = 1,
            magicDefenceBonus = 1
        )
        
        // Test melee attack
        val meleeDefenceRoll = combatStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        // Expected: (1 + 9) * (1 + 64) = 10 * 65 = 650
        assertEquals(650, meleeDefenceRoll)
        
        // Test magic attack
        val magicDefenceRoll = combatStats.getDefenceRoll(AttackStyle.MAGIC)
        // Expected: (9 + 1) * (1 + 64) = 10 * 65 = 650
        assertEquals(650, magicDefenceRoll)
    }
} 