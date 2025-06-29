package com.osrs.toa.weapons

import com.osrs.toa.Tick
import com.osrs.toa.Health
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.PlayerLoadout
import com.osrs.toa.sims.Zebak
import com.osrs.toa.sims.Akkha
import com.osrs.toa.sims.ZebakMainFightStrategy
import com.osrs.toa.sims.AkkhaMainFightStrategy
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon
import com.osrs.toa.weapons.SpecStrategy
import com.osrs.toa.weapons.TestStrategy
import com.osrs.toa.weapons.NoSpecStrategy
import com.osrs.toa.weapons.ConditionalSpecStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SpecStrategyTest {

    @Test
    fun `should create ZebakMainFightStrategy`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        assertNotNull(strategy)
    }

    @Test
    fun `should create AkkhaMainFightStrategy`() {
        val loadout = createTestAkkhaLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        assertNotNull(strategy)
    }

    @Test
    fun `ZebakMainFightStrategy should return correct weapons on tick 0`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotNull(specWeapon)
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `ZebakMainFightStrategy should return correct weapons on non-zero tick`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotNull(specWeapon)
        assertTrue(shouldSpec) // Should spec on non-zero tick when health > 500
    }

    @Test
    fun `AkkhaMainFightStrategy should return correct weapons on tick 0`() {
        val loadout = createTestAkkhaLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `AkkhaMainFightStrategy should return correct weapons on non-zero tick`() {
        val loadout = createTestAkkhaLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        // shouldSpec depends on akkha.shouldZcbSpec() which depends on health and damage cap
        // For a fresh Akkha with high health, this should be true
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should return BGS when conditions are met`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Zebak starts with high health (>50%) and full defence, so should use BGS
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should return ZCB when BGS conditions not met`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Damage Zebak to below 50% health
        zebak.zebak.takeDamage(1200) // 2130 - 1200 = 930, which is < 50% of 2130
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon) // Should use ZCB instead of BGS
        assertTrue(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should return ZCB when defence reduced enough`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by 13 (threshold is 13)
        zebak.zebak.combatStats.drainDefenceLevel(13)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
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

    @Test
    fun `test strategy should return correct weapons`() {
        val strategy = TestStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `test strategy should return correct weapons on non-zero tick`() {
        val strategy = TestStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `no spec strategy should return correct weapons`() {
        val strategy = NoSpecStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertNull(specWeapon)
        assertFalse(shouldSpec)
    }

    @Test
    fun `no spec strategy should return correct weapons on non-zero tick`() {
        val strategy = NoSpecStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertNull(specWeapon)
        assertFalse(shouldSpec)
    }

    @Test
    fun `conditional spec strategy should spec when condition is met`() {
        val strategy = ConditionalSpecStrategy(true)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `conditional spec strategy should not spec when condition is not met`() {
        val strategy = ConditionalSpecStrategy(false)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec)
    }

    @Test
    fun `conditional spec strategy should not spec on tick 0 regardless of condition`() {
        val strategy = ConditionalSpecStrategy(true)
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec)
    }

    @Test
    fun `ZebakMainFightStrategy should use BGS and Twisted Bow`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        assertTrue(shouldSpec) // Should spec when defence reduction threshold not met
    }

    @Test
    fun `ZebakMainFightStrategy should not BGS spec when defence reduction threshold met`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by more than threshold
        zebak.zebak.combatStats.drainDefenceLevel(20) // Reduce by 20 (initial 70)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec) // Should not spec when defence reduction >= 13
    }

    @Test
    fun `ZebakMainFightStrategy should not spec when health is low`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce health to just below 500
        val toTake = zebak.zebak.health.value - 499
        zebak.zebak.takeDamage(toTake)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNull(specWeapon)
        assertFalse(shouldSpec) // Should not spec when health < 500
    }

    @Test
    fun `ZebakMainFightStrategy should not BGS spec below 50% health`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce health to just below 50%
        val halfHealth = zebak.zebak.health.value / 2
        zebak.zebak.takeDamage(halfHealth + 1) // 1 below 50%
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotEquals(Weapons.BandosGodsword, specWeapon)
    }

    @Test
    fun `AkkhaMainFightStrategy should use Magus Shadow and ZCB`() {
        val loadout = createTestAkkhaLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec) // Should spec when health and damage cap conditions met
    }

    @Test
    fun `AkkhaMainFightStrategy should not spec when health is low`() {
        val loadout = createTestAkkhaLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Reduce health to below 320
        akkha.akkha.takeDamage(1200) // Get to 270 health (below 320)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec when health <= 320
    }

    @Test
    fun `ZebakMainFightStrategy should handle edge case at defence reduction threshold`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce defence by exactly threshold (13)
        zebak.zebak.combatStats.drainDefenceLevel(13) // Reduce by 13 (initial 70)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotEquals(Weapons.BandosGodsword, specWeapon) //Should not BGS spec when defence reduction >= 13
    }

    @Test
    fun `ZebakMainFightStrategy should handle edge case at health threshold`() {
        val loadout = createTestZebakLoadout()
        val zebak = Zebak(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = ZebakMainFightStrategy(zebak.zebak)
        
        // Reduce health to exactly 50%
        val halfHealth = zebak.zebak.health.value / 2
        zebak.zebak.takeDamage(halfHealth)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.Zebak6WayTwistedBow)
        
        assertEquals(Weapons.Zebak6WayTwistedBow, normalWeapon)
        assertNotEquals(Weapons.BandosGodsword, specWeapon)
    }

    @Test
    fun `AkkhaMainFightStrategy should handle edge case at health threshold`() {
        val loadout = createTestAkkhaLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Reduce health to exactly 320
        akkha.akkha.takeDamage(1150) // Get to 320 health (exactly at threshold)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec when health <= 320
    }

    @Test
    fun `should create Akkha with custom strategy`() {
        val loadout = createTestAkkhaLoadout()
        val strategy = TestStrategy()
        val akkha = Akkha(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with no spec strategy`() {
        val loadout = createTestAkkhaLoadout()
        val strategy = NoSpecStrategy()
        val akkha = Akkha(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with conditional spec strategy`() {
        val loadout = createTestAkkhaLoadout()
        val strategy = ConditionalSpecStrategy(true)
        val akkha = Akkha(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
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

    private fun createTestZebakLoadout(): PlayerLoadout {
        val player = createTestPlayer()
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.Zebak6WayTwistedBow
            override val strategy = ZebakMainFightStrategy(Zebak(this, invocationLevel = 530, pathLevel = 3).zebak)
        }
    }

    private fun createTestAkkhaLoadout(): PlayerLoadout {
        val player = createTestPlayer()
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.MagussShadow
            override val strategy = AkkhaMainFightStrategy(Akkha(this, invocationLevel = 530, pathLevel = 3).akkha)
        }
    }
} 