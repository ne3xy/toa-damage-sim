package com.osrs.toa.actors

import com.osrs.toa.Health
import com.osrs.toa.SpecialAttackEnergy
import com.osrs.toa.Tick
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

class GenericCombatEntityTest {

    @Test
    fun `should create entity with basic properties`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100),
            specialAttackEnergy = SpecialAttackEnergy(50),
            hasLightbearer = false
        )

        assertEquals("Test Entity", entity.name)
        assertEquals(100, entity.health.value)
        assertEquals(50, entity.specialAttackEnergy.energy)
        assertFalse(entity.hasLightbearer)
    }

    @Test
    fun `should create entity with default values`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )

        assertEquals(100, entity.specialAttackEnergy.energy)
        assertFalse(entity.hasLightbearer)
        assertNull(entity.specRegenStartTick)
    }

    @Test
    fun `should be alive when health is positive`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(50)
        )
        assertTrue(entity.isAlive)
    }

    @Test
    fun `should not be alive when health is zero`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(0)
        )
        assertFalse(entity.isAlive)
    }

    @Test
    fun `should not be alive when health is negative`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        entity.takeDamage(150)
        assertFalse(entity.isAlive)
    }

    @Test
    fun `should be able to attack when cooldown is over and alive`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        val currentTick = Tick(10)
        assertTrue(entity.canAttack(currentTick))
    }

    @Test
    fun `should not be able to attack when dead`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(0)
        )
        val currentTick = Tick(10)
        assertFalse(entity.canAttack(currentTick))
    }

    @Test
    fun `should not be able to attack during cooldown`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        entity.setLastAttackTick(Tick(10), 5)
        val currentTick = Tick(12) // Still in cooldown
        assertFalse(entity.canAttack(currentTick))
    }

    @Test
    fun `should be able to attack after cooldown`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        entity.setLastAttackTick(Tick(10), 5)
        val currentTick = Tick(16) // Cooldown finished
        assertTrue(entity.canAttack(currentTick))
    }

    @Test
    fun `should take damage`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        entity.takeDamage(30)
        assertEquals(70, entity.health.value)
    }

    @Test
    fun `should throw exception when taking negative damage`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        assertThrows(IllegalArgumentException::class.java) {
            entity.takeDamage(-5)
        }
    }

    @Test
    fun `should return self when taking damage for method chaining`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        val result = entity.takeDamage(20)
        assertSame(entity, result)
    }

    @Test
    fun `should regenerate special attack energy`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100),
            specialAttackEnergy = SpecialAttackEnergy(50)
        )
        entity.regenerateSpecialAttack()
        assertEquals(60, entity.specialAttackEnergy.energy)
    }

    @Test
    fun `should return self when regenerating special attack for method chaining`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        val result = entity.regenerateSpecialAttack()
        assertSame(entity, result)
    }

    @Test
    fun `should set last attack tick`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        val tick = Tick(10)
        entity.setLastAttackTick(tick, 5)
        // Note: We can't directly test the private lastAttackTick field,
        // but we can test the behavior through canAttack
        assertFalse(entity.canAttack(Tick(12)))
        assertTrue(entity.canAttack(Tick(16)))
    }

    @Test
    fun `should throw exception when setting negative attack tick`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        assertThrows(IllegalArgumentException::class.java) {
            entity.setLastAttackTick(Tick(-1), 5)
        }
    }

    @Test
    fun `should return self when setting last attack tick for method chaining`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        val result = entity.setLastAttackTick(Tick(10), 5)
        assertSame(entity, result)
    }

    @Test
    fun `should set spec regen start tick`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        val tick = Tick(10)
        entity.setSpecRegenStartTick(tick)
        assertEquals(tick, entity.specRegenStartTick)
    }

    @Test
    fun `should set spec regen start tick to null`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        entity.setSpecRegenStartTick(null)
        assertNull(entity.specRegenStartTick)
    }

    @Test
    fun `should throw exception when setting negative spec regen start tick`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        assertThrows(IllegalArgumentException::class.java) {
            entity.setSpecRegenStartTick(Tick(-1))
        }
    }

    @Test
    fun `should return self when setting spec regen start tick for method chaining`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100)
        )
        val result = entity.setSpecRegenStartTick(Tick(10))
        assertSame(entity, result)
    }

    @Test
    fun `should handle entity with lightbearer`() {
        val entity = GenericCombatEntity(
            name = "Test Entity",
            health = Health(100),
            hasLightbearer = true
        )
        assertTrue(entity.hasLightbearer)
    }
} 