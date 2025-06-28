package com.osrs.toa.actors

import com.osrs.toa.Health
import com.osrs.toa.SpecialAttackEnergy
import com.osrs.toa.Tick
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon
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
        val player = createTestPlayerWithWeapon(deterministicWeapon)
        
        val initialHealth = target.health.value
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Verify target took the expected damage
        assertEquals(initialHealth - 25, target.health.value)
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
        
        // Verify player is on attack cooldown after special attack
        assertFalse(player.canAttack(Tick(0))) // Should not be able to attack immediately after
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
        val player = createTestPlayerWithWeapon(deterministicWeapon)
        
        val initialEnergy = player.specialAttackEnergy.energy
        val initialHealth = target.health.value
        
        player.attack(Tick(0), target, shouldSpec = { false })
        
        // Verify energy was not consumed
        assertEquals(initialEnergy, player.specialAttackEnergy.energy)
        
        // Verify target took damage from main weapon
        assertEquals(initialHealth - 25, target.health.value)
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
        val player = createTestPlayerWithWeapon(deterministicWeapon)
        
        val initialHealth = target.health.value
        
        player.attack(Tick(0), target, shouldSpec = { false })
        player.attack(Tick(5), target, shouldSpec = { false }) // After cooldown
        player.attack(Tick(10), target, shouldSpec = { false }) // After cooldown
        
        // Target should have taken damage from multiple attacks (3 * 25 = 75 damage)
        assertEquals(initialHealth - 75, target.health.value)
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

    @Test
    fun `should drink surge potion and restore 25 spec energy`() {
        val player = createTestPlayer()
        
        // Consume some energy first
        player.specialAttackEnergy.consume(50)
        assertEquals(50, player.specialAttackEnergy.energy)
        
        val result = player.drinkSurgePot(Tick(0))
        
        assertTrue(result)
        assertEquals(75, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should not drink surge potion when at full spec`() {
        val player = createTestPlayer()
        
        // Player starts with full spec (100)
        assertEquals(100, player.specialAttackEnergy.energy)
        
        val result = player.drinkSurgePot(Tick(0))
        
        assertFalse(result)
        assertEquals(100, player.specialAttackEnergy.energy) // Should remain unchanged
    }

    @Test
    fun `should not drink surge potion when on cooldown`() {
        val player = createTestPlayer()
        
        // Consume some energy first
        player.specialAttackEnergy.consume(50)
        assertEquals(50, player.specialAttackEnergy.energy)
        
        // Drink first potion
        val firstResult = player.drinkSurgePot(Tick(0))
        assertTrue(firstResult)
        assertEquals(75, player.specialAttackEnergy.energy)
        
        // Try to drink again immediately (should fail due to cooldown)
        val secondResult = player.drinkSurgePot(Tick(1))
        assertFalse(secondResult)
        assertEquals(75, player.specialAttackEnergy.energy) // Should remain unchanged
    }

    @Test
    fun `should be able to drink surge potion after cooldown expires`() {
        val player = createTestPlayer()
        
        // Consume some energy first
        player.specialAttackEnergy.consume(50)
        assertEquals(50, player.specialAttackEnergy.energy)
        
        // Drink first potion
        val firstResult = player.drinkSurgePot(Tick(0))
        assertTrue(firstResult)
        assertEquals(75, player.specialAttackEnergy.energy)
        
        // Try to drink again after cooldown expires (500 ticks)
        val secondResult = player.drinkSurgePot(Tick(500))
        assertTrue(secondResult)
        assertEquals(100, player.specialAttackEnergy.energy) // Should be capped at 100
    }

    @Test
    fun `should not exceed max spec energy when drinking surge potion`() {
        val player = createTestPlayer()
        
        // Set energy to 90 (10 below max)
        player.specialAttackEnergy.consume(10)
        assertEquals(90, player.specialAttackEnergy.energy)
        
        val result = player.drinkSurgePot(Tick(0))
        
        assertTrue(result)
        assertEquals(100, player.specialAttackEnergy.energy) // Should be capped at 100, not 115
    }

    @Test
    fun `should handle multiple surge potion uses with proper cooldowns`() {
        val player = createTestPlayer()
        
        // Consume energy to 25
        player.specialAttackEnergy.consume(75)
        assertEquals(25, player.specialAttackEnergy.energy)
        
        // First potion
        val firstResult = player.drinkSurgePot(Tick(0))
        assertTrue(firstResult)
        assertEquals(50, player.specialAttackEnergy.energy)
        
        // Second potion after cooldown
        val secondResult = player.drinkSurgePot(Tick(500))
        assertTrue(secondResult)
        assertEquals(75, player.specialAttackEnergy.energy)
        
        // Third potion after another cooldown
        val thirdResult = player.drinkSurgePot(Tick(1000))
        assertTrue(thirdResult)
        assertEquals(100, player.specialAttackEnergy.energy)
        
        // Fourth potion should fail (at full spec)
        val fourthResult = player.drinkSurgePot(Tick(1500))
        assertFalse(fourthResult)
        assertEquals(100, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should handle edge case of drinking exactly at cooldown boundary`() {
        val player = createTestPlayer()
        
        // Consume some energy
        player.specialAttackEnergy.consume(50)
        assertEquals(50, player.specialAttackEnergy.energy)
        
        // Drink first potion
        val firstResult = player.drinkSurgePot(Tick(0))
        assertTrue(firstResult)
        assertEquals(75, player.specialAttackEnergy.energy)
        
        // Try to drink at exactly 499 ticks (should fail - still on cooldown)
        val secondResult = player.drinkSurgePot(Tick(499))
        assertFalse(secondResult)
        assertEquals(75, player.specialAttackEnergy.energy)
        
        // Try to drink at exactly 500 ticks (should succeed - cooldown expired)
        val thirdResult = player.drinkSurgePot(Tick(500))
        assertTrue(thirdResult)
        assertEquals(100, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should not put surge potion on cooldown if attempted at full spec`() {
        val player = createTestPlayer()
        // Player starts at full spec
        assertEquals(100, player.specialAttackEnergy.energy)
        // Try to drink at tick 0 (should fail)
        val result = player.drinkSurgePot(Tick(0))
        assertFalse(result)
        // Now consume some energy and try to drink at tick 1 (should succeed if not on cooldown)
        player.specialAttackEnergy.consume(25)
        assertEquals(75, player.specialAttackEnergy.energy)
        val result2 = player.drinkSurgePot(Tick(1))
        assertTrue(result2)
        assertEquals(100, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should drink liquid adrenaline once and halve spec costs`() {
        val player = createTestPlayer()
        
        // Drink liquid adrenaline
        val result = player.drinkLiquidAdrenaline(Tick(0))
        assertTrue(result)
        
        // Attack with spec - should use halved cost (75/2 = 37.5)
        val target = createMockTarget()
        player.attack(Tick(1), target, shouldSpec = { true })
        
        // Should have consumed 37.5 energy instead of 75
        // Internal precision: 1000 - 375 = 625, displayed as 62
        assertEquals(62, player.specialAttackEnergy.energy)
    }

    @Test
    fun `should not drink liquid adrenaline twice & should consume full cost after effect expires`() {
        val player = createTestPlayerWithLowSpecWeapon()
        
        // Drink liquid adrenaline first time
        val firstResult = player.drinkLiquidAdrenaline(Tick(0))
        assertTrue(firstResult)
        
        // Try to drink again immediately
        val secondResult = player.drinkLiquidAdrenaline(Tick(1))
        assertFalse(secondResult)
        
        // Attack at tick 0 (effect active) - should consume halved cost (50/2 = 25)
        val target = createMockTarget()
        player.attack(Tick(0), target, shouldSpec = { true })
        assertEquals(75, player.specialAttackEnergy.energy) // 100 - 25 = 75
        
        // Wait for effect to expire and try to drink again
        val thirdResult = player.drinkLiquidAdrenaline(Tick(250))
        assertFalse(thirdResult)
        
        // Attack at tick 249 (effect still active) - should consume halved cost
        player.attack(Tick(245), target, shouldSpec = { true })
        assertEquals(50, player.specialAttackEnergy.energy) // 50 - 25 = 25
        
        // Attack at tick 250 (effect expired) - should consume full cost
        player.attack(Tick(250), target, shouldSpec = { true })
        assertEquals(0, player.specialAttackEnergy.energy) // 25 - 50 = 0 (capped at 0)
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

    private val deterministicWeapon = object : Weapon {
        override val name = "Deterministic Weapon"
        override val attackSpeed = 5
        override fun attack(target: CombatEntity): Int {
            return 25 // Always deals exactly 25 damage
        }
    }

    private fun createTestPlayerWithLowSpecWeapon(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity, Weapons.MagussShadow, lowSpecWeapon)
    }

    // Test weapon with 50 spec cost
    private val lowSpecWeapon = object : SpecWeapon {
        override val name = "Low Spec Weapon"
        override val attackSpeed = 5
        override val specialAttackCost = 50
        override fun attack(target: CombatEntity): Int {
            return 30 // Fixed damage for testing
        }
    }
}