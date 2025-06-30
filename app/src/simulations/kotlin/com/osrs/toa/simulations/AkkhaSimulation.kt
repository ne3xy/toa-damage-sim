package com.osrs.toa.simulations

import com.osrs.toa.*
import com.osrs.toa.actors.*
import com.osrs.toa.sims.Akkha
import com.osrs.toa.sims.AkkhaBoss
import com.osrs.toa.sims.AkkhaConstants
import com.osrs.toa.sims.AkkhaMainFightStrategy
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.osrs.toa.simulations.DEFAULT_ITERATIONS

class AkkhaAppTest {
    
    @Test
    fun `test Akkha with Lightbearer and surge pots`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = true,
            useSurgePots = true,
            useLiquidAdrenaline = false
        )
        println("Akkha Lightbearer + Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Akkha with Lightbearer, no surge pots`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = true,
            useSurgePots = false,
            useLiquidAdrenaline = false
        )
        println("Akkha Lightbearer + No Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Akkha with Lightbearer, surge pots, and liquid adrenaline`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = true,
            useSurgePots = true,
            useLiquidAdrenaline = true
        )
        println("Akkha Lightbearer + Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Akkha with Lightbearer, no surge pots, but with liquid adrenaline`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = true,
            useSurgePots = false,
            useLiquidAdrenaline = true
        )
        println("Akkha Lightbearer + No Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Akkha with Magus Ring and surge pots`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = false,
            useSurgePots = true,
            useLiquidAdrenaline = false
        )
        println("Akkha Magus Ring + Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Akkha with Magus Ring, no surge pots`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = false,
            useSurgePots = false,
            useLiquidAdrenaline = false
        )
        println("Akkha Magus Ring + No Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Akkha with Magus Ring, surge pots, and liquid adrenaline`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = false,
            useSurgePots = true,
            useLiquidAdrenaline = true
        )
        println("Akkha Magus Ring + Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Akkha with Magus Ring, no surge pots, but with liquid adrenaline`() {
        val results = simulateAkkhaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = false,
            useSurgePots = false,
            useLiquidAdrenaline = true
        )
        println("Akkha Magus Ring + No Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    private data class SimulationResults(
        val totalTicks: Int,
        val iterations: Int,
        val averageTicks: Int
    )
    
    private fun simulateAkkhaFights(
        iterations: Int,
        hasLightbearer: Boolean,
        useSurgePots: Boolean,
        useLiquidAdrenaline: Boolean
    ): SimulationResults {
        var totalTicks = 0
        
        repeat(iterations) { iteration ->
            val player = createPlayer(hasLightbearer, useSurgePots, useLiquidAdrenaline)
            val loadout = createAkkhaLoadout(player)
            val monster = Akkha(loadout, invocationLevel = 500, pathLevel = 2)
            val simulator = CombatSimulator(player, monster)
            
            val fightLength = simulator.runSimulation()
            totalTicks += fightLength.value
        }
        
        return SimulationResults(
            totalTicks = totalTicks,
            iterations = iterations,
            averageTicks = totalTicks / iterations
        )
    }
    
    private fun createPlayer(hasLightbearer: Boolean, useSurgePots: Boolean, useLiquidAdrenaline: Boolean): Player {
        return Player(
            GenericCombatEntity(
                health = Health(99),
                name = "Player",
                hasLightbearer = hasLightbearer
            ),
            useSurgePots = useSurgePots,
            useLiquidAdrenaline = useLiquidAdrenaline
        )
    }
    
    private fun createAkkhaLoadout(player: Player): PlayerLoadout {
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = if (player.hasLightbearer) Weapons.TumekensShadow else Weapons.MagussShadow
            override val strategy = AkkhaMainFightStrategy(createAkkhaBoss())
        }
    }
    
    private fun createAkkhaBoss(): AkkhaBoss {
        return AkkhaBoss(ToaCombatEntity(
            name = "Akkha",
            baseHp = AkkhaConstants.BASE_HP,
            invocationLevel = 500,
            pathLevel = 2,
            baseCombatStats = DefaultCombatStats(
                defenceLevel = 80,
                magicLevel = 100,
                rangedDefenceBonus = 60,
                rangedHeavyDefenceBonus = 60,
                magicDefenceBonus = 10
            )
        ), pathLevel = 2, invocationLevel = 500)
    }
} 