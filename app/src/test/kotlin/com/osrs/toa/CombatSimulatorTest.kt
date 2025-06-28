package com.osrs.toa

import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.sims.BossFight
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import io.mockk.*

class CombatSimulatorTest {

    @Test
    fun `should create combat simulator with player and boss`() {
        val player = createTestPlayerNoLightbearer()
        val boss = createMockBossFight()
        
        val simulator = CombatSimulator(player, boss)
        assertNotNull(simulator)
    }

    @Test
    fun `should simulate single tick`() {
        val player = createTestPlayerNoLightbearer()
        val boss = mockk<BossFight>(relaxed = true)
        
        val simulator = CombatSimulator(player, boss)
        simulator.simulateTick()
        
        verify { boss.onTick(Tick(0)) }
    }

    @Test
    fun `should handle special attack energy regeneration`() {
        val player = createTestPlayerNoLightbearer()
        val boss = mockk<BossFight>(relaxed = true)
        
        // Set player to have low energy and start regeneration
        player.specialAttackEnergy.consume(80)
        player.setSpecRegenStartTick(Tick(0))
        
        val simulator = CombatSimulator(player, boss)
        
        // Simulate 51 ticks (to reach tick 50 where first regeneration happens)
        repeat(51) {
            simulator.simulateTick()
        }
        assertEquals(30, player.specialAttackEnergy.energy) // Should have regenerated 10 energy (20 + 10)

        // Simulate another 50 ticks (to reach tick 100 where second regeneration happens)
        repeat(50) {
            simulator.simulateTick()
        }
        assertEquals(40, player.specialAttackEnergy.energy) // Should have regenerated another 10 energy (30 + 10)
    }

    @Test
    fun `should handle lightbearer regeneration`() {
        val player = createTestPlayerWithLightbearer()
        val boss = mockk<BossFight>(relaxed = true)
        
        // Set player to have low energy and start regeneration
        player.specialAttackEnergy.consume(80)
        player.setSpecRegenStartTick(Tick(0))
        
        val simulator = CombatSimulator(player, boss)
        
        // Simulate 26 ticks (to reach tick 25 where regeneration happens)
        repeat(26) {
            simulator.simulateTick()
        }
        assertEquals(30, player.specialAttackEnergy.energy) // Should have regenerated 10 energy (20 + 10)

        // Simulate another 25 ticks (to reach tick 50 where second regeneration happens)
        repeat(25) {
            simulator.simulateTick()
        }
        assertEquals(40, player.specialAttackEnergy.energy) // Should have regenerated another 10 energy (30 + 10)
    }

    @Test
    fun `should not regenerate when energy is full`() {
        val player = spyk(createTestPlayerNoLightbearer())
        val boss = mockk<BossFight>(relaxed = true)
        
        // Player has full energy
        assertEquals(100, player.specialAttackEnergy.energy)
        
        val simulator = CombatSimulator(player, boss)
        
        // Simulate 51 ticks
        repeat(51) {
            simulator.simulateTick()
        }
        
        assertEquals(100, player.specialAttackEnergy.energy) // Should still be full
        verify(exactly = 0) { player.regenerateSpecialAttack() }
    }

    @Test
    fun `should run simulation until fight is over`() {
        val player = createTestPlayerNoLightbearer()
        val boss = mockk<BossFight>(relaxed = true)
        
        // Mock boss to end fight after 5 ticks
        var tickCount = 0
        every { boss.isFightOver() } answers {
            tickCount++
            tickCount > 5
        }
        
        val simulator = CombatSimulator(player, boss)
        val result = simulator.runSimulation()
        
        assertEquals(Tick(5), result)
        verify(exactly = 5) { boss.onTick(any()) }
    }

    @Test
    fun `should run simulation until max ticks reached`() {
        val player = createTestPlayerNoLightbearer()
        val boss = mockk<BossFight>(relaxed = true)
        
        // Mock boss to never end fight
        every { boss.isFightOver() } returns false
        
        val simulator = CombatSimulator(player, boss)
        
        val exception = assertThrows<IllegalStateException> {
            simulator.runSimulation()
        }
        
        assertEquals("Simulation timed out after 700 ticks. Fight did not complete.", exception.message)
        verify(exactly = 700) { boss.onTick(any()) }
    }

    @Test
    fun `should handle spec regen start tick initialization`() {
        val player = createTestPlayerNoLightbearer()
        val boss = mockk<BossFight>(relaxed = true)
        
        // Player has low energy but no regen start tick
        player.specialAttackEnergy.consume(80)
        player.setSpecRegenStartTick(null)
        
        val simulator = CombatSimulator(player, boss)
        simulator.simulateTick()
        
        assertEquals(Tick(0), player.specRegenStartTick)
        
        // Simulate 51 ticks to reach the first regeneration
        repeat(50) {
            simulator.simulateTick()
        }
        
        assertEquals(30, player.specialAttackEnergy.energy) // Should have regenerated 10 energy (20 + 10)
    }

    @Test
    fun `should unset spec regen start tick when energy is full`() {
        val player = createTestPlayerNoLightbearer()
        val boss = mockk<BossFight>(relaxed = true)
        
        // Player has full energy but regen start tick is set
        player.setSpecRegenStartTick(Tick(10))
        
        val simulator = CombatSimulator(player, boss)
        simulator.simulateTick()
        
        assertNull(player.specRegenStartTick)
    }

    private fun createTestPlayerNoLightbearer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity, Weapons.MagussShadow, Weapons.ZaryteCrossbow)
    }

    private fun createTestPlayerWithLightbearer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = true
        )
        return Player(combatEntity, Weapons.MagussShadow, Weapons.ZaryteCrossbow)
    }

    private fun createMockBossFight(): BossFight {
        return mockk<BossFight>(relaxed = true)
    }
} 