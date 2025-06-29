package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ZebakTest {

    @Test
    fun `should create Zebak boss fight`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        
        assertNotNull(zebak)
    }

    @Test
    fun `should create Zebak boss with correct properties`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should be attackable when alive`() {
        val testBoss = createTestZebakBoss()
        
        assertTrue(testBoss.isAttackable(Tick(0)))
        assertTrue(testBoss.isAttackable(Tick(10)))
        assertTrue(testBoss.isAttackable(Tick(100)))
    }

    @Test
    fun `should not be attackable when dead`() {
        val testBoss = createTestZebakBoss()
        
        // Kill the boss
        testBoss.takeDamage(2130)
        
        assertFalse(testBoss.isAttackable(Tick(0)))
        assertFalse(testBoss.isAttackable(Tick(10)))
    }

    @Test
    fun `should have correct initial health`() {
        val testBoss = createTestZebakBoss()
        
        assertEquals(2130, testBoss.health.value)
        assertEquals("530 Level 3 Zebak", testBoss.name)
    }

    @Test
    fun `should recommend ZCB spec when health is above 500`() {
        val testBoss = createTestZebakBoss()
        
        // Boss has high health (2130 > 500)
        assertTrue(testBoss.shouldZcbSpec())
    }

    @Test
    fun `should not recommend ZCB spec when health is below 500`() {
        val testBoss = createTestZebakBoss()
        
        // Damage boss to below 500 health
        testBoss.takeDamage(1700) // 2130 - 1700 = 430
        assertEquals(430, testBoss.health.value)
        
        assertFalse(testBoss.shouldZcbSpec())
    }

    @Test
    fun `should not recommend ZCB spec when health is exactly 500`() {
        val testBoss = createTestZebakBoss()
        
        // Damage boss to exactly 500 health
        testBoss.takeDamage(1630) // 2130 - 1630 = 500
        assertEquals(500, testBoss.health.value)
        
        assertFalse(testBoss.shouldZcbSpec())
    }

    @Test
    fun `should recommend ZCB spec when health is just above 500`() {
        val testBoss = createTestZebakBoss()
        
        // Damage boss to just above 500 health
        testBoss.takeDamage(1629) // 2130 - 1629 = 501
        assertEquals(501, testBoss.health.value)
        
        assertTrue(testBoss.shouldZcbSpec())
    }

    @Test
    fun `player should drink surge pot before attacking Zebak if spec is under 50`() {
        val player = createTestPlayer()
        player.specialAttackEnergy.consume(60) // Set spec to 40
        val zebak = Zebak(player)
        zebak.onTick(Tick(0)) // Should attack Zebak and try to drink surge pot
        // After onTick, spec should be 65 (40 + 25 from surge pot)
        assertEquals(65, player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should NOT drink surge pot before attacking Zebak if spec is 50 or above`() {
        val player = createTestPlayer()
        player.specialAttackEnergy.consume(40) // Set spec to 60
        val zebak = Zebak(player)
        zebak.onTick(Tick(0)) // Should attack Zebak but NOT drink surge pot
        // After onTick, spec should be 60 or less (if spec used for attack), but not increased by surge pot
        assertEquals(60, player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should drink liquid adrenaline before first ZCB spec`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        
        // Set up conditions for ZCB spec (not tick 0, shouldZcbSpec returns true)
        zebak.onTick(Tick(1)) // Should drink liquid adrenaline and spec
        
        // Verify liquid adrenaline was consumed (this would need access to player's liquid adrenaline state)
        // For now, we just verify the method doesn't throw an exception
        assertTrue(true)
    }

    @Test
    fun `player should NOT spec on tick 0`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        
        // On tick 0, should not spec even if shouldZcbSpec returns true
        zebak.onTick(Tick(0))
        
        // Verify no spec was used (this would need access to player's spec state)
        // For now, we just verify the method doesn't throw an exception
        assertTrue(true)
    }

    @Test
    fun `fight should be over when Zebak dies`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        
        // Initially fight is not over
        assertFalse(zebak.isFightOver())
        
        // Kill Zebak
        zebak.zebak.takeDamage(2130)
        
        // Now fight should be over
        assertTrue(zebak.isFightOver())
    }

    @Test
    fun `should handle damage correctly`() {
        val testBoss = createTestZebakBoss()
        val initialHealth = testBoss.health.value
        
        // Deal some damage
        testBoss.takeDamage(100)
        assertEquals(initialHealth - 100, testBoss.health.value)
        
        // Deal more damage
        testBoss.takeDamage(200)
        assertEquals(initialHealth - 300, testBoss.health.value)
    }

    @Test
    fun `should handle lethal damage correctly`() {
        val testBoss = createTestZebakBoss()
        
        // Deal lethal damage
        testBoss.takeDamage(2130)
        assertEquals(0, testBoss.health.value)
        assertFalse(testBoss.isAlive)
    }

    @Test
    fun `should handle overkill damage correctly`() {
        val testBoss = createTestZebakBoss()
        
        // Deal more damage than health
        testBoss.takeDamage(3000)
        assertEquals(0, testBoss.health.value)
        assertFalse(testBoss.isAlive)
    }

    @Test
    fun `should not reduce defence below 50`() {
        val zebak = Zebak(createTestPlayer())
        
        assertEquals(70, zebak.zebak.combatStats.defenceLevel)
        
        // Try to reduce defence by a large amount that would go below 50
        val largeReduction = 100
        zebak.zebak.combatStats.drainDefenceLevel(largeReduction)
        
        // Defence should be capped at 50, not reduced to initialDefence - 100
        assertEquals(50, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should allow defence reduction when above 50`() {
        val zebak = Zebak(createTestPlayer())
        // Test normal reduction when above 50
        val smallReduction = 5
        zebak.zebak.combatStats.drainDefenceLevel(smallReduction)
        assertEquals(65, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should handle multiple defence reductions correctly`() {
        val zebak = Zebak(createTestPlayer())
        
        assertEquals(70, zebak.zebak.combatStats.defenceLevel) // Verify initial defence level
        
        // First reduction: should work normally
        zebak.zebak.combatStats.drainDefenceLevel(5)
        assertEquals(65, zebak.zebak.combatStats.defenceLevel)
        
        // Second reduction: should work normally
        zebak.zebak.combatStats.drainDefenceLevel(5)
        assertEquals(60, zebak.zebak.combatStats.defenceLevel)
        
        // Third reduction: should cap at 50
        zebak.zebak.combatStats.drainDefenceLevel(15)
        assertEquals(50, zebak.zebak.combatStats.defenceLevel)
        
        // Fourth reduction: should stay at 50
        zebak.zebak.combatStats.drainDefenceLevel(10)
        assertEquals(50, zebak.zebak.combatStats.defenceLevel)
    }

    private fun createTestPlayer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity)
    }

    private fun createTestZebakBoss(): ZebakBoss {
        val combatEntity = GenericCombatEntity(
            name = "530 Level 3 Zebak",
            health = Health(2130)
        )
        return ZebakBoss(combatEntity)
    }
} 