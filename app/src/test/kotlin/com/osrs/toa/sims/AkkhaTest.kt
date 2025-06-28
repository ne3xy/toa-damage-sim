package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AkkhaTest {

    @Test
    fun `should create Akkha boss fight`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        
        assertNotNull(akkha)
    }

    @Test
    fun `should create Akkha boss with correct properties`() {
        val player = createTestPlayer()
        val akkha = Akkha(player)
        
        // Access the internal boss through reflection or test the public interface
        // For now, we'll test the public methods
        assertFalse(akkha.isFightOver())
    }

    @Test
    fun `should be attackable when no shadow is alive`() {
        // Create a test boss instance to test attackability
        val testBoss = createTestAkkhaBoss()
        
        assertTrue(testBoss.isAttackable(Tick(0)))
    }

    @Test
    fun `should not be attackable during memory phase`() {
        val testBoss = createTestAkkhaBoss()
        
        // Note: Path level is currently hardcoded to 3 in the Akkha implementation.
        // This will be made configurable in a future update.
        // Memory phase starts at tick 101 and lasts for 21 ticks (5 memories * 4 ticks + 1)
        // So memory phase is from tick 101 to tick 121
        
        assertFalse(testBoss.isAttackable(Tick(102)))
        assertFalse(testBoss.isAttackable(Tick(105)))
        assertFalse(testBoss.isAttackable(Tick(121))) // Last tick of memory phase
    }

    @Test
    fun `should be attackable after memory phase`() {
        val testBoss = createTestAkkhaBoss()
        
        // After memory phase ends, the boss should be attackable
        assertTrue(testBoss.isAttackable(Tick(122))) // First tick after memory phase
        assertTrue(testBoss.isAttackable(Tick(200)))
    }

    @Test
    fun `should be attackable the tick after memories end`() {
        val testBoss = createTestAkkhaBoss()
        
        // Memory phase starts at tick 101 and lasts for 21 ticks (5 memories * 4 ticks + 1)
        // So memory phase ends at tick 121, and boss should be attackable at tick 122
        assertFalse(testBoss.isAttackable(Tick(121))) // Last tick of memory phase
        assertTrue(testBoss.isAttackable(Tick(122)))  // First tick after memory phase
    }

    @Test
    fun `should cap damage at phase size`() {
        val testBoss = createTestAkkhaBoss()
        val initialHealth = testBoss.health.value
        
        // Deal massive damage
        testBoss.takeDamage(1000)
        
        // Should only take damage up to the phase cap
        val expectedDamage = 1470 / 5 // phaseSize = maxHealth / 5
        assertEquals(initialHealth - expectedDamage, testBoss.health.value)
    }

    @Test
    fun `should cap damage at remaining health in phase`() {
        val testBoss = createTestAkkhaBoss()
        
        // Deal some damage first
        testBoss.takeDamage(100)
        val healthAfterFirstDamage = testBoss.health.value
        
        // Deal massive damage
        testBoss.takeDamage(1000)
        
        // Should only take damage up to remaining health in phase
        val remainingInPhase = healthAfterFirstDamage % (1470 / 5)
        assertEquals(healthAfterFirstDamage - remainingInPhase, testBoss.health.value)
    }

    @Test
    fun `should proc shadow at phase boundaries`() {
        val testBoss = createTestAkkhaBoss()
        val initialHealth = testBoss.health.value
        val phaseSize = 1470 / 5 // 294
        
        // Deal massive damage - should be clamped to phase size
        testBoss.takeDamage(1000)
        
        // Should have taken exactly phase size damage
        assertEquals(initialHealth - phaseSize, testBoss.health.value)
        
        testBoss.maybeProcShadow(Tick(10))
        
        assertNotNull(testBoss.shadow)
        assertEquals(255, testBoss.shadow!!.health.value)
    }

    @Test
    fun `should not proc shadow when not at phase boundary`() {
        val testBoss = createTestAkkhaBoss()
        
        // Damage boss but not to a phase boundary
        testBoss.takeDamage(100)
        
        testBoss.maybeProcShadow(Tick(10))
        
        assertNull(testBoss.shadow)
    }

    @Test
    fun `should not proc shadow when dead`() {
        val testBoss = createTestAkkhaBoss()
        
        // Proc & kill all the shadows
        repeat(4) {
            testBoss.takeDamage(1000) // Will be clamped to phase size each time
            testBoss.maybeProcShadow(Tick(0))
            testBoss.shadow!!.takeDamage(1000)
        }
        val lastShadow = testBoss.shadow
        assertFalse(lastShadow!!.isAlive)

        // Kill the boss
        testBoss.takeDamage(1000)
        
        // Boss should be dead
        assertEquals(0, testBoss.health.value)
        
        testBoss.maybeProcShadow(Tick(0))
        
        assertEquals(lastShadow, testBoss.shadow)
    }

    @Test
    fun `should not proc shadow at top of phase`() {
        val testBoss = createTestAkkhaBoss()
        
        // Damage boss to exactly a phase boundary
        testBoss.takeDamage(1470 / 5)
        
        testBoss.maybeProcShadow(Tick(10))
        
        // Should proc shadow
        assertNotNull(testBoss.shadow)
        val shadow = testBoss.shadow!!
        
        // Kill the shadow
        shadow.takeDamage(1500)
        
        // Try to proc again at the same health level
        testBoss.maybeProcShadow(Tick(20))
        
        // Should not proc again
        assertEquals(shadow, testBoss.shadow)
        // Check its still dead
        assertFalse(shadow.isAlive)
    }

    @Test
    fun `should recommend ZCB spec when health is high and damage cap is sufficient`() {
        val testBoss = createTestAkkhaBoss()
        
        // Boss has high health and damage cap > 110
        assertTrue(testBoss.shouldZcbSpec())
    }

    @Test
    fun `should not recommend ZCB spec when health is low`() {
        val testBoss = createTestAkkhaBoss()
        
        // Damage boss to low health (working around the damage clamping)
        repeat(3) {
            testBoss.takeDamage(1000)
            testBoss.maybeProcShadow(Tick(0))
            testBoss.shadow!!.takeDamage(1000)
        }
        testBoss.takeDamage(269)
        assertEquals(319, testBoss.health.value)
        
        assertFalse(testBoss.shouldZcbSpec())
    }

    @Test
    fun `should not recommend ZCB spec when health is above 500 & damage to cap is less than 110`() {
        val testBoss = createTestAkkhaBoss()

        // boss phases at 1176, at 1270 hp we are 94 damage away from phasing; hold zcb
        testBoss.takeDamage(200)
        assertEquals(1270, testBoss.health.value)

        assertFalse(testBoss.shouldZcbSpec())
    }

    @Test
    fun `should recommend ZCB spec when health is above 320 but below 500`() {
        val testBoss = createTestAkkhaBoss()
        
        // Damage boss to health between 320 and 500
        repeat(3) {
            testBoss.takeDamage(1000)
            testBoss.maybeProcShadow(Tick(0))
            testBoss.shadow!!.takeDamage(1000)
        }
        testBoss.takeDamage(118)
        assertEquals(470, testBoss.health.value)

        assertTrue(testBoss.shouldZcbSpec())
    }

    @Test
    fun `should create shadow with correct properties`() {
        val testBoss = createTestAkkhaBoss()
        
        // Proc shadow
        testBoss.takeDamage(1470 / 5)
        testBoss.maybeProcShadow(Tick(10))
        
        val shadow = testBoss.shadow!!
        assertEquals("530 Level 3 Akkha's Shadow", shadow.name)
        assertEquals(255, shadow.health.value)
        // Test attackable behavior instead of accessing private field
        assertFalse(shadow.isAttackable(Tick(15)))
        assertTrue(shadow.isAttackable(Tick(16)))
    }

    @Test
    fun `should make shadow attackable after delay`() {
        val testBoss = createTestAkkhaBoss()
        
        // Proc shadow
        testBoss.takeDamage(1470 / 5)
        testBoss.maybeProcShadow(Tick(10))
        
        val shadow = testBoss.shadow!!
        
        // Should not be attackable immediately
        assertFalse(shadow.isAttackable(Tick(10)))
        assertFalse(shadow.isAttackable(Tick(15)))
        
        // Should be attackable after delay
        assertTrue(shadow.isAttackable(Tick(16)))
        assertTrue(shadow.isAttackable(Tick(20)))
    }

    @Test
    fun `should not make shadow attackable when dead`() {
        val testBoss = createTestAkkhaBoss()
        
        // Proc shadow
        testBoss.takeDamage(1470 / 5)
        testBoss.maybeProcShadow(Tick(10))
        
        val shadow = testBoss.shadow!!
        
        // Kill shadow
        shadow.takeDamage(255)
        
        assertFalse(shadow.isAttackable(Tick(16)))
    }

    @Test
    fun `player should drink surge pot before attacking Akkha if spec is under 50`() {
        val player = createTestPlayer()
        player.specialAttackEnergy.consume(60) // Set spec to 40
        val akkha = Akkha(player)
        akkha.onTick(Tick(0)) // Should attack Akkha and try to drink surge pot
        // After onTick, spec should be 65 (40 + 25 from surge pot)
        assertEquals(65, player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should NOT drink surge pot before attacking Akkha if spec is 50 or above`() {
        val player = createTestPlayer()
        player.specialAttackEnergy.consume(40) // Set spec to 60
        val akkha = Akkha(player)
        akkha.onTick(Tick(0)) // Should attack Akkha but NOT drink surge pot
        // After onTick, spec should be 60 or less (if spec used for attack), but not increased by surge pot
        assertEquals(60, player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should NOT drink surge pot when attacking shadow, even if spec is under 50`() {
        val player = createTestPlayer()
        player.specialAttackEnergy.consume(60) // Set spec to 40
        val akkha = Akkha(player)
        // Proc shadow by dealing damage to phase boundary
        akkha.akkha.takeDamage(1470 / 5) // Deal exactly phase size damage
        akkha.akkha.maybeProcShadow(Tick(0)) // Proc the shadow
        // Wait for shadow to be attackable
        akkha.onTick(Tick(6)) // Shadow becomes attackable at tick 6
        // Spec should remain 40 (no surge pot drank)
        assertEquals(40, player.specialAttackEnergy.energy)
        // Kill the shadow
        akkha.akkha.shadow!!.takeDamage(255) // Kill shadow
        // Now attack Akkha again - should drink surge pot since spec is still under 50
        akkha.onTick(Tick(7)) // Attack Akkha again
        // Spec should now be 65 (40 + 25 from surge pot)
        assertEquals(65, player.specialAttackEnergy.energy)
    }

    private fun createTestPlayer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity, Weapons.MagussShadow, Weapons.ZaryteCrossbow)
    }

    private fun createTestAkkhaBoss(): AkkhaBoss {
        val combatEntity = GenericCombatEntity(
            name = "530 Level 3 Akkha",
            health = Health(1470)
        )
        return AkkhaBoss(combatEntity)
    }
} 