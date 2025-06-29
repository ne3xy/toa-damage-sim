package com.osrs.toa.actors

import com.osrs.toa.BaseHp
import com.osrs.toa.Tick
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

class ToaCombatEntityTest {
    
    @Test
    fun `should create ToaCombatEntity with scaled HP and combat stats`() {
        val baseCombatStats = DefaultCombatStats(defenceLevel = 100, magicLevel = 50)
        val entity = ToaCombatEntity(
            name = "Test Boss",
            baseHp = BaseHp.ZEBAK,
            invocationLevel = 150,
            pathLevel = 2,
            baseCombatStats = baseCombatStats
        )
        
        // Check basic properties
        assertEquals("Test Boss", entity.name)
        assertEquals(BaseHp.ZEBAK, entity.baseHp)
        assertEquals(150, entity.invocationLevel)
        assertEquals(2, entity.pathLevel)
        
        // Check scaled HP (should be higher than base due to invocation and path scaling)
        assertTrue(entity.scaledHp > BaseHp.ZEBAK)
        assertEquals(entity.scaledHp, entity.health.value)
        
        // Check combat stats are wrapped in ToaMonsterCombatStats
        assertTrue(entity.combatStats is ToaMonsterCombatStats)
        assertEquals(100, entity.combatStats.defenceLevel)
        assertEquals(50, entity.combatStats.magicLevel)
    }
    
    @Test
    fun `should handle combat operations correctly`() {
        val entity = ToaCombatEntity(
            name = "Test Boss",
            baseHp = 1000,
            invocationLevel = 0,
            baseCombatStats = DefaultCombatStats(defenceLevel = 50)
        )
        
        val initialHp = entity.health.value
        
        // Test damage
        entity.takeDamage(100)
        assertEquals(initialHp - 100, entity.health.value)
        assertTrue(entity.isAlive)
        
        // Test lethal damage
        entity.takeDamage(initialHp)
        assertEquals(0, entity.health.value)
        assertFalse(entity.isAlive)
    }
    
    @Test
    fun `should handle attack timing correctly`() {
        val entity = ToaCombatEntity(
            name = "Test Boss",
            baseHp = 1000,
            invocationLevel = 0,
            baseCombatStats = DefaultCombatStats(defenceLevel = 50)
        )
        
        val currentTick = Tick(100)
        val weaponDelay = 4
        
        // Initially should be able to attack
        assertTrue(entity.canAttack(currentTick))
        
        // Set last attack tick
        entity.setLastAttackTick(currentTick, weaponDelay)
        
        // Should not be able to attack immediately
        assertFalse(entity.canAttack(currentTick))
        
        // Should be able to attack after delay
        assertTrue(entity.canAttack(Tick(104)))
    }
    
    @Test
    fun `should validate invocation level constraints`() {
        val baseCombatStats = DefaultCombatStats(defenceLevel = 50)
        
        // Should accept valid invocation levels
        ToaCombatEntity(
            name = "Test",
            baseHp = 1000,
            invocationLevel = 0,
            baseCombatStats = baseCombatStats
        )
        
        ToaCombatEntity(
            name = "Test",
            baseHp = 1000,
            invocationLevel = 150,
            baseCombatStats = baseCombatStats
        )
        
        // Should reject negative invocation level
        assertThrows<IllegalArgumentException> {
            ToaCombatEntity(
                name = "Test",
                baseHp = 1000,
                invocationLevel = -5,
                baseCombatStats = baseCombatStats
            )
        }
        
        // Should reject non-divisible-by-5 invocation level
        assertThrows<IllegalArgumentException> {
            ToaCombatEntity(
                name = "Test",
                baseHp = 1000,
                invocationLevel = 7,
                baseCombatStats = baseCombatStats
            )
        }
    }
    
    @Test
    fun `should validate path level constraints`() {
        val baseCombatStats = DefaultCombatStats(defenceLevel = 50)
        
        // Should accept valid path levels
        for (pathLevel in 0..6) {
            ToaCombatEntity(
                name = "Test",
                baseHp = 1000,
                invocationLevel = 0,
                pathLevel = pathLevel,
                baseCombatStats = baseCombatStats
            )
        }
        
        // Should reject invalid path levels
        assertThrows<IllegalArgumentException> {
            ToaCombatEntity(
                name = "Test",
                baseHp = 1000,
                invocationLevel = 0,
                pathLevel = 7,
                baseCombatStats = baseCombatStats
            )
        }
        
        assertThrows<IllegalArgumentException> {
            ToaCombatEntity(
                name = "Test",
                baseHp = 1000,
                invocationLevel = 0,
                pathLevel = -1,
                baseCombatStats = baseCombatStats
            )
        }
    }
} 