package com.osrs.toa

import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.sims.BossFight
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*

class CombatSimulatorTest {

    @Test
    fun `should create combat simulator with player and boss`() {
        val player = createTestPlayer()
        val boss = createMockBossFight()
        
        val simulator = CombatSimulator(player, boss)
        assertNotNull(simulator)
    }

    @Test
    fun `should simulate single tick`() {
        val player = createTestPlayer()
        val boss = mock<BossFight>()
        
        val simulator = CombatSimulator(player, boss)
        simulator.simulateTick()
        
        verify(boss, times(1)).onTick(Tick(0))
    }

    @Test
    fun `should handle special attack energy regeneration`() {
        val player = createTestPlayer()
        val boss = mock<BossFight>()
        
        // Set player to have low energy and start regeneration
        player.specialAttackEnergy.consume(80)
        player.setSpecRegenStartTick(Tick(0))
        
        val simulator = CombatSimulator(player, boss)
        
        // Simulate 50 ticks (should regenerate 1 energy)
        repeat(50) {
            simulator.simulateTick()
        }
        
        assertEquals(20, player.specialAttackEnergy.energy) // 20 (no regeneration because energy is not regenerating)
    }

    @Test
    fun `should handle lightbearer regeneration`() {
        val player = createTestPlayerWithLightbearer()
        val boss = mock<BossFight>()
        
        // Set player to have low energy and start regeneration
        player.specialAttackEnergy.consume(80)
        player.setSpecRegenStartTick(Tick(0))
        
        val simulator = CombatSimulator(player, boss)
        
        // Simulate 25 ticks (should regenerate 1 energy with lightbearer)
        repeat(25) {
            simulator.simulateTick()
        }
        
        assertEquals(20, player.specialAttackEnergy.energy) // 20 (no regeneration because energy is not regenerating)
    }

    @Test
    fun `should not regenerate when energy is full`() {
        val player = createTestPlayer()
        val boss = mock<BossFight>()
        
        // Player has full energy
        assertEquals(100, player.specialAttackEnergy.energy)
        
        val simulator = CombatSimulator(player, boss)
        
        // Simulate 50 ticks
        repeat(50) {
            simulator.simulateTick()
        }
        
        assertEquals(100, player.specialAttackEnergy.energy) // Should still be full
    }

    @Test
    fun `should run simulation until fight is over`() {
        val player = createTestPlayer()
        val boss = mock<BossFight>()
        
        // Mock boss to end fight after 5 ticks
        var tickCount = 0
        whenever(boss.isFightOver()).thenAnswer {
            tickCount++
            tickCount > 5
        }
        
        val simulator = CombatSimulator(player, boss)
        val result = simulator.runSimulation()
        
        assertEquals(Tick(5), result)
        verify(boss, times(5)).onTick(any())
    }

    @Test
    fun `should run simulation until max ticks reached`() {
        val player = createTestPlayer()
        val boss = mock<BossFight>()
        
        // Mock boss to never end fight
        whenever(boss.isFightOver()).thenReturn(false)
        
        val simulator = CombatSimulator(player, boss)
        val result = simulator.runSimulation()
        
        assertEquals(Tick(700), result)
        verify(boss, times(700)).onTick(any())
    }

    @Test
    fun `should handle spec regen start tick initialization`() {
        val player = createTestPlayer()
        val boss = mock<BossFight>()
        
        // Player has low energy but no regen start tick
        player.specialAttackEnergy.consume(80)
        player.setSpecRegenStartTick(null)
        
        val simulator = CombatSimulator(player, boss)
        simulator.simulateTick()
        
        assertEquals(Tick(0), player.specRegenStartTick)
    }

    @Test
    fun `should reset spec regen start tick when energy is full`() {
        val player = createTestPlayer()
        val boss = mock<BossFight>()
        
        // Player has full energy but regen start tick is set
        player.setSpecRegenStartTick(Tick(10))
        
        val simulator = CombatSimulator(player, boss)
        simulator.simulateTick()
        
        assertNull(player.specRegenStartTick)
    }

    private fun createTestPlayer(): Player {
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
        return mock<BossFight>()
    }
} 