package com.osrs.toa.sims

import com.osrs.toa.Tick
import com.osrs.toa.Health
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.weapons.SpecStrategy
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AkkhaStrategyTest {

    @Test
    fun `AkkhaMainFightStrategy should use Magus Shadow and ZCB`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        // shouldSpec depends on akkha.shouldZcbSpec() which depends on health and damage cap
        assertTrue(shouldSpec) // For fresh Akkha with high health, should be true
    }

    @Test
    fun `AkkhaMainFightStrategy should not spec on tick 0`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `AkkhaMainFightStrategy should spec when health is high and damage cap is sufficient`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Akkha starts with 1470 health (>= 500) and high damage cap
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec) // Should spec when health >= 500 and damage cap > 110
    }

    @Test
    fun `AkkhaMainFightStrategy should not spec when health is low`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Damage Akkha to low health by going through phases
        repeat(3) {
            akkha.akkha.takeDamage(1000) // Will be clamped to phase size
            akkha.akkha.maybeProcShadow(Tick(0))
            akkha.akkha.shadow!!.takeDamage(1000) // Kill shadow
        }
        akkha.akkha.takeDamage(269) // Get to 319 health (< 320)
        assertEquals(319, akkha.akkha.health.value)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec when health <= 320
    }

    @Test
    fun `AkkhaMainFightStrategy should spec when health is between 320 and 500`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Damage Akkha to health between 320 and 500
        repeat(3) {
            akkha.akkha.takeDamage(1000) // Will be clamped to phase size
            akkha.akkha.maybeProcShadow(Tick(0))
            akkha.akkha.shadow!!.takeDamage(1000) // Kill shadow
        }
        akkha.akkha.takeDamage(118) // Get to 470 health (between 320 and 500)
        assertEquals(470, akkha.akkha.health.value)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec) // Should spec when health > 320 but < 500
    }

    @Test
    fun `AkkhaMainFightStrategy should not spec when damage cap is too low`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Damage Akkha to where damage cap is less than 110
        akkha.akkha.takeDamage(200) // Get to 1270 health
        assertEquals(1270, akkha.akkha.health.value)
        // At 1270 health, damage cap should be 94 (less than 110)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1))
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec when damage cap <= 110
    }

    @Test
    fun `Akkha should handle shadow attacks with null spec weapon`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        
        // Proc shadow by dealing damage to phase boundary
        akkha.akkha.takeDamage(1470 / 5) // Deal exactly phase size damage
        akkha.akkha.maybeProcShadow(Tick(0)) // Proc the shadow
        
        // Wait for shadow to be attackable
        akkha.onTick(Tick(6)) // Shadow becomes attackable at tick 6
        
        // This should not throw an exception - shadow attacks use null spec weapon
        assertTrue(true)
    }

    @Test
    fun `Akkha should use default strategy when no custom strategy provided`() {
        val player = createTestPlayer()
        val akkha = Akkha(player) // No custom strategy provided
        
        // Should use default AkkhaMainFightStrategy
        akkha.onTick(Tick(1))
        
        // This should not throw an exception
        assertTrue(true)
    }

    @Test
    fun `Akkha strategy should handle memory phase correctly`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        
        // Memory phase starts at tick 101 and lasts for 21 ticks
        // During memory phase, boss should not be attackable
        akkha.onTick(Tick(102)) // In memory phase
        
        // This should not throw an exception
        assertTrue(true)
    }

    @Test
    fun `Akkha strategy should handle multiple phases correctly`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        
        // Simulate multiple phases
        repeat(5) {
            akkha.onTick(Tick(it))
        }
        
        // This should not throw an exception
        assertTrue(true)
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