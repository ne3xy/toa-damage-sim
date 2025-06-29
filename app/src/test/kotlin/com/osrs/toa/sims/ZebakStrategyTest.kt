package com.osrs.toa.sims

import com.osrs.toa.Tick
import com.osrs.toa.Health
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.PlayerLoadout
import com.osrs.toa.sims.ZebakMainFightStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.osrs.toa.weapons.TestStrategy
import com.osrs.toa.weapons.NoSpecStrategy
import com.osrs.toa.weapons.ConditionalSpecStrategy

class ZebakStrategyTest {
    private fun createTestPlayer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity)
    }
    private fun createTestLoadout(): PlayerLoadout {
        val player = createTestPlayer()
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.Zebak6WayTwistedBow
            override val strategy = ZebakMainFightStrategy(Zebak.create(this, invocationLevel = 530, pathLevel = 3).zebak)
        }
    }

    @Test
    fun `ZebakMainFightStrategy should use BGS and Twisted Bow`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        assertTrue(shouldSpec) // Should spec when defence reduction threshold not met
    }

    @Test
    fun `ZebakMainFightStrategy should switch to ZCB after defence reduction threshold`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by more than threshold (13)
        zebak.zebak.combatStats.drainDefenceLevel(15)
        assertEquals(55, zebak.zebak.combatStats.defenceLevel) // 70 - 15 = 55
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB after defence reduction
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should switch to ZCB when health is below 50%`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak to below 50% health
        val scaledHp = zebak.zebak.health.value
        zebak.zebak.takeDamage(scaledHp / 2 + 1) // More than half health
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB when health < 50%
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use BGS when both conditions are met`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Verify initial conditions: high health and no defence reduction
        val initialHealth = zebak.zebak.health.value
        val initialDefence = zebak.zebak.combatStats.defenceLevel
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon) // Should use BGS initially
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should not spec on tick 0`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `ZebakMainFightStrategy should use BGS when defence reduction is below threshold`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by less than threshold (13)
        zebak.zebak.combatStats.drainDefenceLevel(10)
        assertEquals(60, zebak.zebak.combatStats.defenceLevel) // 70 - 10 = 60
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon) // Should still use BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use BGS when health is above 50%`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak but keep health above 50%
        val scaledHp = zebak.zebak.health.value
        zebak.zebak.takeDamage(scaledHp / 4) // 25% damage, still above 50%
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon) // Should still use BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should handle edge case of exactly 50% health`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        val scaledHp = zebak.zebak.health.value
        // Damage Zebak to exactly 50% health
        zebak.zebak.takeDamage(scaledHp / 2)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB when health <= 50%
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should handle edge case of exactly 13 defence reduction`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by exactly 13 (threshold)
        zebak.zebak.combatStats.drainDefenceLevel(13)
        assertEquals(57, zebak.zebak.combatStats.defenceLevel) // 70 - 13 = 57
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB when defence reduced >= 13
        assertTrue(shouldSpec)
    }

    @Test
    fun `should create Zebak with custom strategy`() {
        val loadout = createTestLoadout()
        val strategy = TestStrategy()
        val zebak = Zebak.create(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should create Zebak with no spec strategy`() {
        val loadout = createTestLoadout()
        val strategy = NoSpecStrategy()
        val zebak = Zebak.create(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should create Zebak with conditional spec strategy`() {
        val loadout = createTestLoadout()
        val strategy = ConditionalSpecStrategy(true)
        val zebak = Zebak.create(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `test strategy should spec when condition is met`() {
        val strategy = TestStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `no spec strategy should not spec`() {
        val strategy = NoSpecStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(null, specWeapon)
        assertFalse(shouldSpec)
    }

    @Test
    fun `conditional spec strategy should spec when condition is met`() {
        val strategy = ConditionalSpecStrategy(true)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `conditional spec strategy should not spec when condition is not met`() {
        val strategy = ConditionalSpecStrategy(false)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec)
    }

    @Test
    fun `conditional spec strategy should not spec on tick 0 regardless of condition`() {
        val strategy = ConditionalSpecStrategy(true)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec)
    }

    @Test
    fun `should create Zebak with default strategy`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3) // No custom strategy provided
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should create Zebak with custom strategy and different parameters`() {
        val loadout = createTestLoadout()
        val strategy = TestStrategy()
        val zebak = Zebak.create(loadout, strategy, invocationLevel = 500, pathLevel = 2)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should create Zebak with no spec strategy and different parameters`() {
        val loadout = createTestLoadout()
        val strategy = NoSpecStrategy()
        val zebak = Zebak.create(loadout, strategy, invocationLevel = 500, pathLevel = 2)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should create Zebak with conditional spec strategy and different parameters`() {
        val loadout = createTestLoadout()
        val strategy = ConditionalSpecStrategy(true)
        val zebak = Zebak.create(loadout, strategy, invocationLevel = 500, pathLevel = 2)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should create Zebak with default strategy and different parameters`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 500, pathLevel = 2) // No custom strategy provided
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
} 