package com.osrs.toa.sims

import com.osrs.toa.Tick
import com.osrs.toa.Health
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.PlayerLoadout
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.weapons.SpecStrategy
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.osrs.toa.weapons.TestStrategy
import com.osrs.toa.weapons.NoSpecStrategy
import com.osrs.toa.weapons.ConditionalSpecStrategy

class AkkhaStrategyTest {

    @Test
    fun `AkkhaMainFightStrategy should use Magus Shadow and ZCB`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        // shouldSpec depends on akkha.shouldZcbSpec() which depends on health and damage cap
        assertTrue(shouldSpec) // For fresh Akkha with high health, should be true
    }

    @Test
    fun `AkkhaMainFightStrategy should not spec on tick 0`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(0), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec on tick 0
    }

    @Test
    fun `AkkhaMainFightStrategy should spec when health is high and damage cap is sufficient`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Akkha starts with 1470 health (>= 500) and high damage cap
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec) // Should spec when health >= 500 and damage cap > 110
    }

    @Test
    fun `AkkhaMainFightStrategy should not spec when health is low`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Damage Akkha to low health by going through phases
        repeat(3) {
            akkha.akkha.takeDamage(1000) // Will be clamped to phase size
            akkha.akkha.maybeProcShadow(Tick(0))
            // Assert shadow spawns
            assertNotNull(akkha.akkha.shadow, "Shadow should spawn at phase boundary")
            akkha.akkha.shadow!!.takeDamage(1000) // Kill shadow
        }
        akkha.akkha.takeDamage(269) // Get to 319 health (< 320)
        assertEquals(319, akkha.akkha.health.value)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec when health <= 320
    }

    @Test
    fun `AkkhaMainFightStrategy should spec when health is between 320 and 500`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Damage Akkha to health between 320 and 500
        repeat(3) {
            akkha.akkha.takeDamage(1000) // Will be clamped to phase size
            akkha.akkha.maybeProcShadow(Tick(0))
            // Assert shadow spawns
            assertNotNull(akkha.akkha.shadow, "Shadow should spawn at phase boundary")
            akkha.akkha.shadow!!.takeDamage(1000) // Kill shadow
        }
        akkha.akkha.takeDamage(118) // Get to 470 health (between 320 and 500)
        assertEquals(470, akkha.akkha.health.value)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec) // Should spec when health > 320 but < 500
    }

    @Test
    fun `AkkhaMainFightStrategy should not spec when damage cap is too low`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val strategy = AkkhaMainFightStrategy(akkha.akkha)
        
        // Damage Akkha to where damage cap is less than 110
        akkha.akkha.takeDamage(200) // Get to 1270 health
        assertEquals(1270, akkha.akkha.health.value)
        // At 1270 health, damage cap should be 94 (less than 110)
        
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertFalse(shouldSpec) // Should not spec when damage cap <= 110
    }

    @Test
    fun `Akkha should handle shadow attacks with null spec weapon`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
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
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3) // No custom strategy provided
        
        // Should use default AkkhaMainFightStrategy
        akkha.onTick(Tick(1))
        
        // This should not throw an exception
        assertTrue(true)
    }

    @Test
    fun `Akkha strategy should handle memory phase correctly`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Memory phase starts at tick 101 and lasts for 21 ticks
        // During memory phase, boss should not be attackable
        akkha.onTick(Tick(102)) // In memory phase
        
        // This should not throw an exception
        assertTrue(true)
    }

    @Test
    fun `Akkha strategy should handle multiple phases correctly`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Simulate multiple phases
        repeat(5) {
            akkha.onTick(Tick(it))
        }
        
        // This should not throw an exception
        assertTrue(true)
    }

    @Test
    fun `should create Akkha with custom strategy`() {
        val loadout = createTestLoadout()
        val strategy = TestStrategy()
        val akkha = Akkha(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with no spec strategy`() {
        val loadout = createTestLoadout()
        val strategy = NoSpecStrategy()
        val akkha = Akkha(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with conditional spec strategy`() {
        val loadout = createTestLoadout()
        val strategy = ConditionalSpecStrategy(true)
        val akkha = Akkha(loadout, strategy, invocationLevel = 530, pathLevel = 3)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `test strategy should spec when condition is met`() {
        val strategy = TestStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec)
    }

    @Test
    fun `no spec strategy should not spec`() {
        val strategy = NoSpecStrategy()
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.MagussShadow)
        
        assertEquals(Weapons.MagussShadow, normalWeapon)
        assertEquals(null, specWeapon)
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
    fun `should create Akkha with default strategy`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3) // No custom strategy provided
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with custom strategy and different parameters`() {
        val loadout = createTestLoadout()
        val strategy = TestStrategy()
        val akkha = Akkha(loadout, strategy, invocationLevel = 500, pathLevel = 2)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with no spec strategy and different parameters`() {
        val loadout = createTestLoadout()
        val strategy = NoSpecStrategy()
        val akkha = Akkha(loadout, strategy, invocationLevel = 500, pathLevel = 2)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with conditional spec strategy and different parameters`() {
        val loadout = createTestLoadout()
        val strategy = ConditionalSpecStrategy(true)
        val akkha = Akkha(loadout, strategy, invocationLevel = 500, pathLevel = 2)
        
        assertNotNull(akkha)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should create Akkha with default strategy and different parameters`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 500, pathLevel = 2) // No custom strategy provided
        
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

    private fun createTestLoadout(): PlayerLoadout {
        val player = createTestPlayer()
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.MagussShadow
            override val strategy = AkkhaMainFightStrategy(Akkha(this, invocationLevel = 530, pathLevel = 3).akkha)
        }
    }
} 