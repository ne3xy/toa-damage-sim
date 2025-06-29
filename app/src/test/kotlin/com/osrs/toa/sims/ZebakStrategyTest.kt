package com.osrs.toa.sims

import com.osrs.toa.Tick
import com.osrs.toa.Health
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ZebakStrategyTest {

    @Test
    fun `ZebakMainFightStrategy should use BGS when health above 50% and defence not reduced enough`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Zebak starts with 2130 health (>50% of 2130) and 70 defence
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use ZCB when health below 50%`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak to below 50% health (2130 * 0.5 = 1065)
        zebak.zebak.takeDamage(1100) // 2130 - 1100 = 1030, which is < 50%
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB instead of BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use ZCB when defence reduced by 13 or more`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by exactly 13 (threshold)
        zebak.zebak.combatStats.drainDefenceLevel(13)
        assertEquals(57, zebak.zebak.combatStats.defenceLevel) // 70 - 13 = 57
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB instead of BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use ZCB when defence reduced by more than 13`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by more than 13
        zebak.zebak.combatStats.drainDefenceLevel(15)
        assertEquals(55, zebak.zebak.combatStats.defenceLevel) // 70 - 15 = 55
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB instead of BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use BGS when defence reduced by less than 13`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by less than 13
        zebak.zebak.combatStats.drainDefenceLevel(10)
        assertEquals(60, zebak.zebak.combatStats.defenceLevel) // 70 - 10 = 60
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon) // Should still use BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use ZCB when both health and defence conditions are not met`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak to below 50% health AND reduce defence by 13
        zebak.zebak.takeDamage(1100) // Below 50% health
        zebak.zebak.combatStats.drainDefenceLevel(13) // Defence reduced by threshold
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should not spec on tick 0`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotNull(specWeapon) // Still returns a weapon, but shouldSpec is false
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `ZebakMainFightStrategy should spec on non-zero tick when health above 500`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Zebak starts with 2130 health (>500)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotNull(specWeapon)
        assertTrue(shouldSpec) // Should spec on non-zero tick when health > 500
    }

    @Test
    fun `ZebakMainFightStrategy should not spec when health below 500`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak to below 500 health
        zebak.zebak.takeDamage(1700) // 2130 - 1700 = 430 (< 500)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotNull(specWeapon) // Still returns a weapon, but shouldSpec is false
        assertFalse(shouldSpec) // Should not spec when health < 500
    }

    @Test
    fun `ZebakMainFightStrategy should handle edge case of exactly 50% health`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak to exactly 50% health (2130 * 0.5 = 1065)
        zebak.zebak.takeDamage(1065) // 2130 - 1065 = 1065 (exactly 50%)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB when health <= 50%
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should handle edge case of exactly 13 defence reduction`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by exactly 13 (threshold)
        zebak.zebak.combatStats.drainDefenceLevel(13)
        assertEquals(57, zebak.zebak.combatStats.defenceLevel) // 70 - 13 = 57
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB when defence reduced >= 13
        assertTrue(shouldSpec)
    }

    private fun createTestPlayer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity)
    }
} 