package com.osrs.toa.weapons

import com.osrs.toa.Tick
import com.osrs.toa.Health
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.sims.Zebak
import com.osrs.toa.sims.Akkha
import com.osrs.toa.sims.ZebakMainFightStrategy
import com.osrs.toa.sims.AkkhaMainFightStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SpecStrategyTest {

    @Test
    fun `should create ZebakMainFightStrategy`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        assertNotNull(strategy)
    }

    @Test
    fun `should create AkkhaMainFightStrategy`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        assertNotNull(strategy)
    }

    @Test
    fun `ZebakMainFightStrategy should return correct weapons on tick 0`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotNull(specWeapon)
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `ZebakMainFightStrategy should return correct weapons on non-zero tick`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotNull(specWeapon)
        assertTrue(shouldSpec) // Should spec on non-zero tick when health > 500
    }

    @Test
    fun `AkkhaMainFightStrategy should return correct weapons on tick 0`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `AkkhaMainFightStrategy should return correct weapons on non-zero tick`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        // shouldSpec depends on akkha.shouldZcbSpec() which depends on health and damage cap
        // For a fresh Akkha with high health, this should be true
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should return BGS when conditions are met`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Zebak starts with high health (>50%) and full defence, so should use BGS
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should return ZCB when BGS conditions not met`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak to below 50% health
        zebak.zebak.takeDamage(1200) // 2130 - 1200 = 930, which is < 50% of 2130
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB instead of BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should return ZCB when defence reduced enough`() {
        val player = createTestPlayer()
        val zebak = Zebak(player)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by 13 (threshold is 13)
        zebak.zebak.combatStats.drainDefenceLevel(13)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB instead of BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `strategy should handle null spec weapon gracefully`() {
        // Test that the Player.attack method can handle null spec weapons
        val player = createTestPlayer()
        val target = createTestTarget()
        
        // This should not throw an exception
        player.attack(Tick(0), target, Weapons.MagussShadow, null, false)
        
        assertTrue(true) // If we get here, no exception was thrown
    }

    private fun createTestPlayer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity)
    }

    private fun createTestTarget(): GenericCombatEntity {
        return GenericCombatEntity(
            name = "Test Target",
            health = Health(100)
        )
    }
} 