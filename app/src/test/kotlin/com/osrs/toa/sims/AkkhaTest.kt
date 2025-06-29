package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.PlayerLoadout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AkkhaTest {

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

    @Test
    fun `should create Akkha boss fight`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        assertNotNull(akkha)
    }

    @Test
    fun `should create Akkha boss with correct properties`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should be attackable when no shadow is alive`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertTrue(akkha.akkha.isAttackable(Tick(0)))
    }

    @Test
    fun `should not be attackable during memory phase`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Note: Path level is currently hardcoded to 3 in the Akkha implementation.
        // This will be made configurable in a future update.
        // Memory phase starts at tick 101 and lasts for 21 ticks (5 memories * 4 ticks + 1)
        // So memory phase is from tick 101 to tick 121
        
        assertFalse(akkha.akkha.isAttackable(Tick(102)))
        assertFalse(akkha.akkha.isAttackable(Tick(105)))
        assertFalse(akkha.akkha.isAttackable(Tick(121))) // Last tick of memory phase
    }

    @Test
    fun `should be attackable after memory phase`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // After memory phase ends, the boss should be attackable
        assertTrue(akkha.akkha.isAttackable(Tick(122))) // First tick after memory phase
        assertTrue(akkha.akkha.isAttackable(Tick(200)))
    }

    @Test
    fun `should be attackable the tick after memories end`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Memory phase starts at tick 101 and lasts for 21 ticks (5 memories * 4 ticks + 1)
        // So memory phase ends at tick 121, and boss should be attackable at tick 122
        assertFalse(akkha.akkha.isAttackable(Tick(121))) // Last tick of memory phase
        assertTrue(akkha.akkha.isAttackable(Tick(122)))  // First tick after memory phase
    }

    @Test
    fun `should cap damage at phase size`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val initialHealth = akkha.akkha.health.value
        
        // Deal massive damage
        akkha.akkha.takeDamage(1000)
        
        // Should only take damage up to the phase cap
        val expectedDamage = 1470 / 5 // phaseSize = maxHealth / 5
        assertEquals(initialHealth - expectedDamage, akkha.akkha.health.value)
    }

    @Test
    fun `should cap damage at remaining health in phase`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Deal some damage first
        akkha.akkha.takeDamage(100)
        val healthAfterFirstDamage = akkha.akkha.health.value
        
        // Deal massive damage
        akkha.akkha.takeDamage(1000)
        
        // Should only take damage up to remaining health in phase
        val remainingInPhase = healthAfterFirstDamage % (1470 / 5)
        assertEquals(healthAfterFirstDamage - remainingInPhase, akkha.akkha.health.value)
    }

    @Test
    fun `should proc shadow at phase boundaries`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val initialHealth = akkha.akkha.health.value
        val phaseSize = initialHealth / 5
        
        // Damage boss to exactly a phase boundary
        akkha.akkha.takeDamage(phaseSize)
        
        // Should have taken exactly phase size damage
        assertEquals(initialHealth - phaseSize, akkha.akkha.health.value)
        
        akkha.akkha.maybeProcShadow(Tick(10))
        
        assertNotNull(akkha.akkha.shadow)
        assertEquals(255, akkha.akkha.shadow!!.health.value)
    }

    @Test
    fun `should not proc shadow when not at phase boundary`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Damage boss but not to a phase boundary
        akkha.akkha.takeDamage(100)
        
        akkha.akkha.maybeProcShadow(Tick(10))
        
        assertNull(akkha.akkha.shadow)
    }

    @Test
    fun `should not proc shadow when dead`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Proc & kill all the shadows
        repeat(4) {
            akkha.akkha.takeDamage(1000) // Will be clamped to phase size each time
            akkha.akkha.maybeProcShadow(Tick(0))
            akkha.akkha.shadow!!.takeDamage(1000)
        }
        val lastShadow = akkha.akkha.shadow
        assertFalse(lastShadow!!.isAlive)

        // Kill the boss
        akkha.akkha.takeDamage(1000)
        
        // Boss should be dead
        assertEquals(0, akkha.akkha.health.value)
        
        akkha.akkha.maybeProcShadow(Tick(0))
        
        assertEquals(lastShadow, akkha.akkha.shadow)
    }

    @Test
    fun `should not proc shadow at top of phase`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val phaseSize = akkha.akkha.health.value / 5
        
        // Damage boss to exactly a phase boundary
        akkha.akkha.takeDamage(phaseSize)
        
        akkha.akkha.maybeProcShadow(Tick(10))
        
        // Should proc shadow
        assertNotNull(akkha.akkha.shadow)
        val shadow = akkha.akkha.shadow!!
        
        // Kill the shadow
        shadow.takeDamage(1500)
        
        // Try to proc again at the same health level
        akkha.akkha.maybeProcShadow(Tick(20))
        
        // Should not proc again
        assertEquals(shadow, akkha.akkha.shadow)
    }

    @Test
    fun `should create Akkha with correct shadow name`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val phaseSize = akkha.akkha.health.value / 5
        
        // Proc shadow
        akkha.akkha.takeDamage(phaseSize)
        akkha.akkha.maybeProcShadow(Tick(0))
        
        val shadow = akkha.akkha.shadow!!
        assertEquals("530 Level 3 Akkha's Shadow", shadow.name)
        assertEquals( 255, shadow.health.value)
        // Test attackable behavior instead of accessing private field
        assertFalse(shadow.isAttackable(Tick(5))) // Should not be attackable before tick 6
        assertTrue(shadow.isAttackable(Tick(6))) // Should be attackable at tick 6
    }

    @Test
    fun `player should drink surge pot before attacking Akkha if spec is under 50`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        loadout.player.specialAttackEnergy.consume(60) // Set spec to 40
        akkha.onTick(Tick(0)) // Should attack Akkha and try to drink surge pot
        // After onTick, spec should be 65 (40 + 25 from surge pot)
        assertEquals(65, loadout.player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should NOT drink surge pot before attacking Akkha if spec is 50 or above`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        loadout.player.specialAttackEnergy.consume(40) // Set spec to 60
        akkha.onTick(Tick(0)) // Should attack Akkha but NOT drink surge pot
        // After onTick, spec should be 60 or less (if spec used for attack), but not increased by surge pot
        assertEquals(60, loadout.player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should NOT drink surge pot when attacking shadow, even if spec is under 50`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        loadout.player.specialAttackEnergy.consume(60) // Set spec to 40
        // Proc shadow by dealing damage to phase boundary
        val phaseSize = akkha.akkha.health.value / 5
        akkha.akkha.takeDamage(phaseSize)
        akkha.akkha.maybeProcShadow(Tick(0))
        akkha.onTick(Tick(6)) // Shadow becomes attackable at tick 6
        // Spec should remain 40 (no surge pot drank)
        assertEquals(40, loadout.player.specialAttackEnergy.energy)
        // Kill the shadow
        akkha.akkha.shadow!!.takeDamage(260) // Kill shadow
        // Now attack Akkha again - should drink surge pot since spec is still under 50
        akkha.onTick(Tick(7)) // Attack Akkha again
        // Spec should now be 65 (40 + 25 from surge pot)
        assertEquals(65, loadout.player.specialAttackEnergy.energy)
    }

    @Test
    fun `Akkha shadow should be at 210 HP at invocation 500 for path level 0`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 500, pathLevel = 0)
        
        // Deal damage to trigger shadow phase
        akkha.akkha.takeDamage(akkha.akkha.health.value / 5) // Phase size
        akkha.akkha.maybeProcShadow(Tick(0))
        
        assertNotNull(akkha.akkha.shadow)
        assertEquals(210, akkha.akkha.shadow!!.health.value)
    }

    @Test
    fun `Akkha shadow should be at 225 HP at invocation 500 for path level 1`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 500, pathLevel = 1)
        
        // Deal damage to trigger shadow phase
        akkha.akkha.takeDamage(akkha.akkha.health.value / 5) // Phase size
        akkha.akkha.maybeProcShadow(Tick(0))
        
        assertNotNull(akkha.akkha.shadow)
        assertEquals(225, akkha.akkha.shadow!!.health.value)
    }

    @Test
    fun `Akkha shadow should be at 235 HP at invocation 500 for path level 2`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 500, pathLevel = 2)
        
        // Deal damage to trigger shadow phase
        akkha.akkha.takeDamage(akkha.akkha.health.value / 5) // Phase size
        akkha.akkha.maybeProcShadow(Tick(0))
        
        assertNotNull(akkha.akkha.shadow)
        assertEquals(235, akkha.akkha.shadow!!.health.value)
    }

    @Test
    fun `Akkha shadow should be at 245 HP at invocation 500 for path level 3`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 500, pathLevel = 3)
        
        // Deal damage to trigger shadow phase
        akkha.akkha.takeDamage(akkha.akkha.health.value / 5) // Phase size
        akkha.akkha.maybeProcShadow(Tick(0))
        
        assertNotNull(akkha.akkha.shadow)
        assertEquals(245, akkha.akkha.shadow!!.health.value)
    }

    @Test
    fun `Akkha shadow should be at 260 HP at invocation 500 for path level 4`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 500, pathLevel = 4)
        
        // Deal damage to trigger shadow phase
        akkha.akkha.takeDamage(akkha.akkha.health.value / 5) // Phase size
        akkha.akkha.maybeProcShadow(Tick(0))
        
        assertNotNull(akkha.akkha.shadow)
        assertEquals(260, akkha.akkha.shadow!!.health.value)
    }

    @Test
    fun `Akkha should have correct initial health`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(1470, akkha.akkha.health.value)
    }

    @Test
    fun `Akkha should be attackable on tick 0`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertTrue(akkha.akkha.isAttackable(Tick(0)))
    }

    @Test
    fun `Akkha should handle shadow phase correctly`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Deal damage to trigger shadow phase
        akkha.akkha.takeDamage(294) // 1470 / 5 = 294 (phase size)
        akkha.akkha.maybeProcShadow(Tick(0))
        
        assertNotNull(akkha.akkha.shadow)
        assertEquals(255, akkha.akkha.shadow!!.health.value) // Shadow health is scaled, not phase size
    }

    @Test
    fun `Akkha should handle multiple shadow phases`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // First shadow phase
        akkha.akkha.takeDamage(294)
        akkha.akkha.maybeProcShadow(Tick(0))
        assertNotNull(akkha.akkha.shadow)
        
        // Kill first shadow
        akkha.akkha.shadow!!.takeDamage(294)
        
        // Second shadow phase
        akkha.akkha.takeDamage(294)
        akkha.akkha.maybeProcShadow(Tick(0))
        assertNotNull(akkha.akkha.shadow)
    }

    @Test
    fun `Akkha should handle memory phase correctly`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Memory phase starts at tick 102, ends at tick 121
        assertTrue(akkha.akkha.isAttackable(Tick(101))) // Attackable before memory
        assertFalse(akkha.akkha.isAttackable(Tick(102))) // Memory starts at tick 102
        assertFalse(akkha.akkha.isAttackable(Tick(121))) // Memory ends at tick 121
        assertTrue(akkha.akkha.isAttackable(Tick(122))) // Back to normal after memory
    }

    @Test
    fun `should handle damage correctly`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val initialHealth = akkha.akkha.health.value
        val phaseSize = initialHealth / 5
        
        // Deal some damage within phase size
        akkha.akkha.takeDamage(100)
        assertEquals(initialHealth - 100, akkha.akkha.health.value)
        
        // Deal more damage - should be capped to remaining phase size
        val remainingInPhase = akkha.akkha.health.value % phaseSize
        akkha.akkha.takeDamage(200)
        assertEquals(initialHealth - 100 - remainingInPhase, akkha.akkha.health.value)
    }

    @Test
    fun `should handle lethal damage correctly`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val scaledHp = akkha.akkha.health.value
        val phaseSize = scaledHp / 5
        
        // Deal lethal damage - should be capped to phase size
        akkha.akkha.takeDamage(scaledHp)
        assertEquals(scaledHp - phaseSize, akkha.akkha.health.value)
        assertTrue(akkha.akkha.isAlive) // Akkha doesn't die from lethal damage
    }

    @Test
    fun `should handle overkill damage correctly`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val initialHealth = akkha.akkha.health.value
        val phaseSize = initialHealth / 5
        
        // Deal more damage than health - should be capped to phase size
        akkha.akkha.takeDamage(3000)
        assertEquals(initialHealth - phaseSize, akkha.akkha.health.value) // Health reduced by phase size due to cap
        assertTrue(akkha.akkha.isAlive) // Akkha doesn't die from overkill damage
    }

    @Test
    fun `should create Akkha with correct name`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals("530 Level 3 Akkha", akkha.akkha.name)
    }

    @Test
    fun `should create Akkha with correct health`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(1470, akkha.akkha.health.value)
    }

    @Test
    fun `should create Akkha with correct defence level`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(80, akkha.akkha.combatStats.defenceLevel)
    }

    @Test
    fun `should create Akkha with correct magic level`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(100, akkha.akkha.combatStats.magicLevel)
    }

    @Test
    fun `should create Akkha with correct phase size`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(294, akkha.akkha.health.value / 5)
    }

    @Test
    fun `debug shadow health calculation`() {
        val loadout = createTestLoadout()
        val akkha = Akkha(loadout, invocationLevel = 530, pathLevel = 3)
        val phaseSize = akkha.akkha.health.value / 5
        
        // Proc shadow
        akkha.akkha.takeDamage(phaseSize)
        akkha.akkha.maybeProcShadow(Tick(0))
        
        val shadow = akkha.akkha.shadow!!
        println("Shadow health: ${shadow.health.value}")
        println("Expected: 255")
        assertEquals(255, shadow.health.value)
    }
}