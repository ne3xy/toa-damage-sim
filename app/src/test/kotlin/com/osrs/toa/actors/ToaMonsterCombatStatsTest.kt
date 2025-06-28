package com.osrs.toa.actors

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class ToaMonsterCombatStatsTest {
    @Test
    fun `should scale defence roll correctly for invocationLevel 0`() {
        val baseStats = DefaultCombatStats(defenceLevel = 100, meleeStabDefenceBonus = 50)
        val monsterStats = ToaMonsterCombatStats(baseStats, invocationLevel = 0)
        val baseRoll = baseStats.getDefenceRoll(AttackStyle.MELEE_STAB)
        val expected = baseRoll
        assertEquals(expected, monsterStats.getDefenceRoll(AttackStyle.MELEE_STAB))
    }

    @Test
    fun `should scale defence roll correctly for invocationLevel 5`() {
        val baseStats = DefaultCombatStats(defenceLevel = 80, rangedDefenceBonus = 30)
        val monsterStats = ToaMonsterCombatStats(baseStats, invocationLevel = 5)
        val baseRoll = baseStats.getDefenceRoll(AttackStyle.RANGED)
        val expected = (baseRoll * 1.02).toInt() 
        assertEquals(expected, monsterStats.getDefenceRoll(AttackStyle.RANGED))
    }

    @Test
    fun `should scale defence roll for magic and invocationLevel 50`() {
        val baseStats = DefaultCombatStats(defenceLevel = 50, magicLevel = 70, magicDefenceBonus = 20)
        val monsterStats = ToaMonsterCombatStats(baseStats, invocationLevel = 50)
        val baseRoll = baseStats.getDefenceRoll(AttackStyle.MAGIC)
        val expected = (baseRoll * 1.2).toInt() 
        assertEquals(expected, monsterStats.getDefenceRoll(AttackStyle.MAGIC))
    }

    @Test
    fun `should scale defence roll for magic and invocationLevel 500`() {
        val baseStats = DefaultCombatStats(defenceLevel = 50, magicLevel = 70, magicDefenceBonus = 20)
        val monsterStats = ToaMonsterCombatStats(baseStats, invocationLevel = 500)
        val baseRoll = baseStats.getDefenceRoll(AttackStyle.MAGIC)
        val expected = (baseRoll * 3).toInt() 
        assertEquals(expected, monsterStats.getDefenceRoll(AttackStyle.MAGIC))
    }

    @Test
    fun `should validate invocation level is non-negative`() {
        val baseStats = DefaultCombatStats(defenceLevel = 100, meleeStabDefenceBonus = 50)
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = -1)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = -5)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = -10)
        }
    }

    @Test
    fun `should validate invocation level is divisible by 5`() {
        val baseStats = DefaultCombatStats(defenceLevel = 100, meleeStabDefenceBonus = 50)
        
        // These should throw exceptions
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 1)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 2)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 3)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 4)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 6)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 7)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 8)
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            ToaMonsterCombatStats(baseStats, invocationLevel = 9)
        }
    }

    @Test
    fun `should accept valid invocation levels`() {
        val baseStats = DefaultCombatStats(defenceLevel = 100, meleeStabDefenceBonus = 50)
        
        // These should not throw exceptions
        assertDoesNotThrow {
            ToaMonsterCombatStats(baseStats, invocationLevel = 0)
        }
        
        assertDoesNotThrow {
            ToaMonsterCombatStats(baseStats, invocationLevel = 5)
        }
        
        assertDoesNotThrow {
            ToaMonsterCombatStats(baseStats, invocationLevel = 10)
        }
        
        assertDoesNotThrow {
            ToaMonsterCombatStats(baseStats, invocationLevel = 15)
        }
        
        assertDoesNotThrow {
            ToaMonsterCombatStats(baseStats, invocationLevel = 20)
        }
    }
} 