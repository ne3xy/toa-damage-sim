package com.osrs.toa.actors

import com.osrs.toa.Health
import com.osrs.toa.SpecialAttackEnergy
import com.osrs.toa.Tick
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.weapons.Weapon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PlayerTest {

    @Test
    fun `should create player with weapons`() {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        val player = Player(combatEntity, Weapons.MagussShadow, Weapons.ZaryteCrossbow)
        
        assertEquals("Test Player", player.name)
        assertEquals(99, player.health.value)
        assertEquals(100, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should attack with main weapon when can attack`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Verify target took damage (we can't predict exact damage due to randomness)
        assertTrue(target.health.value < 100)
    }

    @Test
    fun `should not attack when cannot attack`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        // Set player to have just attacked
        player.setLastAttackTick(Tick(0), 5)
        
        val initialHealth = target.health.value
        player.attack(Tick(2), target) // Still in cooldown
        
        assertEquals(initialHealth, target.health.value)
    }

    @Test
    fun `should use special attack when energy is sufficient`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        // Ensure player has enough energy for ZCB spec (75)
        assertEquals(100, player.specialAttackEnergy.energy)
        
        player.attack(Tick(0), target, shouldSpec = { true })
        
        // Verify energy was consumed
        assertEquals(25, player.specialAttackEnergy.energy) // 100 - 75
    }

    @Test
    fun `should not use special attack when energy is insufficient`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        // Consume energy to make it insufficient
        player.specialAttackEnergy.consume(80)
        assertEquals(20, player.specialAttackEnergy.energy)
        
        player.attack(Tick(0), target, shouldSpec = { true })
        
        // Verify energy was not consumed (still 20)
        assertEquals(20, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should use main weapon when shouldSpec returns false`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        val initialEnergy = player.specialAttackEnergy.energy
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Verify energy was not consumed
        assertEquals(initialEnergy, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should not attack when dead`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        // Kill the player
        player.takeDamage(99)
        assertFalse(player.isAlive)
        
        val initialHealth = target.health.value
        player.attack(Tick(0), target)
        
        assertEquals(initialHealth, target.health.value)
    }

    @Test
    fun `should set attack cooldown after attacking`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        player.attack(Tick(10), target)
        
        // Should not be able to attack immediately after
        assertFalse(player.canAttack(Tick(11)))
    }

    @Test
    fun `should set special attack cooldown after special attack`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        player.attack(Tick(10), target, shouldSpec = { true })
        
        // Should not be able to attack immediately after
        assertFalse(player.canAttack(Tick(11)))
    }

    @Test
    fun `should handle multiple attacks`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        player.attack(Tick(0), target)
        player.attack(Tick(5), target) // After cooldown
        player.attack(Tick(10), target) // After cooldown
        
        // Target should have taken damage from multiple attacks
        assertTrue(target.health.value < 100)
    }

    @Test
    fun `should handle special attack with exact energy cost`() {
        val target = createMockTarget()
        val player = createTestPlayer()
        
        // Set energy to exactly match ZCB spec cost
        player.specialAttackEnergy.consume(25) // 100 - 75 = 25
        assertEquals(75, player.specialAttackEnergy.energy)
        
        player.attack(Tick(0), target, shouldSpec = { true })
        
        assertEquals(0, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should handle player with lightbearer`() {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = true
        )
        val player = Player(combatEntity, Weapons.MagussShadow, Weapons.ZaryteCrossbow)
        
        assertTrue(player.hasLightbearer)
    }

    @Test
    fun `should handle fast weapon cooldown edge case`() {
        val target = createMockTarget()
        val player = createTestPlayerWithWeapon(fastWeapon)
        
        // Attack at tick 0 with main weapon
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Should not be able to attack at tick 0 (still in cooldown)
        assertFalse(player.canAttack(Tick(0)))
        
        // Should be able to attack at tick 1 (cooldown finished: 0 + 1 = 1)
        assertTrue(player.canAttack(Tick(1)))
    }

    @Test
    fun `should handle slow weapon cooldown edge case`() {
        val target = createMockTarget()
        val player = createTestPlayerWithWeapon(slowWeapon)
        
        // Attack at tick 0 with main weapon
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Should not be able to attack at tick 8 (still in cooldown)
        assertFalse(player.canAttack(Tick(8)))
        
        // Should be able to attack at tick 9 (cooldown finished: 0 + 9 = 9)
        assertTrue(player.canAttack(Tick(9)))
    }

    @Test
    fun `should handle multiple attacks with different weapon speeds`() {
        val target = createMockTarget()
        val player = createTestPlayerWithWeapon(fastWeapon)
        
        // First attack at tick 0 with main weapon
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Second attack at tick 1 (after fast weapon cooldown: 0 + 1 = 1)
        player.attack(Tick(1), target, shouldSpec = { false })
        
        // Third attack at tick 2 (after fast weapon cooldown: 1 + 1 = 2)
        player.attack(Tick(2), target, shouldSpec = { false })
        
        // Target should have taken damage from multiple attacks
        assertTrue(target.health.value < 100)
    }

    @Test
    fun `should handle weapon speed transition`() {
        val target = createMockTarget()
        val player = createTestPlayerWithWeapon(fastWeapon)
        
        // Attack with fast weapon at tick 0 with main weapon
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Should be able to attack at tick 1 (0 + 1 = 1)
        assertTrue(player.canAttack(Tick(1)))
        
        // Create new player with slow weapon
        val slowPlayer = createTestPlayerWithWeapon(slowWeapon)
        
        // Attack with slow weapon at tick 0 with main weapon
        slowPlayer.attack(Tick(0), target, shouldSpec = { false })
        
        // Should not be able to attack at tick 1 (still in slow weapon cooldown: 0 + 9 = 9)
        assertFalse(slowPlayer.canAttack(Tick(1)))
        
        // Should be able to attack at tick 9 (0 + 9 = 9)
        assertTrue(slowPlayer.canAttack(Tick(9)))
    }

    private fun createTestPlayer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity, Weapons.MagussShadow, Weapons.ZaryteCrossbow)
    }

    private fun createTestPlayerWithWeapon(weapon: Weapon): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity, weapon, Weapons.ZaryteCrossbow)
    }

    private fun createMockTarget(): GenericCombatEntity {
        return GenericCombatEntity(
            name = "Test Target",
            health = Health(100)
        )
    }

    // Test weapons with different attack speeds
    private val fastWeapon = object : Weapon {
        override val name = "Fast Weapon"
        override val attackSpeed = 1
        override fun attack(target: CombatEntity): Int {
            return 10 // Fixed damage for testing
        }
    }

    private val slowWeapon = object : Weapon {
        override val name = "Slow Weapon"
        override val attackSpeed = 9
        override fun attack(target: CombatEntity): Int {
            return 20 // Fixed damage for testing
        }
    }
}